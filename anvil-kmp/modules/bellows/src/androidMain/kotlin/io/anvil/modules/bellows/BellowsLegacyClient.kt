package io.anvil.modules.bellows

fun interface BellowsLegacyClient {
    suspend fun route(prompt: String, modelHint: String?): String
}
