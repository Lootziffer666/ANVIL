// Gate B4 — KMP Core Pipeline
// Deps: :core:contracts, :core:domain, kotlinx.serialization
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
    namespace = "io.anvil.core.pipeline"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
