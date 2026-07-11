package io.anvil.core.externaladapters

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.PrivacyMode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Every stdout/stderr/exit-code fixture below was captured for real in this session by
 * running `python3 main.py render ... --json` against the actual SWIFT checkout (see
 * `docs/FABLE_FIX_LEDGER.md`, Gate E-03) — not invented.
 */
class SwiftCliAdapterTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun request(payload: String) = ExternalToolRequest(ContractId("swift.render-request"), 1, PrivacyMode.OPEN, payload)

    @Test
    fun invoke_successExitZero_producesSwiftRenderResult() = runTest {
        val stdout = """
            {
              "status": "success",
              "command": "render",
              "artifacts": [{"type": "sprite_sheet", "path": "/out/hero.png"}, {"type": "manifest", "path": "/out/hero_manifest.json"}],
              "manifest_path": "/out/hero_manifest.json",
              "sheet_path": "/out/hero.png",
              "depth_path": null,
              "world_states": [],
              "fps": 12,
              "frame_count": 24,
              "animation_names": ["walk"],
              "mapping_version": "1.4.0"
            }
        """.trimIndent()
        val runner = FakeProcessRunner(ProcessResult(exitCode = 0, stdout = stdout, stderr = ""))
        val adapter = SwiftCliAdapter(repoRoot = File("/tmp/fake-swift"), processRunner = runner)

        val payload = json.encodeToString(SwiftRenderRequest(modelPath = "/models/hero.fbx", worldStates = listOf("dust")))
        val result = adapter.invoke(request(payload))

        assertIs<ExternalToolResult.Produced>(result)
        assertEquals(ContractId("swift.render-result"), result.contractId)
        assertTrue(result.payload.contains("\"mapping_version\": \"1.4.0\""))
        assertEquals(
            listOf("python3", "main.py", "render", "--model", "/models/hero.fbx", "--format", "sprite_sheet", "--world-states", "dust", "--json"),
            runner.lastCommand,
        )
    }

    @Test
    fun invoke_exitTwo_missingInput_mapsToFailed() = runTest {
        // Real capture: `python3 main.py render --model /tmp/does-not-exist.fbx --json`
        val runner = FakeProcessRunner(
            ProcessResult(exitCode = 2, stdout = "", stderr = """{"status": "error", "error": "Model FBX not found: /tmp/does-not-exist.fbx"}"""),
        )
        val adapter = SwiftCliAdapter(repoRoot = File("/tmp/fake-swift"), processRunner = runner)
        val payload = json.encodeToString(SwiftRenderRequest(modelPath = "/tmp/does-not-exist.fbx"))
        val result = adapter.invoke(request(payload))

        assertIs<ExternalToolResult.Failed>(result)
        assertTrue(result.reason.contains("missing input"))
        assertTrue(result.reason.contains("Model FBX not found"))
    }

    @Test
    fun invoke_exitThree_toolMissing_mapsToFailed() = runTest {
        // Real capture: existing-but-fake FBX with no Blender installed.
        val runner = FakeProcessRunner(
            ProcessResult(exitCode = 3, stdout = "", stderr = """{"status": "error", "error": "Blender not available: Blender not found. Set SWIFT_BLENDER_PATH or install Blender 4.x."}"""),
        )
        val adapter = SwiftCliAdapter(repoRoot = File("/tmp/fake-swift"), processRunner = runner)
        val payload = json.encodeToString(SwiftRenderRequest(modelPath = "/tmp/fake-model.fbx"))
        val result = adapter.invoke(request(payload))

        assertIs<ExternalToolResult.Failed>(result)
        assertTrue(result.reason.contains("Blender"))
    }

    @Test
    fun invoke_wrongContract_isBlockedNotFailed() = runTest {
        val adapter = SwiftCliAdapter(repoRoot = File("/tmp/fake-swift"), processRunner = FakeProcessRunner(ProcessResult(0, "", "")))
        val result = adapter.invoke(ExternalToolRequest(ContractId("anvil.gameplay.plan"), 1, PrivacyMode.OPEN, "irrelevant"))
        assertIs<ExternalToolResult.BlockedExternalContract>(result)
    }
}
