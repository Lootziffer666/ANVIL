package io.anvil.modules.target

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

class TargetAdapterModule : ModuleSlotContract {
    override val name: String = "anvil-target-adapter"
    override val purpose: String =
        "Plans target project assembly from production bundles without changing source truths."

    private var quality = QualityState.STABLE
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun qualityState(): QualityState = quality

    override suspend fun boot(ctx: ModuleContext): BootResult = BootResult(
        moduleId = name,
        qualityState = quality,
        executionPhase = ExecutionPhase.IDLE,
        message = "Target Adapter booted; it produces build plans, not CUE readiness verdicts.",
    )

    override suspend fun handle(step: ModuleRunStep): StepResult = when (TargetOperation.valueOf(step.operation)) {
        TargetOperation.PREPARE -> {
            val build = prepare(json.decodeFromString<ProductionBundle>(step.payload))
            val payload = json.encodeToString(build)
            complete(step.context, RunnableBuild.SCHEMA, payload)
        }
        TargetOperation.VALIDATE -> {
            val report = validate(json.decodeFromString<RunnableBuild>(step.payload))
            val payload = json.encodeToString(report)
            complete(step.context, BuildHealthReport.SCHEMA, payload)
        }
    }

    fun prepare(bundle: ProductionBundle): RunnableBuild {
        require(bundle.schema == ProductionBundle.SCHEMA) { "Unsupported production bundle schema: ${bundle.schema}" }
        require(bundle.gameplayPlanRef.isNotBlank()) { "Target Adapter requires gameplayPlanRef." }
        require(bundle.sceneBundleRef.isNotBlank()) { "Target Adapter requires sceneBundleRef." }

        val imports = mutableListOf(
            importStep("scene", bundle.sceneBundleRef, "Content/Anvil/Scenes/scene.bundle.json", TargetImportKind.SCENE),
            importStep("gameplay", bundle.gameplayPlanRef, "Content/Anvil/Gameplay/gameplay.plan.json", TargetImportKind.GAMEPLAY),
        )
        bundle.actorBundleRefs.forEachIndexed { index, ref ->
            imports += importStep("actor-$index", ref, "Content/Anvil/Actors/actor-$index.bundle.json", TargetImportKind.ACTOR)
        }
        bundle.shadedConfigRef?.let { imports += importStep("shaded", it, "Content/Anvil/Shaded/shaded.config.json", TargetImportKind.SHADED) }
        bundle.audioCueGraphRef?.let { imports += importStep("audio", it, "Content/Anvil/Audio/audio.cue-graph.json", TargetImportKind.AUDIO) }
        bundle.inputActionMapRef?.let { imports += importStep("input", it, "Config/Anvil/input.action-map.json", TargetImportKind.INPUT) }
        bundle.assetManifestRefs.forEachIndexed { index, ref ->
            imports += importStep("asset-manifest-$index", ref, "Content/Anvil/Assets/manifest-$index.json", TargetImportKind.ASSET_MANIFEST)
        }

        return RunnableBuild(
            buildId = RunnableBuildId("RBLD_${stableHash(bundle.bundleId.value + bundle.target.engine.name)}"),
            productionBundleRef = bundle.bundleId,
            target = bundle.target,
            projectLayout = layoutFor(bundle.target),
            importPlan = imports,
            launchPlan = launchPlanFor(bundle.target),
            healthChecks = listOf(
                BuildHealthCheck("imports-present", "All required import refs are available.", imports.filter { it.required }.map { it.sourceRef }),
                BuildHealthCheck("cue-can-start", "CUE can later start the target build.", bundle.proofRequirementRefs),
            ),
            buildStatus = BuildStatus.PLANNED,
        )
    }

    fun validate(build: RunnableBuild): BuildHealthReport {
        val checks = listOf(
            check("has-layout", build.projectLayout.root.isNotBlank(), "RunnableBuild must define a project root."),
            check("has-required-imports", build.importPlan.any { it.kind == TargetImportKind.SCENE } && build.importPlan.any { it.kind == TargetImportKind.GAMEPLAY }, "RunnableBuild requires scene and gameplay imports."),
            check("has-launch-plan", build.launchPlan.command.isNotBlank(), "RunnableBuild must define a launch command for CUE."),
            check("adapter-owner-only", build.adapterOwner == name, "Target Adapter owns target assembly only, not technical readiness."),
        )
        val status = if (checks.all { it.passed }) build.buildStatus else BuildStatus.BLOCKED
        return BuildHealthReport(
            buildRef = build.buildId,
            status = status,
            passed = checks.filter { it.passed },
            failed = checks.filterNot { it.passed },
        )
    }

    private fun importStep(id: String, sourceRef: String, targetPath: String, kind: TargetImportKind) = TargetImportStep(
        id = BuildStepId("IMP_${id.uppercase().replace('-', '_')}"),
        sourceRef = sourceRef,
        targetPath = targetPath,
        kind = kind,
    )

    private fun layoutFor(target: TargetDescriptor): ProjectLayout = when (target.engine) {
        TargetEngine.UNREAL, TargetEngine.UEFN -> ProjectLayout("AnvilUnreal", "Content", "Source", "Config", "Saved/AnvilArtifacts")
        TargetEngine.GODOT -> ProjectLayout("AnvilGodot", "res://content", "res://scripts", "res://config", "res://artifacts")
        TargetEngine.WEB -> ProjectLayout("anvil-web", "public/content", "src", "config", "artifacts")
        TargetEngine.KORGE -> ProjectLayout("anvil-korge", "resources", "src/commonMain", "config", "build/anvil-artifacts")
    }

    private fun launchPlanFor(target: TargetDescriptor): LaunchPlan = when (target.engine) {
        TargetEngine.UNREAL -> LaunchPlan("UnrealEditor", listOf("AnvilUnreal.uproject", "-game"))
        TargetEngine.UEFN -> LaunchPlan("UnrealEditorFortnite", listOf("AnvilUEFN"))
        TargetEngine.GODOT -> LaunchPlan("godot", listOf("--path", "AnvilGodot"))
        TargetEngine.WEB -> LaunchPlan("npm", listOf("run", "dev"), expectedHealthEndpoint = "http://127.0.0.1:5173")
        TargetEngine.KORGE -> LaunchPlan("./gradlew", listOf("runJvm"))
    }

    private fun complete(context: ModuleContext, type: String, payload: String): StepResult.Completed {
        val hash = stableHash(payload)
        return StepResult.Completed(
            artifact = ModuleArtifactRef(
                id = "ART_TARGET_$hash",
                workspaceId = context.workspaceId,
                runId = context.runId,
                moduleOrigin = name,
                type = type,
                uri = "${context.artifactRoot}/target/$hash.json",
                sha256 = "sha256:$hash",
                timestamp = "pending-artifact-writer-timestamp",
            ),
            payload = payload,
            executionPhase = ExecutionPhase.COMPLETE,
        )
    }

    private fun check(id: String, passed: Boolean, message: String) = BuildValidationCheck(id, passed, message)

    private fun stableHash(text: String): String = text.fold(0) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }.toString(16)
}
