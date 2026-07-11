package io.anvil.core.run

import io.anvil.core.artifacts.ArtifactManifest
import io.anvil.core.artifacts.ArtifactOrigin
import io.anvil.core.artifacts.ArtifactId
import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.artifacts.RunId
import io.anvil.core.artifacts.WorkspaceId
import io.anvil.core.contracts.AnvilContractRegistry
import io.anvil.core.contracts.ContractId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RunDependencyResolverTest {

    private val resolver = RunDependencyResolver(AnvilContractRegistry.default)

    private fun step(id: String, dependsOn: List<String> = emptyList()) =
        RunPlanStep(id = id, moduleId = "gameplay", operation = "COMPILE", payload = "{}", dependsOn = dependsOn)

    @Test
    fun order_linearChain_ordersByDependency() {
        val plan = RunPlan(
            planId = RunPlanId("P"), workspaceId = "ws", runId = "r", artifactRoot = "root",
            steps = listOf(step("S3", listOf("S2")), step("S1"), step("S2", listOf("S1"))),
        )
        assertEquals(listOf("S1", "S2", "S3"), resolver.order(plan).map { it.id })
    }

    @Test
    fun order_independentSteps_preservesRelativeOrderWithoutForcingDependency() {
        val plan = RunPlan(
            planId = RunPlanId("P"), workspaceId = "ws", runId = "r", artifactRoot = "root",
            steps = listOf(step("A"), step("B")),
        )
        assertEquals(listOf("A", "B"), resolver.order(plan).map { it.id })
    }

    @Test
    fun order_missingDependency_throws() {
        val plan = RunPlan(
            planId = RunPlanId("P"), workspaceId = "ws", runId = "r", artifactRoot = "root",
            steps = listOf(step("S1", listOf("GHOST"))),
        )
        assertFailsWith<RunDependencyException> { resolver.order(plan) }
    }

    @Test
    fun order_cycle_throws() {
        val plan = RunPlan(
            planId = RunPlanId("P"), workspaceId = "ws", runId = "r", artifactRoot = "root",
            steps = listOf(step("S1", listOf("S2")), step("S2", listOf("S1"))),
        )
        assertFailsWith<RunDependencyException> { resolver.order(plan) }
    }

    @Test
    fun resolveInput_forbiddenConsumerContract_throws() {
        val s = step("S1").copy(
            moduleId = "bard",
            inputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 1),
        )
        assertFailsWith<Exception> {
            resolver.resolveInput(s, emptyMap(), emptyMap(), ArtifactRegistry())
        }
    }

    @Test
    fun resolveInput_unknownContractVersion_throws() {
        val s = step("S1").copy(inputSelector = RunInputSelector.LatestArtifactByContract(ContractId("anvil.gameplay.plan"), 99))
        assertFailsWith<Exception> {
            resolver.resolveInput(s, emptyMap(), emptyMap(), ArtifactRegistry())
        }
    }

    @Test
    fun resolveInput_artifactByStep_returnsPriorPayload() {
        val s = step("S2").copy(inputSelector = RunInputSelector.ArtifactByStep("S1"))
        val payload = resolver.resolveInput(s, emptyMap(), mapOf("S1" to "{\"x\":1}"), ArtifactRegistry())
        assertEquals("{\"x\":1}", payload)
    }

    @Test
    fun resolveInput_latestArtifactByContract_findsMatchingManifest() {
        val manifest = ArtifactManifest(
            artifactId = ArtifactId("ART_1"),
            createdAt = "2026-07-11T00:00:00Z",
            origin = ArtifactOrigin("anvil-gameplay-compiler", WorkspaceId("ws"), RunId("r")),
            type = "anvil.gameplay.plan/v1",
            uri = "artifact://gameplay/1.json",
            sizeBytes = 2,
            checksumSha256 = "sha256:" + "0".repeat(64),
        )
        val registry = ArtifactRegistry(artifacts = listOf(manifest))
        val s = step("S2").copy(inputSelector = RunInputSelector.LatestArtifactByContract(ContractId("anvil.gameplay.plan"), 1))
        val payload = resolver.resolveInput(
            s,
            priorArtifactsByStepId = mapOf("S1" to manifest),
            priorPayloadsByStepId = mapOf("S1" to "{\"plan\":true}"),
            registry = registry,
        )
        assertEquals("{\"plan\":true}", payload)
    }
}
