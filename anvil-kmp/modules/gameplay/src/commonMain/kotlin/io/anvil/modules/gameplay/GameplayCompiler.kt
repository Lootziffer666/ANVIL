package io.anvil.modules.gameplay

import io.anvil.core.contracts.BootResult
import io.anvil.core.contracts.ExecutionPhase
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameplayCompilerModule : ModuleSlotContract {
    override val name: String = "anvil-gameplay-compiler"
    override val purpose: String =
        "Compiles play grammar and capability casts into executable gameplay rule contracts."

    private var quality = QualityState.STABLE
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        classDiscriminator = "kind"
    }

    override fun qualityState(): QualityState = quality

    override suspend fun boot(ctx: ModuleContext): BootResult = BootResult(
        moduleId = name,
        qualityState = quality,
        executionPhase = ExecutionPhase.IDLE,
        message = "Gameplay Compiler booted; rule truth is isolated from BARD, SHADED, and Actor Runtime.",
    )

    override suspend fun handle(step: ModuleRunStep): StepResult = when (GameplayOperation.valueOf(step.operation)) {
        GameplayOperation.COMPILE -> {
            val plan = compile(json.decodeFromString<GameplayCompileRequest>(step.payload))
            val payload = json.encodeToString(plan)
            complete(step.context, GameplayPlan.SCHEMA, payload)
        }
        GameplayOperation.VALIDATE -> {
            val report = validate(json.decodeFromString<GameplayPlan>(step.payload))
            val payload = json.encodeToString(report)
            complete(step.context, GameplayValidationReport.SCHEMA, payload)
        }
    }

    fun compile(request: GameplayCompileRequest): GameplayPlan {
        require(request.schema == GameplayCompileRequest.SCHEMA) { "Unsupported compile request schema: ${request.schema}" }
        require(request.verbs.isNotEmpty()) { "Gameplay compile requires at least one verb." }
        require(request.capabilityCast.isNotEmpty()) { "Gameplay compile requires at least one capability." }

        val interactions = request.verbs.take(3).mapIndexed { index, verb ->
            val event = WorldEventId("EVT_${verb.uppercase()}_${index + 1}")
            InteractionDefinition(
                id = InteractionId("INT_${verb.uppercase()}_${index + 1}"),
                verb = verb,
                actorCapabilities = listOf(request.capabilityCast[index % request.capabilityCast.size]),
                targetTags = listOf("interactive", verb),
                conditions = listOf(
                    Condition.Distance(max = 1.5),
                    Condition.Capability(request.capabilityCast[index % request.capabilityCast.size]),
                ),
                effects = listOf(
                    Effect.PatchState(
                        StatePatch(
                            path = StatePath("world.interactions.$verb.count"),
                            operation = StatePatchOperation.INCREMENT,
                            value = "1",
                        ),
                    ),
                    Effect.Emit(event),
                ),
                recovery = RecoveryRule(
                    summary = request.recoveryModel,
                    failureEffects = listOf(
                        Effect.PatchState(
                            StatePatch(
                                path = StatePath("world.recovery.$verb.available"),
                                operation = StatePatchOperation.SET,
                                value = "true",
                            ),
                        ),
                    ),
                    newOptions = request.playerRoles.map { role -> "$role can recover from failed $verb" },
                ),
            )
        }

        return GameplayPlan(
            planId = GameplayPlanId("GPL_${stableHash(request.requestId + request.creativeBriefRef)}"),
            creativeBriefRef = request.creativeBriefRef,
            productionIntentRef = request.productionIntentRef,
            interactions = interactions,
            worldEvents = interactions.flatMap { interaction ->
                interaction.effects.filterIsInstance<Effect.Emit>().map { emit ->
                    WorldEvent(
                        id = emit.event,
                        name = emit.event.value,
                        payloadPaths = interaction.effects.filterIsInstance<Effect.PatchState>().map { it.patch.path },
                    )
                }
            },
            proofRequirements = interactions.mapIndexed { index, interaction ->
                GameplayProofRequirement(
                    id = ProofRequirementId("GPR_${interaction.id.value}"),
                    interactionRef = interaction.id,
                    requiredEvidence = listOf(
                        GameplayEvidenceKind.STATE_BEFORE,
                        GameplayEvidenceKind.INPUT_ACTION,
                        GameplayEvidenceKind.STATE_AFTER,
                        GameplayEvidenceKind.WORLD_EVENT,
                    ),
                    acceptanceCriterionRef = request.acceptanceCriteria.getOrNull(index),
                )
            },
        )
    }

    fun validate(plan: GameplayPlan): GameplayValidationReport {
        val checks = listOf(
            check("has-interactions", plan.interactions.isNotEmpty(), "GameplayPlan must define executable interactions."),
            check("effects-change-state", plan.interactions.all { interaction ->
                interaction.effects.any { it is Effect.PatchState || it is Effect.Emit }
            }, "Every interaction must change state or emit a world event."),
            check("proofs-defined", plan.proofRequirements.isNotEmpty(), "GameplayPlan must define proof requirements for CUE."),
            check("no-visual-ownership", plan.rulesOwner == name, "GameplayPlan owns rules only, not visual or actor-belief truth."),
        )
        return GameplayValidationReport(
            planRef = plan.planId,
            passed = checks.filter { it.passed },
            failed = checks.filterNot { it.passed },
        )
    }

    private fun complete(context: ModuleContext, type: String, payload: String): StepResult.Completed {
        val hash = stableHash(payload)
        return StepResult.Completed(
            artifact = ModuleArtifactRef(
                id = "ART_GAMEPLAY_$hash",
                workspaceId = context.workspaceId,
                runId = context.runId,
                moduleOrigin = name,
                type = type,
                uri = "${context.artifactRoot}/gameplay/$hash.json",
                sha256 = "sha256:$hash",
                timestamp = "pending-artifact-writer-timestamp",
            ),
            payload = payload,
            executionPhase = ExecutionPhase.COMPLETE,
        )
    }

    private fun check(id: String, passed: Boolean, message: String) = GameplayValidationCheck(id, passed, message)

    private fun stableHash(text: String): String = text.fold(0) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }.toString(16)
}
