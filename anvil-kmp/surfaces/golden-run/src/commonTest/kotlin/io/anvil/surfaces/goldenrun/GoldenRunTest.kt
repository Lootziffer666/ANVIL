package io.anvil.surfaces.goldenrun

import io.anvil.core.artifacts.ArtifactId
import io.anvil.core.artifacts.ArtifactWriteRequest
import io.anvil.core.artifacts.ArtifactWriter
import io.anvil.core.artifacts.InMemoryArtifactStore
import io.anvil.core.contracts.BootResult
import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExecutionPhase
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult
import io.anvil.core.handoff.HandoffAudience
import io.anvil.core.handoff.HandoffExportRequest
import io.anvil.core.handoff.HandoffExporter
import io.anvil.core.handoff.HandoffFormat
import io.anvil.core.handoff.HandoffPackageId
import io.anvil.core.run.RunContractRef
import io.anvil.core.run.RunPlan
import io.anvil.core.run.RunPlanId
import io.anvil.core.run.RunPlanStep
import io.anvil.core.run.RunStatus
import io.anvil.core.run.RunStepStatus
import io.anvil.core.run.RunSurface
import io.anvil.core.sync.WorkspaceSyncBundleId
import io.anvil.core.sync.WorkspaceSyncExportRequest
import io.anvil.core.sync.WorkspaceSyncService
import io.anvil.modules.acoustic.AcousticCapabilities
import io.anvil.modules.acoustic.AcousticOperation
import io.anvil.modules.acoustic.AcousticProducerModule
import io.anvil.modules.acoustic.AcousticProducerOperation
import io.anvil.modules.acoustic.AcousticProvider
import io.anvil.modules.acoustic.AcousticRuntimeModule
import io.anvil.modules.acoustic.AudioAsset
import io.anvil.modules.acoustic.AudioAssetId
import io.anvil.modules.acoustic.AudioAssetManifest
import io.anvil.modules.acoustic.AudioGenerationCost
import io.anvil.modules.acoustic.AudioGenerationKind
import io.anvil.modules.acoustic.AudioGenerationProvenance
import io.anvil.modules.acoustic.AudioGenerationRequest
import io.anvil.modules.acoustic.AudioGenerationResult
import io.anvil.modules.acoustic.AudioIntent
import io.anvil.modules.acoustic.AudioProviderId
import io.anvil.modules.gameplay.GameplayCompilerModule
import io.anvil.modules.gameplay.GameplayOperation
import io.anvil.modules.interfacecompiler.InterfaceCompilerModule
import io.anvil.modules.interfacecompiler.InterfaceOperation
import io.anvil.modules.scene.SceneCompilerModule
import io.anvil.modules.scene.SceneOperation
import io.anvil.modules.target.ProductionBundle
import io.anvil.modules.target.ProductionBundleId
import io.anvil.modules.target.TargetAdapterModule
import io.anvil.modules.target.TargetDescriptor
import io.anvil.modules.target.TargetEngine
import io.anvil.modules.target.TargetOperation
import io.anvil.modules.target.TargetPlatform
import io.anvil.surfaces.goldenrun.fixtures.GoldenRunFakeBardPort
import io.anvil.surfaces.goldenrun.fixtures.GoldenRunFakeCuePort
import io.anvil.surfaces.goldenrun.fixtures.GoldenRunFakeWizardPort
import io.anvil.surfaces.goldenrun.fixtures.GoldenRunFixture
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Gate I — smallest end-to-end proof that the whole chain
 * CreativeSeed → BARD → WIZARD → Gameplay → Scene → Interface → Acoustic (runtime +
 * producer) → Target → CUE → Handoff → Sync actually runs, with real payloads flowing
 * through the real `RunSurface`/`ArtifactStore`/`HandoffExporter`/`WorkspaceSyncService`
 * machinery — not just compiling.
 *
 * Every external system (BARD/WIZARD/SWIFT/SHADED/CUE) is a `Fake*Port` fixture (Gate
 * E), explicitly not claiming real creative/production/technical fidelity. Gameplay,
 * Scene, Interface, Acoustic and Target are the real module implementations.
 */
class GoldenRunTest {

    // classDiscriminator = "kind" matches GameplayCompilerModule's own Json config — GameplayPlan's
    // sealed Condition/Effect types already declare an explicit `type` property, which collides
    // with kotlinx.serialization's default "type" discriminator key.
    private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "kind" }

    /** Deterministic, network-free stand-in for a generative audio provider (local re-implementation — `FakeAcousticProvider` lives in `:modules:acoustic`'s test sources and is not visible across module boundaries). */
    private class LocalFakeAcousticProvider : AcousticProvider {
        override val id = AudioProviderId("fake-acoustic")
        override suspend fun capabilities() = AcousticCapabilities(id, isLocal = true, supportedKinds = listOf(AudioGenerationKind.MUSIC), commercialUseAllowed = true)
        override suspend fun estimate(request: AudioGenerationRequest) = AudioGenerationCost(estimatedCredits = 120)
        override suspend fun generate(request: AudioGenerationRequest, privacyMode: PrivacyMode): AudioGenerationResult {
            val asset = AudioAsset(
                assetId = AudioAssetId("AUD_${request.requestId}"),
                generationKind = request.kind,
                durationMs = request.durationMs,
                format = request.outputFormat,
                sampleRate = 44100,
                channels = 2,
                uri = "fixture://golden-run/${request.requestId}.mp3",
                checksumSha256 = "sha256:" + "7".repeat(64),
            )
            val provenance = AudioGenerationProvenance(provider = id, providerModel = "fixture-model-v1", sourcePromptHash = "sha256:" + "8".repeat(64))
            return AudioGenerationResult.Generated(asset, provenance, AudioGenerationCost(estimatedCredits = 120, actualCredits = 120))
        }
    }

    private class ExecutionCountingModule(private val delegate: ModuleSlotContract) : ModuleSlotContract {
        var executionCount = 0
            private set
        override val name: String get() = delegate.name
        override val purpose: String get() = delegate.purpose
        override fun qualityState(): QualityState = delegate.qualityState()
        override suspend fun boot(ctx: ModuleContext): BootResult = delegate.boot(ctx)
        override suspend fun handle(step: ModuleRunStep): StepResult {
            executionCount++
            return delegate.handle(step)
        }
    }

    @Test
    fun goldenRun_producesArtifactsHandoffAndSyncBundleAcrossTheWholeChain() = runTest {
        // ── 1-2: BARD + WIZARD fixtures (outside RunSurface — external system truth) ──
        val briefResult = GoldenRunFakeBardPort().invoke(
            ExternalToolRequest(ContractId("anvil.bard.creative-seed"), 1, PrivacyMode.OPEN, GoldenRunFixture.CREATIVE_SEED),
        )
        assertIs<ExternalToolResult.Produced>(briefResult)
        val assessmentResult = GoldenRunFakeWizardPort().invoke(
            ExternalToolRequest(ContractId("anvil.bard.production-intent"), 1, PrivacyMode.OPEN, briefResult.payload),
        )
        assertIs<ExternalToolResult.Produced>(assessmentResult)

        val creativeBriefRef = "fixture-creative-brief"
        val productionIntentRef = "fixture-production-intent"

        // Precompute deterministic model-level refs via the pure compile() functions
        // directly. Module payloads carry logical string refs (gameplayPlanRef, ...), not
        // ArtifactStore hash IDs — this is not circular, these ids only depend on fields we
        // already control (verbs, intentId, ...).
        val gameplayRequest = GoldenRunFixture.gameplayRequest(creativeBriefRef, productionIntentRef)
        val previewGameplayPlan = GameplayCompilerModule().compile(gameplayRequest)
        val interactionRefs = previewGameplayPlan.interactions.map { it.id.value }

        val sceneIntent = GoldenRunFixture.sceneIntent(creativeBriefRef, productionIntentRef, previewGameplayPlan.planId.value, interactionRefs)
        val previewSceneBundle = SceneCompilerModule().compile(sceneIntent)

        val interfaceIntent = GoldenRunFixture.interfaceIntent(creativeBriefRef, previewGameplayPlan.planId.value, previewSceneBundle.sceneId.value)

        val audioIntent = AudioIntent(
            intentId = "INTENT_GOLDEN_AUDIO",
            workspaceId = GoldenRunFixture.WORKSPACE_ID,
            runId = GoldenRunFixture.RUN_ID,
            creativeBriefRef = creativeBriefRef,
            gameplayPlanRef = previewGameplayPlan.planId.value,
            sceneBundleRef = previewSceneBundle.sceneId.value,
            emotionalFunctions = listOf("rising-tension-percussion"),
            worldStateInputs = listOf("danger", "pressure"),
            requiredCues = listOf("danger-rise", "path-opened"),
        )

        val audioGenerationRequest = AudioGenerationRequest(
            requestId = "REQ_GOLDEN_AUDIO",
            kind = AudioGenerationKind.MUSIC,
            provider = AudioProviderId("fake-acoustic"),
            prompt = "tense coop percussion layer, danger rising as the crate moves",
            durationMs = 8_000,
        )

        // ── 3-8: real modules through RunSurface (dependency ordering + contract guards) ──
        val modules: Map<String, ModuleSlotContract> = mapOf(
            "gameplay" to GameplayCompilerModule(),
            "scene" to SceneCompilerModule(),
            "interface" to InterfaceCompilerModule(),
            "acoustic" to AcousticRuntimeModule(),
            "acoustic-producer" to AcousticProducerModule(providers = mapOf(AudioProviderId("fake-acoustic") to LocalFakeAcousticProvider())),
            "target" to TargetAdapterModule(),
        )
        val surface = RunSurface(modules = modules)

        val plan = RunPlan(
            planId = RunPlanId("PLAN_GOLDEN_RUN"),
            workspaceId = GoldenRunFixture.WORKSPACE_ID,
            runId = GoldenRunFixture.RUN_ID,
            artifactRoot = GoldenRunFixture.ARTIFACT_ROOT,
            steps = listOf(
                RunPlanStep(
                    id = "S_GAMEPLAY", moduleId = "gameplay", operation = GameplayOperation.COMPILE.name,
                    payload = json.encodeToString(gameplayRequest),
                    outputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 1),
                ),
                RunPlanStep(
                    id = "S_SCENE", moduleId = "scene", operation = SceneOperation.COMPILE.name,
                    payload = json.encodeToString(sceneIntent),
                    dependsOn = listOf("S_GAMEPLAY"),
                    inputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 1),
                    outputContract = RunContractRef(ContractId("anvil.scene-bundle"), 1),
                ),
                RunPlanStep(
                    id = "S_INTERFACE", moduleId = "interface", operation = InterfaceOperation.COMPILE.name,
                    payload = json.encodeToString(interfaceIntent),
                    dependsOn = listOf("S_SCENE"),
                    inputContract = RunContractRef(ContractId("anvil.scene-bundle"), 1),
                    outputContract = RunContractRef(ContractId("anvil.interface.bundle"), 1),
                ),
                RunPlanStep(
                    id = "S_ACOUSTIC", moduleId = "acoustic", operation = AcousticOperation.COMPILE.name,
                    payload = json.encodeToString(audioIntent),
                    dependsOn = listOf("S_SCENE"),
                    inputContract = RunContractRef(ContractId("anvil.scene-bundle"), 1),
                    outputContract = RunContractRef(ContractId("anvil.audio-cue-graph"), 1),
                ),
                RunPlanStep(
                    id = "S_AUDIO_ASSET", moduleId = "acoustic-producer", operation = AcousticProducerOperation.GENERATE.name,
                    payload = json.encodeToString(audioGenerationRequest),
                    dependsOn = listOf("S_GAMEPLAY"),
                    outputContract = RunContractRef(ContractId("anvil.audio-asset-manifest"), 1),
                ),
                RunPlanStep(
                    id = "S_TARGET", moduleId = "target", operation = TargetOperation.PREPARE.name,
                    payload = json.encodeToString(
                        ProductionBundle(
                            bundleId = ProductionBundleId("PB_GOLDEN_1"),
                            workspaceId = GoldenRunFixture.WORKSPACE_ID,
                            runId = GoldenRunFixture.RUN_ID,
                            target = TargetDescriptor(TargetEngine.WEB, "1.0", TargetPlatform.WEB),
                            creativeBriefRef = creativeBriefRef,
                            gameplayPlanRef = previewGameplayPlan.planId.value,
                            sceneBundleRef = previewSceneBundle.sceneId.value,
                            actorBundleRefs = listOf("swift.actor-bundle/fixture"),
                            shadedConfigRef = "shaded.scene-config/fixture-scene",
                            audioCueGraphRef = "acg-fixture-ref",
                            inputActionMapRef = "ifb-fixture-ref",
                            assetManifestRefs = listOf("aud-fixture-ref"),
                        ),
                    ),
                    dependsOn = listOf("S_INTERFACE", "S_ACOUSTIC", "S_AUDIO_ASSET"),
                    inputContract = RunContractRef(ContractId("anvil.audio-cue-graph"), 1),
                    outputContract = RunContractRef(ContractId("anvil.runnable-build"), 1),
                ),
            ),
        )

        val summary = surface.execute(plan, createdAt = GoldenRunFixture.CREATED_AT)

        assertEquals(RunStatus.COMPLETE, summary.status, "records: ${summary.records}")
        assertEquals(6, summary.records.size)
        assertTrue(summary.records.all { it.status == RunStepStatus.COMPLETED })
        assertEquals(6, summary.registry.artifacts.size)
        assertTrue(summary.registry.artifacts.all { it.checksumSha256.startsWith("sha256:") })

        val gameplayArtifact = summary.records.first { it.stepId == "S_GAMEPLAY" }.artifact!!
        val sceneArtifact = summary.records.first { it.stepId == "S_SCENE" }.artifact!!
        val targetArtifact = summary.records.first { it.stepId == "S_TARGET" }.artifact!!

        assertEquals("anvil.gameplay.plan/v1", gameplayArtifact.type)
        assertEquals("anvil.scene-bundle/v1", sceneArtifact.type)
        assertEquals(AudioAssetManifest.SCHEMA, summary.records.first { it.stepId == "S_AUDIO_ASSET" }.artifact!!.type)
        assertEquals("anvil.runnable-build/v1", targetArtifact.type)

        // ── 9: CUE fixture — separate playable/temporal/audio proofs, never merged ──────
        val cue = GoldenRunFakeCuePort()
        val playableProof = cue.invoke(ExternalToolRequest(ContractId("cue.playable-proof"), 1, PrivacyMode.OPEN, targetArtifact.artifactId.value))
        val temporalProof = cue.invoke(ExternalToolRequest(ContractId("cue.temporal-proof"), 1, PrivacyMode.OPEN, targetArtifact.artifactId.value))
        val audioProof = cue.invoke(ExternalToolRequest(ContractId("cue.audio-proof"), 1, PrivacyMode.OPEN, targetArtifact.artifactId.value))
        assertIs<ExternalToolResult.Produced>(playableProof)
        assertIs<ExternalToolResult.Produced>(temporalProof)
        assertIs<ExternalToolResult.Produced>(audioProof)
        assertTrue(
            setOf(playableProof.contractId, temporalProof.contractId, audioProof.contractId).size == 3,
            "CUE proofs must stay three separate artifacts, never merged",
        )

        // ── 10: ArtifactStore actually persists real payloads (Gate D-04) ───────────────
        val store = InMemoryArtifactStore()
        val writer = ArtifactWriter()
        val gameplayPayload = json.encodeToString(previewGameplayPlan)
        writer.writeAndPersist(
            ArtifactWriteRequest(
                artifactRef = ModuleArtifactRef(
                    id = "ART_STORE_GOLDEN_GAMEPLAY",
                    workspaceId = GoldenRunFixture.WORKSPACE_ID,
                    runId = GoldenRunFixture.RUN_ID,
                    moduleOrigin = "anvil-gameplay-compiler",
                    type = "anvil.gameplay.plan/v1",
                    uri = "artifact://golden-run/gameplay-plan.json",
                    sha256 = "sha256:placeholder",
                    timestamp = GoldenRunFixture.CREATED_AT,
                ),
                payload = gameplayPayload,
                createdAt = GoldenRunFixture.CREATED_AT,
            ),
            store = store,
        )
        assertTrue(store.exists(ArtifactId("ART_STORE_GOLDEN_GAMEPLAY")))
        assertEquals(gameplayPayload, store.read(ArtifactId("ART_STORE_GOLDEN_GAMEPLAY"))?.payload)

        // ── 11: registry carries sha256 + a well-formed (possibly empty) parentRefs list ──
        assertTrue(summary.registry.artifacts.all { it.parentRefs.all { ref -> ref.isNotBlank() } })

        // ── 12: Handoff export from this run's registry ─────────────────────────────────
        val handoffExporter = HandoffExporter()
        val handoffResult = handoffExporter.export(
            request = HandoffExportRequest(
                packageId = HandoffPackageId("HP_GOLDEN_RUN"),
                workspaceId = GoldenRunFixture.WORKSPACE_ID,
                runId = GoldenRunFixture.RUN_ID,
                title = "Golden Run — coop crate/path scenario",
                goal = "Hand off the produced GameplayPlan/SceneBundle/RunnableBuild artifacts for review.",
                audience = HandoffAudience.MANUAL,
                format = HandoffFormat.MARKDOWN,
                artifactRefs = listOf(gameplayArtifact.artifactId.value, sceneArtifact.artifactId.value, targetArtifact.artifactId.value),
                nextGates = listOf("Gate J: real SWIFT/SHADED adapters"),
                constraints = listOf("No BARD code in ANVIL", "LOCAL_ONLY never falls back to cloud"),
                definitionOfDone = listOf("Golden run artifacts reviewed"),
                killCriteria = listOf("Any contract violation blocks the run"),
            ),
            sourceRegistry = summary.registry,
            createdAt = GoldenRunFixture.CREATED_AT,
        )
        assertEquals(3, handoffResult.packageData.artifactRefs.size)

        // ── 13: Workspace Sync bundle from the same run ─────────────────────────────────
        val syncService = WorkspaceSyncService()
        val syncResult = syncService.exportArtifact(
            request = WorkspaceSyncExportRequest(
                bundleId = WorkspaceSyncBundleId("SYNC_GOLDEN_RUN"),
                workspaceId = GoldenRunFixture.WORKSPACE_ID,
                runId = GoldenRunFixture.RUN_ID,
                exportedAt = GoldenRunFixture.CREATED_AT,
                exportedFrom = "golden-run-test-device",
                deviceId = "golden-run-test-device",
                runRefs = listOf(GoldenRunFixture.RUN_ID),
            ),
            registry = summary.registry,
            runSummaries = listOf(summary),
        )
        assertEquals(6, syncResult.bundle.registry.artifacts.size)
        assertTrue(syncResult.bundle.warnings.isEmpty())

        println("GOLDEN RUN — commit-independent evidence")
        println("run.status=${summary.status}")
        println("run.planId=${summary.planRef.value}")
        println("artifact.count=${summary.registry.artifacts.size}")
        println("handoff.artifactId=${handoffResult.artifactWrite.envelope.manifest.artifactId.value}")
        println("sync.artifactId=${syncResult.artifactWrite.envelope.manifest.artifactId.value}")
        println("cue.playableProof=${playableProof.contractId.value}")
        println("cue.temporalProof=${temporalProof.contractId.value}")
        println("cue.audioProof=${audioProof.contractId.value}")
    }

    @Test
    fun goldenRun_negativeCase_unknownContractVersionBlocksBeforeExecution() = runTest {
        val counting = ExecutionCountingModule(GameplayCompilerModule())
        val surface = RunSurface(modules = mapOf("gameplay" to counting))
        val request = GoldenRunFixture.gameplayRequest("fixture-creative-brief", "fixture-production-intent")
        val plan = RunPlan(
            planId = RunPlanId("PLAN_NEGATIVE"),
            workspaceId = GoldenRunFixture.WORKSPACE_ID,
            runId = "run-negative-1",
            artifactRoot = GoldenRunFixture.ARTIFACT_ROOT,
            steps = listOf(
                RunPlanStep(
                    id = "S1", moduleId = "gameplay", operation = GameplayOperation.COMPILE.name,
                    payload = json.encodeToString(request),
                    inputContract = RunContractRef(ContractId("anvil.gameplay.plan"), 99), // unknown version
                ),
            ),
        )
        val summary = surface.execute(plan, createdAt = GoldenRunFixture.CREATED_AT)
        assertEquals(RunStatus.BLOCKED, summary.status)
        assertEquals(0, counting.executionCount, "Module must never run when its input contract version is unsupported")
    }
}
