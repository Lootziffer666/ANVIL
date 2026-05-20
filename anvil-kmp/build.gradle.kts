// ANVIL KMP — Root Build
// Stand: 2026-05-20
// Dieser Root-Build deklariert nur Plugins und Konventionen.
// Kein Code, kein Compilat hier.

plugins {
    // KMP-Plugins — version über libs.versions.toml
    alias(libs.plugins.kotlin.multiplatform)   apply false
    alias(libs.plugins.kotlin.android)         apply false
    alias(libs.plugins.kotlin.serialization)   apply false
    alias(libs.plugins.compose.multiplatform)  apply false
    alias(libs.plugins.compose.compiler)       apply false
    alias(libs.plugins.android.application)    apply false
    alias(libs.plugins.android.library)        apply false
    alias(libs.plugins.sqldelight)             apply false
}

// ── Subproject-Konventionen ────────────────────────────────────────────────────
// Alle Subprojekte erben diese Basis-Konfiguration.
subprojects {
    // Kotlin-Kompilier-Optionen für alle Module
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            // Strikte Warnung → Fehler (kein Silencing)
            allWarningsAsErrors = true
            // Opt-ins global freigeben (nicht in jedem Modul wiederholen)
            freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
}

// ── Quality Guard ──────────────────────────────────────────────────────────────
// Workspace-Safety: kein Build darf außerhalb des Projekt-Root schreiben.
// Phase-MVP-Kill-Kriterium: Änderungen außerhalb Workspace.rootPath → FAILED.
// (Vollständige Enforcement-Logik kommt in :core:quality — hier nur Basis-Guard)
gradle.taskGraph.whenReady {
    allTasks.forEach { task ->
        task.doFirst {
            // Sicherheits-Assertion: Build-Output liegt im Projektverzeichnis
            val buildDir = task.project.layout.buildDirectory.asFile.orNull
            if (buildDir != null) {
                require(buildDir.startsWith(rootProject.rootDir)) {
                    "ANVIL Quality Guard: Build-Output liegt außerhalb des Repo-Root! " +
                    "Task: ${task.path}, Output: $buildDir"
                }
            }
        }
    }
}
