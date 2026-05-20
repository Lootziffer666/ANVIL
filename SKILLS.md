# SKILLS.md — KI-Agenten-Fähigkeitsprofil für Anvil

> Was KI-Agenten (Claude, Hyperagent, Codex) in diesem Repo beherrschen müssen,
> um sicher und korrekt arbeiten zu können.

---

## Pflicht-Wissen (vor jeder Session)

| Thema | Quelle | Warum |
|-------|--------|-------|
| Kanonische Begriffe | `docs/ANVIL_CONCEPT_CONTRACT.md` | Falsches Naming = falscher Code |
| Modul-Vertrag | `docs/MODULE_CONTRACT.md` | Jedes Modul folgt dem Contract |
| Offene Gates | `GATES.md` | Nur genehmigte Gates implementieren |
| Bekannte Risiken | `docs/KNOWN_DRIFT_RISKS.md` | Vor Scope Creep schützen |
| KMP-Architektur | `docs/EXECUTION_CORE_ARCHITECTURE.md` | Abhängigkeiten korrekt halten |

---

## Kotlin Multiplatform — Pflicht-Skills

### Target-Split

```kotlin
// commonMain — Shared Business Logic
expect class LocalModelBridge {
    suspend fun complete(request: ModelRequest): ModelResponse
}

// androidMain
actual class LocalModelBridge : ... { /* LiteRT */ }

// jvmMain
actual class LocalModelBridge : ... { /* llama.cpp JNI */ }
```

**Regel:** `expect/actual` NUR für `LocalModelBridge`. Alles andere bleibt in `commonMain`.

### Serialization

```kotlin
// RICHTIG: @Serializable auf allen Domain-Typen
@Serializable
data class Workspace(
    val id: WorkspaceId,
    val name: String,
    val rootPath: String,
    // ...
)

// IDs als inline value class
@JvmInline
@Serializable
value class WorkspaceId(val value: String)

// Timestamps: ISO-8601 String, nie Long
val createdAt: String  // "2026-05-20T09:00:00Z"
```

### Coroutines

```kotlin
// suspend-Funktionen für alle async-Operationen in Contracts
interface ModuleSlotContract {
    suspend fun boot(ctx: ModuleContext): BootResult
    suspend fun handle(step: RunStep): StepResult
}

// Kein blockierendes I/O in commonMain
// Okio-Nutzung: immer über suspendCancellableCoroutine oder withContext
```

---

## Gradle — Pflicht-Skills

### Version Catalog

```toml
# libs.versions.toml — immer über Catalog referenzieren
[libraries]
okio = { group = "com.squareup.okio", name = "okio", version.ref = "okio" }

# In build.gradle.kts:
implementation(libs.okio)          # richtig
implementation("com.squareup.okio:okio:3.9.0")  # falsch — nie hardcoden
```

### Modul-Build-Struktur

```kotlin
// Jedes :core:* und :modules:* Modul folgt diesem Schema:
kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        commonMain.dependencies { /* shared deps */ }
        androidMain.dependencies { /* android-only */ }
        val desktopMain by getting { /* jvm-only */ }
    }
}
```

### Abhängigkeits-Richtung (nie verletzen)

```
:core:contracts
    ↑
:core:domain  :core:pipeline  :core:quality
    ↑
:modules:forge:knight  :modules:bellows
    ↑
:surfaces:commander
    ↑
:app:android  :app:desktop
```

---

## Quality Guard — Pflicht-Wissen

| State | Bedeutung | Wann |
|-------|-----------|------|
| `STABLE` | Vollständig funktionsfähig | Alle Checks grün |
| `DEGRADED` | Eingeschränkt — läuft, aber nicht voll | Cloud nicht erreichbar, aber lokal OK |
| `BLOCKED` | Wartet auf Bedingung | Gate nicht erfüllt, Feature-Branch fehlt |
| `FAILED` | Harter Fehler — Eingriff nötig | Kill-Kriterium ausgelöst |

**Kill-Kriterien (immer `FAILED`):**
- Änderung außerhalb `Workspace.rootPath`
- `PrivacyMode.LOCAL_ONLY` + Cloud-Request
- Modul ohne `ModuleSlotContract`
- Commit ohne Gate-Referenz (Prozess-Kill, kein Code-Kill)

---

## Bellows — LLM-Routing

```kotlin
// ModelRequest immer mit explizitem PrivacyMode
val request = ModelRequest(
    capability = ModelCapability.CODE_PATCH,
    messages = listOf(ChatMessage(role = "user", content = "...")),
    privacyMode = PrivacyMode.LOCAL_ONLY,  // oder CLOUD_ALLOWED
    costCapUsd = 0.05f,
    priority = RequestPriority.INTERACTIVE
)

// Fehlerfall: nie null, immer Exception
// BellowsExhaustedException wenn kein Provider verfügbar
```

---

## Knight — Datei-I/O

```kotlin
// Okio als einzige File-I/O-Lösung in commonMain
// Kein java.io.File in commonMain!
val source: Source = FileSystem.SYSTEM.source(path)

// Diffs: Unified Diff Format
// diff-match-patch (Apache 2.0) für Diff-Berechnung + Anzeige
// Kein Tree-sitter im MVP
// Kein Monaco / CodeMirror (JS-Engine, inkompatibel mit Compose)
```

---

## Was KI-Agenten NICHT dürfen

| Verboten | Begründung |
|----------|------------|
| Hilt für DI einführen | Android-only, KMP-inkompatibel |
| iOS-Target hinzufügen | Explizit kein MVP-Scope |
| `app/` oder `src/core/` Code-Dateien anlegen | Historisch, eingefroren |
| Gates ohne Freigabe implementieren | Nur freigegebene Gates |
| Cloud-Fallback bei `LOCAL_ONLY` | Privacy-Kill-Kriterium |
| `@Suppress("all")` oder Warnung unterdrücken | `allWarningsAsErrors = true` |
| Raw String als ID verwenden | Immer `@JvmInline value class` |
