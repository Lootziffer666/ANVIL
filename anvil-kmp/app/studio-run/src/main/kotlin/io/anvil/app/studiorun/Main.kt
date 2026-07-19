package io.anvil.app.studiorun

import io.anvil.core.contracts.ContractId
import io.anvil.core.contracts.ExternalToolPort
import io.anvil.core.contracts.ExternalToolRequest
import io.anvil.core.contracts.ExternalToolResult
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.externaladapters.CueCliAdapter
import io.anvil.core.externaladapters.ShadedCliAdapter
import io.anvil.core.externaladapters.SwiftCliAdapter
import io.anvil.core.externaladapters.WizardHttpAdapter
import io.anvil.core.handoff.HandoffAudience
import io.anvil.core.handoff.HandoffExportRequest
import io.anvil.core.handoff.HandoffExporter
import io.anvil.core.handoff.HandoffFormat
import io.anvil.core.handoff.HandoffPackageId
import io.anvil.core.run.RunStatus
import io.anvil.core.run.RunSurface
import io.anvil.core.sync.WorkspaceSyncBundleId
import io.anvil.core.sync.WorkspaceSyncExportRequest
import io.anvil.core.sync.WorkspaceSyncService
import io.anvil.modules.target.TargetEngine
import io.anvil.modules.target.TargetPlatform
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlin.system.exitProcess

/**
 * Studio Run — the "3 Random words und ne Engineauswahl" CLI: assembles a real
 * [io.anvil.core.run.RunPlan] across Anvil's native module chain and executes it through
 * the real [RunSurface], wrapped by real [ExternalToolPort] adapters for WIZARD/SHADED/
 * SWIFT/CUE-AGENT where configured — this is the piece that was missing (Gate C1): every
 * adapter and every native module already existed and was individually proven (Gate E-03,
 * R-19..21, Gate I), but nothing assembled them into one runnable program outside of tests.
 *
 * Every external system that is NOT configured falls back to a self-labeled
 * [FixtureBardPort]/[FixtureWizardPort]/[FixtureSwiftPort]/[FixtureShadedPort]/
 * [FixtureCuePort] — never silently faked as real. BARD always stays a fixture: no real
 * BARD adapter exists anywhere in this codebase.
 */
fun main(args: Array<String>) = runBlocking {
    // Force UTF-8 stdout/stderr — a POSIX/C locale (no LANG set, seen live in this
    // sandbox) otherwise mangles every non-ASCII console character (em dashes, etc.)
    // into "?", the same class of bug :app:bellows-gateway's Main.kt already guards
    // against for Windows legacy codepages.
    System.setOut(java.io.PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(java.io.PrintStream(System.err, true, Charsets.UTF_8))

    val cli = Cli(args)
    val seedWords = cli.seed()
    if (seedWords.isEmpty()) {
        System.err.println("Nutzung: studio-run --seed \"drei zufällige worte\" [--engine WEB|UNREAL|GODOT|KORGE|UEFN] [--platform WEB|WINDOWS|ANDROID|LINUX|MACOS] [--verbs a,b,c] [--roles a,b,c] [--out <dir>] [--cue-target-url <url>]")
        exitProcess(2)
    }

    val runId = "run-${Instant.now().epochSecond}-${UUID.randomUUID().toString().take(8)}"
    val createdAt = Instant.now().toString()
    val outDir = File(cli.value("--out") ?: "studio-run-out/$runId")
    outDir.mkdirs()

    val client = HttpClient(Java) {
        expectSuccess = false
        install(HttpTimeout) { requestTimeoutMillis = 20_000 }
    }

    val systems = ExternalSystems.resolve(cli, seedWords, client)
    println("=".repeat(64))
    println(" STUDIO RUN — seed: ${seedWords.joinToString(" ")}")
    println(" runId: $runId")
    println("-".repeat(64))
    systems.all.forEach { (name, wiring) ->
        println(" $name: ${if (wiring.isReal) "REAL" else "FIXTURE"} (${wiring.port.toolId})")
    }
    println("=".repeat(64))

    println("\n-- Health --")
    systems.all.forEach { (name, wiring) ->
        val health = wiring.port.health()
        println(" $name.health = ${health.quality} — ${health.message}")
    }

    println("\n-- WIZARD production assessment --")
    // WIZARD's real `/api/production-assessment` contract requires exactly `{"brief": "<non-empty
    // string>"}` (WIZARD/src/lib/contracts/productionAssessment.ts, isProductionAssessmentRequest) —
    // found live against the real deployed WIZARD server (a prior `{"seedWords": [...], "note": ...}`
    // shape passed against a fixture/mock but 400'd against the real one). No real BARD exists to
    // expand the seed into a proper brief, so this is honestly just the raw seed text, not a
    // fabricated AI-expanded one.
    val briefText = Json.encodeToString(WizardProductionAssessmentRequest(brief = seedWords.joinToString(" ")))
    val wizardResult = systems.wizard.port.invoke(
        ExternalToolRequest(ContractId("anvil.wizard.production-assessment-request"), 1, PrivacyMode.OPEN, briefText),
    )
    printToolResult("wizard", wizardResult)

    println("\n-- Native module chain (RunSurface) --")
    val assembly = StudioRunAssembler.assemble(
        StudioRunInput(
            seedWords = seedWords,
            workspaceId = cli.value("--workspace") ?: "ws-studio-run",
            runId = runId,
            artifactRoot = "artifact://studio-run/$runId",
            verbs = cli.list("--verbs") ?: listOf("interact"),
            playerRoles = cli.list("--roles") ?: listOf("player"),
            targetEngine = cli.value("--engine")?.let { TargetEngine.valueOf(it.uppercase()) } ?: TargetEngine.WEB,
            targetPlatform = cli.value("--platform")?.let { TargetPlatform.valueOf(it.uppercase()) } ?: TargetPlatform.WEB,
        ),
    )
    val surface = RunSurface(modules = assembly.modules)
    val summary = surface.execute(assembly.plan, createdAt)

    println(" status = ${summary.status}")
    summary.records.forEach { record ->
        println("  [${record.status}] ${record.stepId} (${record.moduleId}.${record.operation}) quality=${record.qualityState}${record.message?.let { " — $it" } ?: ""}")
    }

    val json = Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true }
    File(outDir, "run-summary.json").writeText(json.encodeToString(summary))

    if (summary.status != RunStatus.COMPLETE) {
        println("\nRun did not complete (status=${summary.status}) — skipping CUE proofs and Handoff/Sync export.")
        exitProcess(1)
    }

    val targetArtifact = summary.records.first { it.stepId == "S_TARGET" }.artifact!!

    val cueTargetUrl = cli.value("--cue-target-url")
    println("\n-- CUE proofs --")
    if (cueTargetUrl != null) {
        listOf("cue.playable-proof", "cue.temporal-proof", "cue.audio-proof").forEach { contractId ->
            val result = systems.cue.port.invoke(ExternalToolRequest(ContractId(contractId), 1, PrivacyMode.OPEN, cueTargetUrl))
            printToolResult(contractId, result)
            // CUE maps genuine negative verdicts (exit 1, e.g. "KEIN AUDIO-VERTRAG GEFUNDEN")
            // to Produced too — real output, not a failure. Without surfacing the verdict
            // here, "Produced(637 chars)" reads like a pass; found in the first full relay
            // run, where the audio proof was an (expected, honest) negative.
            if (result is ExternalToolResult.Produced) {
                cueVerdictOf(result.payload)?.let { println("   verdict: $it") }
            }
        }
    } else {
        println(" skipped — no --cue-target-url supplied (S_TARGET produces a build PLAN, not a served URL; pass the URL of an already-running build to get a real CUE verdict).")
    }

    println("\n-- Handoff + Workspace Sync export --")
    val handoffResult = HandoffExporter().export(
        request = HandoffExportRequest(
            packageId = HandoffPackageId("HP_$runId"),
            workspaceId = assembly.plan.workspaceId,
            runId = runId,
            title = "Studio Run — ${seedWords.joinToString(" ")}",
            goal = "Hand off the produced GameplayPlan/SceneBundle/RunnableBuild artifacts for review.",
            audience = HandoffAudience.MANUAL,
            format = HandoffFormat.MARKDOWN,
            artifactRefs = summary.registry.artifacts.map { it.artifactId.value },
            nextGates = listOf("Real CUE proofs against a served build", "Real SHADED/SWIFT asset generation from this seed"),
            constraints = listOf("No BARD code in ANVIL", "LOCAL_ONLY never falls back to cloud", "Audio asset generation intentionally out of scope (separate pipeline)"),
            definitionOfDone = listOf("Studio Run artifacts reviewed"),
            killCriteria = listOf("Any contract violation blocks the run"),
        ),
        sourceRegistry = summary.registry,
        createdAt = createdAt,
    )
    File(outDir, "handoff.json").writeText(json.encodeToString(handoffResult.packageData))

    val syncResult = WorkspaceSyncService().exportArtifact(
        request = WorkspaceSyncExportRequest(
            bundleId = WorkspaceSyncBundleId("SYNC_$runId"),
            workspaceId = assembly.plan.workspaceId,
            runId = runId,
            exportedAt = createdAt,
            exportedFrom = "studio-run-cli",
            deviceId = "studio-run-cli",
            runRefs = listOf(runId),
        ),
        registry = summary.registry,
        runSummaries = listOf(summary),
    )
    File(outDir, "sync-bundle.json").writeText(json.encodeToString(syncResult.bundle))

    println(" wrote: ${outDir.path}/{run-summary,handoff,sync-bundle}.json")
    println("\nSTUDIO RUN COMPLETE — target=${targetArtifact.artifactId.value} (${assembly.plan.steps.last().payload.length} chars in final step payload)")
}

/** WIZARD's real wire shape (WIZARD/src/lib/contracts/productionAssessment.ts `ProductionAssessmentRequest`). */
@Serializable
internal data class WizardProductionAssessmentRequest(val brief: String, val maxPerRole: Int? = null)

/**
 * Extracts the `verdict` field from a CUE check's JSON stdout (all of CUE's
 * *-check commands emit one). Null for non-JSON or verdict-less payloads —
 * printing then just falls back to the plain Produced(...) line.
 */
internal fun cueVerdictOf(payload: String): String? = try {
    Json.parseToJsonElement(payload).jsonObject["verdict"]?.jsonPrimitive?.contentOrNull
} catch (_: Exception) {
    null
}

private fun printToolResult(label: String, result: ExternalToolResult) {
    when (result) {
        is ExternalToolResult.Produced -> println(" $label = Produced(${result.contractId.value}, ${result.payload.length} chars)")
        is ExternalToolResult.Failed -> println(" $label = Failed(${result.reason})")
        is ExternalToolResult.BlockedExternalContract -> println(" $label = BlockedExternalContract(${result.reason})")
    }
}

class Cli(argv: Array<String>) {
    private val flags = mutableMapOf<String, String>()

    init {
        var i = 0
        while (i < argv.size) {
            val a = argv[i]
            if (a.startsWith("--")) {
                val next = argv.getOrNull(i + 1)
                if (next != null && !next.startsWith("--")) {
                    flags[a] = next; i += 2
                } else {
                    flags[a] = "true"; i += 1
                }
            } else {
                i += 1
            }
        }
    }

    fun value(name: String): String? = flags[name]?.takeUnless { it == "true" }
    fun list(name: String): List<String>? = value(name)?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
    fun seed(): List<String> = value("--seed")?.trim()?.split(Regex("\\s+"))?.filter { it.isNotBlank() } ?: emptyList()
}

/** Which [ExternalToolPort] backs each external system, and whether it is real or a fixture. */
data class SystemWiring(val port: ExternalToolPort, val isReal: Boolean)

class ExternalSystems(
    val bard: SystemWiring,
    val wizard: SystemWiring,
    val swift: SystemWiring,
    val shaded: SystemWiring,
    val cue: SystemWiring,
) {
    val all: Map<String, SystemWiring> = mapOf("bard" to bard, "wizard" to wizard, "swift" to swift, "shaded" to shaded, "cue" to cue)

    companion object {
        fun resolve(cli: Cli, seedWords: List<String>, client: HttpClient): ExternalSystems {
            val wizardBaseUrl = cli.value("--wizard-url") ?: System.getenv("WIZARD_BASE_URL")
            val shadedRepoPath = cli.value("--shaded-repo") ?: System.getenv("SHADED_REPO_PATH")
            val swiftRepoPath = cli.value("--swift-repo") ?: System.getenv("SWIFT_REPO_PATH")
            val cueRepoPath = cli.value("--cue-repo") ?: System.getenv("CUE_AGENT_REPO_PATH")

            return ExternalSystems(
                bard = SystemWiring(FixtureBardPort(seedWords), isReal = false),
                wizard = if (wizardBaseUrl != null) {
                    SystemWiring(WizardHttpAdapter(wizardBaseUrl, client), isReal = true)
                } else {
                    SystemWiring(FixtureWizardPort(), isReal = false)
                },
                swift = if (swiftRepoPath != null) {
                    SystemWiring(SwiftCliAdapter(repoRoot = File(swiftRepoPath)), isReal = true)
                } else {
                    SystemWiring(FixtureSwiftPort(), isReal = false)
                },
                shaded = if (shadedRepoPath != null) {
                    SystemWiring(ShadedCliAdapter(repoRoot = File(shadedRepoPath)), isReal = true)
                } else {
                    SystemWiring(FixtureShadedPort(), isReal = false)
                },
                cue = if (cueRepoPath != null) {
                    SystemWiring(CueCliAdapter(repoRoot = File(cueRepoPath)), isReal = true)
                } else {
                    SystemWiring(FixtureCuePort(), isReal = false)
                },
            )
        }
    }
}
