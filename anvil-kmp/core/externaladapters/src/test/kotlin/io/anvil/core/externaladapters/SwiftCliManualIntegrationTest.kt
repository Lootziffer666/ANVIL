package io.anvil.core.externaladapters

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Manual smoke test against the *real* SWIFT checkout — not run in normal CI. Gated by
 * `SWIFT_REPO_PATH` (absolute path to a SWIFT checkout); skips (no-op pass) if unset,
 * so CI without that sibling repo present never fails. This mirrors the
 * `RUN_ELEVENLABS_SMOKE`/`ELEVENLABS_API_KEY` gating used for the ElevenLabs provider.
 *
 * Run explicitly with:
 * `SWIFT_REPO_PATH=/path/to/SWIFT ./gradlew :core:externaladapters:test --tests "*SwiftCliManualIntegrationTest*"`
 */
class SwiftCliManualIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val repoPathEnv = System.getenv("SWIFT_REPO_PATH")

    @Test
    fun health_realCli_reachable() = runTest {
        val repoPath = repoPathEnv ?: return@runTest
        val adapter = SwiftCliAdapter(repoRoot = File(repoPath))
        val health = adapter.health()
        assertTrue(health.quality == QualityState.STABLE, "expected STABLE, got ${health.quality}: ${health.message}")
    }

    @Test
    fun invoke_realCli_missingModel_reportsExitTwo() = runTest {
        val repoPath = repoPathEnv ?: return@runTest
        val adapter = SwiftCliAdapter(repoRoot = File(repoPath))
        val payload = json.encodeToString(SwiftRenderRequest(modelPath = "/tmp/anvil-golden-run-does-not-exist.fbx"))
        val result = adapter.invoke(ExternalToolRequest(ContractId("swift.render-request"), 1, PrivacyMode.OPEN, payload))
        assertIs<ExternalToolResult.Failed>(result)
        assertTrue(result.reason.contains("missing input"), "expected exit-2 missing-input mapping, got: ${result.reason}")
    }
}
