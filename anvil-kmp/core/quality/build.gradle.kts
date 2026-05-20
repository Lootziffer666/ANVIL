// Gate B2 — KMP Core Quality
// Deps: :core:contracts (CLAUDE.md §3)
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:contracts"))
        }
    }
}

android {
    namespace = "io.anvil.core.quality"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
