package io.anvil.core.externaladapters

import io.anvil.core.contracts.QualityState
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Manual smoke test against the *real* CUE-AGENT checkout — not run in normal CI. Gated
 * by `CUE_AGENT_REPO_PATH` (absolute path to a CUE-AGENT checkout with `npm install`
 * already run); skips (no-op pass) if unset. Only exercises `health()` (`cue doctor
 * --json`) here — `playable-check`/`temporal-check` additionally need a reachable
 * Playwright Chromium and a live URL, which is a separate, heavier smoke test.
 *
 * Run explicitly with:
 * `CUE_AGENT_REPO_PATH=/path/to/CUE-AGENT ./gradlew :core:externaladapters:test --tests "*CueCliManualIntegrationTest*"`
 */
class CueCliManualIntegrationTest {

    private val repoPathEnv = System.getenv("CUE_AGENT_REPO_PATH")

    @Test
    fun health_realCli_producesParsableDoctorReport() = runTest {
        val repoPath = repoPathEnv ?: return@runTest
        val adapter = CueCliAdapter(repoRoot = File(repoPath))
        val health = adapter.health()
        // Node.js itself must always be an "ok" check on any machine that can run this
        // test at all; we don't assert STABLE overall since Playwright/API keys are
        // legitimately absent in a fresh checkout.
        assertTrue(
            health.quality == QualityState.STABLE || health.quality == QualityState.DEGRADED,
            "expected a parsed doctor report (STABLE or DEGRADED), got FAILED: ${health.message}",
        )
    }
}
