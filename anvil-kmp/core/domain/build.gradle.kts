// Gate B3 — KMP Core Domain
// Deps: :core:contracts (CLAUDE.md §3)
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
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "io.anvil.core.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
