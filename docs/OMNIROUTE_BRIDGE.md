# OmniRoute Gateway Bridge

**Gate:** A20
**Status:** Superseded / Referenz — Bellows bleibt der kanonische ANVIL-Gateway
**Quelle:** [diegosouzapw/OmniRoute](https://github.com/diegosouzapw/OmniRoute) — Starred von Lootziffer666


## Entscheidung 2026-07-11 — Bellows bleibt

OmniRoute wird **nicht** als A20/B20-Implementierung in ANVIL übernommen.
ANVIL behält **Bellows** als kanonischen Gateway- und Modellrouting-Pfad.
OmniRoute bleibt lediglich eine externe Referenz für Gateway-Ideen.

Konsequenzen:

1. Keine neue OmniRoute-Abhängigkeit im aktiven KMP-Build.
2. Keine zweite Provider-Wahrheit neben Bellows.
3. Credentials bleiben im Bellows `CredentialVault`-Pfad.
4. A20 wird als `superseded` geführt, nicht als nächstes Implementierungsziel.
5. Weitere Gateway-Arbeit erfolgt als Bellows-Härtung, nicht als OmniRoute-Bridge.

## Zweck

> Never stop coding. Free AI gateway: one endpoint, 160+ providers.

OmniRoute ist ein einheitlicher AI-Gateway-Proxy:
- **Ein Endpoint** statt 10 Provider-URLs
- **160+ Provider** automatisch verfügbar
- **Smart Fallback** — wenn ein Provider ausfällt, wechselt OmniRoute automatisch
- **Desktop/PWA** — läuft auf Windows und Android

## Problem ohne OmniRoute

```
Android:  10 Provider × Token einrichten = 10× Setup
Windows:  10 Provider × Token einrichten = 10× Setup
Sync:     Token-Metadaten syncen, Keys auf beiden Geräten neu eingeben
```

## Lösung mit OmniRoute

```
OmniRoute läuft auf Heimserver (oder lokal)
  ↕
Android → http://omni:8090/v1  ← ein Endpoint
Windows → http://omni:8090/v1  ← derselbe Endpoint
```

→ Tokens nur einmal in OmniRoute konfigurieren.
→ Alle Geräte nutzen denselben Gateway.

## Installation

```bash
# Docker (empfohlen)
docker run -p 8090:8090 omniproxy/omniroute:latest

# Oder lokal
git clone https://github.com/diegosouzapw/OmniRoute.git
cd OmniRoute
npm install && npm start
```

## Anvil-Integration

In Anvil: einen einzelnen Provider "OmniRoute" konfigurieren:

```javascript
addCustomProvider("omniroute", "OmniRoute Gateway", "http://localhost:8090/v1", "bearer");
```

Das ersetzt alle einzelnen Provider-Konfigurationen —
ein Token, ein Endpoint, alle Modelle.

## Netzwerk-Setup

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Android     │     │  Heimserver  │     │  Windows    │
│  Anvil App   │────▶│  OmniRoute   │◀────│  Anvil.exe  │
│  📱          │     │  + LocalAI   │     │  🖥️         │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                    ┌──────┴──────┐
                    │  Provider   │
                    │  OpenAI     │
                    │  Anthropic  │
                    │  Nvidia     │
                    │  etc.       │
                    └─────────────┘
```

## Combo: OmniRoute + LocalAI

Auf dem Heimserver:
1. **LocalAI** (Port 8080) — lokale Modelle, kein GPU nötig
2. **OmniRoute** (Port 8090) — Gateway zu Cloud-Providern + LocalAI

→ Anvil zeigt auf OmniRoute → OmniRoute routet intelligent:
  - Schnelle Anfragen → Cloud Provider
  - Nacht-Batch-Jobs → LocalAI (lokal, kostenlos)
  - Fallback bei Cloud-Ausfall → LocalAI

## Features

| Feature | Details |
|---------|---------|
| Provider | 160+ (OpenAI, Anthropic, Google, Nvidia, etc.) |
| Fallback | Automatisch bei Provider-Ausfall |
| Kompression | RTK+Caveman — bis ~95% Context-Einsparung |
| Protokolle | MCP, A2A, REST |
| Multimodal | Text, Vision, Audio, Video |
| Desktop | Native App + PWA |
