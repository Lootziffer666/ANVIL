// Gate B19/A19 — Workspace Sync over Artifact/Run Registry
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:artifacts"))
            implementation(project(":core:contracts"))
            implementation(project(":core:run"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
