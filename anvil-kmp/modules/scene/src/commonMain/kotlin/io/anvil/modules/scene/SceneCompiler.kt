package io.anvil.modules.scene

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

class SceneCompilerModule : ModuleSlotContract {
    override val name: String = "anvil-scene-compiler"
    override val purpose: String =
        "Compiles production intent and gameplay anchors into SceneBundle spatial contracts."

    private var quality = QualityState.STABLE
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override fun qualityState(): QualityState = quality

    override suspend fun boot(ctx: ModuleContext): BootResult = BootResult(
        moduleId = name,
        qualityState = quality,
        executionPhase = ExecutionPhase.IDLE,
        message = "Scene Compiler booted; spatial truth is isolated from gameplay, actor, audio, and visual truth.",
    )

    override suspend fun handle(step: ModuleRunStep): StepResult = when (SceneOperation.valueOf(step.operation)) {
        SceneOperation.COMPILE -> {
            val bundle = compile(json.decodeFromString<SceneIntent>(step.payload))
            val payload = json.encodeToString(bundle)
            complete(step.context, SceneBundle.SCHEMA, payload)
        }
        SceneOperation.VALIDATE -> {
            val report = validate(json.decodeFromString<SceneBundle>(step.payload))
            val payload = json.encodeToString(report)
            complete(step.context, SceneValidationReport.SCHEMA, payload)
        }
    }

    fun compile(intent: SceneIntent): SceneBundle {
        require(intent.schema == SceneIntent.SCHEMA) { "Unsupported scene intent schema: ${intent.schema}" }
        require(intent.interactionRefs.isNotEmpty()) { "Scene compile requires at least one interaction ref." }

        val start = SpatialAnchor(id = AnchorId("ANC_START"), label = "player-start", transform = Transform3(0.0, 0.0, 0.0))
        val focus = SpatialAnchor(id = AnchorId("ANC_FOCUS"), label = "first-interaction", transform = Transform3(3.0, 0.0, 0.0))
        val camera = SpatialAnchor(id = AnchorId("ANC_CAMERA"), label = "main-camera", transform = Transform3(-4.0, -5.0, 3.0, yaw = 35.0, pitch = -20.0))
        val anchors = listOf(start, focus, camera)

        return SceneBundle(
            sceneId = SceneId("SCN_${stableHash(intent.intentId + intent.gameplayPlanRef)}"),
            creativeBriefRef = intent.creativeBriefRef,
            productionIntentRef = intent.productionIntentRef,
            gameplayPlanRef = intent.gameplayPlanRef,
            environmentRefs = intent.environmentRefs,
            entities = listOf(
                SceneEntity(EntityId("ENT_INTERACTION_FOCUS"), "interaction-target", anchorRef = focus.id, tags = listOf("interactive")),
            ),
            anchors = anchors,
            collision = CollisionMap(
                walkableBounds = Bounds2(-6.0, -6.0, 6.0, 6.0),
                blockers = emptyList(),
            ),
            navigation = NavigationGraph(
                nodes = listOf(
                    NavigationNode("NAV_START", start.id),
                    NavigationNode("NAV_FOCUS", focus.id),
                ),
                edges = listOf(NavigationEdge("NAV_START", "NAV_FOCUS"), NavigationEdge("NAV_FOCUS", "NAV_START")),
            ),
            spawnPoints = intent.requiredRoles.ifEmpty { listOf("player") }.mapIndexed { index, role ->
                SpawnPoint("SPAWN_${role.uppercase()}_${index + 1}", role, start.id)
            },
            cameras = listOf(CameraPlan(id = CameraId("CAM_MAIN"), mode = CameraMode.FIXED, anchorRef = camera.id, targetAnchorRef = focus.id)),
            interactionZones = intent.interactionRefs.mapIndexed { index, interactionRef ->
                InteractionZone(
                    id = ZoneId("ZONE_INTERACTION_${index + 1}"),
                    interactionRef = interactionRef,
                    anchorRef = focus.id,
                    radius = 1.75,
                    proofTag = "scene-zone-$interactionRef",
                )
            },
            shadedConfigRef = intent.shadedConfigRef,
            audioZoneRefs = intent.audioZoneRefs,
        )
    }

    fun validate(bundle: SceneBundle): SceneValidationReport {
        val anchorIds = bundle.anchors.map { it.id }.toSet()
        val checks = listOf(
            check("has-anchor", bundle.anchors.isNotEmpty(), "SceneBundle must define spatial anchors."),
            check("has-spawn", bundle.spawnPoints.isNotEmpty(), "SceneBundle must define at least one spawn point."),
            check("has-camera", bundle.cameras.isNotEmpty(), "SceneBundle must define at least one camera."),
            check("has-interaction-zone", bundle.interactionZones.isNotEmpty(), "SceneBundle must define interaction zones."),
            check("refs-existing-anchors", bundle.spawnPoints.all { it.anchorRef in anchorIds } && bundle.interactionZones.all { it.anchorRef in anchorIds }, "Spawn points and zones must reference existing anchors."),
            check("space-owner-only", bundle.spaceOwner == name, "SceneBundle owns space only, not gameplay or visual truth."),
        )
        return SceneValidationReport(
            sceneRef = bundle.sceneId,
            passed = checks.filter { it.passed },
            failed = checks.filterNot { it.passed },
        )
    }

    private fun complete(context: ModuleContext, type: String, payload: String): StepResult.Completed {
        val hash = stableHash(payload)
        return StepResult.Completed(
            artifact = ModuleArtifactRef(
                id = "ART_SCENE_$hash",
                workspaceId = context.workspaceId,
                runId = context.runId,
                moduleOrigin = name,
                type = type,
                uri = "${context.artifactRoot}/scene/$hash.json",
                sha256 = "sha256:$hash",
                timestamp = context.createdAt,
            ),
            payload = payload,
            executionPhase = ExecutionPhase.COMPLETE,
        )
    }

    private fun check(id: String, passed: Boolean, message: String) = SceneValidationCheck(id, passed, message)

    private fun stableHash(text: String): String = text.fold(0) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }.toString(16)
}
