package io.anvil.core.handoff

import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.artifacts.ArtifactWriteRequest
import io.anvil.core.artifacts.ArtifactWriteResult
import io.anvil.core.artifacts.ArtifactWriter
import io.anvil.core.contracts.ModuleArtifactRef
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue

class HandoffExporter(
    private val artifactWriter: ArtifactWriter = ArtifactWriter(),
    private val json: Json = Json { prettyPrint = true; encodeDefaults = true },
) {
    fun export(
        request: HandoffExportRequest,
        sourceRegistry: ArtifactRegistry,
        createdAt: String,
        currentRegistry: ArtifactRegistry = sourceRegistry,
    ): HandoffExportResult {
        val findings = validate(request)
        require(findings.all { it.passed }) { findings.filterNot { it.passed }.joinToString { it.message } }

        val manifestsById = sourceRegistry.artifacts.associateBy { it.artifactId.value }
        val missingRefs = request.artifactRefs.filterNot { it in manifestsById }
        require(missingRefs.isEmpty()) { "Unsupported or missing artifact refs: ${missingRefs.joinToString()}" }

        val handoffRefs = request.artifactRefs.map { artifactId ->
            val manifest = manifestsById.getValue(artifactId)
            HandoffArtifactRef(
                artifactId = manifest.artifactId.value,
                type = manifest.type,
                uri = manifest.uri,
                checksumSha256 = manifest.checksumSha256,
                moduleOrigin = manifest.origin.module,
                createdAt = manifest.createdAt,
            )
        }

        val body = when (request.format) {
            HandoffFormat.MARKDOWN -> renderMarkdown(request, handoffRefs)
            HandoffFormat.JSON -> json.encodeToString(
                HandoffPackage(
                    packageId = request.packageId,
                    workspaceId = request.workspaceId,
                    runId = request.runId,
                    title = request.title,
                    goal = request.goal,
                    audience = request.audience,
                    format = request.format,
                    artifactRefs = handoffRefs,
                    nextGates = request.nextGates,
                    constraints = request.constraints,
                    definitionOfDone = request.definitionOfDone,
                    killCriteria = request.killCriteria,
                    contextFiles = request.contextFiles,
                    parentRefs = request.parentRefs + request.artifactRefs,
                    body = "",
                ),
            )
        }

        val checksum = stableSha256(body)
        val artifactId = "ART_HANDOFF_${stableHash(request.packageId.value + checksum)}"
        val artifactRef = ModuleArtifactRef(
            id = artifactId,
            workspaceId = request.workspaceId,
            runId = request.runId,
            moduleOrigin = MODULE_ORIGIN,
            type = HandoffPackage.SCHEMA,
            uri = "artifact://handoff/${request.packageId.value}",
            sha256 = checksum,
            timestamp = createdAt,
            parentArtifactId = request.artifactRefs.firstOrNull(),
        )
        val writeResult = artifactWriter.write(
            ArtifactWriteRequest(
                artifactRef = artifactRef,
                payload = body,
                createdAt = createdAt,
                parentRefs = request.parentRefs + request.artifactRefs,
            ),
            currentRegistry = currentRegistry,
        )

        return HandoffExportResult(
            packageData = HandoffPackage(
                packageId = request.packageId,
                workspaceId = request.workspaceId,
                runId = request.runId,
                title = request.title,
                goal = request.goal,
                audience = request.audience,
                format = request.format,
                artifactRefs = handoffRefs,
                nextGates = request.nextGates,
                constraints = request.constraints,
                definitionOfDone = request.definitionOfDone,
                killCriteria = request.killCriteria,
                contextFiles = request.contextFiles,
                parentRefs = request.parentRefs + request.artifactRefs,
                body = body,
            ),
            artifactWrite = writeResult,
        )
    }

    fun validate(request: HandoffExportRequest): List<HandoffValidationFinding> = listOf(
        HandoffValidationFinding("schema", request.schema == HandoffExportRequest.SCHEMA, "Request schema must be ${HandoffExportRequest.SCHEMA}."),
        HandoffValidationFinding("package-id", request.packageId.value.isNotBlank(), "Package id is required."),
        HandoffValidationFinding("workspace", request.workspaceId.isNotBlank(), "Workspace id is required."),
        HandoffValidationFinding("run", request.runId.isNotBlank(), "Run id is required."),
        HandoffValidationFinding("title", request.title.isNotBlank(), "Title is required."),
        HandoffValidationFinding("goal", request.goal.isNotBlank(), "Goal is required."),
        HandoffValidationFinding("artifact-refs", request.artifactRefs.isNotEmpty(), "At least one artifact ref is required."),
        HandoffValidationFinding("kill-criteria", request.killCriteria.isNotEmpty(), "Kill criteria are mandatory for handoff packs."),
    )

    private fun renderMarkdown(request: HandoffExportRequest, artifacts: List<HandoffArtifactRef>): String = buildString {
        appendLine("# Agent Prompt Pack: ${request.title}")
        appendLine()
        appendLine("## Ziel")
        appendLine(request.goal)
        appendLine()
        appendLine("## Ist-Zustand")
        appendLine("- Workspace: `${request.workspaceId}`")
        appendLine("- Run: `${request.runId}`")
        appendLine("- Package: `${request.packageId.value}`")
        appendLine("- Audience: `${request.audience}`")
        appendLine()
        appendSection("Nächste Gates", request.nextGates)
        appendSection("Harte Constraints", request.constraints)
        appendSection("Definition of Done", request.definitionOfDone)
        appendSection("Kill-Kriterien", request.killCriteria)
        appendLine("## Agent-Zielsystem")
        appendLine(request.audience.name)
        appendLine()
        appendLine("## Kontext-Dateien")
        if (request.contextFiles.isEmpty()) appendLine("- Keine direkten Dateikontexte; nutze die referenzierten Artifacts.") else request.contextFiles.forEach { appendLine("- `$it`") }
        appendLine()
        appendLine("## Artifact-Beweise")
        artifacts.forEach { artifact ->
            appendLine("- `${artifact.artifactId}` — `${artifact.type}` — `${artifact.uri}` — `${artifact.checksumSha256}` — Ursprung `${artifact.moduleOrigin}`")
        }
    }

    private fun StringBuilder.appendSection(title: String, values: List<String>) {
        appendLine("## $title")
        if (values.isEmpty()) appendLine("- Nicht angegeben.") else values.forEach { appendLine("- $it") }
        appendLine()
    }

    private fun stableSha256(value: String): String = "sha256:${stableHash(value)}"

    private fun stableHash(value: String): String {
        var hash = 0
        value.forEach { char -> hash = (hash * 31) + char.code }
        return hash.absoluteValue.toString(16).padStart(8, '0')
    }

    private companion object { const val MODULE_ORIGIN = "anvil-handoff-exporter" }
}

@kotlinx.serialization.Serializable
data class HandoffExportResult(
    val packageData: HandoffPackage,
    val artifactWrite: ArtifactWriteResult,
)
