package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class MemoryEntry(
    val id: String,
    val workspaceId: String,
    val runId: String? = null,
    val content: String,
    val kind: MemoryKind,
    val timestamp: String,
)

enum class MemoryKind {
    LOG,
    OBSERVATION,
    DECISION,
    ERROR,
}
