package io.anvil.modules.target

import kotlinx.serialization.Serializable

@Serializable
enum class TargetOperation { PREPARE, VALIDATE }

@JvmInline
@Serializable
value class ProductionBundleId(val value: String)

@JvmInline
@Serializable
value class RunnableBuildId(val value: String)

@JvmInline
@Serializable
value class BuildStepId(val value: String)

@Serializable
data class TargetDescriptor(
    val engine: TargetEngine,
    val version: String,
    val platform: TargetPlatform,
)

@Serializable
enum class TargetEngine { UNREAL, GODOT, WEB, KORGE, UEFN }

@Serializable
enum class TargetPlatform { WINDOWS, ANDROID, WEB, LINUX, MACOS }

@Serializable
data class ProductionBundle(
    val schema: String = SCHEMA,
    val bundleId: ProductionBundleId,
    val workspaceId: String,
    val runId: String,
    val target: TargetDescriptor,
    val creativeBriefRef: String,
    val gameplayPlanRef: String,
    val sceneBundleRef: String,
    val actorBundleRefs: List<String>,
    val shadedConfigRef: String? = null,
    val audioCueGraphRef: String? = null,
    val inputActionMapRef: String? = null,
    val assetManifestRefs: List<String> = emptyList(),
    val proofRequirementRefs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.production-bundle/v1" }
}

@Serializable
data class RunnableBuild(
    val schema: String = SCHEMA,
    val buildId: RunnableBuildId,
    val productionBundleRef: ProductionBundleId,
    val target: TargetDescriptor,
    val projectLayout: ProjectLayout,
    val importPlan: List<TargetImportStep>,
    val launchPlan: LaunchPlan,
    val healthChecks: List<BuildHealthCheck>,
    val buildStatus: BuildStatus,
    val adapterOwner: String = "anvil-target-adapter",
) {
    companion object { const val SCHEMA = "anvil.runnable-build/v1" }
}

@Serializable
data class ProjectLayout(
    val root: String,
    val contentDir: String,
    val sourceDir: String,
    val configDir: String,
    val artifactDir: String,
)

@Serializable
data class TargetImportStep(
    val id: BuildStepId,
    val sourceRef: String,
    val targetPath: String,
    val kind: TargetImportKind,
    val required: Boolean = true,
)

@Serializable
enum class TargetImportKind { SCENE, GAMEPLAY, ACTOR, SHADED, AUDIO, INPUT, ASSET_MANIFEST }

@Serializable
data class LaunchPlan(
    val command: String,
    val args: List<String>,
    val expectedHealthEndpoint: String? = null,
)

@Serializable
data class BuildHealthCheck(
    val id: String,
    val description: String,
    val requiredArtifactRefs: List<String>,
)

@Serializable
enum class BuildStatus { PLANNED, BLOCKED, BUILT }

@Serializable
data class BuildHealthReport(
    val schema: String = SCHEMA,
    val buildRef: RunnableBuildId,
    val status: BuildStatus,
    val passed: List<BuildValidationCheck>,
    val failed: List<BuildValidationCheck>,
) {
    companion object { const val SCHEMA = "anvil.build-health-report/v1" }
}

@Serializable
data class BuildValidationCheck(
    val id: String,
    val passed: Boolean,
    val message: String,
)
