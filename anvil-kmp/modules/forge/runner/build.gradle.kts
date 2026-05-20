// Gate B11 — ForgeRunner: konkrete RunEngine-Implementierung
// Deps: :core:contracts + :core:domain + :core:pipeline + :core:quality + coroutines + datetime
// Keine anderen :modules:*-Imports (CLAUDE.md §3)
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
            implementation(project(":core:pipeline"))
            implementation(project(":core:quality"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "io.anvil.modules.forge.runner"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
