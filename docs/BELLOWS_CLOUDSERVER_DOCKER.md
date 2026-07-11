# ANVIL-BELLOWS Cloudserver Docker Routing

**Gate:** B9 — Bellows Gateway
**Status:** Deployment-Blueprint / externes Repo
**Entscheidung:** ANVIL-BELLOWS bleibt ein eigenes Repo; ANVIL nutzt es als kanonischen Gateway.

## Zielbild

Du betreibst **Anvil-Bellows** auf einem Cloudserver als privaten,
OpenAI-kompatiblen Routing-Endpunkt. Jedes Gerät, das sich am Server anmelden
darf, kann denselben `/v1`-Endpoint nutzen und muss keine Provider-Keys lokal
halten.

```
Gerät erlaubt? ──▶ HTTPS + Bearer/VPN/Auth ──▶ Anvil-Bellows :8765/v1
 Android                                         │
 Windows                                         ├── OpenRouter / OpenAI / ...
 Laptop                                          └── lokale/remote Modellserver
```

## Harte Grenzen

1. **Kein OmniRoute.** Bellows bleibt die Gateway-Wahrheit.
2. **Keine Secrets auf Clients.** Geräte kennen nur den Bellows-Gateway-Token.
3. **Provider-Keys bleiben serverseitig.** `apiKeyRef` oder `apiKeyEnv`, niemals
   Klartext in Git oder Client-Configs.
4. **Nicht offen ins Internet ohne Auth.** Öffentlich erreichbar nur mit TLS und
   Gateway-Token; besser zusätzlich VPN oder Reverse-Proxy-Allowlist.
5. **LOCAL_ONLY bleibt hart.** Wenn ein Client `X-Anvil-Privacy: local_only`
   sendet, darf Bellows nicht auf Cloud-Provider ausweichen.

## Empfohlene Server-Topologie

| Schicht | Aufgabe |
|---------|---------|
| Reverse Proxy | TLS, Domain, optional IP-Allowlist / Basic Auth / mTLS |
| Bellows Container | OpenAI-kompatibler `/v1`-Gateway, Routing, Provider-Fallback |
| Secret Storage | Docker Secrets, `.env` außerhalb Git oder Bellows CredentialVault |
| Provider | OpenRouter/OpenAI/etc. oder eigene OpenAI-kompatible lokale Server |

## Docker-Compose Blueprint

> Platzhalter: Das Image kommt aus dem separaten **Anvil-Bellows** Repo/Registry.
> In diesem ANVIL-Repo wird absichtlich kein Bellows-Dockerfile gepflegt.

```yaml
services:
  bellows:
    image: ${BELLOWS_IMAGE:-ghcr.io/iig/anvil-bellows:latest}
    container_name: anvil-bellows
    restart: unless-stopped
    environment:
      ANVIL_BELLOWS_VAULT_PASSWORD_FILE: /run/secrets/bellows_vault_password
      BELLOWS_GATEWAY_TOKEN_FILE: /run/secrets/bellows_gateway_token
      OPENROUTER_API_KEY_FILE: /run/secrets/openrouter_api_key
    volumes:
      - bellows-data:/data
      - ./bellows.config.json:/config/bellows.config.json:ro
    secrets:
      - bellows_vault_password
      - bellows_gateway_token
      - openrouter_api_key
    command: ["serve", "--config", "/config/bellows.config.json", "--host", "0.0.0.0", "--port", "8765"]
    networks:
      - bellows-private

  caddy:
    image: caddy:2
    container_name: anvil-bellows-proxy
    restart: unless-stopped
    ports:
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy-data:/data
      - caddy-config:/config
    networks:
      - bellows-private

secrets:
  bellows_vault_password:
    file: ./secrets/bellows_vault_password.txt
  bellows_gateway_token:
    file: ./secrets/bellows_gateway_token.txt
  openrouter_api_key:
    file: ./secrets/openrouter_api_key.txt

volumes:
  bellows-data:
  caddy-data:
  caddy-config:

networks:
  bellows-private:
```

## Beispiel `bellows.config.json`

```jsonc
{
  "host": "0.0.0.0",
  "port": 8765,
  "gatewayKeyEnv": "BELLOWS_GATEWAY_TOKEN",
  "providers": [
    {
      "id": "openrouter",
      "baseUrl": "https://openrouter.ai/api/v1",
      "apiKeyEnv": "OPENROUTER_API_KEY",
      "models": ["openai/gpt-4o-mini", "anthropic/claude-3.5-sonnet"],
      "local": false,
      "headers": {
        "HTTP-Referer": "https://bellows.example.com",
        "X-Title": "Anvil Bellows"
      }
    }
  ]
}
```

Wenn das Bellows-Image nur normale Env-Variablen und keine `*_FILE`-Varianten
unterstützt, muss der Container-Entrypoint im separaten Anvil-Bellows Repo die
Secrets aus `/run/secrets/*` in Env-Variablen übersetzen. Diese Übersetzung gehört
nicht in das ANVIL-Repo.

## Beispiel `Caddyfile`

```caddyfile
bellows.example.com {
  encode zstd gzip

  reverse_proxy bellows:8765

  header {
    Strict-Transport-Security "max-age=31536000; includeSubDomains; preload"
    X-Content-Type-Options "nosniff"
    Referrer-Policy "no-referrer"
  }
}
```

## Client-Konfiguration

Jedes erlaubte Gerät nutzt denselben OpenAI-kompatiblen Endpoint:

```bash
export OPENAI_BASE_URL="https://bellows.example.com/v1"
export OPENAI_API_KEY="<bellows-gateway-token>"
```

Für lokale-only Requests:

```bash
curl https://bellows.example.com/v1/chat/completions \
  -H "Authorization: Bearer <bellows-gateway-token>" \
  -H "Content-Type: application/json" \
  -H "X-Anvil-Privacy: local_only" \
  -d '{"model":"hermes","messages":[{"role":"user","content":"ping"}]}'
```

## Betriebskriterien

- `/health` ist von erlaubten Geräten erreichbar.
- `/v1/models` zeigt nur die bewusst konfigurierten Modelle.
- Falscher oder fehlender Bearer-Token ergibt `401`.
- `X-Anvil-Privacy: local_only` führt ohne lokalen Provider zu `503`, nicht zu
  Cloud-Fallback.
- Secrets liegen in Docker Secrets, Vault oder serverseitiger `.env`, nie in Git.
