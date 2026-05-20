// Gate B6 — Knight: Datei-I/O
// Deps: :core:contracts + :core:domain + Okio (CLAUDE.md §3)
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
            implementation(project(":core:domain"))
            implementation(libs.okio)
        }
    }
}

android {
    namespace = "io.anvil.modules.forge.knight"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
