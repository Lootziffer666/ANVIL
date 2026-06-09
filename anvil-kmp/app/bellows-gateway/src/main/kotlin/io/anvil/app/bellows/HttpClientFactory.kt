package io.anvil.app.bellows

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

/**
 * Baut den geteilten Ktor-Client (JVM-Engine) für alle Provider-Adapter.
 *
 * - `expectSuccess = false`: Nicht-2xx-Antworten werfen nicht im Client; der Adapter
 *   liest Status + Body selbst und entscheidet (DEGRADED, Fallback).
 * - großzügiger Request-Timeout für langsame lokale Modelle (z.B. Hermes auf CPU).
 */
fun buildHttpClient(requestTimeoutMillis: Long = 180_000): HttpClient =
    HttpClient(Java) {
        expectSuccess = false
        install(ContentNegotiation) { json(gatewayJson) }
        install(HttpTimeout) {
            this.requestTimeoutMillis = requestTimeoutMillis
            connectTimeoutMillis = 15_000
        }
    }
