# ANVIL-BELLOWS Gateway — Bedienung

**Gate:** B9 — Bellows Gateway (Produktionsreife)
**Status:** Verbindlich / nutzbar
**Plattform-Ziel:** Windows / Desktop (JVM 17+)

> Bellows ist ein lokaler, **OpenAI-kompatibler** LLM-Router — ein Endpoint für viele
> Backends (Cloud **und** lokal). Du startest ihn auf deinem PC und hängst OpenCode
> (oder jedes andere OpenAI-kompatible Tool) daran. Vorbild: LiteLLM / OpenRouter /
> OmniRoute (`docs/OMNIROUTE_BRIDGE.md`).

```
                         ┌──────────────────────────────────────┐
   OpenCode ──────────▶  │  Bellows Gateway   :8765/v1           │
   (http://…:8765/v1)    │  Router + Provider-Adapter            │
                         └───────────────┬───────────┬──────────┘
                                         │           │
                          Cloud (Bearer) │           │ lokal (kein Key)
                                         ▼           ▼
                                  OpenRouter/…   LM Studio / Ollama / llama.cpp
                                                 (z.B. Hermes)
```

---

## 1. Bauen & installieren (Windows)

Voraussetzung: **JDK 17+** (`java -version`).

```bat
cd anvil-kmp
gradlew.bat :app:bellows-gateway:installDist
```

Ergebnis (lauffähig, inkl. aller Libs):

```
anvil-kmp\app\bellows-gateway\build\install\bellows\bin\bellows.bat
```

Tipp: Diesen `bin`-Ordner in den `PATH` aufnehmen, dann genügt überall `bellows`.
(Alternativ einmalig `gradlew.bat :app:bellows-gateway:run --args="serve"`.)

---

## 2. Konfigurieren

```bat
bellows config init
```

Legt `%APPDATA%\anvil-bellows\bellows.config.json` an (Beispiel mit OpenRouter +
lokalem Hermes). Pfad jederzeit: `bellows config path`.

```jsonc
{
  "host": "127.0.0.1",
  "port": 8765,
  "providers": [
    {
      "id": "openrouter",
      "baseUrl": "https://openrouter.ai/api/v1",
      "apiKeyRef": "openrouter",                  // Verweis auf den Vault — KEIN Key hier!
      "models": ["openai/gpt-4o-mini", "anthropic/claude-3.5-sonnet"],
      "local": false,
      "headers": { "HTTP-Referer": "https://anvil.local", "X-Title": "Anvil Bellows" }
    },
    {
      "id": "local-hermes",
      "baseUrl": "http://localhost:1234/v1",      // LM Studio / Ollama / llama.cpp
      "models": ["hermes", "NousResearch/Hermes-3-Llama-3.1-8B"],
      "local": true                               // zählt als „lokal" für LOCAL_ONLY
    }
  ]
}
```

Felder pro Provider:

| Feld | Bedeutung |
|------|-----------|
| `id` | stabile ID, taucht in `modelUsed` auf (`openrouter:…`). |
| `baseUrl` | Basis-URL **inkl. `/v1`**. `/chat/completions` wird angehängt. |
| `apiKeyRef` | Verweis auf einen Schlüssel im **CredentialVault** (empfohlen). |
| `apiKeyEnv` | Alternative: Name einer Umgebungsvariable mit dem Key. |
| `models` | Modelle, die dieser Provider bedient. Leer = Passthrough (bedient alles). |
| `local` | `true` für lokale Server (PrivacyMode `LOCAL_ONLY`). |
| `headers` | zusätzliche HTTP-Header. |

---

## 3. API-Keys sicher ablegen (CredentialVault)

Keys liegen **nie im Klartext** (`docs/SAFETY_POLICY.md` §3) — sondern in einem
verschlüsselten **JCEKS-Keystore** neben der Config. Der Keystore wird mit einem
Master-Passwort geschützt:

```bat
set ANVIL_BELLOWS_VAULT_PASSWORD=dein-master-passwort
bellows key set openrouter
:: Secret wird verdeckt abgefragt und verschlüsselt gespeichert
bellows key list
```

Das `apiKeyRef: "openrouter"` in der Config zeigt auf genau diesen Eintrag.

**Ohne Vault** (einfacher, weniger sicher): `apiKeyEnv` statt `apiKeyRef` setzen und
den Key als Umgebungsvariable bereitstellen.

---

## 4. Starten

```bat
set ANVIL_BELLOWS_VAULT_PASSWORD=dein-master-passwort   :: nur falls apiKeyRef genutzt wird
bellows serve
```

```
------------------------------------------------
 Anvil Bellows Gateway
 Endpoint : http://127.0.0.1:8765/v1
 Health   : http://127.0.0.1:8765/health
 Auth     : offen (nur localhost empfohlen)
 Provider : openrouter, local-hermes (lokal)
------------------------------------------------
```

Optional: `bellows serve --host 0.0.0.0 --port 9000` (im LAN erreichbar — dann
unbedingt einen Gateway-Token setzen, siehe §7).

---

## 5. OpenCode anbinden

OpenCode spricht jeden OpenAI-kompatiblen Endpoint an. In der OpenCode-Konfiguration
einen Provider hinzufügen:

```jsonc
// opencode.json  (Beispiel)
{
  "provider": {
    "bellows": {
      "npm": "@ai-sdk/openai-compatible",
      "options": {
        "baseURL": "http://127.0.0.1:8765/v1",
        "apiKey": "sk-anvil"          // beliebig, außer der Gateway hat einen Token (§7)
      },
      "models": {
        "openai/gpt-4o-mini": {},
        "hermes": {}
      }
    }
  }
}
```

Alternativ über Umgebungsvariablen vieler Tools:
`OPENAI_BASE_URL=http://127.0.0.1:8765/v1` und `OPENAI_API_KEY=sk-anvil`.

Modelle, die der Gateway kennt: `GET http://127.0.0.1:8765/v1/models`.

---

## 6. Hermes lokal betreiben (Beispiele)

Bellows ist der Router — das Modell läuft in einem lokalen OpenAI-Server:

- **LM Studio** → „Local Server" starten (Default `http://localhost:1234/v1`), Hermes laden.
- **Ollama** → `ollama run hermes3`; OpenAI-Endpoint: `http://localhost:11434/v1`.
- **llama.cpp** → `llama-server -m hermes-3.gguf --port 8080`; Endpoint `http://localhost:8080/v1`.

`baseUrl` des `local-hermes`-Providers entsprechend setzen, `local: true` lassen.

Nur lokal routen (keine Cloud, garantiert): Header `X-Anvil-Privacy: local_only`
mitschicken. Ist kein lokaler Provider verfügbar, antwortet der Gateway mit `503`
(`bellows_exhausted`) — **kein** stiller Cloud-Fallback.

---

## 7. Endpoints & Sicherheit

| Methode | Pfad | Zweck |
|---------|------|-------|
| `POST` | `/v1/chat/completions` | Chat (`stream: true|false`) |
| `GET`  | `/v1/models` | bekannte Modelle |
| `GET`  | `/health` | Router- + Adapter-Zustand |

**Header:**
- `Authorization: Bearer <token>` — nötig, wenn ein Gateway-Token gesetzt ist.
- `X-Anvil-Privacy: local_only` — erzwingt lokales Routing (kein Cloud-Fallback).

**Gateway absichern** (empfohlen, sobald nicht nur `127.0.0.1`):

```bat
bellows key set gateway
```
Dann in der Config `"gatewayKeyRef": "gateway"` setzen. Clients müssen den Token als
`Authorization: Bearer …` senden; sonst `401`.

Weiteres:
- Default-Bind ist `127.0.0.1` (nur lokal). `0.0.0.0` nur mit Token.
- Keystore (`bellows.keystore.jceks`) ist verschlüsselt; nicht ins Git committen.

---

## 8. CLI-Referenz

```
bellows serve   [--config <pfad>] [--host <h>] [--port <p>]
bellows config  init | path        [--config <pfad>] [--force]
bellows key     set <ref> | list | rm <ref>   [--config <pfad>]
bellows models  [--config <pfad>]
bellows help
```

Vault-Passwort: Umgebungsvariable `ANVIL_BELLOWS_VAULT_PASSWORD`.

---

## 9. Fehlerbilder

| Symptom | Ursache / Lösung |
|---------|------------------|
| `503 bellows_exhausted` | Kein passender/gesunder Provider. `LOCAL_ONLY` ohne lokalen Server, oder alle Upstreams down. |
| `401 invalid_api_key` | Gateway-Token gesetzt, Client sendet ihn nicht/falsch. |
| Start bricht ab: „Vault …" | `apiKeyRef` genutzt, aber `ANVIL_BELLOWS_VAULT_PASSWORD` fehlt. |
| Provider bleibt `DEGRADED` | Upstream antwortet mit Fehler (Key falsch, Modell unbekannt) → `/health` prüfen, Server-Log lesen. |

---

## 10. Cloudserver / Docker

Für deinen Cloudserver-Plan gilt: ANVIL-BELLOWS bleibt ein eigenes Repo und wird als privater, OpenAI-kompatibler Gateway per Docker betrieben. Dieses ANVIL-Repo dokumentiert nur die Routing- und Sicherheitsgrenzen; der konkrete Container-Build gehört ins Bellows-Repo. Siehe [`BELLOWS_CLOUDSERVER_DOCKER.md`](BELLOWS_CLOUDSERVER_DOCKER.md).

---

## 11. Status & Grenzen (ehrlich)

- ✅ OpenAI-kompatibel: `chat/completions`, `models`, `health`, Bearer-Auth, SSE-Streaming.
- ✅ Cloud + lokal, Privacy `LOCAL_ONLY` hart, Fallback-Kette, verschlüsselter Key-Store.
- ✅ Verifiziert: Unit-/Integrations-Tests + Live-Smoke (echter `serve` gegen Upstream).
- ⚠️ SSE-Streaming ist aktuell **nicht-inkrementell**: der Upstream wird vollständig
  abgerufen und dann als Chunks emittiert (für OpenCode/OpenAI-Clients kompatibel).
  Echtes token-weises Passthrough-Streaming ist ein Folgeschritt.
- ⚠️ Noch kein Cost-Cap-Enforcement / kein Rate-Limit (Felder vorbereitet).
- ⚠️ Android-Build-Targets sind opt-in (`-Panvil.android=true`, SDK nötig); der
  Gateway selbst ist JVM/Desktop.
