// Gate B9 — KMP Bellows (LLM-Routing, OpenAI-kompatible Adapter)
// androidTarget() ist opt-in (-Panvil.android=true) — Default JVM-only.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val withAndroid = (providers.gradleProperty("anvil.android").orNull ?: "false").toBoolean()
if (withAndroid) apply(plugin = libs.plugins.android.library.get().pluginId)

kotlin {
    jvm()
    if (withAndroid) androidTarget()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:contracts"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.neg)
            implementation(libs.ktor.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

if (withAndroid) {
    extensions.configure<com.android.build.api.dsl.LibraryExtension> {
        namespace = "io.anvil.modules.bellows"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
    }
}
