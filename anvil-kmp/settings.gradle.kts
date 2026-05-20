// ANVIL KMP — Settings
// Stand: 2026-05-20
// Monorepo-Wurzel: anvil-kmp/

rootProject.name = "anvil"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// ── Core Modules ─────────────────────────────────────────────────────────────
// Interfaces, Domain-Typen, Pipeline-Sealed-Types, QualityState
// Keine Abhängigkeiten untereinander außer: domain ← contracts
include(":core:contracts")   // ModuleSlotContract, BellowsContract, CheckpointCapable
include(":core:domain")      // Workspace, Run, Artifact, Snapshot, MemoryEntry
include(":core:pipeline")    // RunStep sealed, RunResult sealed, StepRecord
include(":core:quality")     // QualityState, QualityGuard, QualityReport

// ── Feature Modules ───────────────────────────────────────────────────────────
// Implementierungen der Core-Contracts
include(":modules:forge:knight")    // Datei-I/O (Okio), Unified Diff, diff-match-patch
include(":modules:bellows")         // LLM-Routing: BellowsRouter + Provider-Adapters

// ── Surfaces ──────────────────────────────────────────────────────────────────
// Compose Multiplatform UI
include(":surfaces:commander")      // Desktop-Shell: Workspace-Browser, Diff-Viewer, Run-Log

// ── Apps ─────────────────────────────────────────────────────────────────────
include(":app:android")             // Compose Android entry point
include(":app:desktop")             // Compose Desktop entry point (JVM)

// ── Donor Analysis (read-only, kein Kotlin) ───────────────────────────────────
// Docs und Pattern-Analyse für kekegdsz/android-gradle-smart-build u.a.
// Kein Gradle-Modul — nur Verzeichnis-Konvention
// donor-analysis/  ← wird NICHT per include() eingebunden
