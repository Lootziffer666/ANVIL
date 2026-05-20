package io.anvil.core.contracts

import kotlinx.serialization.Serializable

interface BellowsContract {
    suspend fun route(request: ModelRequest): ModelResponse
}

@Serializable
data class ModelRequest(
    val prompt: String,
    val privacyMode: PrivacyMode = PrivacyMode.OPEN,
    val modelHint: String? = null,
)

@Serializable
data class ModelResponse(
    val content: String,
    val modelUsed: String,
)

enum class PrivacyMode {
    LOCAL_ONLY,
    OPEN,
}

class BellowsExhaustedException(msg: String) : Exception(msg)
