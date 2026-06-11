package io.anvil.modules.bellows

import kotlinx.serialization.Serializable

/**
 * Deklarative Gateway-Konfiguration (JSON). Enthält **niemals** Klartext-Schlüssel —
 * nur Verweise auf den CredentialVault ([ProviderConfig.apiKeyRef]) oder auf
 * Umgebungsvariablen ([ProviderConfig.apiKeyEnv]). Adressiert Risk 5 (kein Key im Klartext).
 */
@Serializable
data class BellowsConfig(
    val providers: List<ProviderConfig> = emptyList(),
    val host: String = "127.0.0.1",
    val port: Int = 8765,
    /** Optionaler Vault-Verweis auf einen Bearer-Token, der den Gateway-Endpoint schützt. */
    val gatewayKeyRef: String? = null,
    /** Alternativ: Umgebungsvariable, die den Gateway-Bearer-Token enthält (kein Vault nötig). */
    val gatewayKeyEnv: String? = null,
)

@Serializable
data class ProviderConfig(
    /** Stabile ID, z.B. "openrouter" oder "local-hermes". */
    val id: String,
    /** Basis-URL inkl. `/v1`, z.B. `https://openrouter.ai/api/v1` oder `http://localhost:1234/v1`. */
    val baseUrl: String,
    /** Verweis auf den Schlüssel im CredentialVault (NICHT der Schlüssel selbst). */
    val apiKeyRef: String? = null,
    /** Alternativ: Name einer Umgebungsvariable, die den Schlüssel enthält. */
    val apiKeyEnv: String? = null,
    /** Modell-IDs, die dieser Provider bedient. Leer ⇒ Passthrough (bedient jedes Modell). */
    val models: List<String> = emptyList(),
    /** true ⇒ lokaler Server (zählt als `isLocal` für PrivacyMode.LOCAL_ONLY). */
    val local: Boolean = false,
    /** Zusätzliche HTTP-Header (z.B. OpenRouter `HTTP-Referer`). */
    val headers: Map<String, String> = emptyMap(),
)
