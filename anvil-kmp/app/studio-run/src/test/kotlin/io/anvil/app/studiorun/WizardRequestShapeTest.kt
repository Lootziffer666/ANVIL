package io.anvil.app.studiorun

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks in the exact wire shape WIZARD's real `/api/production-assessment` validator
 * requires (`WIZARD/src/lib/contracts/productionAssessment.ts` `isProductionAssessmentRequest`
 * — `brief` non-empty string required, no other required fields). Found live against the
 * real deployed WIZARD server: an earlier `{"seedWords": [...], "note": "..."}` shape (no
 * `brief` field at all) passed every test here because those all used a fixture/mock that
 * never validated the body, then 400'd for real. This test would have caught it.
 */
class WizardRequestShapeTest {

    @Test
    fun encodesExactlyTheFieldWizardsValidatorRequires() {
        val json = Json.encodeToString(WizardProductionAssessmentRequest(brief = "lantern rust harbor"))
        assertEquals("""{"brief":"lantern rust harbor"}""", json)
    }

    @Test
    fun omitsMaxPerRoleWhenNotSet_neverSendsNull() {
        // WIZARD's validator does `typeof r.maxPerRole !== "number"` when the field is present at
        // all — an explicit `"maxPerRole":null` would fail that check just as badly as a wrong type.
        val json = Json.encodeToString(WizardProductionAssessmentRequest(brief = "x"))
        assertEquals(false, json.contains("maxPerRole"), "must omit maxPerRole entirely, never send it as null: $json")
    }
}
