package io.anvil.modules.target.web

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebTargetWriterTest {

    private lateinit var outputDir: java.io.File

    @BeforeTest
    fun setUp() {
        outputDir = Files.createTempDirectory("anvil-web-target-test").toFile()
    }

    @AfterTest
    fun tearDown() {
        outputDir.deleteRecursively()
    }

    @Test
    fun assemble_writesAllExpectedFilesAndReturnsAssembledStatus() {
        val writer = WebTargetWriter()
        val result = writer.assemble(
            WebTargetAssembleRequest(
                outputDir = outputDir,
                gameplayPlanJson = """{"schema":"anvil.gameplay.plan/v1"}""",
                sceneBundleJson = """{"schema":"anvil.scene-bundle/v1"}""",
                interfaceBundleJson = """{"schema":"anvil.interface.bundle/v1"}""",
                audioAssetManifestJson = """{"schema":"anvil.audio-asset-manifest/v1"}""",
                audioCueGraphJson = """{"schema":"anvil.audio-cue-graph/v1"}""",
                swiftActorBundleRef = "ART_SWIFT_1",
                shadedSceneConfigRef = "ART_SHADED_1",
            ),
        )
        assertEquals(WebTargetStatus.ASSEMBLED, result.status)
        assertEquals(8, result.writtenFiles.size)
        result.writtenFiles.forEach { assertTrue(java.io.File(it).exists(), "expected $it to exist") }

        assertTrue(java.io.File(outputDir, "content/gameplay.plan.json").readText().contains("anvil.gameplay.plan/v1"))
        assertTrue(java.io.File(outputDir, "audio/manifest.json").readText().contains("anvil.audio-asset-manifest/v1"))
        assertTrue(java.io.File(outputDir, "audio/runtime.ts").readText().contains("export async function startAudioRuntime"))
        assertTrue(java.io.File(outputDir, "audio/runtime.ts").readText().contains("ANVIL_AUDIO"))
    }

    @Test
    fun toneJsRuntimeWriter_neverAutoplaysAndAlwaysDisposesCleanly() {
        val writer = ToneJsRuntimeWriter()
        val result = writer.write(
            outputDir,
            ToneJsRuntimeRequest(
                preloadList = listOf("a.json"),
                buses = listOf("master", "music"),
                debugStateInputs = listOf("danger"),
                manifestJson = "{}",
                cueGraphJson = "{}",
            ),
        )
        val runtimeText = java.io.File(result.runtimePath).readText()
        assertTrue(runtimeText.contains("never autoplay"))
        assertTrue(runtimeText.contains("export function disposeAudioRuntime"))
        assertTrue(runtimeText.contains("\"master\""))
        assertTrue(runtimeText.contains("\"danger\""))
    }
}
