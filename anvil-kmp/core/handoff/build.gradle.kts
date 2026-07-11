// Gate B18/A8 — Handoff Export over Artifact Layer
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
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
