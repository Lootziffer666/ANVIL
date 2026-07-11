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
 * Real adapter for CUE-AGENT's documented CLI (`cue doctor|playable-check|temporal-check
 * --json`), verified against the actual `bin/cue.js` in this session — not guessed. There
 * is deliberately no `audio-check` support: that command does not exist in CUE-AGENT yet
 * (see `docs/GOLDEN_RUN_REPORT.md` Gate H), and this adapter does not invent it.
 *
 * `playable-check`/`temporal-check` both exit `1` for a genuine negative verdict (e.g.
 * "NICHT BELEGT") — that is real, valid CUE output, not a failure, so both exit codes 0
 * and 1 map to [ExternalToolResult.Produced]. Only "no JSON on stdout at all" (crash,
 * missing browser, etc.) maps to [ExternalToolResult.Failed].
 */
class CueCliAdapter(
    private val repoRoot: File,
    private val nodeExecutable: String = "node",
    private val processRunner: ProcessRunner = RealProcessRunner(),
) : ExternalToolPort {

    override val toolId: String = "cue-agent-cli"
    override val acceptedInputContracts: List<ContractId> = listOf(ContractId("anvil.runnable-build"))
    override val producedOutputContracts: List<ContractId> = listOf(ContractId("cue.playable-proof"), ContractId("cue.temporal-proof"))

    override suspend fun health(): ExternalToolHealth {
        val result = processRunner.run(listOf(nodeExecutable, "bin/cue.js", "doctor", "--json"), repoRoot, timeoutSeconds = 60)
        if (result.stdout.isBlank()) {
            return ExternalToolHealth(toolId, QualityState.FAILED, "cue doctor produced no JSON on stdout (exit ${result.exitCode}): ${result.stderr.trim().take(300)}")
        }
        return try {
            val doctor = json.decodeFromString<CueDoctorReport>(result.stdout.trim())
            val requiredFailing = doctor.checks.filter { it.required && !it.ok }
            when {
                requiredFailing.isEmpty() -> ExternalToolHealth(toolId, QualityState.STABLE, "cue doctor: all required checks pass.")
                else -> ExternalToolHealth(
                    toolId,
                    QualityState.DEGRADED,
                    "cue doctor: required checks failing: ${requiredFailing.joinToString { "${it.name} (${it.detail})" }}",
                )
            }
        } catch (e: Exception) {
            ExternalToolHealth(toolId, QualityState.FAILED, "cue doctor output was not valid JSON: ${e.message}")
        }
    }

    override suspend fun invoke(request: ExternalToolRequest): ExternalToolResult {
        val subcommand = when (request.contractId.value) {
            "cue.playable-proof" -> "playable-check"
            "cue.temporal-proof" -> "temporal-check"
            else -> return ExternalToolResult.BlockedExternalContract("CueCliAdapter does not produce ${request.contractId.value}.")
        }
        val url = request.payload
        val result = processRunner.run(listOf(nodeExecutable, "bin/cue.js", subcommand, url, "--json"), repoRoot, timeoutSeconds = 180)
        if (result.stdout.isBlank()) {
            return ExternalToolResult.Failed(
                "CUE-AGENT $subcommand produced no JSON on stdout (exit ${result.exitCode}): ${result.stderr.trim().take(500)}",
            )
        }
        return ExternalToolResult.Produced(request.contractId, 1, result.stdout.trim())
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
data class CueDoctorCheck(
    val name: String,
    val required: Boolean,
    val ok: Boolean,
    val detail: String,
)

@Serializable
data class CueDoctorReport(
    val ok: Boolean,
    val lang: String,
    val checks: List<CueDoctorCheck>,
)
