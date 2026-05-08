# Module Roadmap

ANVIL as Android-based development environment with seamless Windows handoff.
Based on analysis of 194 starred repos (2026-05-08).

## Verdict

31 repos are directly integrable as modules. 28 are strong enablers (need adapter work).
The rest is bookmarks/inspiration.

## Module Map

```text
┌─────────────────────────────────────────────────────────┐
│                    ANVIL (Meta-Layer)                    │
│  Principles │ Templates │ Gates │ Knownbugs              │
└───────┬─────────────┬─────────────┬─────────────┬───────┘
        │             │             │             │
┌───────┴─────┐ ┌─────┴───────┐ ┌─────┴───────┐ ┌─────┴───────┐
│  OPENDORK     │ │  CATALON      │ │ CAT.-GUARD   │ │  DEAFPIPER   │
│  (C# Runtime) │ │  (TS/UI)      │ │ (Kotlin/📱)  │ │  (Py/Pipe)   │
├───────────────┤ ├─────────────┤ ├─────────────┤ ├─────────────┤
│ +outlines     │ │ +preact       │ │ +lottie      │ │ +kivy        │
│ +dspy         │ │ +hyperapp     │ │ +openai-andr │ │ +p4a         │
│ +promptfoo    │ │ +VvvebJs      │ │ +swipe-act   │ │ +crawl4ai    │
│ +crawl4ai     │ │ +pagedraw     │ │ +theme-mgr   │ │ +liteparse   │
│ +memvid       │ │ +ui-builder   │ │ +uiautomator │ │ +unsloth     │
│ +agentscope   │ │ +flet-builder │ │ +apk-tools   │ │ +custom-gpt  │
└───────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
        │                                     │
        └────────── QtScrcpy ─────────────────┘
                  (Android ↔ Windows)
```

---

## Phase 0 — Foundation (3 Gates)

The handoff backbone. Without these, ANVIL is just docs.

### Gate MOD-001: Kivy Core Bootstrap

**Module:** [kivy/kivy](https://github.com/kivy/kivy)
**Target:** ANVIL runtime layer
**Why:** Python UI framework that runs identically on Android and Windows. This is the single most important dependency — it makes "seamless handoff" physically possible.

- [ ] Fork or pin kivy release version
- [ ] Minimal ANVIL app scaffold: one screen, one state object, runs on both targets
- [ ] Document build path (desktop via pip, Android via p4a in Gate MOD-002)
- [ ] Acceptance: `python main.py` shows ANVIL shell on desktop
- [ ] Kill: If kivy cannot render a basic list + input on Android without >2s startup

### Gate MOD-002: Python-for-Android Pipeline

**Module:** [kivy/python-for-android](https://github.com/kivy/python-for-android)
**Depends on:** MOD-001
**Target:** DEAFPIPER → Android native
**Why:** Compiles Python + kivy into real APKs. DEAFPIPER pipelines become native Android apps.

- [ ] p4a toolchain working in local dev (or CI)
- [ ] ANVIL scaffold from MOD-001 compiles to APK
- [ ] APK installs and runs on real device or emulator
- [ ] Acceptance: APK under 30 MB, cold start under 3s
- [ ] Kill: If p4a cannot include numpy/scipy (DEAFPIPER needs them)

### Gate MOD-003: QtScrcpy Handoff Bridge

**Module:** [barry-ran/QtScrcpy](https://github.com/barry-ran/QtScrcpy)
**Target:** ANVIL ↔ Windows bridge
**Why:** Screen mirroring + control between Android and Windows, no root. The "seamless" in seamless handoff.

- [ ] Build QtScrcpy for Windows
- [ ] Connect to Android device (USB + WiFi)
- [ ] Verify input latency acceptable for dev work (<100ms)
- [ ] Document handoff workflow: code on Windows → mirror/test on Android → back
- [ ] Acceptance: Full bidirectional control without root
- [ ] Kill: If latency >300ms or requires custom Android ROM

---

## Phase 1 — OPENDORK Intelligence Layer (5 Gates)

OPENDORK is the C# runtime / intelligence backbone. These modules add LLM discipline.

### Gate MOD-004: Outlines Integration

**Module:** [dottxt-ai/outlines](https://github.com/dottxt-ai/outlines)
**Target:** OPENDORK validation layer
**Why:** Structured LLM output — guarantees JSON schemas, regex patterns. No more "hope the model returns valid data."

- [ ] Install outlines in OPENDORK Python sidecar (or bridge to C#)
- [ ] Define 3 core schemas: tool-call, user-intent, structured-response
- [ ] Wire into OPENDORK's existing LLM call path
- [ ] Acceptance: All LLM outputs conform to schema or fail explicitly
- [ ] Kill: If outlines cannot run on-device (Android) — then defer to server-only

### Gate MOD-005: DSPy Pipeline Wiring

**Module:** [stanfordnlp/dspy](https://github.com/stanfordnlp/dspy)
**Target:** OPENDORK declarative chains
**Why:** Replaces prompt-engineering with programmatic LLM pipelines. Modules compose, optimize, and self-evaluate.

- [ ] DSPy environment in OPENDORK
- [ ] Port one existing OPENDORK chain to DSPy module
- [ ] Compare quality + latency vs. raw prompt
- [ ] Acceptance: DSPy chain matches or beats raw prompt on eval set
- [ ] Kill: If DSPy overhead >2x latency for simple chains

### Gate MOD-006: Promptfoo Quality Gates

**Module:** [promptfoo/promptfoo](https://github.com/promptfoo/promptfoo)
**Target:** OPENDORK testing
**Why:** Systematic prompt evaluation. Catches regressions before they ship.

- [ ] promptfoo config for OPENDORK's top 5 prompts
- [ ] Eval dataset: 20+ cases per prompt
- [ ] CI integration: promptfoo runs on every OPENDORK PR
- [ ] Acceptance: Failing eval blocks merge
- [ ] Kill: None — this is pure quality infrastructure

### Gate MOD-007: Crawl4AI Data Module

**Module:** [unclecode/crawl4ai](https://github.com/unclecode/crawl4ai)
**Target:** OPENDORK data ingestion
**Why:** AI-native web crawling. Turns any URL into structured, LLM-ready data.

- [ ] crawl4ai running as OPENDORK data provider
- [ ] Ingest pipeline: URL → markdown → chunked → indexed
- [ ] Rate limiting + respect robots.txt
- [ ] Acceptance: 100 URLs processed without crashes, structured output usable
- [ ] Kill: If crawl4ai requires headless Chrome on Android (too heavy)

### Gate MOD-008: Memvid Long-Term Memory

**Module:** [memvid/memvid](https://github.com/memvid/memvid)
**Target:** OPENDORK memory layer
**Why:** Encodes knowledge as compressed video. Massive context windows without token costs.

- [ ] memvid integration in OPENDORK knowledge store
- [ ] Encode ANVIL docs + project context as memvid archive
- [ ] Retrieval: query → relevant context in <500ms
- [ ] Acceptance: Retrieval quality comparable to RAG, storage 10x smaller
- [ ] Kill: If encoding time >1h for moderate corpus

---

## Phase 2 — CATALON UI Layer (4 Gates)

CATALON is the TS/UI design surface. These modules make building interfaces fast.

### Gate MOD-009: Preact Micro-Frontend

**Module:** [preactjs/preact](https://github.com/preactjs/preact)
**Target:** CATALON UI runtime
**Why:** 3KB React alternative. Full compat, fraction of the weight. Perfect for embedded Android WebViews.

- [ ] Replace or add preact as CATALON's UI runtime
- [ ] Port one existing CATALON component
- [ ] Verify WebView performance on Android
- [ ] Acceptance: Component renders in <100ms on mid-range Android
- [ ] Kill: If preact compat layer breaks CATALON's existing component library

### Gate MOD-010: VvvebJs Visual Builder

**Module:** [givanz/VvvebJs](https://github.com/givanz/VvvebJs)
**Target:** CATALON WYSIWYG
**Why:** Zero-framework drag-and-drop page builder. Users can compose layouts without code.

- [ ] VvvebJs integrated in CATALON workspace
- [ ] Custom component palette (ANVIL-specific blocks)
- [ ] Export: layout → deployable HTML/CSS
- [ ] Acceptance: Non-coder can build a functional screen in <5 min
- [ ] Kill: If VvvebJs output is not clean enough for production

### Gate MOD-011: Flet Visual Builder

**Module:** [raffieeey/Flet-Visual-Builder](https://github.com/raffieeey/Flet-Visual-Builder)
**Target:** ANVIL Python UI path
**Why:** Python-native visual builder. Bridges kivy (runtime) with visual design (CATALON mindset).

- [ ] Flet builder running locally
- [ ] Export: design → kivy-compatible layout
- [ ] Acceptance: Design → working Android screen in one pipeline
- [ ] Kill: If Flet output doesn't map to kivy widgets cleanly

### Gate MOD-012: Pagedraw Design Pipeline

**Module:** [Pagedraw/pagedraw](https://github.com/Pagedraw/pagedraw)
**Target:** CATALON Atelier
**Why:** Design-to-code pipeline. Sketch → working frontend.

- [ ] Pagedraw instance running
- [ ] Custom CATALON template library
- [ ] Export → preact components (Gate MOD-009)
- [ ] Acceptance: Exported code passes lint, renders correctly
- [ ] Kill: If pagedraw is unmaintained and breaks on modern browsers

---

## Phase 3 — CATALON-GUARD Mobile Layer (4 Gates)

CATALON-GUARD is the Kotlin/Android guardian. These modules make the mobile app polished.

### Gate MOD-013: Lottie Animations

**Module:** [airbnb/lottie-android](https://github.com/airbnb/lottie-android)
**Target:** CATALON-GUARD UI
**Why:** After Effects → JSON → native Android animation. Ship micro-animations without frame-by-frame work.

- [ ] Lottie dependency in CATALON-GUARD
- [ ] 3 starter animations: loading, success, transition
- [ ] Performance profiling on low-end device
- [ ] Acceptance: Animations run at 60fps on 2GB RAM device
- [ ] Kill: None — Lottie is battle-tested

### Gate MOD-014: OpenAI Android SDK

**Module:** [sunnat629/openai-android](https://github.com/sunnat629/openai-android)
**Target:** CATALON-GUARD AI bridge
**Why:** Direct OpenAI access from Kotlin. No server round-trip for simple queries.

- [ ] SDK integrated in CATALON-GUARD
- [ ] Secure API key storage (Android Keystore)
- [ ] Streaming responses in UI
- [ ] Acceptance: Chat response starts rendering in <1s
- [ ] Kill: If SDK doesn't support streaming or function calling

### Gate MOD-015: Swipe + Theme UX

**Modules:** [st235/SwipeToActionLayout](https://github.com/st235/SwipeToActionLayout), [imandolatkia/Android-Animated-Theme-Manager](https://github.com/imandolatkia/Android-Animated-Theme-Manager)
**Target:** CATALON-GUARD interactions
**Why:** Native swipe gestures + smooth theme transitions. The "it just feels right" layer.

- [ ] SwipeToActionLayout on list items
- [ ] Animated theme switching (light/dark/custom)
- [ ] Haptic feedback integration
- [ ] Acceptance: Gesture latency <16ms, theme switch <300ms
- [ ] Kill: If either library conflicts with Material 3

### Gate MOD-016: APK Build + Test Pipeline

**Modules:** [DeadWaveWave/demo2apk](https://github.com/DeadWaveWave/demo2apk), [openatx/android-uiautomator-server](https://github.com/openatx/android-uiautomator-server)
**Target:** ANVIL build automation
**Why:** Web demos → APK in one step. Automated UI testing on real device.

- [ ] demo2apk pipeline for CATALON web prototypes
- [ ] uiautomator server on test device
- [ ] Smoke test suite: 10 core flows
- [ ] Acceptance: Build + test cycle under 5 min
- [ ] Kill: If demo2apk output APKs crash on install

---

## Phase 4 — DEAFPIPER Pipeline Layer (3 Gates)

DEAFPIPER is the Python pipeline engine. These modules add AI processing power.

### Gate MOD-017: AgentScope Multi-Agent

**Module:** [agentscope-ai/agentscope](https://github.com/agentscope-ai/agentscope)
**Target:** OPENDORK + DEAFPIPER orchestration
**Why:** Multi-agent framework. Agents communicate, delegate, retry. OPENDORK becomes a team, not a singleton.

- [ ] AgentScope in DEAFPIPER environment
- [ ] Define 3 agent roles: researcher, validator, formatter
- [ ] Wire to OPENDORK's outlines (MOD-004) for structured handoff
- [ ] Acceptance: Multi-agent pipeline produces better output than single agent
- [ ] Kill: If AgentScope overhead makes simple tasks 3x slower

### Gate MOD-018: Unsloth Fine-Tuning

**Module:** [unslothai/unsloth](https://github.com/unslothai/unsloth)
**Target:** OPENDORK custom models
**Why:** 2x faster fine-tuning, 60% less VRAM. Makes custom OPENDORK models practical on consumer hardware.

- [ ] Unsloth environment on training machine
- [ ] Fine-tune small model on OPENDORK domain data
- [ ] Compare: base model vs. fine-tuned on 50 eval cases
- [ ] Acceptance: Fine-tuned model wins on domain tasks
- [ ] Kill: If fine-tuning requires >24GB VRAM even with unsloth

### Gate MOD-019: LiteParse + Custom-GPT Pipeline

**Modules:** [run-llama/liteparse](https://github.com/run-llama/liteparse), [SamurAIGPT/Open-Custom-GPT](https://github.com/SamurAIGPT/Open-Custom-GPT)
**Target:** DEAFPIPER input + deployment
**Why:** Fast document parsing → structured data. Custom GPT configs → deployable agents.

- [ ] LiteParse as DEAFPIPER's document ingestion front-end
- [ ] Parse PDF, DOCX, HTML → unified markdown
- [ ] Open-Custom-GPT for packaging DEAFPIPER agents
- [ ] Acceptance: Any document → structured chunks in <2s
- [ ] Kill: If LiteParse can't handle German text / special characters

---

## Tier 2 — Enabler Index

These 28 repos need adapter work but are worth tracking. Not gated yet — pull into a gate when the Phase dependency is met.

### Android Ecosystem
| Repo | Use Case | Depends On |
|------|----------|------------|
| JStumpp/awesome-android | Library sourcebook | — |
| Best-Flutter-UI-Templates | UI pattern reference | MOD-009 |
| android/architecture-samples | Arch reference | MOD-013 |
| awesome-android-ui | Component library | MOD-015 |
| open-event-droidgen | App generator patterns | MOD-016 |

### AI & Knowledge
| Repo | Use Case | Depends On |
|------|----------|------------|
| awesome-local-llm | On-device model reference | MOD-018 |
| awesome-mcp-servers | Provider interface pattern | MOD-004 |
| open-webui-plugins | Plugin architecture | MOD-017 |
| CubeSandbox | Sandboxed execution | MOD-017 |
| public-apis | API directory for providers | MOD-007 |
| cherry-studio | Multi-model client reference | MOD-014 |
| llm-wiki-agent | Auto-wiki for docs | MOD-007 |
| second-brain | Knowledge management | MOD-008 |
| mempalace | Memory organization | MOD-008 |
| easy-agent | Lightweight agent pattern | MOD-017 |
| deep-searcher | Deep search for artifacts | MOD-007 |

### UI Components
| Repo | Use Case | Depends On |
|------|----------|------------|
| open-props | CSS custom properties | MOD-009 |
| daisyui | Tailwind components | MOD-009 |
| tabler-icons | 5000+ SVG icons (MIT) | MOD-009 |
| heroicons | Tailwind icons | MOD-009 |
| animate.css | CSS animations | MOD-009 |
| open-app-builder | App assembly pattern | MOD-010 |

### Dev & Pipeline
| Repo | Use Case | Depends On |
|------|----------|------------|
| usebruno/bruno | Git-versionable API client | MOD-006 |
| go-gitea/gitea | Self-hosted Git on Android | MOD-002 |
| OpenAPI-Specification | API standard for modules | MOD-006 |
| auto-dev | AI-powered IDE plugin | MOD-005 |

### Doc & Testing
| Repo | Use Case | Depends On |
|------|----------|------------|
| Documentation-Compendium | Template standards | — |
| awesome-test-automation | Testing strategies | MOD-016 |
| awesome-guidelines | Coding standards | — |

---

## Rules

1. Each gate gets its own branch and PR. Naming: `mod-NNN-short-name`.
2. No module enters ANVIL without a working acceptance test.
3. Kill criteria are real — if hit, the module is archived, not forced.
4. Tier 2 repos become gates only when their Phase dependency is complete.
5. Phase 0 is blocking. Do not start Phases 1–4 until MOD-001 + MOD-002 + MOD-003 are merged.
6. Modules stay in their target project repo (OPENDORK, CATALON, etc.). ANVIL tracks the roadmap, not the code.

## Status

| Phase | Gates | Status |
|-------|-------|--------|
| Phase 0 — Foundation | MOD-001 → MOD-003 | ⬜ Not started |
| Phase 1 — OPENDORK | MOD-004 → MOD-008 | ⬜ Blocked by Phase 0 |
| Phase 2 — CATALON | MOD-009 → MOD-012 | ⬜ Blocked by Phase 0 |
| Phase 3 — CATALON-GUARD | MOD-013 → MOD-016 | ⬜ Blocked by Phase 0 |
| Phase 4 — DEAFPIPER | MOD-017 → MOD-019 | ⬜ Blocked by Phase 0 |
