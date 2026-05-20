// Gate B5 — KMP Bellows (Bridge Adapter)
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
            implementation(project(":core:quality"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "io.anvil.modules.bellows"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
