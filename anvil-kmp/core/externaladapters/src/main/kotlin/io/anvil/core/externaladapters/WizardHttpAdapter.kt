package io.anvil.core.externaladapters

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Real HTTP adapter for WIZARD's `anvil.wizard.production-assessment/v1` contract
 * (WIZARD `POST /api/production-assessment`, capability-advertised on `GET /api/health`).
 *
 * WIZARD is the sole owner/producer of this contract (CLAUDE.md: "ANVIL muss WIZARDs
 * Output nie per LLM neu interpretieren") — this adapter shells out over HTTP and relays
 * WIZARD's own JSON verbatim; it never re-derives or re-ranks the assessment itself.
 * A response that does not declare the expected contract id is treated as
 * [ExternalToolResult.BlockedExternalContract], never silently accepted or faked.
 */
class WizardHttpAdapter(
    private val baseUrl: String,
    private val client: HttpClient,
    private val bearerToken: String? = null,
    private val maxResponseBytes: Int = DEFAULT_MAX_RESPONSE_BYTES,
) : ExternalToolPort {

    override val toolId: String = "wizard-http"
    override val acceptedInputContracts: List<ContractId> =
        listOf(ContractId("anvil.wizard.production-assessment-request"))
    override val producedOutputContracts: List<ContractId> =
        listOf(ContractId("anvil.wizard.production-assessment"))

    override suspend fun health(): ExternalToolHealth {
        return try {
            val response = client.get(healthUrl())
            if (!response.status.isSuccess()) {
                return ExternalToolHealth(
                    toolId,
                    QualityState.DEGRADED,
                    "WIZARD /api/health at $baseUrl returned HTTP ${response.status.value}.",
                )
            }
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val supportsAssessment = body["capabilities"]?.jsonObject
                ?.get("productionAssessment")?.jsonPrimitive?.booleanOrNull ?: false
            if (supportsAssessment) {
                ExternalToolHealth(toolId, QualityState.STABLE, "WIZARD reachable at $baseUrl; production-assessment capability confirmed.")
            } else {
                ExternalToolHealth(
                    toolId,
                    QualityState.DEGRADED,
                    "WIZARD reachable at $baseUrl but does not advertise the production-assessment capability.",
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ExternalToolHealth(toolId, QualityState.DEGRADED, "WIZARD unreachable at $baseUrl: ${e.message ?: e.toString()}")
        }
    }

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult {
        if (request.contractId.value != "anvil.wizard.production-assessment-request") {
            return ExternalToolResult.BlockedExternalContract(
                "WizardHttpAdapter does not accept ${request.contractId.value}.",
            )
        }

        val text: String
        try {
            val response = client.post(assessmentUrl()) {
                contentType(ContentType.Application.Json)
                bearerToken?.takeIf { it.isNotBlank() }?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                setBody(request.payload)
            }
            text = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return ExternalToolResult.Failed(
                    "WIZARD production-assessment call failed (HTTP ${response.status.value}): ${text.take(500)}",
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ExternalToolResult.Failed("WIZARD production-assessment call failed: ${e.message ?: e.toString()}")
        }

        if (text.toByteArray(Charsets.UTF_8).size > maxResponseBytes) {
            return ExternalToolResult.Failed(
                "WIZARD response exceeds the size guard (${text.length} chars > $maxResponseBytes bytes) — refusing to relay.",
            )
        }

        val parsed = try {
            json.parseToJsonElement(text).jsonObject
        } catch (e: Exception) {
            return ExternalToolResult.Failed("WIZARD returned malformed JSON: ${e.message ?: e.toString()}")
        }

        val declaredContract = parsed["contract"]?.jsonPrimitive?.contentOrNull
        if (declaredContract != EXPECTED_CONTRACT) {
            return ExternalToolResult.BlockedExternalContract(
                "WIZARD response declares contract '$declaredContract', expected '$EXPECTED_CONTRACT'.",
            )
        }

        return ExternalToolResult.Produced(ContractId("anvil.wizard.production-assessment"), 1, text)
    }

    private fun assessmentUrl() = "${baseUrl.trimEnd('/')}/api/production-assessment"
    private fun healthUrl() = "${baseUrl.trimEnd('/')}/api/health"

    private companion object {
        const val EXPECTED_CONTRACT = "anvil.wizard.production-assessment/v1"
        const val DEFAULT_MAX_RESPONSE_BYTES = 2_000_000
        val json = Json { ignoreUnknownKeys = true }
    }
}
