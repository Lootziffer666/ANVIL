package io.anvil.modules.bard

import io.anvil.core.contracts.BootResult
import io.anvil.core.contracts.ExecutionPhase
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BardModule(
    private val profile: BardProfile = BardProfile(
        id = "mini-me",
        version = "1.0.0",
        checksum = "sha256:profile-checksum-required-at-packaging",
    ),
) : ModuleSlotContract {
    override val name: String = "anvil-bard"
    override val purpose: String =
        "Meaning compiler: transforms human intuition into versioned creative contracts for WIZARD, ANVIL, and CUE evidence review."

    private var quality = QualityState.STABLE
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun qualityState(): QualityState = quality

    override suspend fun boot(ctx: ModuleContext): BootResult {
        quality = if (ctx.privacyMode == PrivacyMode.LOCAL_ONLY) QualityState.STABLE else QualityState.DEGRADED
        return BootResult(
            moduleId = name,
            qualityState = quality,
            executionPhase = ExecutionPhase.IDLE,
            message = "ANVIL-BARD booted as creative-intent compiler; private profile content is not exported.",
        )
    }

    override suspend fun handle(step: ModuleRunStep): StepResult = when (BardOperation.valueOf(step.operation)) {
        BardOperation.GENERATE -> complete(
            step = step,
            type = CreativeVariants.SCHEMA,
            payload = json.encodeToString(generate(json.decodeFromString<CreativeSeed>(step.payload))),
            phase = ExecutionPhase.GENERATING,
        )
        BardOperation.LOCK -> complete(
            step = step,
            type = CreativeBrief.SCHEMA,
            payload = json.encodeToString(lock(json.decodeFromString<LockRequest>(step.payload))),
            phase = ExecutionPhase.VALIDATING,
        )
        BardOperation.REVISE -> complete(
            step = step,
            type = CreativeBrief.SCHEMA,
            payload = json.encodeToString(revise(json.decodeFromString<RevisionRequest>(step.payload))),
            phase = ExecutionPhase.VALIDATING,
        )
        BardOperation.CHALLENGE -> complete(
            step = step,
            type = BardChallengeReport.SCHEMA,
            payload = json.encodeToString(challenge(json.decodeFromString<CreativeBrief>(step.payload))),
            phase = ExecutionPhase.VALIDATING,
        )
        BardOperation.AUDIT -> complete(
            step = step,
            type = CreativeFidelityReport.SCHEMA,
            payload = json.encodeToString(audit(json.decodeFromString<CreativeFidelityInput>(step.payload))),
            phase = ExecutionPhase.VALIDATING,
        )
    }

    fun generate(seed: CreativeSeed): CreativeVariants {
        require(seed.schema == CreativeSeed.SCHEMA) { "Unsupported seed schema: ${seed.schema}" }
        require(seed.privacyMode == PrivacyMode.LOCAL_ONLY) { "BARD MVP requires LOCAL_ONLY." }
        val assumptions = assumptionsFor(seed)
        return CreativeVariants(
            seedRef = seed.seedId,
            profile = profile.publicRef(),
            variants = listOf(
                variant(seed, "shared-mischief", "asymmetric cooperation", "Players create recoverable trouble together."),
                variant(seed, "quiet-rescue", "emotional recovery", "Players read the world, split roles, and rescue meaning from mistakes."),
                variant(seed, "chaos-contract", "world reaction", "The world treats every ignored cue as a remembered social event."),
            ),
            assumptions = assumptions,
        )
    }

    fun lock(request: LockRequest): CreativeBrief {
        val selected = request.variants.variants.first { it.id == request.selectedVariantId }
        return briefFrom(request.variants.seedRef, selected, request.parentArtifactId, CreativeBriefStatus.LOCKED)
    }

    fun revise(request: RevisionRequest): CreativeBrief = when (assessmentDecision(request.assessment)) {
        BardProductionDecision.ACCEPT -> request.brief.copy(version = request.brief.version + 1)
        BardProductionDecision.ADAPT -> request.brief.copy(
            version = request.brief.version + 1,
            status = CreativeBriefStatus.DRAFT,
            openQuestions = request.brief.openQuestions + "Production feedback requires non-core adaptation.",
        )
        BardProductionDecision.ESCALATE -> request.brief.copy(
            version = request.brief.version + 1,
            status = CreativeBriefStatus.DRAFT,
            openQuestions = request.brief.openQuestions + "Human review required: production feedback touches a core or structural lock.",
        )
        BardProductionDecision.REJECT -> request.brief.copy(
            version = request.brief.version + 1,
            openQuestions = request.brief.openQuestions + "Rejected substitution contradicts the creative contract.",
        )
    }

    fun challenge(brief: CreativeBrief): BardChallengeReport {
        val checks = listOf(
            challengeCheck("mini-me-profile-ref", brief.profile.id == profile.id, "Only public profile reference is exported."),
            challengeCheck("causality-intact", brief.worldGrammar.laws.isNotEmpty(), "World laws must explain reactions."),
            challengeCheck("not-linearized", brief.playGrammar.coreLoop.size >= 3, "Brief must define repeatable play, not only plot."),
            challengeCheck("absurdity-has-cause", brief.worldGrammar.forbiddenShortcuts.contains("Plot progress without observable cause"), "Absurdity cannot replace cause."),
            challengeCheck("no-technical-ready", true, "BARD does not issue CUE readiness verdicts."),
        )
        return BardChallengeReport(briefRef = brief.briefId, passed = checks.filter { it.passed }, failed = checks.filterNot { it.passed })
    }

    fun audit(input: CreativeFidelityInput): CreativeFidelityReport {
        val criteria = input.brief.acceptanceCriteria.map { criterion ->
            val refs = input.proofRefs.filter { it.contains(criterion.id) }
            FidelityCriterionResult(
                id = criterion.id,
                status = if (refs.isEmpty()) FidelityStatus.INSUFFICIENT_EVIDENCE else FidelityStatus.PASS,
                evidenceRefs = refs,
            )
        }
        return CreativeFidelityReport(
            briefRef = input.brief.briefId,
            proofRefs = input.proofRefs,
            verdict = if (criteria.any { it.status == FidelityStatus.INSUFFICIENT_EVIDENCE }) {
                FidelityVerdict.INSUFFICIENT_EVIDENCE
            } else {
                FidelityVerdict.ALIGNED
            },
            passed = criteria.filter { it.status == FidelityStatus.PASS },
            failed = criteria.filter { it.status == FidelityStatus.FAIL },
            insufficientEvidence = criteria.filter { it.status == FidelityStatus.INSUFFICIENT_EVIDENCE },
            recommendedRevisions = emptyList(),
        )
    }

    private fun complete(step: ModuleRunStep, type: String, payload: String, phase: ExecutionPhase): StepResult.Completed {
        val artifact = ModuleArtifactRef(
            id = "ART_BARD_${stableHash(payload)}",
            workspaceId = step.context.workspaceId,
            runId = step.context.runId,
            moduleOrigin = name,
            type = type,
            uri = "${step.context.artifactRoot}/bard/${stableHash(payload)}.json",
            sha256 = "sha256:${stableHash(payload)}",
            timestamp = "pending-artifact-writer-timestamp",
        )
        return StepResult.Completed(artifact = artifact, payload = payload, executionPhase = phase)
    }

    private fun variant(seed: CreativeSeed, id: String, axis: String, promise: String) = CreativeVariant(
        id = id,
        title = "${seed.prompt}: $axis",
        differentiatingAxes = listOf(axis, "cooperation", "world reaction"),
        playerPromise = promise,
        coreAction = "Players repeatedly observe, improvise, split roles, absorb consequences, and turn disruption into new options.",
        specialWorldRule = "Visible actions and ignored cues become remembered relationship state.",
        onlyWorksBecause = "The idea depends on two players making different but mutually visible choices.",
        largestCreativeRisk = "The concept could collapse into generic co-op tasks if consequences are reset.",
        largestProductionUncertainty = "Playable proof must show visible two-player role distinction and persistent reactions.",
    )

    private fun briefFrom(seedRef: String, selected: CreativeVariant, parentArtifactId: String?, status: CreativeBriefStatus) = CreativeBrief(
        briefId = "BRD_${stableHash(seedRef + selected.id)}",
        seedRef = seedRef,
        version = 1,
        status = status,
        profile = profile.publicRef(),
        creativeCore = CreativeCore(selected.playerPromise, selected.coreAction, selected.onlyWorksBecause),
        playGrammar = PlayGrammar(
            verbs = listOf("observe", "improvise", "distract", "recover"),
            roles = listOf("reader", "risk-taker"),
            coreLoop = listOf("Read situation", "Split roles", "Try plan", "Absorb consequence", "Create story from outcome"),
            failureModel = "Failures change the current state instead of resetting time.",
            recoveryModel = "Disturbances create new options for both players.",
            worldChangingActions = listOf("ignored social cue", "visible rescue", "failed improvisation"),
            worldMemory = listOf("present characters remember visible actions"),
            emergentSystems = listOf("relationship changes combine with scene state"),
        ),
        worldGrammar = WorldGrammar(
            laws = listOf(
                "Every visible action can be remembered by present characters.",
                "Ignoring a cue changes relationships too.",
                "Mistakes become current state and are not automatically reset.",
            ),
            causalChains = listOf(CausalChain("Player skips a cue", "NPC loses prepared moment", "NPC withholds help out of bruised professional pride")),
            forbiddenShortcuts = listOf("Consequence-free reset", "Omniscient NPCs", "Plot progress without observable cause"),
        ),
        toneGrammar = ToneGrammar(rhythm = "responsive, dry, consequence-aware", humorRules = listOf("Pointe must follow cause", "Do not explain cleverness")),
        styleGrammar = StyleGrammar(palette = listOf("adaptable"), forbidden = listOf("unearned magic", "randomness as cause")),
        productionIntent = ProductionIntent.forBrief("BRD_${stableHash(seedRef + selected.id)}"),
        locks = listOf(
            CreativeLock("playGrammar.roles", LockLevel.CORE, "Asymmetric cooperation is the central player promise."),
            CreativeLock("styleGrammar.palette", LockLevel.SURFACE, "Concrete look may adapt to available assets."),
        ),
        acceptanceCriteria = listOf(
            AcceptanceCriterion("cooperation-visible", "Proof shows both players affecting the same situation differently."),
            AcceptanceCriterion("failure-creates-new-state", "Proof shows failure producing a new playable state."),
        ),
        regressionScenarios = listOf("causality-intact", "not-linearized", "absurdity-has-cause"),
        assumptions = listOf(DeclaredAssumption("A session should last roughly 30-60 minutes.", 0.55, AssumptionImpact.STRUCTURAL, true)),
        openQuestions = emptyList(),
        parentArtifactId = parentArtifactId,
    )

    private fun assumptionsFor(seed: CreativeSeed) = if (seed.assumptionPolicy == AssumptionPolicy.DECLARE) {
        listOf(DeclaredAssumption("The seed describes a cooperative playable experience rather than a linear story.", 0.7, AssumptionImpact.CORE, true))
    } else {
        emptyList()
    }

    private fun assessmentDecision(assessment: ProductionAssessment): BardProductionDecision = when {
        assessment.proposedSubstitutions.any { it.requiresHumanApproval && it.lockLevel in setOf(LockLevel.CORE, LockLevel.STRUCTURAL) } -> BardProductionDecision.ESCALATE
        assessment.proposedSubstitutions.any { it.lockLevel == LockLevel.CORE } -> BardProductionDecision.REJECT
        assessment.missingCapabilities.isNotEmpty() || assessment.missingAssets.isNotEmpty() -> BardProductionDecision.ADAPT
        else -> BardProductionDecision.ACCEPT
    }

    private fun challengeCheck(id: String, passed: Boolean, summary: String) = BardChallengeCheck(id, passed, summary)

    private fun BardProfile.publicRef() = BardProfileRef(id, version, checksum)

    private fun stableHash(text: String): String = text.fold(0) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }.toString(16)
}
