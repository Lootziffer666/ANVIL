package io.anvil.core.externaladapters

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolHealth
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.QualityState
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Real adapter for SWIFT's documented CLI contract (`SWIFT/docs/ORCHESTRATION.md`,
 * verified against the actual `main.py` in this session — not guessed):
 *
 * - `python main.py render --model <fbx> [--anim <fbx>] --json` on stdout: a single JSON
 *   summary object on success (exit 0); nothing on stdout, `{"status":"error",...}` on
 *   stderr on failure.
 * - Exit codes: `0` success, `1` generic failure, `2` missing input (e.g. FBX not found),
 *   `3` required external tool missing (Blender not available).
 *
 * SWIFT does not mutate world state (Invariante 2) — this adapter only shells out and
 * relays SWIFT's own JSON; it never invents fields SWIFT didn't produce.
 */
class SwiftCliAdapter(
    private val repoRoot: File,
    private val pythonExecutable: String = "python3",
    private val processRunner: ProcessRunner = RealProcessRunner(),
) : ExternalToolPort {

    override val toolId: String = "swift-cli"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("swift.render-request"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("swift.actor-bundle"), ContractId("swift.render-result"))

    override suspend fun health(): ExternalToolHealth {
        val result = processRunner.run(listOf(pythonExecutable, "main.py"), repoRoot, timeoutSeconds = 30)
        return if (result.exitCode == 0) {
            ExternalToolHealth(toolId, QualityState.STABLE, "SWIFT CLI reachable at ${repoRoot.path} (help exited 0).")
        } else {
            ExternalToolHealth(
                toolId,
                QualityState.DEGRADED,
                "SWIFT CLI at ${repoRoot.path} did not exit 0 for a bare invocation (exit ${result.exitCode}): ${result.stderr.trim().take(300)}",
            )
        }
    }

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult {
        if (request.contractId.value != "swift.render-request") {
            return ExternalToolResult.BlockedExternalContract("SwiftCliAdapter does not accept ${request.contractId.value}.")
        }
        val renderRequest = json.decodeFromString<SwiftRenderRequest>(request.payload)
        val command = buildList {
            add(pythonExecutable); add("main.py"); add("render")
            add("--model"); add(renderRequest.modelPath)
            renderRequest.animPath?.let { add("--anim"); add(it) }
            renderRequest.outputPath?.let { add("--output"); add(it) }
            add("--format"); add(renderRequest.format)
            if (renderRequest.worldStates.isNotEmpty()) { add("--world-states"); add(renderRequest.worldStates.joinToString(",")) }
            if (renderRequest.variants.isNotEmpty()) { add("--variants"); add(renderRequest.variants.joinToString(",")) }
            renderRequest.fps?.let { add("--fps"); add(it.toString()) }
            add("--json")
        }
        val result = processRunner.run(command, repoRoot, timeoutSeconds = 600)
        return when (result.exitCode) {
            0 -> ExternalToolResult.Produced(ContractId("swift.render-result"), 1, result.stdout.trim())
            2 -> ExternalToolResult.Failed("SWIFT: missing input (exit 2) — ${errorDetail(result.stderr)}")
            3 -> ExternalToolResult.Failed("SWIFT: required external tool missing, e.g. Blender (exit 3) — ${errorDetail(result.stderr)}")
            else -> ExternalToolResult.Failed("SWIFT render failed (exit ${result.exitCode}) — ${errorDetail(result.stderr)}")
        }
    }

    private fun errorDetail(stderr: String): String = stderr.trim().ifBlank { "no error detail on stderr" }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
data class SwiftRenderRequest(
    val modelPath: String,
    val animPath: String? = null,
    val outputPath: String? = null,
    val format: String = "sprite_sheet",
    val worldStates: List<String> = emptyList(),
    val variants: List<String> = emptyList(),
    val fps: Int? = null,
)
