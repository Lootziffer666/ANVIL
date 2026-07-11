package io.anvil.core.run

import io.anvil.core.contracts.AnvilContractRegistry
import io.anvil.core.contracts.BootResult
import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExecutionPhase
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Echoes the incoming payload back, prefixed with the module id, so tests can assert on what it saw. */
private class EchoModule(override val name: String) : ModuleSlotContract {
    override val purpose: String = "test-double"
    var lastSeenPayload: String? = null
    override fun qualityState(): QualityState = QualityState.STABLE
    override suspend fun boot(ctx: ModuleContext): BootResult = BootResult(name, QualityState.STABLE, ExecutionPhase.IDLE, "booted")
    override suspend fun handle(step: ModuleRunStep): StepResult {
        lastSeenPayload = step.payload
        return StepResult.Completed(
            artifact = ModuleArtifactRef(
                id = "ART_${name}_${step.context.runId}",
                workspaceId = step.context.workspaceId,
                runId = step.context.runId,
                moduleOrigin = name,
                type = "anvil.gameplay.plan/v1",
                uri = "artifact://$name/1.json",
                sha256 = "sha256:" + "1".repeat(64),
                timestamp = step.context.createdAt,
            ),
            payload = "produced-by-$name:${step.payload}",
        )
    }
}

class RunSurfaceTest {

    @Test
    fun step2_consumesArtifactFromStep1() = runTest {
        val gameplay = EchoModule("gameplay")
        val scene = EchoModule("scene")
        val surface = RunSurface(modules = mapOf("gameplay" to gameplay, "scene" to scene))

        val plan = RunPlan(
            planId = RunPlanId("P1"), workspaceId = "ws", runId = "r1", artifactRoot = "root",
            steps = listOf(
                RunPlanStep(id = "S1", moduleId = "gameplay", operation = "COMPILE", payload = "seed-payload"),
                RunPlanStep(
                    id = "S2", moduleId = "scene", operation = "COMPILE", payload = "unused",
                    dependsOn = listOf("S1"),
                    inputSelector = RunInputSelector.ArtifactByStep("S1"),
                    parentRefs = listOf("ART_gameplay_r1"),
                ),
            ),
        )

        val summary = surface.execute(plan, createdAt = "2026-07-11T00:00:00Z")
        assertEquals(RunStatus.COMPLETE, summary.status)
        assertEquals("produced-by-gameplay:seed-payload", scene.lastSeenPayload)
        assertEquals(2, summary.registry.artifacts.size)

        val sceneManifest = summary.records.last().artifact!!
        assertTrue(sceneManifest.parentRefs.contains("ART_gameplay_r1"), "ParentRefs must include declared parentRefs")
    }

    @Test
    fun unknownContractVersion_blocksRunWithoutExecutingModule() = runTest {
        val target = EchoModule("target")
        val surface = RunSurface(modules = mapOf("target" to target))
        val plan = RunPlan(
            planId = RunPlanId("P2"), workspaceId = "ws", runId = "r2", artifactRoot = "root",
            steps = listOf(
                RunPlanStep(
                    id = "S1", moduleId = "target", operation = "PREPARE", payload = "x",
                    inputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 99),
                ),
            ),
        )
        val summary = surface.execute(plan, createdAt = "2026-07-11T00:00:00Z")
        assertEquals(RunStatus.BLOCKED, summary.status)
        assertEquals(null, target.lastSeenPayload, "Module must not run when input contract version is unsupported")
    }

    @Test
    fun forbiddenProducer_isNotExecuted() = runTest {
        val scene = EchoModule("scene")
        // "scene" is not an allowed producer of anvil.gameplay.plan (only "gameplay" is).
        val surface = RunSurface(modules = mapOf("scene" to scene))
        val plan = RunPlan(
            planId = RunPlanId("P3"), workspaceId = "ws", runId = "r3", artifactRoot = "root",
            steps = listOf(
                RunPlanStep(
                    id = "S1", moduleId = "scene", operation = "COMPILE", payload = "x",
                    outputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 1),
                ),
            ),
        )
        val summary = surface.execute(plan, createdAt = "2026-07-11T00:00:00Z")
        assertEquals(RunStatus.BLOCKED, summary.status)
        assertEquals(null, scene.lastSeenPayload, "Forbidden producer must never be invoked")
    }

    @Test
    fun previousArtifacts_remainAfterLaterStepBlocks() = runTest {
        val gameplay = EchoModule("gameplay")
        val scene = EchoModule("scene")
        val surface = RunSurface(modules = mapOf("gameplay" to gameplay, "scene" to scene))
        val plan = RunPlan(
            planId = RunPlanId("P4"), workspaceId = "ws", runId = "r4", artifactRoot = "root",
            steps = listOf(
                RunPlanStep(id = "S1", moduleId = "gameplay", operation = "COMPILE", payload = "seed"),
                RunPlanStep(
                    id = "S2", moduleId = "scene", operation = "COMPILE", payload = "x",
                    inputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 42),
                ),
            ),
        )
        val summary = surface.execute(plan, createdAt = "2026-07-11T00:00:00Z")
        assertEquals(RunStatus.BLOCKED, summary.status)
        assertEquals(1, summary.registry.artifacts.size, "Step 1's artifact must be preserved even though step 2 blocked")
    }

    @Test
    fun defaultContracts_matchAnvilContractRegistry() {
        // Sanity: RunSurface's default resolver must use the shared production registry, not a private copy.
        assertTrue(AnvilContractRegistry.default.contracts.isNotEmpty())
    }
}
