package io.anvil.core.externaladapters

import io.anvil.core.contracts.QualityState
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Manual smoke test against the *real* SHADED checkout — not run in normal CI. Gated by
 * `SHADED_REPO_PATH` (absolute path to a SHADED checkout); skips (no-op pass) if unset.
 * Only exercises `health()` here (fast, no browser launch) — a full `invoke()` smoke test
 * needs a reachable headless Chromium and real scene/actor fixture files, which is a
 * separate, heavier smoke test (see RealGoldenRunTest, R-19).
 *
 * Run explicitly with:
 * `SHADED_REPO_PATH=/path/to/SHADED ./gradlew :core:externaladapters:test --tests "*ShadedCliManualIntegrationTest*"`
 */
class ShadedCliManualIntegrationTest {

    private val repoPathEnv = System.getenv("SHADED_REPO_PATH")

    @Test
    fun health_realCli_confirmsOrchestrateJsIsReachable() = runTest {
        val repoPath = repoPathEnv ?: return@runTest
        val adapter = ShadedCliAdapter(repoRoot = File(repoPath))
        val health = adapter.health()
        assertEquals(QualityState.STABLE, health.quality, "expected STABLE (argument-less probe returns missing_input fast), got: ${health.message}")
    }
}
