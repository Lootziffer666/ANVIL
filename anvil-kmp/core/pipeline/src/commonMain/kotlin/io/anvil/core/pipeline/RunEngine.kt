package io.anvil.core.pipeline

// Abstrakte Engine-Schnittstelle — konkrete Implementierung in :modules:forge:runner (B11+).
// Hält :core:pipeline frei von Modul-Abhängigkeiten.
interface RunEngine {
    suspend fun step(context: RunContext, step: RunStep): RunResult
    suspend fun run(context: RunContext, steps: List<RunStep>): List<StepRecord>
}
