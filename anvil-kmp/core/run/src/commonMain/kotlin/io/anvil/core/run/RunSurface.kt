package io.anvil.core.run

import io.anvil.core.artifacts.ArtifactManifest
import io.anvil.core.artifacts.ArtifactRegistry
import io.anvil.core.artifacts.ArtifactWriteRequest
import io.anvil.core.artifacts.ArtifactWriter
import io.anvil.core.contracts.AnvilContractRegistry
import io.anvil.core.contracts.ContractRegistry
import io.anvil.core.contracts.ContractViolationException
import io.anvil.core.contracts.ModuleContext
import io.anvil.core.contracts.ModuleRunStep
import io.anvil.core.contracts.ModuleSlotContract
import io.anvil.core.contracts.QualityState
import io.anvil.core.contracts.StepResult

class RunSurface(
    private val modules: Map<String, ModuleSlotContract>,
    private val artifactWriter: ArtifactWriter = ArtifactWriter(),
    private val contracts: ContractRegistry = AnvilContractRegistry.default,
    private val resolver: RunDependencyResolver = RunDependencyResolver(contracts),
) {
    suspend fun execute(plan: RunPlan, createdAt: String): RunSummary {
        require(plan.steps.isNotEmpty()) { "RunPlan requires at least one step." }
        var registry = ArtifactRegistry()
        val records = mutableListOf<RunStepRecord>()
        val artifactsByStepId = mutableMapOf<String, ArtifactManifest>()
        val payloadsByStepId = mutableMapOf<String, String>()

        val orderedSteps = try {
            resolver.order(plan)
        } catch (violation: RunDependencyException) {
            return blockedSummary(plan, registry, "ORDER", violation.message)
        }

        for (step in orderedSteps) {
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

            val resolvedPayload = try {
                resolver.resolveInput(step, artifactsByStepId, payloadsByStepId, registry)
            } catch (violation: Exception) {
                records += contractRejection(step, violation)
                break
            }

            try {
                resolver.requireOutputAllowed(step.outputContract, step.moduleId)
            } catch (violation: ContractViolationException) {
                records += contractRejection(step, violation)
                break
            }

            val context = ModuleContext(
                moduleId = step.moduleId,
                workspaceId = plan.workspaceId,
                runId = plan.runId,
                artifactRoot = plan.artifactRoot,
                createdAt = createdAt,
            )
            val result = module.handle(ModuleRunStep(operation = step.operation, payload = resolvedPayload, context = context))
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
                    val outputViolation = step.outputContract?.let { ref ->
                        val expectedType = "${contracts.requireSupported(ref.id, ref.version).id.value}/v${ref.version}"
                        if (write.envelope.manifest.type != expectedType) {
                            "Step '${step.id}' produced type '${write.envelope.manifest.type}' but outputContract requires '$expectedType'."
                        } else {
                            null
                        }
                    }
                    if (outputViolation != null) {
                        records += RunStepRecord(
                            stepId = step.id,
                            moduleId = step.moduleId,
                            operation = step.operation,
                            status = RunStepStatus.REJECTED,
                            qualityState = QualityState.BLOCKED,
                            message = outputViolation,
                        )
                        break
                    }
                    registry = write.registry
                    artifactsByStepId[step.id] = write.envelope.manifest
                    payloadsByStepId[step.id] = result.payload
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

    private fun contractRejection(step: RunPlanStep, violation: Exception) = RunStepRecord(
        stepId = step.id,
        moduleId = step.moduleId,
        operation = step.operation,
        status = RunStepStatus.REJECTED,
        qualityState = QualityState.BLOCKED,
        message = violation.message ?: "Contract violation.",
    )

    private fun blockedSummary(plan: RunPlan, registry: ArtifactRegistry, operation: String, message: String?) = RunSummary(
        planRef = plan.planId,
        workspaceId = plan.workspaceId,
        runId = plan.runId,
        status = RunStatus.BLOCKED,
        records = listOf(
            RunStepRecord(
                stepId = "N/A",
                moduleId = "N/A",
                operation = operation,
                status = RunStepStatus.REJECTED,
                qualityState = QualityState.BLOCKED,
                message = message ?: "Run plan could not be ordered.",
            ),
        ),
        registry = registry,
    )

    private fun statusFor(plan: RunPlan, records: List<RunStepRecord>): RunStatus = when {
        records.any { it.status == RunStepStatus.FAILED } -> RunStatus.FAILED
        records.size < plan.steps.size || records.any { it.status == RunStepStatus.REJECTED || it.status == RunStepStatus.MISSING_MODULE } -> RunStatus.BLOCKED
        else -> RunStatus.COMPLETE
    }
}
