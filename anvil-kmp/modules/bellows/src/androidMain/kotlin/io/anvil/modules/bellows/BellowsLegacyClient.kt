package io.anvil.modules.bellows

// Minimal contract the standalone ANVIL-BELLOWS project must satisfy.
// Implemented / wrapped in :app:android — never here.
fun interface BellowsLegacyClient {
    suspend fun complete(prompt: String, modelHint: String?): String
}
