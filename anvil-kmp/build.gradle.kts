// ANVIL KMP — Root Build
// Stand: 2026-05-20
// Dieser Root-Build deklariert nur Plugins und Konventionen.
// Kein Code, kein Compilat hier.

plugins {
    // KMP-Plugins — version über libs.versions.toml
    alias(libs.plugins.kotlin.multiplatform)   apply false
    alias(libs.plugins.kotlin.jvm)             apply false
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
    // Kotlin-Kompilier-Optionen für alle Module (compilerOptions-DSL, Gradle-9-fest)
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            // Strikte Warnung → Fehler (kein Silencing)
            allWarningsAsErrors.set(true)
            // Nur Opt-ins, deren Marker in jedem Modul auflösbar sind.
            // (kotlinx.serialization ist in allen aktiven Modulen auf dem Classpath;
            //  Coroutines-Opt-ins gehören modul-lokal dorthin, wo sie gebraucht werden.)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
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
