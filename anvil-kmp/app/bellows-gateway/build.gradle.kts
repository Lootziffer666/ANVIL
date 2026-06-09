// Gate B9 — Bellows Gateway (JVM Application)
// Lauffähiger OpenAI-kompatibler Gateway für Windows/Desktop.
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
    implementation(project(":modules:bellows"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Ktor-Client-Engine (JVM) für die Provider-Adapter
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.content.neg)
    implementation(libs.ktor.serialization.json)

    // Ktor-Server (Gateway-Endpoint /v1/...)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.neg)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    runtimeOnly(libs.slf4j.simple)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.neg)
    testImplementation(libs.kotlinx.coroutines.core)
}

application {
    applicationName = "bellows"
    mainClass.set("io.anvil.app.bellows.MainKt")
}
