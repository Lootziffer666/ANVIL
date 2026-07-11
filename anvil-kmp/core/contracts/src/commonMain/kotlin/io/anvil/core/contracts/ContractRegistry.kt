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

@Serializable
data class ContractRegistry(
    val schema: String = SCHEMA,
    val contracts: List<ContractDescriptor>,
) {
    fun descriptorFor(id: ContractId, version: Int): ContractDescriptor? =
        contracts.firstOrNull { it.id == id && it.version == version }

    fun requireSupported(id: ContractId, version: Int): ContractDescriptor =
        descriptorFor(id, version) ?: error(
            "Unsupported contract ${id.value}/v$version. Supported: " +
                contracts.joinToString { "${it.id.value}/v${it.version}" },
        )

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
            descriptor("anvil.scene-bundle", ContractOwner.SCENE, listOf("scene"), listOf("target", "cue", "shaded")),
            descriptor("anvil.interface.bundle", ContractOwner.INTERFACE, listOf("interface"), listOf("target", "cue")),
            descriptor("anvil.audio-cue-graph", ContractOwner.ACOUSTIC, listOf("acoustic"), listOf("target", "cue")),
            descriptor("anvil.runnable-build", ContractOwner.TARGET, listOf("target"), listOf("cue", "commander")),
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
