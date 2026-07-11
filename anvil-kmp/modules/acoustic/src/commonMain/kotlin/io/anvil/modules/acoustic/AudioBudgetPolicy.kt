package io.anvil.modules.acoustic

import kotlinx.serialization.Serializable

@Serializable
enum class AudioBudgetVerdict { ALLOWED, NEEDS_APPROVAL, BLOCKED }

/**
 * Conservative Starter-plan defaults (Gate F-06). These are configurable budget
 * assumptions valid at the time of writing, not an eternal hard-coded truth — see
 * `docs/GOLDEN_RUN_REPORT.md` for the ElevenLabs plan basis this was derived from.
 */
@Serializable
data class AudioBudgetPolicy(
    val maxMusicCreditsPerRun: Int = 5400,
    val maxSfxCreditsPerRun: Int = 2000,
    val maxTotalCreditsPerRun: Int = 7000,
    val allowPaidRetry: Boolean = false,
) {
    fun evaluate(kind: AudioGenerationKind, estimatedCredits: Int, alreadySpentCreditsThisRun: Int): AudioBudgetVerdict {
        val kindLimit = when (kind) {
            AudioGenerationKind.MUSIC -> maxMusicCreditsPerRun
            AudioGenerationKind.SFX -> maxSfxCreditsPerRun
            AudioGenerationKind.VOICE, AudioGenerationKind.AMBIENCE -> maxSfxCreditsPerRun
        }
        if (estimatedCredits > kindLimit) return AudioBudgetVerdict.BLOCKED
        val projectedTotal = alreadySpentCreditsThisRun + estimatedCredits
        return when {
            projectedTotal > maxTotalCreditsPerRun -> AudioBudgetVerdict.BLOCKED
            projectedTotal > (maxTotalCreditsPerRun * 0.75).toInt() -> AudioBudgetVerdict.NEEDS_APPROVAL
            else -> AudioBudgetVerdict.ALLOWED
        }
    }
}
