// Gate B16/A7 — Artifact Output Layer MVP
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:contracts"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
