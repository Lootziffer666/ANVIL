// Gate I — Golden Run proof.
// This is the one module allowed to import both :core:* and :modules:* (CLAUDE.md §3:
// "surfaces:* → darf :core:*, :modules:* importieren"). It ships no production code —
// only a deterministic end-to-end fixture and its test — so it stays out of any
// production dependency graph (:app:bellows-gateway does not depend on it).
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:contracts"))
            implementation(project(":core:artifacts"))
            implementation(project(":core:run"))
            implementation(project(":core:handoff"))
            implementation(project(":core:sync"))
            implementation(project(":modules:gameplay"))
            implementation(project(":modules:scene"))
            implementation(project(":modules:interface"))
            implementation(project(":modules:acoustic"))
            implementation(project(":modules:target"))
            implementation(libs.kotlinx.serialization.json)
        }
        // Real Golden Run R-19: RealGoldenRunTest needs the JVM-only real adapters
        // (ProcessBuilder/HTTP are not KMP-portable) plus a real Ktor HTTP engine —
        // gated by env vars, mirrors *ManualIntegrationTest's existing pattern.
        val jvmTest by getting {
            dependencies {
                implementation(project(":core:externaladapters"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.java)
            }
        }
    }
}
