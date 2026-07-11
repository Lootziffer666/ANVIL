package io.anvil.modules.gameplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GameplayOperation { COMPILE, VALIDATE }

@JvmInline
@Serializable
value class GameplayPlanId(val value: String)

@JvmInline
@Serializable
value class InteractionId(val value: String)

@JvmInline
@Serializable
value class WorldEventId(val value: String)

@JvmInline
@Serializable
value class StatePath(val value: String)

@JvmInline
@Serializable
value class ProofRequirementId(val value: String)

@Serializable
data class GameplayCompileRequest(
    val schema: String = SCHEMA,
    val requestId: String,
    val workspaceId: String,
    val runId: String,
    val creativeBriefRef: String,
    val productionIntentRef: String,
    val verbs: List<String>,
    val playerRoles: List<String>,
    val worldChangingActions: List<String>,
    val recoveryModel: String,
    val capabilityCast: List<String>,
    val acceptanceCriteria: List<String>,
) {
    companion object { const val SCHEMA = "anvil.gameplay.compile-request/v1" }
}

@Serializable
data class GameplayPlan(
    val schema: String = SCHEMA,
    val planId: GameplayPlanId,
    val creativeBriefRef: String,
    val productionIntentRef: String,
    val interactions: List<InteractionDefinition>,
    val worldEvents: List<WorldEvent>,
    val proofRequirements: List<GameplayProofRequirement>,
    val rulesOwner: String = "anvil-gameplay-compiler",
) {
    companion object { const val SCHEMA = "anvil.gameplay.plan/v1" }
}

@Serializable
data class InteractionDefinition(
    val schema: String = SCHEMA,
    val id: InteractionId,
    val verb: String,
    val actorCapabilities: List<String>,
    val targetTags: List<String>,
    val conditions: List<Condition>,
    val effects: List<Effect>,
    val recovery: RecoveryRule,
) {
    companion object { const val SCHEMA = "anvil.gameplay.interaction/v1" }
}

@Serializable
sealed class Condition {
    abstract val type: String

    @Serializable
    @SerialName("distance")
    data class Distance(val max: Double) : Condition() { override val type: String = "distance" }

    @Serializable
    @SerialName("handsFree")
    data class HandsFree(val value: Boolean) : Condition() { override val type: String = "handsFree" }

    @Serializable
    @SerialName("capability")
    data class Capability(val capability: String) : Condition() { override val type: String = "capability" }

    @Serializable
    @SerialName("worldStateEquals")
    data class WorldStateEquals(val path: StatePath, val value: String) : Condition() {
        override val type: String = "worldStateEquals"
    }
}

@Serializable
sealed class Effect {
    abstract val type: String

    @Serializable
    @SerialName("inventory.attach")
    data class InventoryAttach(val slot: String) : Effect() { override val type: String = "inventory.attach" }

    @Serializable
    @SerialName("actor.movementModifier")
    data class ActorMovementModifier(val multiplier: Double) : Effect() {
        override val type: String = "actor.movementModifier"
    }

    @Serializable
    @SerialName("state.patch")
    data class PatchState(val patch: StatePatch) : Effect() { override val type: String = "state.patch" }

    @Serializable
    @SerialName("emit")
    data class Emit(val event: WorldEventId) : Effect() { override val type: String = "emit" }
}

@Serializable
data class StatePatch(
    val schema: String = SCHEMA,
    val path: StatePath,
    val operation: StatePatchOperation,
    val value: String,
) {
    companion object { const val SCHEMA = "anvil.gameplay.state-patch/v1" }
}

@Serializable
enum class StatePatchOperation { SET, INCREMENT, APPEND, REMOVE }

@Serializable
data class WorldEvent(
    val schema: String = SCHEMA,
    val id: WorldEventId,
    val name: String,
    val payloadPaths: List<StatePath>,
) {
    companion object { const val SCHEMA = "anvil.gameplay.world-event/v1" }
}

@Serializable
data class RecoveryRule(
    val summary: String,
    val failureEffects: List<Effect>,
    val newOptions: List<String>,
)

@Serializable
data class GameplayProofRequirement(
    val schema: String = SCHEMA,
    val id: ProofRequirementId,
    val interactionRef: InteractionId,
    val requiredEvidence: List<GameplayEvidenceKind>,
    val acceptanceCriterionRef: String? = null,
) {
    companion object { const val SCHEMA = "anvil.gameplay.proof-requirement/v1" }
}

@Serializable
enum class GameplayEvidenceKind {
    STATE_BEFORE,
    INPUT_ACTION,
    STATE_AFTER,
    WORLD_EVENT,
    RECOVERY_OPTION,
    SCREENSHOT_REF,
}

@Serializable
data class GameplayValidationReport(
    val schema: String = SCHEMA,
    val planRef: GameplayPlanId,
    val passed: List<GameplayValidationCheck>,
    val failed: List<GameplayValidationCheck>,
) {
    companion object { const val SCHEMA = "anvil.gameplay.validation-report/v1" }
}

@Serializable
data class GameplayValidationCheck(
    val id: String,
    val passed: Boolean,
    val message: String,
)
