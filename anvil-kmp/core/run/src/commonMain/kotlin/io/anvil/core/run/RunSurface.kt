package io.anvil.core.run

import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.artifacts.ArtifactWriteRequest
import io.anvil.core.artifacts.ArtifactWriter
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult

class RunSurface(
    private val modules: Map<String, ModuleSlotContract>,
    private val artifactWriter: ArtifactWriter = ArtifactWriter(),
) {
    suspend fun execute(plan: RunPlan, createdAt: String): RunSummary {
        require(plan.steps.isNotEmpty()) { "RunPlan requires at least one step." }
        var registry = ArtifactRegistry()
        val records = mutableListOf<RunStepRecord>()

        for (step in plan.steps) {
            val module = modules[step.moduleId]
            if (module == null) {
                records += RunStepRecord(
                    stepId = step.id,
                    moduleId = step.moduleId,
                    operation = step.operation,
                    status = RunStepStatus.MISSING_MODULE,
                    qualityState = QualityState.BLOCKED,
                    message = "Module not registered for run.",
                )
                break
            }

            val context = ModuleContext(
                moduleId = step.moduleId,
                workspaceId = plan.workspaceId,
                runId = plan.runId,
                artifactRoot = plan.artifactRoot,
                createdAt = createdAt,
            )
            val result = module.handle(ModuleRunStep(operation = step.operation, payload = step.payload, context = context))
            when (result) {
                is StepResult.Completed -> {
                    val write = artifactWriter.write(
                        request = ArtifactWriteRequest(
                            artifactRef = result.artifact,
                            payload = result.payload,
                            createdAt = createdAt,
                            parentRefs = step.parentRefs,
                        ),
                        currentRegistry = registry,
                    )
                    registry = write.registry
                    records += RunStepRecord(
                        stepId = step.id,
                        moduleId = step.moduleId,
                        operation = step.operation,
                        status = RunStepStatus.COMPLETED,
                        qualityState = result.qualityState,
                        artifact = write.envelope.manifest,
                    )
                }
                is StepResult.Rejected -> {
                    records += RunStepRecord(
                        stepId = step.id,
                        moduleId = step.moduleId,
                        operation = step.operation,
                        status = RunStepStatus.REJECTED,
                        qualityState = result.qualityState,
                        message = result.reason,
                    )
                    break
                }
                is StepResult.Failed -> {
                    records += RunStepRecord(
                        stepId = step.id,
                        moduleId = step.moduleId,
                        operation = step.operation,
                        status = RunStepStatus.FAILED,
                        qualityState = result.qualityState,
                        message = result.reason,
                    )
                    break
                }
            }
        }

        return RunSummary(
            planRef = plan.planId,
            workspaceId = plan.workspaceId,
            runId = plan.runId,
            status = statusFor(plan, records),
            records = records,
            registry = registry,
        )
    }

    private fun statusFor(plan: RunPlan, records: List<RunStepRecord>): RunStatus = when {
        records.any { it.status == RunStepStatus.FAILED } -> RunStatus.FAILED
        records.size < plan.steps.size || records.any { it.status == RunStepStatus.REJECTED || it.status == RunStepStatus.MISSING_MODULE } -> RunStatus.BLOCKED
        else -> RunStatus.COMPLETE
    }
}
