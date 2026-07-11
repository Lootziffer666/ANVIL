package io.anvil.core.contracts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ContractRegistryTest {

    @Test
    fun defaultRegistry_hasNoDuplicateIdVersionPairs() {
        assertEquals(emptyList(), AnvilContractRegistry.default.validateRegistry())
    }

    @Test
    fun requireSupported_knownVersion_returnsDescriptor() {
        val descriptor = AnvilContractRegistry.default.requireSupported(ContractId("anvil.gameplay.plan"), 1)
        assertEquals(ContractOwner.GAMEPLAY, descriptor.owner)
    }

    @Test
    fun requireSupported_unknownVersion_throws() {
        assertFailsWith<ContractViolationException> {
            AnvilContractRegistry.default.requireSupported(ContractId("anvil.gameplay.plan"), 99)
        }
    }

    @Test
    fun requireSupported_unknownId_throws() {
        assertFailsWith<ContractViolationException> {
            AnvilContractRegistry.default.requireSupported(ContractId("anvil.does-not-exist"), 1)
        }
    }

    @Test
    fun requireProducerAllowed_forbiddenProducer_throws() {
        assertFailsWith<ContractViolationException> {
            AnvilContractRegistry.default.requireProducerAllowed(ContractId("anvil.gameplay.plan"), 1, "scene")
        }
    }

    @Test
    fun requireProducerAllowed_allowedProducer_returnsDescriptor() {
        val descriptor = AnvilContractRegistry.default.requireProducerAllowed(ContractId("anvil.gameplay.plan"), 1, "gameplay")
        assertEquals(ContractId("anvil.gameplay.plan"), descriptor.id)
    }

    @Test
    fun requireConsumerAllowed_forbiddenConsumer_throws() {
        assertFailsWith<ContractViolationException> {
            AnvilContractRegistry.default.requireConsumerAllowed(ContractId("cue.audio-proof"), 1, "gameplay")
        }
    }

    @Test
    fun requireConsumerAllowed_allowedConsumer_returnsDescriptor() {
        val descriptor = AnvilContractRegistry.default.requireConsumerAllowed(ContractId("cue.audio-proof"), 1, "bard")
        assertEquals(ContractOwner.CUE, descriptor.owner)
    }

    @Test
    fun validateRegistry_detectsDuplicateIdVersion() {
        val duplicated = ContractRegistry(
            contracts = listOf(
                ContractDescriptor(ContractId("dup.contract"), 1, ContractOwner.ANVIL, listOf("a"), listOf("b")),
                ContractDescriptor(ContractId("dup.contract"), 1, ContractOwner.ANVIL, listOf("a"), listOf("b")),
            ),
        )
        val duplicates = duplicated.validateRegistry()
        assertTrue(duplicates.contains("dup.contract/v1"))
    }

    @Test
    fun requireConsumerAllowed_wizardProductionAssessment_allowsAnvilItself() {
        // Real Golden Run R-06: WizardHttpAdapter (core:externaladapters) consumes this
        // contract directly inside ANVIL, not only via a downstream run-plan step.
        val descriptor = AnvilContractRegistry.default.requireConsumerAllowed(
            ContractId("anvil.wizard.production-assessment"), 1, "anvil",
        )
        assertEquals(ContractOwner.WIZARD, descriptor.owner)
    }

    @Test
    fun requireConsumerAllowed_wizardProductionAssessment_stillAllowsExistingDownstreamConsumers() {
        val id = ContractId("anvil.wizard.production-assessment")
        listOf("gameplay", "scene", "interface", "acoustic", "target", "cue").forEach { consumer ->
            AnvilContractRegistry.default.requireConsumerAllowed(id, 1, consumer)
        }
    }

    @Test
    fun newExternalSeamContracts_areRegisteredWithSingleOwner() {
        val ids = listOf(
            "anvil.wizard.production-assessment" to ContractOwner.WIZARD,
            "anvil.wizard.capability-cast" to ContractOwner.WIZARD,
            "swift.actor-bundle" to ContractOwner.SWIFT,
            "swift.render-result" to ContractOwner.SWIFT,
            "shaded.scene-config" to ContractOwner.SHADED,
            "shaded.actor-binding" to ContractOwner.SHADED,
            "cue.playable-proof" to ContractOwner.CUE,
            "cue.temporal-proof" to ContractOwner.CUE,
            "cue.audio-proof" to ContractOwner.CUE,
            "anvil.audio-asset-manifest" to ContractOwner.ACOUSTIC,
            "anvil.web-audio-runtime-bundle" to ContractOwner.ACOUSTIC,
        )
        ids.forEach { (id, owner) ->
            val descriptor = AnvilContractRegistry.default.requireSupported(ContractId(id), 1)
            assertEquals(owner, descriptor.owner, "Contract $id must have exactly one owner: $owner")
        }
    }
}
