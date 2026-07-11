package io.anvil.core.run

import io.anvil.core.artifacts.ArtifactManifest
import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.contracts.ContractRegistry

/**
 * Resolves [RunPlan] step ordering and input payloads from prior artifacts.
 *
 * Responsible for: dependency checks, cycle detection, missing-step blocking,
 * artifact-input resolution, contract-version checks, producer/consumer checks.
 * Not responsible for: module execution, retries, filesystem I/O, domain logic.
 */
class RunDependencyResolver(private val contracts: ContractRegistry) {

    /** Topologically orders [plan]'s steps by `dependsOn`. Throws [RunDependencyException] on cycles or missing refs. */
    fun order(plan: RunPlan): List<RunPlanStep> {
        val byId = plan.steps.associateBy { it.id }
        plan.steps.forEach { step ->
            val missing = step.dependsOn.filterNot { it in byId }
            if (missing.isNotEmpty()) {
                throw RunDependencyException("Step '${step.id}' depends on missing step(s): ${missing.joinToString()}")
            }
        }

        val ordered = mutableListOf<RunPlanStep>()
        val visited = mutableSetOf<String>()
        val visiting = mutableSetOf<String>()

        fun visit(step: RunPlanStep, path: List<String>) {
            if (step.id in visited) return
            if (step.id in visiting) {
                throw RunDependencyException("Dependency cycle detected: ${(path + step.id).joinToString(" -> ")}")
            }
            visiting += step.id
            step.dependsOn.forEach { depId -> visit(byId.getValue(depId), path + step.id) }
            visiting -= step.id
            visited += step.id
            ordered += step
        }

        plan.steps.forEach { visit(it, emptyList()) }
        return ordered
    }

    /**
     * Resolves the effective payload for [step] given [priorArtifacts] (already-produced
     * manifests keyed by their originating step id) and the [registry] of written artifact
     * payloads (step id -> raw payload string), enforcing contract producer/consumer rules.
     */
    fun resolveInput(
        step: RunPlanStep,
        priorArtifactsByStepId: Map<String, ArtifactManifest>,
        priorPayloadsByStepId: Map<String, String>,
        registry: ArtifactRegistry,
    ): String {
        step.inputContract?.let { ref -> contracts.requireConsumerAllowed(ref.id, ref.version, step.moduleId) }

        val selector = step.inputSelector ?: return step.payload
        return when (selector) {
            is RunInputSelector.InlinePayload -> selector.payload
            is RunInputSelector.ArtifactByStep -> {
                priorPayloadsByStepId[selector.stepId]
                    ?: throw RunDependencyException("No prior artifact payload recorded for step '${selector.stepId}'.")
            }
            is RunInputSelector.LatestArtifactByContract -> {
                val descriptor = contracts.requireSupported(selector.contractId, selector.version)
                val expectedType = "${descriptor.id.value}/v${descriptor.version}"
                val manifest = registry.artifacts
                    .filter { it.type == expectedType }
                    .maxByOrNull { it.createdAt }
                    ?: throw RunDependencyException(
                        "No artifact found in registry for contract ${selector.contractId.value}/v${selector.version}.",
                    )
                priorPayloadsByStepId[stepIdFor(manifest, priorArtifactsByStepId)]
                    ?: throw RunDependencyException(
                        "Artifact ${manifest.artifactId.value} matched contract but has no recorded payload.",
                    )
            }
        }
    }

    /** Enforces that [producerModuleId] may legally produce [outputContractId]/v[version]. */
    fun requireOutputAllowed(outputContract: RunContractRef?, producerModuleId: String) {
        outputContract?.let { ref -> contracts.requireProducerAllowed(ref.id, ref.version, producerModuleId) }
    }

    private fun stepIdFor(manifest: ArtifactManifest, priorArtifactsByStepId: Map<String, ArtifactManifest>): String =
        priorArtifactsByStepId.entries.firstOrNull { it.value.artifactId == manifest.artifactId }?.key
            ?: throw RunDependencyException("Artifact ${manifest.artifactId.value} is not tracked by step id.")
}

class RunDependencyException(message: String) : IllegalStateException(message)
