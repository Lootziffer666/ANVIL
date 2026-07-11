package io.anvil.modules.bard

import io.anvil.core.contracts.PrivacyMode
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class BardOperation { GENERATE, LOCK, REVISE, CHALLENGE, AUDIT }

@Serializable
enum class BardFreedom { @SerialName("directed") DIRECTED, @SerialName("open") OPEN, @SerialName("wild") WILD }

@Serializable
enum class AssumptionPolicy { @SerialName("declare") DECLARE }

@Serializable
enum class AssumptionImpact { @SerialName("core") CORE, @SerialName("structural") STRUCTURAL, @SerialName("surface") SURFACE, @SerialName("suggestion") SUGGESTION }

@Serializable
enum class LockLevel { @SerialName("core") CORE, @SerialName("structural") STRUCTURAL, @SerialName("surface") SURFACE, @SerialName("suggestion") SUGGESTION }

@Serializable
enum class CreativeBriefStatus { @SerialName("draft") DRAFT, @SerialName("locked") LOCKED }

@Serializable
enum class BardProductionDecision { ACCEPT, ADAPT, ESCALATE, REJECT }

@Serializable
enum class FidelityVerdict { @SerialName("aligned") ALIGNED, @SerialName("drifted") DRIFTED, @SerialName("insufficient_evidence") INSUFFICIENT_EVIDENCE }

@Serializable
enum class FidelityStatus { @SerialName("pass") PASS, @SerialName("fail") FAIL, @SerialName("insufficient_evidence") INSUFFICIENT_EVIDENCE }

@Serializable
data class BardProfile(val id: String, val version: String, val checksum: String)

@Serializable
data class BardProfileRef(val id: String, val version: String, val checksum: String)

@Serializable
data class CreativeSeed(
    val schema: String = SCHEMA,
    val seedId: String,
    val workspaceId: String,
    val prompt: String,
    val freedom: BardFreedom,
    val audience: Audience,
    val mustHave: List<String> = emptyList(),
    val mustAvoid: List<String> = emptyList(),
    val contextRefs: List<String> = emptyList(),
    val privacyMode: PrivacyMode = PrivacyMode.LOCAL_ONLY,
    val assumptionPolicy: AssumptionPolicy = AssumptionPolicy.DECLARE,
) {
    companion object { const val SCHEMA = "anvil.bard.creative-seed/v1" }
}

@Serializable
data class Audience(val group: String, val experience: String)

@Serializable
data class DeclaredAssumption(
    val assumption: String,
    val confidence: Double,
    val impact: AssumptionImpact,
    val mayAutoRevise: Boolean,
)

@Serializable
data class CreativeVariants(
    val schema: String = SCHEMA,
    val seedRef: String,
    val profile: BardProfileRef,
    val variants: List<CreativeVariant>,
    val assumptions: List<DeclaredAssumption>,
) {
    companion object { const val SCHEMA = "anvil.bard.creative-variants/v1" }
}

@Serializable
data class CreativeVariant(
    val id: String,
    val title: String,
    val differentiatingAxes: List<String>,
    val playerPromise: String,
    val coreAction: String,
    val specialWorldRule: String,
    val onlyWorksBecause: String,
    val largestCreativeRisk: String,
    val largestProductionUncertainty: String,
)

@Serializable
data class LockRequest(
    val variants: CreativeVariants,
    val selectedVariantId: String,
    val parentArtifactId: String? = null,
)

@Serializable
data class CreativeBrief(
    val schema: String = SCHEMA,
    val briefId: String,
    val seedRef: String,
    val version: Int,
    val status: CreativeBriefStatus,
    val profile: BardProfileRef,
    val creativeCore: CreativeCore,
    val playGrammar: PlayGrammar,
    val worldGrammar: WorldGrammar,
    val toneGrammar: ToneGrammar,
    val styleGrammar: StyleGrammar,
    val productionIntent: ProductionIntent,
    val locks: List<CreativeLock>,
    val acceptanceCriteria: List<AcceptanceCriterion>,
    val regressionScenarios: List<String>,
    val assumptions: List<DeclaredAssumption>,
    val openQuestions: List<String>,
    val parentArtifactId: String? = null,
) {
    companion object { const val SCHEMA = "anvil.bard.creative-brief/v1" }
}

@Serializable
data class CreativeCore(val playerPromise: String, val coreAction: String, val onlyWorksBecause: String)

@Serializable
data class PlayGrammar(
    val verbs: List<String>,
    val roles: List<String>,
    val coreLoop: List<String>,
    val failureModel: String,
    val recoveryModel: String,
    val worldChangingActions: List<String>,
    val worldMemory: List<String>,
    val emergentSystems: List<String>,
)

@Serializable
data class WorldGrammar(
    val laws: List<String>,
    val causalChains: List<CausalChain>,
    val forbiddenShortcuts: List<String>,
)

@Serializable
data class CausalChain(val trigger: String, val stateChange: String, val laterConsequence: String)

@Serializable
data class ToneGrammar(val rhythm: String, val humorRules: List<String>)

@Serializable
data class StyleGrammar(val palette: List<String>, val forbidden: List<String>)

@Serializable
data class CreativeLock(val path: String, val level: LockLevel, val reason: String)

@Serializable
data class AcceptanceCriterion(val id: String, val description: String)

@Serializable
data class ProductionIntent(
    val schema: String = SCHEMA,
    val creativeBriefRef: String,
    val capabilityIntent: List<String>,
    val assetRoleIntent: List<String>,
    val interactionRequirements: List<String>,
    val worldIntent: WorldIntent,
    val characterIntent: List<String>,
    val audioIntent: List<String>,
    val uiIntent: List<String>,
    val locks: List<CreativeLock>,
    val acceptanceCriteria: List<AcceptanceCriterion>,
) {
    companion object {
        const val SCHEMA = "anvil.bard.production-intent/v1"
        fun forBrief(briefId: String) = ProductionIntent(
            creativeBriefRef = briefId,
            capabilityIntent = listOf("two-player visible cooperation", "persistent consequence state"),
            assetRoleIntent = listOf("roles, not concrete asset IDs"),
            interactionRequirements = listOf("players can choose different roles in the same situation"),
            worldIntent = WorldIntent(
                climate = emptyList(),
                materialHistory = emptyList(),
                reactiveStates = listOf("characters remember visible action and ignored cues"),
                temporalArc = listOf("failure changes later state"),
            ),
            characterIntent = listOf("characters react from observed state, not omniscience"),
            audioIntent = emptyList(),
            uiIntent = listOf("show consequences without claiming technical readiness"),
            locks = emptyList(),
            acceptanceCriteria = emptyList(),
        )
    }
}

@Serializable
data class WorldIntent(
    val climate: List<String>,
    val materialHistory: List<String>,
    val reactiveStates: List<String>,
    val temporalArc: List<String>,
)

@Serializable
data class ProductionAssessment(
    val schema: String = SCHEMA,
    val creativeBriefRef: String,
    val capabilityCast: Map<String, String> = emptyMap(),
    val starterKit: Map<String, String> = emptyMap(),
    val missingCapabilities: List<String> = emptyList(),
    val missingAssets: List<String> = emptyList(),
    val shadedPass: Map<String, String> = emptyMap(),
    val surfacePass: Map<String, String> = emptyMap(),
    val characterPipeline: Map<String, String> = emptyMap(),
    val provenanceWarnings: List<String> = emptyList(),
    val proposedSubstitutions: List<ProposedSubstitution> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.wizard.production-assessment/v1" }
}

@Serializable
data class ProposedSubstitution(
    val affectedPath: String,
    val lockLevel: LockLevel,
    val reason: String,
    val requiresHumanApproval: Boolean,
)

@Serializable
data class RevisionRequest(val brief: CreativeBrief, val assessment: ProductionAssessment)

@Serializable
data class BardChallengeReport(
    val schema: String = SCHEMA,
    val briefRef: String,
    val passed: List<BardChallengeCheck>,
    val failed: List<BardChallengeCheck>,
) {
    companion object { const val SCHEMA = "anvil.bard.challenge-report/v1" }
}

@Serializable
data class BardChallengeCheck(val id: String, val passed: Boolean, val summary: String)

@Serializable
data class CreativeFidelityInput(
    val brief: CreativeBrief,
    val playableProofRefs: List<String>,
    val temporalProofRefs: List<String>,
    val screenshotRefs: List<String>,
    val flowResultRefs: List<String>,
) {
    val proofRefs: List<String>
        get() = playableProofRefs + temporalProofRefs + screenshotRefs + flowResultRefs
}

@Serializable
data class CreativeFidelityReport(
    val schema: String = SCHEMA,
    val briefRef: String,
    val proofRefs: List<String>,
    val verdict: FidelityVerdict,
    val passed: List<FidelityCriterionResult>,
    val failed: List<FidelityCriterionResult>,
    val insufficientEvidence: List<FidelityCriterionResult>,
    val recommendedRevisions: List<String>,
) {
    companion object { const val SCHEMA = "anvil.bard.creative-fidelity/v1" }
}

@Serializable
data class FidelityCriterionResult(
    val id: String,
    val status: FidelityStatus,
    val evidenceRefs: List<String>,
)
