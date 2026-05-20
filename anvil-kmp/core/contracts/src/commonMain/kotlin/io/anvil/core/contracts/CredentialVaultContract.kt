package io.anvil.core.contracts

import kotlinx.serialization.Serializable

// Adressiert Risk 5: kein Klartext-Speicher für API-Keys.
// Implementierungen müssen plattformspezifische Verschlüsselung nutzen:
// Android → EncryptedSharedPreferences, JVM → Java Keystore.
interface CredentialVaultContract {
    suspend fun store(key: CredentialKey, secret: String)
    suspend fun retrieve(key: CredentialKey): String?
    suspend fun delete(key: CredentialKey)
    fun qualityState(): QualityState
}

@JvmInline
@Serializable
value class CredentialKey(val id: String)
