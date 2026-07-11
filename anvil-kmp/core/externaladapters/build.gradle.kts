// Gate E-03 — real CLI adapters for sibling systems (SWIFT, CUE-AGENT).
// JVM-only (ProcessBuilder is not KMP-portable), plain `kotlin.jvm` like
// `:app:bellows-gateway`. Depends only on `:core:contracts` (the ExternalToolPort
// contract) — no other `:core:*` or `:modules:*` dependency, matching
// "core:X darf core:contracts importieren" (CLAUDE.md §3).
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
