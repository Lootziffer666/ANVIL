// Gate B2 — ANVIL-BARD creative contract module
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
