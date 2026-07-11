// ANVIL KMP — Settings
// Stand: Gate B9 — Bellows Gateway (Produktionsreife)
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

// ── Bellows Gateway Build-Slice (JVM/Windows) ──────────────────────────────────
// Gate B9 fokussiert auf den lauffähigen Bellows-Gateway. Eingebunden ist genau
// der Dependency-Graph des Gateways:
include(":core:contracts")          // ModelRequest/Response, BellowsContract, PrivacyMode, CredentialVaultContract
include(":modules:bellows")         // BellowsRouter + OpenAI-kompatible Provider-Adapter (KMP, Ktor-Client)
include(":modules:bard")            // ANVIL-BARD: Bedeutungskompiler / CreativeBrief + ProductionIntent
include(":modules:gameplay")        // Gameplay Compiler: Regelwahrheit / InteractionDefinition + StatePatch
include(":modules:scene")           // Scene Compiler / 3D-RE-GEN: SceneBundle + räumliche Wahrheit
include(":modules:target")          // Target Adapter: ProductionBundle + RunnableBuild
include(":modules:interface")       // Interface Compiler: InputActionMap + HUDState
include(":app:bellows-gateway")     // JVM: Ktor-Server (/v1/...), CLI, JCEKS-CredentialVault

// ── Vorerst nicht im Gateway-Build ─────────────────────────────────────────────
// Diese KMP-/Compose-Module gehören zur Anvil-IDE (Knight, Commander) und ziehen
// androidTarget()/Compose mit. Sie sind NICHT Teil des Bellows-Gateways und
// werden hier ausgeklammert, bis der Desktop-/Android-App-Layer steht.
// (Quellen bleiben erhalten — nur aus dem aktuellen Build-Graph genommen.)
// include(":core:domain")
// include(":core:pipeline")
// include(":core:quality")
// include(":modules:forge:knight")
// include(":surfaces:commander")
// include(":app:android")            // Verzeichnis existiert noch nicht — Android-only
// include(":app:desktop")            // Verzeichnis existiert noch nicht — Compose-Desktop
