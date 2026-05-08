# HuggingFace Launcher

**Gate:** A16  
**Status:** Verbindlich

## Zweck

Anvil kann Modelle von HuggingFace direkt starten — über die
Inference API oder als Referenz für lokale Nutzung (Ollama, llama.cpp).

## Funktionen

### 1. Model-Suche
- Nach Name, Task, Downloads suchen
- Top-Modelle pro Kategorie anzeigen

### 2. Quick-Launch
- Inference API: direkter Chat/Completion über HF Token
- Kein Download nötig für API-Modelle

### 3. Model Card
- Beschreibung, Lizenz, Downloads, Tags
- Pipeline-Tag (text-generation, fill-mask, etc.)

### 4. Lokaler Export (Referenz)
- Ollama-Befehl generieren
- llama.cpp Kommando generieren
- GGUF-Link bereitstellen (wenn verfügbar)

## HuggingFace API

- Inference API: `https://api-inference.huggingface.co/models/{model_id}`
- Model Info: `https://huggingface.co/api/models/{model_id}`
- Search: `https://huggingface.co/api/models?search={query}&sort=downloads`

## Top-Modelle (vorkonfiguriert)

| Model | Task | Downloads |
|-------|------|-----------|
| meta-llama/Llama-3.3-70B-Instruct | text-generation | 10M+ |
| mistralai/Mistral-Large-Instruct-2411 | text-generation | 1M+ |
| Qwen/Qwen2.5-72B-Instruct | text-generation | 5M+ |
| google/gemma-2-27b-it | text-generation | 2M+ |
| deepseek-ai/DeepSeek-R1 | text-generation | 5M+ |
| sentence-transformers/all-MiniLM-L6-v2 | sentence-similarity | 50M+ |
| openai/whisper-large-v3 | automatic-speech-recognition | 10M+ |
| stabilityai/stable-diffusion-xl-base-1.0 | text-to-image | 15M+ |
| facebook/bart-large-mnli | zero-shot-classification | 10M+ |
| microsoft/phi-3.5-mini-instruct | text-generation | 3M+ |

## Regeln

1. Token wird über Token Manager verwaltet (Gate A13)
2. Kein Download in Anvil selbst — nur API oder Referenz
3. Model Card zeigt Lizenz — Nutzer muss prüfen
4. Rate Limits beachten (HF Free: 1000 req/Tag)
