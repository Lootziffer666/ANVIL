# Bellows — LLM-Routing (KMP)

> Gate B9 — Produktionsreife. Kanonischer Name laut `ANVIL_CONCEPT_CONTRACT.md`.
> Verboten: ~~Provider Hub~~, ~~AI Layer~~.

Bellows ist der LLM-Routing-Layer von Anvil: **ein** OpenAI-kompatibler Zugang über
viele Backends — Cloud (OpenAI, OpenRouter, Nvidia, Groq, …) **und** lokal
(LM Studio, Ollama, llama.cpp-Server, vLLM — z.B. für *Hermes*).

Vorbild: LiteLLM / OpenRouter / OmniRoute (`docs/OMNIROUTE_BRIDGE.md`).

## Bausteine (`commonMain`, KMP — JVM & Android)

| Typ | Aufgabe |
|-----|---------|
| `BellowsRouter` | Implementiert `BellowsContract`. Privacy-Filter → Modell-Match → Health → Fallback-Kette. |
| `ProviderAdapter` | Interface für einen Upstream. `OpenAiCompatibleAdapter` deckt jeden OpenAI-kompatiblen Endpoint ab. |
| `OpenAiCompatibleAdapter` | Ktor-Client-Adapter: POST `{baseUrl}/chat/completions`, Bearer-Auth, OpenAI-Parsing. |
| `BellowsConfig` / `ProviderConfig` | Deklarative Konfiguration (JSON). Enthält **nie** Klartext-Keys, nur Vault-/Env-Verweise. |
| `ProviderFactory` | Baut Adapter aus Config + aufgelösten Secrets + geteiltem `HttpClient`. |
| `wire/OpenAiDto` | Kanonische OpenAI-Wire-Typen (Request/Response/Chunks/Models/Error). |

## Routing-Regeln

1. **Privacy** — `PrivacyMode.LOCAL_ONLY` filtert auf `isLocal`-Adapter; **niemals** Cloud-Fallback
   (Kill-Kriterium, `docs/SAFETY_POLICY.md` §4).
2. **Modell-Match** — Adapter, die `model` bedienen, zuerst; übrige als Fallback.
3. **Health** — `QualityState.FAILED`-Adapter werden übersprungen.
4. **Fallback-Kette** — der erste erfolgreiche Adapter gewinnt; sonst `BellowsExhaustedException`.

## Lauffähiger Gateway

Der lauffähige Server + CLI liegt in `:app:bellows-gateway` (JVM/Desktop).
Anleitung: [`docs/BELLOWS_GATEWAY.md`](../../../docs/BELLOWS_GATEWAY.md).

```bash
# aus anvil-kmp/
./gradlew :app:bellows-gateway:installDist
app/bellows-gateway/build/install/bellows/bin/bellows config init
app/bellows-gateway/build/install/bellows/bin/bellows serve
# → OpenAI-kompatibel unter http://127.0.0.1:8765/v1
```

## Tests

```bash
./gradlew :modules:bellows:jvmTest          # Router, Privacy, Fallback, Adapter (Mock-Engine)
./gradlew :app:bellows-gateway:test         # HTTP-Contract, SSE, Auth, CredentialVault
```

## Anvil-Bellows als Upstream-Provider (Komposition statt Duplikat)

Es gibt zwei "Bellows": diesen Kotlin-nativen Router (`:modules:bellows` +
`:app:bellows-gateway`, Port 8765) und das eigenständige `Anvil-Bellows`-Repo
(Python/LiteLLM, Port 4000, `Anvil-Bellows/README.md`). Siehe
`docs/ANVIL_CONCEPT_CONTRACT.md` ("Anvil × Anvil-Bellows") — das sind bewusst getrennte
Prozesse, keine Konkurrenz: Der standalone Proxy hält die echten Cloud-Credentials
(Vertex AI, OpenRouter) inkl. Budget-Schutz und ist über HTTP von jeder Sprache aus
erreichbar; dieser Kotlin-Router ist die In-Process-Schnittstelle für Anvils eigene
Module/Orchestrierung (`RunSurface`).

Statt Cloud-Credentials ein zweites Mal hier zu verwalten, kann dieser Router den
standalone Proxy einfach als **einen weiteren `ProviderAdapter`** behandeln — er ist
bereits ein OpenAI-kompatibler Endpoint, `OpenAiCompatibleAdapter` deckt ihn ohne
Zusatzcode ab:

```json
{
  "providers": [
    {
      "id": "anvil-bellows",
      "baseUrl": "http://<bellows-host>:4000/v1",
      "apiKeyEnv": "ANVIL_BELLOWS_MASTER_KEY",
      "models": ["gpt-4o", "gpt-4o-mini", "refinery", "refinery-deepseek", "qwen-coder"],
      "local": false
    }
  ]
}
```

(`apiKeyEnv` statt `apiKeyRef`, wenn der Master-Key schon als Umgebungsvariable vorliegt
— kein Vault-Eintrag nötig. Modellnamen aus `Anvil-Bellows/config.yaml`s `model_list`
übernehmen.) Verifiziert per Mock-Engine gegen die reale Anvil-Bellows-Antwortform in
[`ProviderFactoryTest`](src/commonTest/kotlin/io/anvil/modules/bellows/ProviderFactoryTest.kt).
