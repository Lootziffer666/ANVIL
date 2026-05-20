package io.anvil.core.domain

import kotlinx.serialization.Serializable

@JvmInline @Serializable value class WorkspaceId(val value: String)
@JvmInline @Serializable value class RunId(val value: String)
@JvmInline @Serializable value class PlanId(val value: String)
@JvmInline @Serializable value class TaskId(val value: String)
@JvmInline @Serializable value class ArtifactId(val value: String)
@JvmInline @Serializable value class SnapshotId(val value: String)
@JvmInline @Serializable value class MemoryEntryId(val value: String)
