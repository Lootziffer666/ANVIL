package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class MemoryEntry(
    val id: MemoryEntryId,
    val workspaceId: WorkspaceId,
    val runId: RunId? = null,
    val content: String,
    val timestamp: String,
    val tags: List<String> = emptyList(),
)
