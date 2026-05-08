# Provider Registry

**Gate:** A14  
**Status:** Verbindlich

## Zweck

Anvil unterstützt mehrere KI-Provider. Jeder Provider hat:
- Einen eindeutigen ID
- API-Endpunkt(e)
- Verfügbare Modelle
- Token-Anforderungen
- Status

## Mitgelieferte Provider

| Provider | ID | API Base | Auth |
|----------|----|----------|------|
| Nvidia NIM | `nvidia` | `https://integrate.api.nvidia.com/v1` | Bearer Token |
| HuggingFace | `huggingface` | `https://api-inference.huggingface.co` | Bearer Token |
| OpenAI | `openai` | `https://api.openai.com/v1` | Bearer Token |
| Anthropic | `anthropic` | `https://api.anthropic.com/v1` | x-api-key Header |
| Google AI | `google` | `https://generativelanguage.googleapis.com/v1beta` | API Key param |
| Groq | `groq` | `https://api.groq.com/openai/v1` | Bearer Token |
| Mistral | `mistral` | `https://api.mistral.ai/v1` | Bearer Token |
| Together AI | `together` | `https://api.together.xyz/v1` | Bearer Token |
| OmniRoute Gateway | `omniroute` | `http://localhost:8090/v1` | Bearer |
| LocalAI (Any HW) | `localai` | `http://localhost:8080` | Keine |
| Ollama (Local) | `ollama` | `http://localhost:11434` | Keine |

## Custom Provider hinzufügen

```json
{
  "id": "my-provider",
  "name": "Mein Provider",
  "apiBase": "https://api.example.com/v1",
  "authType": "bearer",
  "models": [],
  "status": "untested"
}
```

## Regeln

1. Jeder Provider braucht mindestens: id, name, apiBase, authType
2. Ohne Token: Status = `needs-token`
3. Custom Provider werden in localStorage gespeichert
4. Mitgelieferte Provider können nicht gelöscht werden
