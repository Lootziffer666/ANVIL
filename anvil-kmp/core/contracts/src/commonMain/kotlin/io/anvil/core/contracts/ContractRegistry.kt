package io.anvil.core.contracts

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ContractId(val value: String)

@Serializable
enum class ContractOwner {
    ANVIL,
    BARD,
    WIZARD,
    GAMEPLAY,
    SCENE,
    INTERFACE,
    ACOUSTIC,
    TARGET,
    CUE,
    BELLOWS,
    SWIFT,
    SHADED,
}

@Serializable
data class ContractDescriptor(
    val id: ContractId,
    val version: Int,
    val owner: ContractOwner,
    val allowedProducers: List<String>,
    val allowedConsumers: List<String>,
    val failClosedOnUnknownVersion: Boolean = true,
)

class ContractViolationException(message: String) : IllegalStateException(message)

@Serializable
data class ContractRegistry(
    val schema: String = SCHEMA,
    val contracts: List<ContractDescriptor>,
) {
    fun descriptorFor(id: ContractId, version: Int): ContractDescriptor? =
        contracts.firstOrNull { it.id == id && it.version == version }

    /** Fail-closed lookup: throws for unsupported or unknown contract/version combinations. */
    fun requireSupported(id: ContractId, version: Int): ContractDescriptor =
        descriptorFor(id, version) ?: throw ContractViolationException(
            "Unsupported contract ${id.value}/v$version. Supported: " +
                contracts.joinToString { "${it.id.value}/v${it.version}" },
        )

    /** Throws unless [producer] is an allowed producer of [id]/v[version]. */
    fun requireProducerAllowed(id: ContractId, version: Int, producer: String): ContractDescriptor {
        val descriptor = requireSupported(id, version)
        if (producer !in descriptor.allowedProducers) {
            throw ContractViolationException(
                "Producer '$producer' is not allowed to produce ${id.value}/v$version. " +
                    "Allowed producers: ${descriptor.allowedProducers.joinToString()}",
            )
        }
        return descriptor
    }

    /** Throws unless [consumer] is an allowed consumer of [id]/v[version]. */
    fun requireConsumerAllowed(id: ContractId, version: Int, consumer: String): ContractDescriptor {
        val descriptor = requireSupported(id, version)
        if (consumer !in descriptor.allowedConsumers) {
            throw ContractViolationException(
                "Consumer '$consumer' is not allowed to consume ${id.value}/v$version. " +
                    "Allowed consumers: ${descriptor.allowedConsumers.joinToString()}",
            )
        }
        return descriptor
    }

    /**
     * Structural self-check of the registry itself (not of a single lookup).
     * Returns every duplicate `(id, version)` combination found; an empty list means the
     * registry is internally consistent.
     */
    fun validateRegistry(): List<String> {
        val seen = mutableSetOf<Pair<String, Int>>()
        val duplicates = mutableListOf<String>()
        for (descriptor in contracts) {
            val key = descriptor.id.value to descriptor.version
            if (!seen.add(key)) duplicates += "${descriptor.id.value}/v${descriptor.version}"
        }
        return duplicates
    }

    companion object { const val SCHEMA = "anvil.contract-registry/v1" }
}

object AnvilContractRegistry {
    val default = ContractRegistry(
        contracts = listOf(
            descriptor("anvil.artifact.manifest", ContractOwner.ANVIL, listOf("artifact-writer"), listOf("run-surface", "handoff", "sync", "cue")),
            descriptor("anvil.artifact.registry", ContractOwner.ANVIL, listOf("artifact-writer", "run-surface"), listOf("handoff", "sync", "cue", "commander")),
            descriptor("anvil.run.plan", ContractOwner.ANVIL, listOf("commander", "anvil"), listOf("run-surface")),
            descriptor("anvil.run.summary", ContractOwner.ANVIL, listOf("run-surface"), listOf("commander", "handoff", "sync", "cue")),
            descriptor("anvil.handoff.export-request", ContractOwner.ANVIL, listOf("commander", "run-surface"), listOf("handoff")),
            descriptor("anvil.handoff.package", ContractOwner.ANVIL, listOf("handoff"), listOf("commander", "external-agent")),
            descriptor("anvil.workspace-sync.bundle", ContractOwner.ANVIL, listOf("sync"), listOf("sync", "commander")),
            descriptor("anvil.workspace-sync.merge-report", ContractOwner.ANVIL, listOf("sync"), listOf("commander")),
            descriptor("anvil.bard.creative-seed", ContractOwner.BARD, listOf("commander"), listOf("external-bard")),
            descriptor("anvil.bard.creative-brief", ContractOwner.BARD, listOf("external-bard"), listOf("wizard", "gameplay", "scene", "cue")),
            descriptor("anvil.bard.production-intent", ContractOwner.BARD, listOf("external-bard"), listOf("wizard", "gameplay", "scene", "interface", "acoustic", "target")),
            descriptor("anvil.gameplay.plan", ContractOwner.GAMEPLAY, listOf("gameplay"), listOf("scene", "interface", "acoustic", "target", "cue")),
            // "interface" and "acoustic" added here (Gate I Golden Run): both InterfaceIntent
            // and AudioIntent carry a `sceneBundleRef` field in their real module models, so
            // both roles must be allowed consumers, not just downstream target/cue/shaded.
            descriptor("anvil.scene-bundle", ContractOwner.SCENE, listOf("scene"), listOf("target", "cue", "shaded", "interface", "acoustic")),
            descriptor("anvil.interface.bundle", ContractOwner.INTERFACE, listOf("interface"), listOf("target", "cue")),
            descriptor("anvil.audio-cue-graph", ContractOwner.ACOUSTIC, listOf("acoustic"), listOf("target", "cue")),
            descriptor("anvil.runnable-build", ContractOwner.TARGET, listOf("target"), listOf("cue", "commander")),

            // ── Gate B-01: externe Studio-Nähte (Fable-Reparaturauftrag) ────────────
            // "anvil" added (Real Golden Run R-06): WizardHttpAdapter (core:externaladapters)
            // now consumes this contract directly inside ANVIL itself, before any run-plan
            // step relays it onward — previously this was a pure downstream-relay contract.
            descriptor("anvil.wizard.production-assessment", ContractOwner.WIZARD, listOf("wizard"), listOf("anvil", "gameplay", "scene", "interface", "acoustic", "target", "cue")),
            descriptor("anvil.wizard.capability-cast", ContractOwner.WIZARD, listOf("wizard"), listOf("gameplay", "target")),

            descriptor("swift.actor-bundle", ContractOwner.SWIFT, listOf("swift"), listOf("target", "shaded", "cue")),
            descriptor("swift.render-result", ContractOwner.SWIFT, listOf("swift"), listOf("target", "cue")),

            descriptor("shaded.scene-config", ContractOwner.SHADED, listOf("shaded"), listOf("target", "cue")),
            descriptor("shaded.actor-binding", ContractOwner.SHADED, listOf("shaded"), listOf("target", "cue")),

            descriptor("cue.playable-proof", ContractOwner.CUE, listOf("cue"), listOf("bard", "commander")),
            descriptor("cue.temporal-proof", ContractOwner.CUE, listOf("cue"), listOf("bard", "commander")),
            descriptor("cue.audio-proof", ContractOwner.CUE, listOf("cue"), listOf("bard", "commander")),

            descriptor("anvil.audio-asset-manifest", ContractOwner.ACOUSTIC, listOf("acoustic-producer"), listOf("acoustic", "target", "cue")),
            descriptor("anvil.web-audio-runtime-bundle", ContractOwner.ACOUSTIC, listOf("acoustic"), listOf("target", "cue")),
        ),
    )

    private fun descriptor(
        id: String,
        owner: ContractOwner,
        producers: List<String>,
        consumers: List<String>,
    ) = ContractDescriptor(
        id = ContractId(id),
        version = 1,
        owner = owner,
        allowedProducers = producers,
        allowedConsumers = consumers,
    )
}
