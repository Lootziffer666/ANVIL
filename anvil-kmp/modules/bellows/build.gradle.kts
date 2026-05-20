// Gate B5 — Bellows: LLM-Routing
// Deps: :core:contracts + :core:domain (CLAUDE.md §3)
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:contracts"))
            implementation(project(":core:domain"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "io.anvil.modules.bellows"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
