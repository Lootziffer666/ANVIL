package io.anvil.modules.target.web

import java.io.File

enum class WebTargetStatus { PLANNED, ASSEMBLED, RUNNABLE, VERIFIED }

data class WebTargetAssembleRequest(
    val outputDir: File,
    val gameplayPlanJson: String,
    val sceneBundleJson: String,
    val interfaceBundleJson: String,
    val audioAssetManifestJson: String,
    val audioCueGraphJson: String,
    val swiftActorBundleRef: String,
    val shadedSceneConfigRef: String,
)

data class WebTargetAssembleResult(
    val status: WebTargetStatus,
    val writtenFiles: List<String>,
)

/**
 * Assembles a small web fixture from already-produced module outputs (Gate G-03). Every
 * input is a raw string (JSON or ref) — Target never imports Gameplay/Scene/Interface/
 * Acoustic types directly (CLAUDE.md §3: modules may not import other modules).
 *
 * Status discipline: this writer only ever returns [WebTargetStatus.ASSEMBLED] — becoming
 * `RUNNABLE` requires an actual started process + healthcheck (out of this gate's scope),
 * and `VERIFIED` may only ever be set from a CUE proof artifact, never by Target itself.
 */
class WebTargetWriter(private val runtimeWriter: ToneJsRuntimeWriter = ToneJsRuntimeWriter()) {

    fun assemble(request: WebTargetAssembleRequest): WebTargetAssembleResult {
        val contentDir = File(request.outputDir, "content").apply { mkdirs() }
        val written = mutableListOf<String>()
        written += writeFile(File(contentDir, "gameplay.plan.json"), request.gameplayPlanJson)
        written += writeFile(File(contentDir, "scene.bundle.json"), request.sceneBundleJson)
        written += writeFile(File(contentDir, "interface.bundle.json"), request.interfaceBundleJson)
        written += writeFile(File(contentDir, "swift.actor-bundle.ref.txt"), request.swiftActorBundleRef)
        written += writeFile(File(contentDir, "shaded.scene-config.ref.txt"), request.shadedSceneConfigRef)

        val runtimeResult = runtimeWriter.write(
            outputDir = request.outputDir,
            request = ToneJsRuntimeRequest(
                preloadList = listOf("audio/manifest.json", "audio/cue-graph.json"),
                buses = listOf("master", "music", "sfx", "voice"),
                debugStateInputs = listOf("danger", "wonder", "exhaustion"),
                manifestJson = request.audioAssetManifestJson,
                cueGraphJson = request.audioCueGraphJson,
            ),
        )
        written += listOf(runtimeResult.manifestPath, runtimeResult.cueGraphPath, runtimeResult.runtimePath)

        return WebTargetAssembleResult(status = WebTargetStatus.ASSEMBLED, writtenFiles = written)
    }

    private fun writeFile(file: File, content: String): String {
        file.writeText(content)
        return file.path
    }
}
