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
 * `REAL_CUE_DOCTOR_JSON` below is the *exact*, unedited output of
 * `node bin/cue.js doctor --json` captured against the real CUE-AGENT checkout in this
 * session (after `npm install`) — not invented.
 */
class CueCliAdapterTest {

    private val REAL_CUE_DOCTOR_JSON = """
        {
          "ok": false,
          "lang": "de",
          "checks": [
            {"name": "Node.js", "required": true, "ok": true, "detail": "v22.22.2"},
            {"name": "Playwright Chromium", "required": true, "ok": false, "detail": "fehlt — `npm run install-browsers` ausfuehren"},
            {"name": "LLM-Provider", "required": true, "ok": false, "detail": "anthropic — ANTHROPIC_API_KEY fehlt/Platzhalter"},
            {"name": "ffmpeg", "required": false, "ok": false, "detail": "nicht installiert (fuer Video-Render/Audio noetig)"},
            {"name": "ffprobe", "required": false, "ok": false, "detail": "nicht installiert"},
            {"name": "ELEVENLABS_API_KEY", "required": false, "ok": false, "detail": "optional, nicht gesetzt"},
            {"name": "FREESOUND_API_KEY", "required": false, "ok": false, "detail": "optional, nicht gesetzt"}
          ]
        }
    """.trimIndent()

    @Test
    fun health_realDoctorOutput_degradedWithRequiredFailuresListed() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 1, stdout = REAL_CUE_DOCTOR_JSON, stderr = ""))
        val adapter = CueCliAdapter(repoRoot = File("/tmp/fake-cue"), processRunner = runner)
        val health = adapter.health()
        assertEquals(QualityState.DEGRADED, health.quality)
        assertTrue(health.message.contains("Playwright Chromium"))
        assertTrue(health.message.contains("LLM-Provider"))
        assertEquals(listOf("node", "bin/cue.js", "doctor", "--json"), runner.lastCommand)
    }

    @Test
    fun health_allRequiredChecksPass_stable() = runTest {
        val allOk = """{"ok":true,"lang":"de","checks":[{"name":"Node.js","required":true,"ok":true,"detail":"v22"},{"name":"ffmpeg","required":false,"ok":false,"detail":"n/a"}]}"""
        val runner = FakeProcessRunner(ProcessResult(exitCode = 0, stdout = allOk, stderr = ""))
        val adapter = CueCliAdapter(repoRoot = File("/tmp/fake-cue"), processRunner = runner)
        assertEquals(QualityState.STABLE, adapter.health().quality)
    }

    @Test
    fun health_noJsonOnStdout_failed() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 1, stdout = "", stderr = "node: command not found"))
        val adapter = CueCliAdapter(repoRoot = File("/tmp/fake-cue"), processRunner = runner)
        assertEquals(QualityState.FAILED, adapter.health().quality)
    }

    @Test
    fun invoke_playableCheck_negativeVerdictExitOne_isStillProduced() = runTest {
        // Real shape of src/qa/playable.js's `json` object (verified by reading the source
        // in this session) — exit 1 means a real "NICHT BELEGT" verdict, not a crash.
        val playableJson = """
            {"url":"http://localhost:1/","checkedAt":"2026-07-11T00:00:00Z",
             "signals":{"navOk":true,"blank":false,"consoleErrors":0,"pageErrors":0,"serverErrors":0,"interactiveCount":2,"responded":false,"proofCount":2},
             "interaction":"kein interaktives Element gefunden","checks":[],"failed":["responded"],
             "verdict":"NICHT BELEGT SPIELBAR","proofs":["proof/proof-01.png"],"consoleErrorSamples":[],"serverErrorSamples":[],"reportDir":"/tmp/out"}
        """.trimIndent()
        val runner = FakeProcessRunner(ProcessResult(exitCode = 1, stdout = playableJson, stderr = ""))
        val adapter = CueCliAdapter(repoRoot = File("/tmp/fake-cue"), processRunner = runner)

        val result = adapter.invoke(ExternalToolRequest(ContractId("cue.playable-proof"), 1, PrivacyMode.OPEN, "http://localhost:1/"))
        assertIs<ExternalToolResult.Produced>(result)
        assertTrue(result.payload.contains("NICHT BELEGT SPIELBAR"))
        assertEquals(listOf("node", "bin/cue.js", "playable-check", "http://localhost:1/", "--json"), runner.lastCommand)
    }

    @Test
    fun invoke_temporalCheck_dispatchesCorrectSubcommand() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 0, stdout = """{"verdict":"KONSISTENT","score":92}""", stderr = ""))
        val adapter = CueCliAdapter(repoRoot = File("/tmp/fake-cue"), processRunner = runner)
        adapter.invoke(ExternalToolRequest(ContractId("cue.temporal-proof"), 1, PrivacyMode.OPEN, "http://localhost:1/"))
        assertEquals(listOf("node", "bin/cue.js", "temporal-check", "http://localhost:1/", "--json"), runner.lastCommand)
    }

    @Test
    fun invoke_noAudioCheckSupport_isBlockedNotGuessed() = runTest {
        // CUE-AGENT has no `audio-check` command (verified via --help in this session) —
        // this adapter must never invent one.
        val adapter = CueCliAdapter(repoRoot = File("/tmp/fake-cue"), processRunner = FakeProcessRunner(ProcessResult(0, "", "")))
        val result = adapter.invoke(ExternalToolRequest(ContractId("cue.audio-proof"), 1, PrivacyMode.OPEN, "http://localhost:1/"))
        assertIs<ExternalToolResult.BlockedExternalContract>(result)
    }

    @Test
    fun invoke_crashNoStdout_mapsToFailed() = runTest {
        val runner = FakeProcessRunner(ProcessResult(exitCode = 1, stdout = "", stderr = "Error: Cannot find module 'playwright'"))
        val adapter = CueCliAdapter(repoRoot = File("/tmp/fake-cue"), processRunner = runner)
        val result = adapter.invoke(ExternalToolRequest(ContractId("cue.playable-proof"), 1, PrivacyMode.OPEN, "http://localhost:1/"))
        assertIs<ExternalToolResult.Failed>(result)
        assertTrue(result.reason.contains("playwright"))
    }
}
