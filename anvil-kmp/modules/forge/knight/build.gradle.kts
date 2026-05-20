// Gate B6/B7 — Knight: Datei-I/O + kotlinx-datetime
// Deps: :core:contracts + :core:domain + Okio + kotlinx-datetime (CLAUDE.md §3)
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
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "io.anvil.modules.forge.knight"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
