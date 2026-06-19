# Headroom Integration Concept

**Gate:** AX2  
**Stand:** 2026-06-10  
**Quelle:** chopratejas/headroom (37k Stars)  
**Ziel-Bereich:** Bellows (BellowsRouter Pipeline), neues Modul `bellows-headroom`

---

## Zusammenfassung

headroom ist ein Token-Compression-Layer fuer AI-Agents: 60-95% weniger Tokens bei gleicher Antwortqualitaet. Es bietet 6 Algorithmen, einen CacheAligner fuer Provider-KV-Cache-Hits und CCR (Compressed Context Retrieval) fuer reversible Kompression.

Dieses Dokument beschreibt, wie Token-Compression in ANVILs Bellows-Pipeline integriert werden koennte, ohne externe Dependencies einzufuehren.

---

## Was Headroom bietet

| Feature | Beschreibung | ANVIL-Relevanz |
|---------|--------------|----------------|
| SmartCrusher | JSON-Compression (Whitespace, Keys) | Hoch (Tool-Calls, Structured Output) |
| CodeCompressor | AST-basierte Code-Compression | Hoch (IDE/Werkbank = Code-Kontext) |
| Kompress-base | Generische Text-Compression | Mittel (Prose-Kontext) |
| CacheAligner | Prefix-Alignment fuer Provider-KV-Cache | Hoch (Kosten-Reduktion) |
| CCR | Compressed Context Retrieval (reversibel) | Hoch (Lange Runs) |
| Cross-Agent Memory | Auto-Dedup ueber Sessions | Mittel (MemoryEntry-Dedup) |
| headroom learn | Korrektur-Mining aus Failed Sessions | Niedrig (ANVIL hat QualityGuard) |

---

## Wo Compression in die Bellows-Pipeline passt

Aktuelle Pipeline (Gate B9):

```
ModelRequest
  -> BellowsRouter.route(request)
    -> Privacy-Check (LOCAL_ONLY?)
    -> Provider-Match (Modell-Capabilities)
    -> Health-Check
    -> ProviderAdapter.complete(request)
  -> ModelResponse
```

**Vorgeschlagene Pipeline mit Compression:**

```
ModelRequest
  -> CompressionStep.compress(request)     <-- NEU
    -> JSON-Crush (structured fields)
    -> Code-Compress (code blocks in messages)
    -> CacheAlign (prefix optimization)
  -> CompressedModelRequest
  -> BellowsRouter.route(compressedRequest)
    -> Privacy-Check
    -> Provider-Match
    -> Health-Check
    -> ProviderAdapter.complete(compressedRequest)
  -> ModelResponse
  -> CompressionStep.decompress(response)  <-- NEU (nur bei CCR)
  -> ModelResponse (original quality)
```

---

## Integration mit BellowsContract

Aus `:core:contracts` (B2):

```kotlin
interface BellowsContract : ModuleSlotContract {
    suspend fun complete(request: ModelRequest): ModelResponse
}
```

### Vorschlag: CompressionContract

```kotlin
interface CompressionContract {
    suspend fun compress(request: ModelRequest): CompressedRequest
    suspend fun decompress(response: ModelResponse, context: CompressionContext): ModelResponse
    fun estimateSavings(request: ModelRequest): TokenSavingsEstimate
}

data class CompressedRequest(
    val original: ModelRequest,
    val compressed: ModelRequest,
    val context: CompressionContext,
    val savings: TokenSavingsEstimate
)

data class CompressionContext(
    val algorithm: CompressionAlgorithm,
    val reversible: Boolean,
    val metadata: Map<String, String>
)

data class TokenSavingsEstimate(
    val originalTokens: Int,
    val compressedTokens: Int,
    val ratio: Double  // 0.0 - 1.0, lower = better compression
)

enum class CompressionAlgorithm {
    JSON_CRUSH,       // SmartCrusher-equivalent
    CODE_COMPRESS,    // AST-based (CodeCompressor-equivalent)
    TEXT_COMPRESS,    // Kompress-base-equivalent
    CACHE_ALIGN,     // CacheAligner-equivalent
    CCR,             // Compressed Context Retrieval
    COMPOSITE        // Mehrere kombiniert
}
```

---

## Modul-Vorschlag: bellows-headroom

```
anvil-kmp/modules/bellows-headroom/
  build.gradle.kts
  module.json
  src/commonMain/kotlin/dev/anvil/bellows/headroom/
    CompressionContract.kt
    HeadroomCompressor.kt       -- Implementierung
    algorithms/
      JsonCrusher.kt            -- JSON Whitespace + Key Shortening
      CodeCompressor.kt         -- AST-basierte Code-Kompression
      CacheAligner.kt           -- Prefix-Optimization
      TextCompressor.kt         -- Generische Text-Kompression
    ccr/
      CompressedContextStore.kt -- CCR State Management
      ContextRetriever.kt       -- Reversible Decompression
  src/commonTest/kotlin/
    JsonCrusherTest.kt
    CodeCompressorTest.kt
    CacheAlignerTest.kt
```

### module.json

```json
{
  "name": "bellows-headroom",
  "version": "0.1.0",
  "slot": "compression",
  "requires": ["BellowsContract", "ModuleSlotContract"],
  "provides": ["CompressionContract"],
  "privacy": "LOCAL_ONLY",
  "description": "Token-Compression-Layer fuer Bellows LLM-Pipeline"
}
```

---

## Algorithmen im Detail

### 1. JsonCrusher (SmartCrusher-Equivalent)

Fuer Tool-Calls und Structured Output in ModelRequest:

```kotlin
class JsonCrusher : CompressionAlgorithm {
    fun crush(json: String): String {
        // 1. Whitespace entfernen
        // 2. Wiederholende Keys kuerzen (Lookup-Table)
        // 3. Null-Values droppen
        // 4. Boolean -> 0/1
        // Typische Savings: 30-50% bei Tool-Schemas
    }
}
```

**ANVIL-Relevanz:** ModelRequest enthaelt oft Tool-Definitionen (JSON-Schema). Diese sind hochgradig redundant.

### 2. CodeCompressor (AST-basiert)

Fuer Code-Bloecke in Chat-Messages:

```kotlin
class CodeCompressor : CompressionAlgorithm {
    fun compress(code: String, language: String): String {
        // 1. Comments entfernen (ausser Doc-Comments)
        // 2. Import-Statements zu Referenz kuerzen
        // 3. Whitespace normalisieren
        // 4. Identifier kuerzen (mit Lookup-Table)
        // Typische Savings: 40-70% bei Kotlin/Java Code
    }
}
```

**ANVIL-Relevanz:** ANVIL ist eine IDE/Werkbank. Der haeufigste Kontext-Typ ist Code. AST-Compression spart massiv Tokens.

### 3. CacheAligner

Optimiert Message-Prefixes fuer Provider-KV-Cache:

```kotlin
class CacheAligner : CompressionAlgorithm {
    fun align(messages: List<ChatMessage>, provider: String): List<ChatMessage> {
        // 1. System-Prompt immer als erstes (Cache-Hit wahrscheinlich)
        // 2. Stabile Prefixes nicht veraendern (Cache-Key bleibt gleich)
        // 3. Nur Suffix (neue Messages) komprimieren
        // Typische Savings: 20-40% bei Chat-Continuation
    }
}
```

**ANVIL-Relevanz:** BellowsRouter (B9) unterstuetzt bereits Chat-Continuation. CacheAligner optimiert die Token-Kosten dafuer.

### 4. CCR (Compressed Context Retrieval)

Reversible Kompression fuer lange Runs:

```kotlin
class CompressedContextStore {
    private val store: MutableMap<String, CompressedEntry> = mutableMapOf()
    
    fun store(runId: String, context: String): CompressedReference {
        // Komprimiert und speichert Kontext
        // Gibt eine kurze Referenz zurueck (z.B. "[CCR:abc123]")
    }
    
    fun retrieve(reference: CompressedReference): String {
        // Stellt originalen Kontext wieder her
    }
}
```

**ANVIL-Relevanz:** Lange Runs (PLAN-PATCH-DIFF-GATE-ARTIFACT) akkumulieren Kontext. CCR erlaubt es, frueheren Kontext komprimiert zu halten und bei Bedarf wiederherzustellen.

---

## Integration mit BellowsRouter (B9)

Der BellowsRouter bekommt einen optionalen CompressionStep:

```kotlin
class BellowsRouter(
    private val adapters: List<ProviderAdapter>,
    private val compressor: CompressionContract? = null  // Optional
) : BellowsContract {
    
    override suspend fun complete(request: ModelRequest): ModelResponse {
        val effectiveRequest = if (compressor != null) {
            val compressed = compressor.compress(request)
            compressed.compressed
        } else {
            request
        }
        
        // ... bestehende Routing-Logik ...
        val response = selectedAdapter.complete(effectiveRequest)
        
        return if (compressor != null && compressionContext.reversible) {
            compressor.decompress(response, compressionContext)
        } else {
            response
        }
    }
}
```

---

## Privacy-Implikationen

| Aspekt | Bewertung |
|--------|-----------|
| Compression lokal | Ja, alle Algorithmen laufen lokal (kein Cloud-Call) |
| Komprimierte Daten an Provider | Ja, aber weniger Daten = weniger Exposure |
| CCR-Store | Lokal im Workspace, keine externe Persistierung |
| Privacy-Mode-Kompatibilitaet | `LOCAL_ONLY` uneingeschraenkt kompatibel |

**Fazit:** Compression verbessert die Privacy-Situation, weil weniger Token an Provider gesendet werden.

---

## Metriken und QualityGuard

```kotlin
data class CompressionQuality(
    val inputTokens: Int,
    val outputTokens: Int,
    val ratio: Double,
    val lossiness: Lossiness,  // LOSSLESS, REVERSIBLE, LOSSY
    val qualityImpact: QualityState  // STABLE, DEGRADED
)

enum class Lossiness {
    LOSSLESS,    // JsonCrusher: keine Information verloren
    REVERSIBLE,  // CCR: Information wiederherstellbar
    LOSSY        // CodeCompressor (Comments): Information reduziert
}
```

QualityGuard-Integration:
- `ratio > 0.95` (weniger als 5% Savings) -> Skip Compression (Overhead nicht wert)
- `lossiness == LOSSY && qualityImpact == DEGRADED` -> Warnung im RunLog
- CacheAligner: Immer LOSSLESS (nur Reihenfolge-Optimierung)

---

## Abhaengigkeiten und Voraussetzungen

| Voraussetzung | Status | Gate |
|---------------|--------|------|
| BellowsContract | Done | B2 |
| ModelRequest/ModelResponse | Done | B9 |
| BellowsRouter (funktional) | Done | B9 |
| ModuleSlotContract | Done | B2 |
| RunStep-Pipeline (fuer CCR in Runs) | Done | B4 |

**Alle Voraussetzungen sind erfuellt.** Ein bellows-headroom Modul koennte sofort implementiert werden.

---

## Risiken

| Risiko | Schwere | Mitigation |
|--------|---------|------------|
| Compression verschlechtert LLM-Output | Mittel | QualityGuard misst Response-Quality, Fallback auf unkomprimiert |
| AST-Parsing-Fehler bei Code | Niedrig | Fallback auf Text-Compression bei Parse-Error |
| CCR-Store waechst unbegrenzt | Niedrig | TTL + LRU-Eviction, gebunden an Run-Lifecycle |
| Latenz durch Compression-Step | Niedrig | Compression ist CPU-only, typisch <10ms |

---

## Empfehlung

1. **Gate B-Headroom-1:** CompressionContract in `:core:contracts` definieren
2. **Gate B-Headroom-2:** JsonCrusher + CacheAligner implementieren (LOSSLESS, geringes Risiko)
3. **Gate B-Headroom-3:** CodeCompressor (AST) implementieren (LOSSY, braucht Evaluation)
4. **Gate B-Headroom-4:** CCR fuer lange Runs implementieren (REVERSIBLE, braucht Store-Design)
5. **Kein Code-Import** aus headroom. Eigene KMP-native Implementierung.
