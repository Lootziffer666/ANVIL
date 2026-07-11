package io.anvil.core.sync

import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.artifacts.ArtifactWriteRequest
import io.anvil.core.artifacts.ArtifactWriteResult
import io.anvil.core.artifacts.ArtifactWriter
import io.anvil.core.contracts.ModuleArtifactRef
import io.anvil.core.run.RunSummary
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue

class WorkspaceSyncService(
    private val artifactWriter: ArtifactWriter = ArtifactWriter(),
    private val json: Json = Json { prettyPrint = true; encodeDefaults = true },
) {
    fun exportBundle(
        request: WorkspaceSyncExportRequest,
        registry: ArtifactRegistry,
        runSummaries: List<RunSummary>,
    ): WorkspaceSyncBundle {
        val findings = validateRequest(request)
        require(findings.all { it.passed }) { findings.filterNot { it.passed }.joinToString { it.message } }

        val workspaceArtifacts = registry.artifacts.filter { it.origin.workspaceId.value == request.workspaceId }
        val filteredRegistry = registry.copy(artifacts = workspaceArtifacts)
        val selectedRuns = runSummaries
            .filter { it.workspaceId == request.workspaceId }
            .filter { request.runRefs.isEmpty() || it.runId in request.runRefs }
            .map { summary ->
                WorkspaceSyncRunRef(
                    planId = summary.planRef.value,
                    runId = summary.runId,
                    status = summary.status,
                    artifactRefs = summary.records.mapNotNull { it.artifact?.artifactId?.value },
                )
            }

        val warnings = buildList {
            val excludedArtifacts = registry.artifacts.size - workspaceArtifacts.size
            if (excludedArtifacts > 0) add("Excluded $excludedArtifacts artifact manifest(s) from other workspaces.")
            val missingRuns = request.runRefs.filterNot { requested -> selectedRuns.any { it.runId == requested } }
            if (missingRuns.isNotEmpty()) add("Requested run refs not found for workspace ${request.workspaceId}: ${missingRuns.joinToString()}.")
        }

        return WorkspaceSyncBundle(
            bundleId = request.bundleId,
            workspaceId = request.workspaceId,
            exportedAt = request.exportedAt,
            exportedFrom = request.exportedFrom,
            deviceId = request.deviceId,
            registry = filteredRegistry,
            runs = selectedRuns,
            warnings = warnings,
        )
    }

    fun exportArtifact(
        request: WorkspaceSyncExportRequest,
        registry: ArtifactRegistry,
        runSummaries: List<RunSummary>,
        currentRegistry: ArtifactRegistry = registry,
    ): WorkspaceSyncExportResult {
        val bundle = exportBundle(request, registry, runSummaries)
        val payload = json.encodeToString(bundle)
        val checksum = stableSha256(payload)
        val artifactRef = ModuleArtifactRef(
            id = "ART_SYNC_${stableHash(request.bundleId.value + checksum)}",
            workspaceId = request.workspaceId,
            runId = request.runId,
            moduleOrigin = MODULE_ORIGIN,
            type = WorkspaceSyncBundle.SCHEMA,
            uri = "artifact://sync/${request.bundleId.value}",
            sha256 = checksum,
            timestamp = request.exportedAt,
            parentArtifactId = bundle.registry.artifacts.firstOrNull()?.artifactId?.value,
        )
        val writeResult = artifactWriter.write(
            ArtifactWriteRequest(
                artifactRef = artifactRef,
                payload = payload,
                createdAt = request.exportedAt,
                parentRefs = request.parentRefs + bundle.registry.artifacts.map { it.artifactId.value },
            ),
            currentRegistry = currentRegistry,
        )
        return WorkspaceSyncExportResult(bundle = bundle, artifactWrite = writeResult)
    }

    fun merge(current: ArtifactRegistry, incoming: WorkspaceSyncBundle): WorkspaceSyncMergeResult {
        val findings = validateBundle(incoming)
        if (findings.any { !it.passed }) {
            return WorkspaceSyncMergeResult(
                registry = current,
                report = WorkspaceSyncMergeReport(
                    workspaceId = incoming.workspaceId,
                    artifactCountBefore = current.artifacts.size,
                    artifactCountAfter = current.artifacts.size,
                    importedArtifactRefs = emptyList(),
                    skippedArtifactRefs = incoming.registry.artifacts.map { it.artifactId.value },
                    findings = findings,
                ),
            )
        }

        val currentById = current.artifacts.associateBy { it.artifactId.value }
        val incomingWorkspaceArtifacts = incoming.registry.artifacts.filter { it.origin.workspaceId.value == incoming.workspaceId }
        val imported = incomingWorkspaceArtifacts.filterNot { it.artifactId.value in currentById }
        val merged = current.copy(artifacts = current.artifacts + imported)
        return WorkspaceSyncMergeResult(
            registry = merged,
            report = WorkspaceSyncMergeReport(
                workspaceId = incoming.workspaceId,
                artifactCountBefore = current.artifacts.size,
                artifactCountAfter = merged.artifacts.size,
                importedArtifactRefs = imported.map { it.artifactId.value },
                skippedArtifactRefs = incomingWorkspaceArtifacts.filter { it.artifactId.value in currentById }.map { it.artifactId.value },
                findings = findings,
            ),
        )
    }

    fun validateRequest(request: WorkspaceSyncExportRequest): List<WorkspaceSyncValidationFinding> = listOf(
        WorkspaceSyncValidationFinding("schema", request.schema == WorkspaceSyncExportRequest.SCHEMA, "Request schema must be ${WorkspaceSyncExportRequest.SCHEMA}."),
        WorkspaceSyncValidationFinding("bundle-id", request.bundleId.value.isNotBlank(), "Bundle id is required."),
        WorkspaceSyncValidationFinding("workspace", request.workspaceId.isNotBlank(), "Workspace id is required."),
        WorkspaceSyncValidationFinding("run", request.runId.isNotBlank(), "Export run id is required."),
        WorkspaceSyncValidationFinding("timestamp", request.exportedAt.isNotBlank(), "Export timestamp is required."),
        WorkspaceSyncValidationFinding("device", request.deviceId.isNotBlank(), "Device id is required."),
    )

    fun validateBundle(bundle: WorkspaceSyncBundle): List<WorkspaceSyncValidationFinding> = listOf(
        WorkspaceSyncValidationFinding("schema", bundle.schema == WorkspaceSyncBundle.SCHEMA, "Bundle schema must be ${WorkspaceSyncBundle.SCHEMA}."),
        WorkspaceSyncValidationFinding("bundle-id", bundle.bundleId.value.isNotBlank(), "Bundle id is required."),
        WorkspaceSyncValidationFinding("workspace", bundle.workspaceId.isNotBlank(), "Workspace id is required."),
        WorkspaceSyncValidationFinding("timestamp", bundle.exportedAt.isNotBlank(), "Export timestamp is required."),
        WorkspaceSyncValidationFinding("no-cross-workspace-artifacts", bundle.registry.artifacts.all { it.origin.workspaceId.value == bundle.workspaceId }, "Bundle must not contain artifact manifests from other workspaces."),
        WorkspaceSyncValidationFinding("checksums-only", bundle.registry.artifacts.all { it.checksumSha256.startsWith("sha256:") }, "All artifact manifests must carry sha256 checksums."),
    )

    private fun stableSha256(value: String): String = "sha256:${stableHash(value)}"

    private fun stableHash(value: String): String {
        var hash = 0
        value.forEach { char -> hash = (hash * 31) + char.code }
        return hash.absoluteValue.toString(16).padStart(8, '0')
    }

    private companion object { const val MODULE_ORIGIN = "anvil-workspace-sync" }
}

@Serializable
data class WorkspaceSyncExportResult(
    val bundle: WorkspaceSyncBundle,
    val artifactWrite: ArtifactWriteResult,
)

@Serializable
data class WorkspaceSyncMergeResult(
    val registry: ArtifactRegistry,
    val report: WorkspaceSyncMergeReport,
)
