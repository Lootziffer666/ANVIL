package io.anvil.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Run(
    val runId: RunId,
    val workspaceId: WorkspaceId,
    val planId: PlanId,
    val taskId: TaskId,
    val status: RunStatus,
    val startedAt: String,
    val finishedAt: String? = null,
    val artifacts: List<ArtifactId> = emptyList(),
    val logs: List<String> = emptyList(),
    val failureState: String? = null,
    val recoveryStep: String? = null,
    val commandsRun: List<CommandRecord> = emptyList(),
    val changedFiles: List<ChangedFile> = emptyList(),
    val testsRun: List<TestEntry> = emptyList(),
    val humanReviewRequired: Boolean = true,
)

enum class RunStatus { ADAPTING, STABLE, ACT_NOW, FAILED }

@Serializable
data class CommandRecord(
    val command: String,
    val workingDir: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val timestamp: String,
    val allowedBy: String,
)

@Serializable
data class ChangedFile(
    val path: String,
    val action: FileAction,
    val diff: String? = null,
    val timestamp: String,
    val rollbackAvailable: Boolean = true,
)

enum class FileAction { CREATE, MODIFY, DELETE }

@Serializable
data class TestEntry(
    val testName: String,
    val status: TestStatus,
    val output: String,
    val timestamp: String,
)

enum class TestStatus { PASS, FAIL, SKIP, ERROR }
