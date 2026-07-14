package io.anvil.app.studiorun

import io.anvil.core.run.RunStatus
import io.anvil.core.run.RunStepStatus
import io.anvil.core.run.RunSurface
import io.anvil.modules.target.TargetEngine
import io.anvil.modules.target.TargetPlatform
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StudioRunAssemblerTest {

    private fun input(
        seedWords: List<String> = listOf("lantern", "rust", "harbor"),
        verbs: List<String> = listOf("interact"),
        playerRoles: List<String> = listOf("player"),
    ) = StudioRunInput(
        seedWords = seedWords,
        workspaceId = "ws-test",
        runId = "run-test-1",
        artifactRoot = "artifact://studio-run-test",
        verbs = verbs,
        playerRoles = playerRoles,
        targetEngine = TargetEngine.WEB,
        targetPlatform = TargetPlatform.WEB,
    )

    @Test
    fun assemble_isDeterministic_forTheSameInput() {
        val a = StudioRunAssembler.assemble(input())
        val b = StudioRunAssembler.assemble(input())
        assertEquals(a.plan, b.plan)
    }

    @Test
    fun assemble_producesFiveStepsInDependencyOrder() {
        val assembly = StudioRunAssembler.assemble(input())
        assertEquals(listOf("S_GAMEPLAY", "S_SCENE", "S_INTERFACE", "S_ACOUSTIC", "S_TARGET"), assembly.plan.steps.map { it.id })
        assertEquals(setOf("gameplay", "scene", "interface", "acoustic", "target"), assembly.modules.keys)
    }

    @Test
    fun assembledPlan_runsToCompletionThroughRealRunSurface() = runTest {
        val assembly = StudioRunAssembler.assemble(input(seedWords = listOf("copper", "quiet", "engine")))
        val surface = RunSurface(modules = assembly.modules)
        val summary = surface.execute(assembly.plan, createdAt = "2026-07-14T00:00:00Z")

        assertEquals(RunStatus.COMPLETE, summary.status, "records: ${summary.records}")
        assertEquals(5, summary.records.size)
        assertTrue(summary.records.all { it.status == RunStepStatus.COMPLETED })
        assertEquals("anvil.runnable-build/v1", summary.records.first { it.stepId == "S_TARGET" }.artifact!!.type)
    }

    @Test
    fun differentSeeds_produceDifferentGameplayPlanIds() {
        val a = StudioRunAssembler.assemble(input(seedWords = listOf("alpha")))
        val b = StudioRunAssembler.assemble(input(seedWords = listOf("beta")))
        assertTrue(a.plan.steps.first().payload != b.plan.steps.first().payload)
    }

    @Test
    fun rejectsEmptySeed() {
        var threw = false
        try {
            input(seedWords = emptyList())
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue(threw, "StudioRunInput must reject an empty seed word list")
    }
}
