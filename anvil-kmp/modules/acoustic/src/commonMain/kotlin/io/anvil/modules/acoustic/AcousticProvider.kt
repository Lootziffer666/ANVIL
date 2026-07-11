package io.anvil.modules.acoustic

import io.anvil.core.contracts.PrivacyMode
import kotlinx.serialization.Serializable

@Serializable
data class AcousticCapabilities(
    val provider: AudioProviderId,
    val isLocal: Boolean,
    val supportedKinds: List<AudioGenerationKind>,
    val commercialUseAllowed: Boolean,
)

@Serializable
sealed interface AudioGenerationResult {
    @Serializable
    data class Generated(val asset: AudioAsset, val provenance: AudioGenerationProvenance, val cost: AudioGenerationCost) : AudioGenerationResult

    @Serializable
    data class Blocked(val reason: String) : AudioGenerationResult

    @Serializable
    data class Failed(val reason: String) : AudioGenerationResult
}

/**
 * A source of generated or licensed audio material. A provider produces [AudioAsset]s and
 * their provenance/cost — it never touches gameplay or world-state truth (that stays with
 * [AcousticRuntimeModule]).
 *
 * Rules (Gate F-02): `LOCAL_ONLY` blocks remote providers before any network call; budget is
 * checked before network calls; there is no automatic paid retry.
 */
interface AcousticProvider {
    val id: AudioProviderId

    suspend fun capabilities(): AcousticCapabilities
    suspend fun estimate(request: AudioGenerationRequest): AudioGenerationCost
    suspend fun generate(request: AudioGenerationRequest, privacyMode: PrivacyMode): AudioGenerationResult
}
