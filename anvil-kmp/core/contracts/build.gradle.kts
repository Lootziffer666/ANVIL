// Gate B2 — KMP Core Contracts
// Deps: nur Kotlin stdlib + kotlinx.serialization (CLAUDE.md §3)
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
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "io.anvil.core.contracts"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
