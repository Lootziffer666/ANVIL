package io.anvil.modules.forge.runner

import io.anvil.core.contracts.BellowsContract
import io.anvil.core.contracts.BellowsExhaustedException
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.pipeline.RunContext
import io.anvil.core.pipeline.RunEngine
import io.anvil.core.pipeline.RunResult
import io.anvil.core.pipeline.RunStep
import io.anvil.core.pipeline.StepRecord
import io.anvil.core.quality.CommandGuard
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class ForgeRunner(
    private val bellows: BellowsContract,
) : RunEngine {

    override suspend fun step(context: RunContext, step: RunStep): RunResult =
        runCatching { dispatch(context, step) }
            .getOrElse { e ->
                RunResult.Failure(
                    error = e.message ?: "Unbekannter Fehler",
                    recoveryStep = if (e is BellowsExhaustedException) "Lokales Modell prüfen oder PrivacyMode anpassen"
                                   else "Review oder erneut versuchen",
                )
            }

    override suspend fun run(context: RunContext, steps: List<RunStep>): List<StepRecord> {
        val records = mutableListOf<StepRecord>()
        for ((index, s) in steps.withIndex()) {
            val start: Instant = Clock.System.now()
            val result = step(context, s)
            val end: Instant = Clock.System.now()
            records += StepRecord(
                stepId = "${context.runId.value}-$index",
                runId = context.runId,
                stepType = s::class.simpleName ?: "Step",
                resultType = result::class.simpleName ?: "Result",
                timestamp = end.toString(),
                durationMs = (end - start).inWholeMilliseconds,
            )
            if (result is RunResult.Failure || result is RunResult.Blocked) break
        }
        return records
    }

    private suspend fun dispatch(context: RunContext, step: RunStep): RunResult = when (step) {
        is RunStep.Plan -> {
            bellows.route(ModelRequest(prompt = step.goal))
            RunResult.Success(emptyList())
        }
        is RunStep.Execute -> {
            CommandGuard.require(step.command)
            RunResult.Success(emptyList())
        }
        is RunStep.Review -> RunResult.NeedsReview(step.taskId, step.message)
        RunStep.Finish -> RunResult.Success(emptyList())
    }
}
