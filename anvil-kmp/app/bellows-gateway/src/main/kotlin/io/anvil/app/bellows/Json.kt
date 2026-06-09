package io.anvil.app.bellows

import kotlinx.serialization.json.Json

/**
 * Geteilte JSON-Konfiguration für Gateway-Server, Provider-Adapter und CLI.
 *
 * - `ignoreUnknownKeys`: OpenAI-Clients senden Felder (`tools`, `top_p`, …), die wir
 *   (noch) nicht auswerten — dürfen den Parse nicht brechen.
 * - `explicitNulls = false`: leere Felder (z.B. Streaming-Deltas) werden weggelassen.
 * - `encodeDefaults`: stabile OpenAI-Form (`index`, `object`, …).
 */
val gatewayJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    isLenient = true
}
