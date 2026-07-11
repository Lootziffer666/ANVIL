// Gate B17/A10 — Run Surface MVP
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:contracts"))
            implementation(project(":core:artifacts"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
