package io.anvil.core.run

import io.anvil.core.contracts.ContractId
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class RunModelsSerializationTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun runPlanStep_withoutInputSelector_roundtrips() {
        val step = RunPlanStep(id = "S1", moduleId = "gameplay", operation = "COMPILE", payload = "{}")
        val encoded = json.encodeToString(step)
        val decoded = json.decodeFromString<RunPlanStep>(encoded)
        assertEquals(step, decoded)
        assertEquals(null, decoded.inputSelector)
        assertEquals(emptyList(), decoded.dependsOn)
    }

    @Test
    fun runPlanStep_withInlinePayloadSelector_roundtrips() {
        val step = RunPlanStep(
            id = "S1",
            moduleId = "gameplay",
            operation = "COMPILE",
            payload = "{}",
            inputSelector = RunInputSelector.InlinePayload("{\"a\":1}"),
        )
        val decoded = json.decodeFromString<RunPlanStep>(json.encodeToString(step))
        assertEquals(RunInputSelector.InlinePayload("{\"a\":1}"), decoded.inputSelector)
    }

    @Test
    fun runPlanStep_withArtifactByStepSelector_roundtrips() {
        val step = RunPlanStep(
            id = "S2",
            moduleId = "scene",
            operation = "COMPILE",
            payload = "{}",
            dependsOn = listOf("S1"),
            inputSelector = RunInputSelector.ArtifactByStep("S1"),
        )
        val decoded = json.decodeFromString<RunPlanStep>(json.encodeToString(step))
        assertEquals(RunInputSelector.ArtifactByStep("S1"), decoded.inputSelector)
        assertEquals(listOf("S1"), decoded.dependsOn)
    }

    @Test
    fun runPlanStep_withLatestArtifactByContractSelector_roundtrips() {
        val step = RunPlanStep(
            id = "S3",
            moduleId = "target",
            operation = "PREPARE",
            payload = "{}",
            inputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 1),
            outputContract = RunContractRef(ContractId("anvil.runnable-build"), 1),
            inputSelector = RunInputSelector.LatestArtifactByContract(ContractId("anvil.gameplay.plan"), 1),
        )
        val decoded = json.decodeFromString<RunPlanStep>(json.encodeToString(step))
        assertEquals(RunInputSelector.LatestArtifactByContract(ContractId("anvil.gameplay.plan"), 1), decoded.inputSelector)
        assertEquals(RunContractRef(ContractId("anvil.gameplay.plan"), 1), decoded.inputContract)
        assertEquals(RunContractRef(ContractId("anvil.runnable-build"), 1), decoded.outputContract)
    }

    @Test
    fun runPlan_withMultipleSteps_roundtrips() {
        val plan = RunPlan(
            planId = RunPlanId("PLAN_1"),
            workspaceId = "ws",
            runId = "run-1",
            artifactRoot = "artifact://run-1",
            steps = listOf(
                RunPlanStep(id = "S1", moduleId = "gameplay", operation = "COMPILE", payload = "{}"),
                RunPlanStep(id = "S2", moduleId = "scene", operation = "COMPILE", payload = "{}", dependsOn = listOf("S1")),
            ),
        )
        val decoded = json.decodeFromString<RunPlan>(json.encodeToString(plan))
        assertEquals(plan, decoded)
    }
}
