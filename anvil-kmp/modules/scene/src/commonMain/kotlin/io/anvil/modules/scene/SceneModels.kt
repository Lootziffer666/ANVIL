package io.anvil.modules.scene

import kotlinx.serialization.Serializable

@Serializable
enum class SceneOperation { COMPILE, VALIDATE }

@JvmInline
@Serializable
value class SceneId(val value: String)

@JvmInline
@Serializable
value class EntityId(val value: String)

@JvmInline
@Serializable
value class AnchorId(val value: String)

@JvmInline
@Serializable
value class ZoneId(val value: String)

@JvmInline
@Serializable
value class CameraId(val value: String)

@Serializable
data class SceneIntent(
    val schema: String = SCHEMA,
    val intentId: String,
    val workspaceId: String,
    val runId: String,
    val creativeBriefRef: String,
    val productionIntentRef: String,
    val environmentRefs: List<String>,
    val gameplayPlanRef: String,
    val interactionRefs: List<String>,
    val requiredRoles: List<String>,
    val shadedConfigRef: String? = null,
    val audioZoneRefs: List<String> = emptyList(),
) {
    companion object { const val SCHEMA = "anvil.scene.intent/v1" }
}

@Serializable
data class SceneBundle(
    val schema: String = SCHEMA,
    val sceneId: SceneId,
    val creativeBriefRef: String,
    val productionIntentRef: String,
    val gameplayPlanRef: String,
    val environmentRefs: List<String>,
    val entities: List<SceneEntity>,
    val anchors: List<SpatialAnchor>,
    val collision: CollisionMap,
    val navigation: NavigationGraph,
    val spawnPoints: List<SpawnPoint>,
    val cameras: List<CameraPlan>,
    val interactionZones: List<InteractionZone>,
    val shadedConfigRef: String? = null,
    val audioZoneRefs: List<String> = emptyList(),
    val spaceOwner: String = "anvil-scene-compiler",
) {
    companion object { const val SCHEMA = "anvil.scene-bundle/v1" }
}

@Serializable
data class SceneEntity(
    val id: EntityId,
    val role: String,
    val assetRef: String? = null,
    val anchorRef: AnchorId,
    val tags: List<String>,
)

@Serializable
data class SpatialAnchor(
    val schema: String = SCHEMA,
    val id: AnchorId,
    val label: String,
    val transform: Transform3,
) {
    companion object { const val SCHEMA = "anvil.scene.spatial-anchor/v1" }
}

@Serializable
data class Transform3(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double = 0.0,
    val pitch: Double = 0.0,
    val roll: Double = 0.0,
    val scale: Double = 1.0,
)

@Serializable
data class CollisionMap(
    val schema: String = SCHEMA,
    val walkableBounds: Bounds2,
    val blockers: List<Bounds2>,
) {
    companion object { const val SCHEMA = "anvil.scene.collision-map/v1" }
}

@Serializable
data class Bounds2(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double,
)

@Serializable
data class NavigationGraph(
    val schema: String = SCHEMA,
    val nodes: List<NavigationNode>,
    val edges: List<NavigationEdge>,
) {
    companion object { const val SCHEMA = "anvil.scene.navigation-graph/v1" }
}

@Serializable
data class NavigationNode(val id: String, val anchorRef: AnchorId)

@Serializable
data class NavigationEdge(val from: String, val to: String, val traversal: TraversalKind = TraversalKind.WALK)

@Serializable
enum class TraversalKind { WALK, JUMP, DOOR, SCRIPTED }

@Serializable
data class SpawnPoint(
    val id: String,
    val role: String,
    val anchorRef: AnchorId,
)

@Serializable
data class CameraPlan(
    val schema: String = SCHEMA,
    val id: CameraId,
    val mode: CameraMode,
    val anchorRef: AnchorId,
    val targetAnchorRef: AnchorId? = null,
) {
    companion object { const val SCHEMA = "anvil.scene.camera-plan/v1" }
}

@Serializable
enum class CameraMode { FIXED, FOLLOW, ORBIT, FIRST_PERSON }

@Serializable
data class InteractionZone(
    val schema: String = SCHEMA,
    val id: ZoneId,
    val interactionRef: String,
    val anchorRef: AnchorId,
    val radius: Double,
    val proofTag: String,
) {
    companion object { const val SCHEMA = "anvil.scene.interaction-zone/v1" }
}

@Serializable
data class SceneValidationReport(
    val schema: String = SCHEMA,
    val sceneRef: SceneId,
    val passed: List<SceneValidationCheck>,
    val failed: List<SceneValidationCheck>,
) {
    companion object { const val SCHEMA = "anvil.scene.validation-report/v1" }
}

@Serializable
data class SceneValidationCheck(
    val id: String,
    val passed: Boolean,
    val message: String,
)
