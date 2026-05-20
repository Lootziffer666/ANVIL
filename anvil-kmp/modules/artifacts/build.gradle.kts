// Gate B15 — ArtifactEngine: Artifacts mit Manifest auf Disk schreiben
// Deps: :core:contracts (FileIoContract) + :core:domain (Artifact) + serialization + datetime
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
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "io.anvil.modules.artifacts"
    compileSdk = libs.versions.compileSdk.get().toInt()
}
