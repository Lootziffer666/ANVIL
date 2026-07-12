package io.anvil.core.externaladapters

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * All JSON fixtures below are the *exact*, unedited stdout of real
 * `node tools/orchestrate.js ...` invocations against the real SHADED checkout in this
 * session (after the R-07..R-11 atom) — not invented.
 */
class ShadedCliAdapterTest {

    private val REAL_HEALTH_PROBE_JSON = """{"status":"error","code":"missing_input","message":"Fehlendes --project <path> Argument."}"""

    private val REAL_SUCCESS_JSON = """
        {"status":"ok","engineLoaded":true,"ready":true,"actorCount":1,"storyboardSteps":1,
         "params":{"dayNight":0.7,"storm":0.08,"rain":0,"wet":0.7,"puddle":0.85,"fog":0.4,"wind":0.4,"glow":0.6,"decay":0,"snow":0,"snowfall":0,"temperature":0.52,"autumn":0,"bloom":0,"bleach":0},
         "actors":[{"id":1,"label":"verify-test-actor.png","x":0.5,"y":0.6,"scale":1,"anim":"walk","depthLayer":"mid"}],
         "storyboard":[{"name":"Akt 1","dur":4,"p":{"fog":0.4}}]}
    """.trimIndent()

    private val REAL_MISSING_REQUEST_JSON = """{"status":"error","code":"missing_input","message":"Request-Datei nicht gefunden: /home/user/SHADED/tools/does-not-exist.json"}"""

    private val REAL_MISSING_SCENE_JSON = """{"status":"error","code":"missing_input","message":"Szenenbild nicht gefunden: /tmp/nope.png"}"""

    @Test
    fun health_realArgumentLessProbe_isStable() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 2, stdout = REAL_HEALTH_PROBE_JSON, stderr = ""))
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)
        val health = adapter.health()
        assertEquals(QualityState.STABLE, health.quality)
        assertEquals(listOf("node", "tools/orchestrate.js", "--json"), runner.lastCommand)
    }

    @Test
    fun health_unexpectedResponse_isDegraded() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 0, stdout = """{"status":"ok"}""", stderr = ""))
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)
        assertEquals(QualityState.DEGRADED, adapter.health().quality)
    }

    @Test
    fun health_noJsonOnStdout_isFailed() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 1, stdout = "", stderr = "node: command not found"))
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)
        assertEquals(QualityState.FAILED, adapter.health().quality)
    }

    @Test
    fun invoke_realSuccessOutput_isProduced() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 0, stdout = REAL_SUCCESS_JSON, stderr = ""))
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)

        val result = adapter.invoke(
            ExternalToolRequest(ContractId("shaded.scene-project-request"), 1, PrivacyMode.OPEN, """{"scene":"scene.png"}"""),
        )
        val produced = assertIs<ExternalToolResult.Produced>(result)
        assertEquals(ContractId("shaded.scene-project"), produced.contractId)
        assertTrue(produced.payload.contains("\"ready\":true"))

        val command = runner.lastCommand!!
        assertEquals(listOf("node", "tools/orchestrate.js", "--project"), command.take(3))
        assertEquals("--json", command.last())
    }

    @Test
    fun invoke_writesPayloadToTheTempFileItShellsOut() = runTest {
        var seenRequestFileContent: String? = null
        val runner = object : ProcessRunner {
            override fun run(command: List<String>, workingDir: File, timeoutSeconds: Long): ProcessResult {
                val requestPath = command[command.indexOf("--project") + 1]
                seenRequestFileContent = File(requestPath).readText()
                return ProcessResult(exitCode = 0, stdout = REAL_SUCCESS_JSON, stderr = "")
            }
        }
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)
        val payload = """{"scene":"scene.png","params":{"fog":0.4}}"""
        adapter.invoke(ExternalToolRequest(ContractId("shaded.scene-project-request"), 1, PrivacyMode.OPEN, payload))
        assertEquals(payload, seenRequestFileContent)
    }

    @Test
    fun invoke_missingRequestFile_realExitTwo_mapsToFailed() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 2, stdout = REAL_MISSING_REQUEST_JSON, stderr = ""))
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)
        val result = adapter.invoke(ExternalToolRequest(ContractId("shaded.scene-project-request"), 1, PrivacyMode.OPEN, "{}"))
        val failed = assertIs<ExternalToolResult.Failed>(result)
        assertTrue(failed.reason.contains("missing input"))
    }

    @Test
    fun invoke_missingSceneAsset_realExitTwo_mapsToFailed() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 2, stdout = REAL_MISSING_SCENE_JSON, stderr = ""))
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)
        val result = adapter.invoke(
            ExternalToolRequest(ContractId("shaded.scene-project-request"), 1, PrivacyMode.OPEN, """{"scene":"nope.png"}"""),
        )
        val failed = assertIs<ExternalToolResult.Failed>(result)
        assertTrue(failed.reason.contains("Szenenbild nicht gefunden"))
    }

    @Test
    fun invoke_crashNoStdout_mapsToFailed() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 1, stdout = "", stderr = "Error: Cannot find module 'playwright'"))
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = runner)
        val result = adapter.invoke(ExternalToolRequest(ContractId("shaded.scene-project-request"), 1, PrivacyMode.OPEN, "{}"))
        val failed = assertIs<ExternalToolResult.Failed>(result)
        assertTrue(failed.reason.contains("playwright"))
    }

    @Test
    fun invoke_wrongInputContract_isBlockedExternalContract() = runTest {
        val adapter = ShadedCliAdapter(repoRoot = File("/tmp/fake-shaded"), processRunner = FakeProcessRunner(ProcessResult(0, "", "")))
        val result = adapter.invoke(ExternalToolRequest(ContractId("some.other.contract"), 1, PrivacyMode.OPEN, "{}"))
        assertIs<ExternalToolResult.BlockedExternalContract>(result)
    }
}
