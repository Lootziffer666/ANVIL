// Gate C1 — Studio Run: seed-to-prototype orchestrator (JVM Application).
// Wires the real native module chain (Gameplay/Scene/Interface/Acoustic/Target) through
// RunSurface with the real ExternalToolPort adapters (Gate E-03/R-19..21) for WIZARD,
// SHADED, SWIFT, CUE-AGENT — falling back to clearly self-labeled fixtures per system
// when no real endpoint/repo is configured. BARD has no real adapter anywhere in this
// codebase yet, so it always stays a fixture here (same convention as GoldenRunFixture).
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:artifacts"))
    implementation(project(":core:run"))
    implementation(project(":core:handoff"))
    implementation(project(":core:sync"))
    implementation(project(":core:externaladapters"))
    implementation(project(":modules:gameplay"))
    implementation(project(":modules:scene"))
    implementation(project(":modules:interface"))
    implementation(project(":modules:acoustic"))
    implementation(project(":modules:target"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Ktor-Client (JVM) — WizardHttpAdapter braucht einen echten HTTP-Engine.
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.content.neg)
    implementation(libs.ktor.serialization.json)
    runtimeOnly(libs.slf4j.simple)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

application {
    applicationName = "studio-run"
    mainClass.set("io.anvil.app.studiorun.MainKt")
}
