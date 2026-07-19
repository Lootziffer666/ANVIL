package io.anvil.app.studiorun

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Locks in the verdict extraction added after the first full relay run, where CUE's
 * honest negative ("KEIN AUDIO-VERTRAG GEFUNDEN") was invisible behind a
 * "Produced(637 chars)" line — CUE deliberately maps genuine negative verdicts to
 * Produced (exit 1 is real output, not a crash), so studio-run must surface them.
 */
class CueVerdictTest {

    @Test
    fun extractsVerdictFromRealCueReportShape() {
        val payload = """{"url":"http://x/","mode":"generic","checks":[],"failed":["ANVIL_AUDIO_HOOK"],"verdict":"KEIN AUDIO-VERTRAG GEFUNDEN"}"""
        assertEquals("KEIN AUDIO-VERTRAG GEFUNDEN", cueVerdictOf(payload))
    }

    @Test
    fun nullForPayloadWithoutVerdict() {
        assertNull(cueVerdictOf("""{"something":"else"}"""))
    }

    @Test
    fun nullForNonJsonPayload_neverThrows() {
        assertNull(cueVerdictOf("not json at all"))
    }
}
