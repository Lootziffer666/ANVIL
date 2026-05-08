# Nvidia Build Models

**Gate:** A15  
**Status:** Verbindlich  
**API:** Nvidia NIM — https://integrate.api.nvidia.com/v1

## Alle aktuellen Nvidia Build Models (Stand Mai 2026)

### Foundation / Chat
| Model ID | Name | Context | Type |
|----------|------|---------|------|
| meta/llama-3.3-70b-instruct | Llama 3.3 70B | 128K | Chat |
| meta/llama-3.1-405b-instruct | Llama 3.1 405B | 128K | Chat |
| meta/llama-3.1-70b-instruct | Llama 3.1 70B | 128K | Chat |
| meta/llama-3.1-8b-instruct | Llama 3.1 8B | 128K | Chat |
| meta/llama-4-scout-17b-16e-instruct | Llama 4 Scout | 512K | Chat |
| meta/llama-4-maverick-17b-128e-instruct | Llama 4 Maverick | 256K | Chat |
| nvidia/llama-3.1-nemotron-70b-instruct | Nemotron 70B | 128K | Chat |
| nvidia/llama-3.3-nemotron-super-49b-v1 | Nemotron Super 49B | 128K | Chat |
| nvidia/llama-3.1-nemotron-ultra-253b-v1 | Nemotron Ultra 253B | 128K | Chat |
| mistralai/mistral-large-2-instruct | Mistral Large 2 | 128K | Chat |
| mistralai/mixtral-8x22b-instruct-v0.1 | Mixtral 8x22B | 64K | Chat |
| google/gemma-2-27b-it | Gemma 2 27B | 8K | Chat |
| qwen/qwen2.5-72b-instruct | Qwen 2.5 72B | 128K | Chat |
| deepseek-ai/deepseek-r1 | DeepSeek R1 | 64K | Reasoning |

### Code
| Model ID | Name | Context | Type |
|----------|------|---------|------|
| nvidia/llama-3.1-nemotron-nano-8b-v1 | Nemotron Nano 8B | 128K | Code/Chat |
| qwen/qwen2.5-coder-32b-instruct | Qwen 2.5 Coder 32B | 32K | Code |
| meta/codellama-70b | Code Llama 70B | 16K | Code |

### Vision / Multimodal
| Model ID | Name | Type |
|----------|------|------|
| nvidia/vila | VILA | Vision+Language |
| microsoft/phi-3.5-vision-instruct | Phi 3.5 Vision | Vision+Language |
| meta/llama-3.2-90b-vision-instruct | Llama 3.2 90B Vision | Vision+Language |
| meta/llama-3.2-11b-vision-instruct | Llama 3.2 11B Vision | Vision+Language |
| google/deplot | DePlot | Chart→Table |

### Embedding
| Model ID | Name | Dims |
|----------|------|------|
| nvidia/nv-embedqa-e5-v5 | NV-EmbedQA E5 v5 | 1024 |
| nvidia/nv-embed-v2 | NV-Embed v2 | 4096 |
| snowflake/arctic-embed-l-v2.0 | Arctic Embed L v2 | 1024 |
| baai/bge-m3 | BGE-M3 | 1024 |

### Reranking
| Model ID | Name |
|----------|------|
| nvidia/nv-rerankqa-mistral-4b-v3 | NV-RerankQA Mistral 4B |
| nvidia/llama-3.2-nv-rerankqa-1b-v2 | Llama 3.2 NV-RerankQA 1B |

### Image Generation
| Model ID | Name |
|----------|------|
| nvidia/consistory | Consistory |
| stabilityai/stable-diffusion-3-5-large | Stable Diffusion 3.5 Large |
| black-forest-labs/flux-schnell | FLUX Schnell |

### Speech
| Model ID | Name | Type |
|----------|------|------|
| nvidia/parakeet-ctc-0.6b-asr | Parakeet ASR 0.6B | Speech→Text |
| nvidia/fastpitch-hifigan-tts | FastPitch HiFiGAN | Text→Speech |

## Nutzung

```javascript
// Nvidia-Modell auswählen
const model = NVIDIA_MODELS.find(m => m.id === "meta/llama-3.3-70b-instruct");
// Token holen
const key = TokenManager.getKey(tokenId);
// API Call via fetch
```
