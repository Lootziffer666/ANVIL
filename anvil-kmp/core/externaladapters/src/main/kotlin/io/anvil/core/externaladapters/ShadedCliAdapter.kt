package io.anvil.core.externaladapters

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * Real adapter for SHADED's documented headless CLI contract
 * (`docs/ORCHESTRATION.md`, `tools/orchestrate.js --project <path> --json`), verified
 * against the actual script in this session — not guessed.
 *
 * `request.payload` is the raw JSON text of orchestrate.js's own request envelope
 * (`{scene, material?, params?, actors?, storyboard?}`, see docs/ORCHESTRATION.md) —
 * this adapter does not reinterpret or re-derive any of SHADED's scene/actor state,
 * it only writes that payload to a temp file and shells out. Exit codes mirror
 * SwiftCliAdapter's convention: `0` success, `1` generic failure (incl. real
 * console/GL errors during the run), `2` missing input (referenced file not found).
 */
class ShadedCliAdapter(
    private val repoRoot: File,
    private val nodeExecutable: String = "node",
    private val processRunner: ProcessRunner = RealProcessRunner(),
) : ExternalToolPort {

    override val toolId: String = "shaded-cli"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("shaded.scene-project-request"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("shaded.scene-project"))

    /**
     * Fast, safe probe: `orchestrate.js` checks for `--project` before touching Playwright
     * or the server, so an argument-less invocation returns almost instantly with a real,
     * parseable `{"status":"error","code":"missing_input",...}` — proving node + the script
     * itself are reachable and behave per contract, without launching a browser.
     */
    override suspend fun health(): ExternalToolHealth {
        val result = processRunner.run(
            listOf(nodeExecutable, "tools/orchestrate.js", "--json"),
            repoRoot,
            timeoutSeconds = 30,
        )
        if (result.stdout.isBlank()) {
            return ExternalToolHealth(
                toolId,
                QualityState.FAILED,
                "orchestrate.js produced no JSON on stdout (exit ${result.exitCode}): ${result.stderr.trim().take(300)}",
            )
        }
        return try {
            val parsed = json.parseToJsonElement(result.stdout.trim()).jsonObject
            val status = parsed["status"]?.jsonPrimitive?.contentOrNull
            val code = parsed["code"]?.jsonPrimitive?.contentOrNull
            if (status == "error" && code == "missing_input") {
                ExternalToolHealth(toolId, QualityState.STABLE, "SHADED orchestrate.js reachable at ${repoRoot.path} and behaves per contract.")
            } else {
                ExternalToolHealth(toolId, QualityState.DEGRADED, "orchestrate.js responded unexpectedly to an argument-less probe: ${result.stdout.trim().take(300)}")
            }
        } catch (e: Exception) {
            ExternalToolHealth(toolId, QualityState.FAILED, "orchestrate.js output was not valid JSON: ${e.message}")
        }
    }

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult {
        if (request.contractId.value != "shaded.scene-project-request") {
            return ExternalToolResult.BlockedExternalContract("ShadedCliAdapter does not accept ${request.contractId.value}.")
        }

        val requestFile = File.createTempFile("shaded-orchestrate-request-", ".json")
        try {
            requestFile.writeText(request.payload)
            val result = processRunner.run(
                listOf(nodeExecutable, "tools/orchestrate.js", "--project", requestFile.absolutePath, "--json"),
                repoRoot,
                timeoutSeconds = 180,
            )
            if (result.stdout.isBlank()) {
                return ExternalToolResult.Failed(
                    "SHADED orchestrate.js produced no JSON on stdout (exit ${result.exitCode}): ${result.stderr.trim().take(500)}",
                )
            }
            val stdout = result.stdout.trim()
            return when (result.exitCode) {
                0 -> ExternalToolResult.Produced(ContractId("shaded.scene-project"), 1, stdout)
                2 -> ExternalToolResult.Failed("SHADED: missing input (exit 2) — ${errorDetail(stdout)}")
                else -> ExternalToolResult.Failed("SHADED orchestrate.js failed (exit ${result.exitCode}) — ${errorDetail(stdout)}")
            }
        } finally {
            requestFile.delete()
        }
    }

    private fun errorDetail(stdout: String): String = stdout.ifBlank { "no error detail on stdout" }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
