# LocalAI Integration

**Gate:** A16+ (Bonus)  
**Status:** Referenz  
**Quelle:** [mudler/LocalAI](https://github.com/mudler/LocalAI) — Starred von Lootziffer666

## Zweck

> Run any model - LLMs, vision, voice, image, video - on any hardware. No GPU required.

LocalAI ist ein OpenAI-kompatibler API-Server, der jedes Modell
auf *jeder* Hardware laufen lässt — auch ohne GPU, nur langsamer.
Perfekt für Nachtläufe auf schwacher Hardware.

## Features

- **OpenAI-kompatible API** — Drop-in Replacement
- **Kein GPU nötig** — CPU-only Mode, nutzt alle Cores
- **GGUF / GGML Support** — Quantisierte Modelle laufen sofort
- **Multimodal** — LLMs, Vision, TTS, STT, Image Generation, Embeddings
- **Container-ready** — Docker, Kubernetes
- **P2P Mode** — Mehrere schwache Rechner = ein starker

## Installation

```bash
# Docker (empfohlen)
docker run -p 8080:8080 --name localai localai/localai:latest

# Oder nativ
curl https://localai.io/install.sh | sh
localai run
```

## Nutzung in Anvil

LocalAI spricht OpenAI-API. In Anvil:
1. Provider: `localai` wählen
2. API Base: `http://localhost:8080` (oder Netzwerk-IP)
3. Kein Token nötig
4. Modelle über Gallery laden: `localai models list`

## Nacht-Modus Workflow

1. Abends: `localai run --model llama-3.1-8b` starten
2. Anvil-Jobs queuen (Prompt Packs, Batch-Inference)
3. Morgens: Ergebnisse in `outputs/` prüfen

## Relevante Modelle für schwache Hardware

| Modell | RAM | Qualität | Speed (CPU) |
|--------|-----|----------|-------------|
| Llama 3.1 8B Q4 | 6 GB | Gut | ~5 tok/s |
| Phi 3.5 Mini Q4 | 3 GB | Okay | ~10 tok/s |
| Gemma 2 2B Q8 | 3 GB | Basis | ~15 tok/s |
| Qwen 2.5 3B Q4 | 3 GB | Gut | ~8 tok/s |
| Mistral 7B Q4 | 5 GB | Gut | ~5 tok/s |

## P2P / Swarm Mode

```bash
# Rechner A (Head)
localai --p2p

# Rechner B (Worker)
localai --p2p-token <TOKEN_FROM_A>
```

→ Modell wird über beide Rechner verteilt.
