// Gate B2 — KMP Core Contracts
// Deps: nur Kotlin stdlib + kotlinx.serialization (CLAUDE.md §3)
// Gate B9: androidTarget() ist opt-in (-Panvil.android=true) — Default JVM-only.
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
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

if (withAndroid) {
    extensions.configure<com.android.build.api.dsl.LibraryExtension> {
        namespace = "io.anvil.core.contracts"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }
}
