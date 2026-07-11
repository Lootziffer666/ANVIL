package io.anvil.core.externaladapters

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WizardHttpAdapterTest {

    private val requestContract = ContractId("anvil.wizard.production-assessment-request")

    private fun request(payload: String = """{"brief":"a desert coop game"}""") = ExternalToolRequest(
        contractId = requestContract,
        version = 1,
        privacyMode = PrivacyMode.LOCAL_ONLY,
        payload = payload,
    )

    private fun clientOf(engine: MockEngine, timeoutMillis: Long = 5_000) = HttpClient(engine) {
        expectSuccess = false
        install(HttpTimeout) { requestTimeoutMillis = timeoutMillis }
    }

    @Test
    fun invoke_success_relaysWizardsPayloadVerbatim() = runTest {
        val body = """{"contract":"anvil.wizard.production-assessment/v1","brief":"x","totalFound":3}"""
        val engine = MockEngine {
            respond(content = body, status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val result = adapter.invoke(request())
        val produced = assertIs<ExternalToolResult.Produced>(result)
        assertEquals(ContractId("anvil.wizard.production-assessment"), produced.contractId)
        assertEquals(body, produced.payload)
    }

    @Test
    fun invoke_httpBadRequest_returnsFailed() = runTest {
        val engine = MockEngine {
            respond(content = """{"error":"brief required"}""", status = HttpStatusCode.BadRequest)
        }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val result = adapter.invoke(request())
        val failed = assertIs<ExternalToolResult.Failed>(result)
        assertTrue(failed.reason.contains("400"), failed.reason)
    }

    @Test
    fun invoke_httpServerError_returnsFailed() = runTest {
        val engine = MockEngine { respond(content = "boom", status = HttpStatusCode.InternalServerError) }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val result = adapter.invoke(request())
        val failed = assertIs<ExternalToolResult.Failed>(result)
        assertTrue(failed.reason.contains("500"), failed.reason)
    }

    @Test
    fun invoke_timeout_returnsFailed() = runTest {
        val engine = MockEngine {
            delay(200)
            respond(content = """{"contract":"anvil.wizard.production-assessment/v1"}""", status = HttpStatusCode.OK)
        }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine, timeoutMillis = 20))
        val result = adapter.invoke(request())
        assertIs<ExternalToolResult.Failed>(result)
    }

    @Test
    fun invoke_wrongContractInResponse_isBlockedExternalContract() = runTest {
        val engine = MockEngine {
            respond(content = """{"contract":"anvil.wizard.production-assessment/v2","brief":"x"}""", status = HttpStatusCode.OK)
        }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val result = adapter.invoke(request())
        val blocked = assertIs<ExternalToolResult.BlockedExternalContract>(result)
        assertTrue(blocked.reason.contains("v2"), blocked.reason)
    }

    @Test
    fun invoke_malformedJson_returnsFailed() = runTest {
        val engine = MockEngine { respond(content = "{not json", status = HttpStatusCode.OK) }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val result = adapter.invoke(request())
        val failed = assertIs<ExternalToolResult.Failed>(result)
        assertTrue(failed.reason.contains("malformed", ignoreCase = true), failed.reason)
    }

    @Test
    fun invoke_oversizedResponse_returnsFailedWithoutRelaying() = runTest {
        val hugeBrief = "x".repeat(200)
        val body = """{"contract":"anvil.wizard.production-assessment/v1","brief":"$hugeBrief"}"""
        val engine = MockEngine { respond(content = body, status = HttpStatusCode.OK) }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine), maxResponseBytes = 50)
        val result = adapter.invoke(request())
        val failed = assertIs<ExternalToolResult.Failed>(result)
        assertTrue(failed.reason.contains("size guard"), failed.reason)
    }

    @Test
    fun invoke_wrongInputContract_isBlockedExternalContract() = runTest {
        val engine = MockEngine { respond(content = "unused", status = HttpStatusCode.OK) }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val result = adapter.invoke(request().copy(contractId = ContractId("some.other.contract")))
        assertIs<ExternalToolResult.BlockedExternalContract>(result)
    }

    @Test
    fun health_capabilityConfirmed_isStable() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"status":"ok","capabilities":{"productionAssessment":true}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val health = adapter.health()
        assertEquals(QualityState.STABLE, health.quality)
    }

    @Test
    fun health_unreachable_isDegraded() = runTest {
        val engine = MockEngine { respond(content = "down", status = HttpStatusCode.ServiceUnavailable) }
        val adapter = WizardHttpAdapter("http://localhost:3411", clientOf(engine))
        val health = adapter.health()
        assertEquals(QualityState.DEGRADED, health.quality)
    }
}
