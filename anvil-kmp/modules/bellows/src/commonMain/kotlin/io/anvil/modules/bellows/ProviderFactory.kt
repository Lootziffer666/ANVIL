package io.anvil.modules.bellows

import io.ktor.client.HttpClient

/**
 * Baut [ProviderAdapter] aus einer [BellowsConfig].
 *
 * Schlüssel werden über Resolver aufgelöst — die Factory selbst kennt keine
 * Klartext-Schlüssel und persistiert keine. Der Aufrufer löst Vault-Referenzen
 * (suspend) vorab auf und übergibt einfache Lookups.
 *
 * @param client         geteilter Ktor-[HttpClient] mit ContentNegotiation(JSON).
 * @param secretResolver `apiKeyRef` ⇒ Klartext-Schlüssel (z.B. aus dem CredentialVault).
 * @param envResolver    `apiKeyEnv` ⇒ Klartext-Schlüssel aus der Umgebung.
 */
class ProviderFactory(
    private val client: HttpClient,
    private val secretResolver: (ref: String) -> String? = { null },
    private val envResolver: (name: String) -> String? = { null },
) {
    fun build(config: BellowsConfig): List<ProviderAdapter> =
        config.providers.map { p ->
            val key = p.apiKeyRef?.let(secretResolver) ?: p.apiKeyEnv?.let(envResolver)
            OpenAiCompatibleAdapter(
                id = p.id,
                baseUrl = p.baseUrl,
                apiKey = key,
                models = p.models,
                isLocal = p.local,
                client = client,
                extraHeaders = p.headers,
            )
        }
}
