package io.anvil.surfaces.goldenrun

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import io.anvil.core.externaladapters.CueCliAdapter
import io.anvil.core.externaladapters.ShadedCliAdapter
import io.anvil.core.externaladapters.WizardHttpAdapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Real Golden Run (R-19..R-21) — complements [GoldenRunTest] (which proves the internal
 * RunSurface/ArtifactStore/Handoff/Sync chain with `Fake*Port` fixtures for every external
 * studio system). This test proves the OTHER half: that the three real adapters built this
 * round (`WizardHttpAdapter`, `ShadedCliAdapter`, `CueCliAdapter`+`audio-check`) actually
 * reach and interoperate with the real sibling checkouts — no fixtures, no mocks, real
 * process/HTTP calls.
 *
 * Gated by env vars pointing at real sibling checkouts (same pattern as
 * `SwiftCliManualIntegrationTest`/`CueCliManualIntegrationTest`/`ShadedCliManualIntegrationTest`):
 * `WIZARD_REPO_PATH`, `SHADED_REPO_PATH`, `CUE_AGENT_REPO_PATH`. Any missing env var no-op
 * skips its own block (not the whole test) so this can still prove whichever real repos
 * ARE available. BARD/SWIFT are intentionally out of scope for this round (BARD stays a
 * fixture per this round's own mandate; SWIFT's real adapter was proven in Gate E-03).
 *
 * Run explicitly with:
 * `WIZARD_REPO_PATH=/home/user/WIZARD SHADED_REPO_PATH=/home/user/SHADED CUE_AGENT_REPO_PATH=/home/user/CUE-AGENT
 *  ./gradlew :surfaces:golden-run:jvmTest --tests "*RealGoldenRunTest*"`
 */
class RealGoldenRunTest {

    private val wizardRepoPath = System.getenv("WIZARD_REPO_PATH")
    private val shadedRepoPath = System.getenv("SHADED_REPO_PATH")
    private val cueRepoPath = System.getenv("CUE_AGENT_REPO_PATH")

    private var wizardProcess: Process? = null
    private var mockAudioServer: HttpServer? = null
    private var noAudioServer: HttpServer? = null

    @AfterTest
    fun tearDown() {
        // `npm run dev` spawns a shell which spawns the real `next dev` node process —
        // destroying only the top-level ProcessBuilder handle leaves that child running
        // (found as a real leaked process during verification of this test). Kill the
        // whole descendant tree explicitly.
        wizardProcess?.let { p ->
            p.toHandle().descendants().forEach { it.destroyForcibly() }
            p.destroyForcibly()
        }
        mockAudioServer?.stop(0)
        noAudioServer?.stop(0)
    }

    private fun httpClient() = HttpClient(Java) {
        expectSuccess = false
        install(HttpTimeout) { requestTimeoutMillis = 20_000 }
    }

    private suspend fun startWizardDevServer(port: Int): String {
        val process = ProcessBuilder("npm", "run", "dev", "--", "--port", port.toString())
            .directory(File(wizardRepoPath!!))
            .redirectErrorStream(true)
            .redirectOutput(File.createTempFile("wizard-dev-", ".log"))
            .start()
        wizardProcess = process
        val healthUrl = URI.create("http://localhost:$port/api/health").toURL()
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            try {
                val conn = healthUrl.openConnection() as HttpURLConnection
                conn.connectTimeout = 1000
                conn.readTimeout = 1000
                if (conn.responseCode == 200) return "http://localhost:$port"
            } catch (_: Exception) {
                // still starting
            }
            delay(500)
        }
        error("WIZARD dev server did not become healthy within 30s (see process log).")
    }

    private val realAudioMockPage = """
        <!doctype html><html><head><meta charset="utf-8"></head><body style="margin:0">
        <button onclick="window.__fire()">Fire</button>
        <script>
          const debugState = {}; const eventLog = [];
          window.__fire = () => { eventLog.push('stinger:sfx'); };
          window.ANVIL_AUDIO = {
            getDebugState: () => ({ ...debugState }),
            setState: (n, v) => { debugState[n] = v; },
            getEventLog: () => [...eventLog],
          };
        </script></body></html>
    """.trimIndent()

    private val realNoAudioPage = """<!doctype html><html><body><button>Klick</button></body></html>"""

    private fun serveStaticPage(html: String): HttpServer {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val bytes = html.toByteArray()
            exchange.responseHeaders.add("Content-Type", "text/html")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        return server
    }

    @Test
    fun realGoldenRun_wizardShadedCue_interopReallyWorks() = runTest {
        // No-op pass when none of the sibling repos are configured — same convention as
        // Swift/Cue/ShadedCliManualIntegrationTest (normal CI has no sibling checkouts).
        if (wizardRepoPath == null && shadedRepoPath == null && cueRepoPath == null) return@runTest

        var stepsRun = 0
        val evidence = mutableListOf<String>()

        // ── 1-3: real WIZARD (HTTP) ──────────────────────────────────────────────────
        if (wizardRepoPath != null) {
            val baseUrl = startWizardDevServer(port = 3498)
            val wizard = WizardHttpAdapter(baseUrl, httpClient())

            val health = wizard.health()
            assertEquals(QualityState.STABLE, health.quality, health.message)
            evidence += "wizard.health=${health.quality}"
            stepsRun++

            val result = wizard.invoke(
                ExternalToolRequest(
                    ContractId("anvil.wizard.production-assessment-request"), 1, PrivacyMode.OPEN,
                    """{"brief":"ein Koop-Abenteuer im Wald mit Lagerfeuer"}""",
                ),
            )
            val produced = assertIs<ExternalToolResult.Produced>(result)
            assertEquals(ContractId("anvil.wizard.production-assessment"), produced.contractId)
            assertTrue(produced.payload.contains("anvil.wizard.production-assessment/v1"))
            evidence += "wizard.invoke=Produced(${produced.payload.length} chars)"
            stepsRun++

            // Negative case: unreachable port must never silently succeed.
            val unreachable = WizardHttpAdapter("http://localhost:1", httpClient())
            val failure = unreachable.invoke(
                ExternalToolRequest(ContractId("anvil.wizard.production-assessment-request"), 1, PrivacyMode.OPEN, "{}"),
            )
            assertIs<ExternalToolResult.Failed>(failure)
            evidence += "wizard.negativeCase=Failed(as expected)"
            stepsRun++
        } else {
            evidence += "wizard.skipped=WIZARD_REPO_PATH not set"
        }

        // ── 4-6: real SHADED (CLI) ───────────────────────────────────────────────────
        if (shadedRepoPath != null) {
            val shaded = ShadedCliAdapter(repoRoot = File(shadedRepoPath))

            val health = shaded.health()
            assertEquals(QualityState.STABLE, health.quality, health.message)
            evidence += "shaded.health=${health.quality}"
            stepsRun++

            // orchestrate.js resolves paths relative to the request FILE's own directory;
            // this adapter writes the payload to an OS temp file (see ShadedCliAdapter's
            // KDoc "Path requirement"), so paths must be absolute here — the example
            // request file itself uses tools/-relative paths for direct CLI invocation.
            val toolsDir = File(shadedRepoPath, "tools")
            val realRequestJson = """
                {"scene":"${File(shadedRepoPath, "file_00000000974871f49fe71f6b456f9579.png").absolutePath}",
                 "params":{"fog":0.4,"dayNight":0.7},
                 "actors":[{"sheet":"${File(toolsDir, "verify-test-actor.png").absolutePath}",
                            "manifest":"${File(toolsDir, "verify-test-actor.json").absolutePath}",
                            "x":0.5,"y":0.6,"anim":"idle","label":"test-actor"}],
                 "storyboard":[{"name":"Akt 1","dur":4,"p":{"fog":0.4}}]}
            """.trimIndent()
            val result = shaded.invoke(
                ExternalToolRequest(ContractId("shaded.scene-project-request"), 1, PrivacyMode.OPEN, realRequestJson),
            )
            val produced = assertIs<ExternalToolResult.Produced>(result)
            assertTrue(produced.payload.contains("\"ready\":true"), produced.payload)
            evidence += "shaded.invoke=Produced(ready=true)"
            stepsRun++

            // Negative case: a scene file that does not exist must map to Failed, not a silent pass.
            val brokenRequest = """{"scene":"does-not-exist-${System.nanoTime()}.png"}"""
            val brokenResult = shaded.invoke(
                ExternalToolRequest(ContractId("shaded.scene-project-request"), 1, PrivacyMode.OPEN, brokenRequest),
            )
            val brokenFailed = assertIs<ExternalToolResult.Failed>(brokenResult)
            assertTrue(brokenFailed.reason.contains("missing input"), brokenFailed.reason)
            evidence += "shaded.negativeCase=Failed(missing input, as expected)"
            stepsRun++
        } else {
            evidence += "shaded.skipped=SHADED_REPO_PATH not set"
        }

        // ── 7-9: real CUE-AGENT (CLI) ────────────────────────────────────────────────
        if (cueRepoPath != null) {
            val cue = CueCliAdapter(repoRoot = File(cueRepoPath))

            val health = cue.health()
            assertTrue(
                health.quality == QualityState.STABLE || health.quality == QualityState.DEGRADED,
                "expected a parsed doctor report, got FAILED: ${health.message}",
            )
            evidence += "cue.health=${health.quality}"
            stepsRun++

            mockAudioServer = serveStaticPage(realAudioMockPage)
            val audioUrl = "http://127.0.0.1:${mockAudioServer!!.address.port}/"
            val audioResult = cue.invoke(ExternalToolRequest(ContractId("cue.audio-proof"), 1, PrivacyMode.OPEN, audioUrl))
            val audioProduced = assertIs<ExternalToolResult.Produced>(audioResult)
            assertTrue(audioProduced.payload.contains("AUDIO-VERTRAG BELEGT"), audioProduced.payload)
            evidence += "cue.audioCheck=Produced(BELEGT)"
            stepsRun++

            // Real negative case, not a crash: a page with no window.ANVIL_AUDIO at all.
            noAudioServer = serveStaticPage(realNoAudioPage)
            val noAudioUrl = "http://127.0.0.1:${noAudioServer!!.address.port}/"
            val noAudioResult = cue.invoke(ExternalToolRequest(ContractId("cue.audio-proof"), 1, PrivacyMode.OPEN, noAudioUrl))
            val noAudioProduced = assertIs<ExternalToolResult.Produced>(noAudioResult)
            assertTrue(noAudioProduced.payload.contains("KEIN AUDIO-VERTRAG GEFUNDEN"), noAudioProduced.payload)
            evidence += "cue.audioCheckNegative=Produced(KEIN AUDIO-VERTRAG GEFUNDEN, not Failed)"
            stepsRun++
        } else {
            evidence += "cue.skipped=CUE_AGENT_REPO_PATH not set"
        }

        assertTrue(stepsRun > 0, "No sibling repo env vars were set — this test verified nothing real. Set at least one of WIZARD_REPO_PATH/SHADED_REPO_PATH/CUE_AGENT_REPO_PATH.")

        println("REAL GOLDEN RUN — commit-independent evidence")
        evidence.forEach { println(it) }
        println("stepsActuallyRun=$stepsRun")
    }
}
