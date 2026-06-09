package io.anvil.app.bellows

import io.anvil.core.contracts.CredentialKey
import io.anvil.core.contracts.CredentialVaultContract
import io.anvil.core.contracts.QualityState
import java.io.File
import java.security.KeyStore
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * JVM/Desktop-Implementierung von [CredentialVaultContract] auf Basis eines
 * **JCEKS-Keystores** (docs/SAFETY_POLICY.md §3). API-Keys liegen damit nie im
 * Klartext auf der Platte — der Keystore ist mit einem Master-Passwort verschlüsselt.
 *
 * Schlüssel werden als PBE-`SecretKeyEntry` abgelegt (Standard-Weg, um beliebige
 * Strings in einem Keystore zu speichern).
 *
 * @param storeFile Pfad zur `.jceks`-Datei.
 * @param password  Master-Passwort (z.B. aus `ANVIL_BELLOWS_VAULT_PASSWORD`).
 */
class JvmCredentialVault(
    private val storeFile: File,
    private val password: CharArray,
) : CredentialVaultContract {

    private val keyStore: KeyStore = KeyStore.getInstance("JCEKS")
    private val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBE")
    private var quality: QualityState = QualityState.STABLE

    init {
        if (storeFile.exists()) {
            storeFile.inputStream().use { keyStore.load(it, password) }
        } else {
            keyStore.load(null, password)
            persist()
        }
    }

    override suspend fun store(key: CredentialKey, secret: String) {
        val secretKey = factory.generateSecret(PBEKeySpec(secret.toCharArray()))
        keyStore.setEntry(
            key.id,
            KeyStore.SecretKeyEntry(secretKey),
            KeyStore.PasswordProtection(password),
        )
        persist()
    }

    override suspend fun retrieve(key: CredentialKey): String? {
        if (!keyStore.containsAlias(key.id)) return null
        val entry = keyStore.getEntry(key.id, KeyStore.PasswordProtection(password))
            as? KeyStore.SecretKeyEntry ?: return null
        val spec = factory.getKeySpec(entry.secretKey, PBEKeySpec::class.java) as PBEKeySpec
        return String(spec.password)
    }

    override suspend fun delete(key: CredentialKey) {
        if (keyStore.containsAlias(key.id)) {
            keyStore.deleteEntry(key.id)
            persist()
        }
    }

    override fun qualityState(): QualityState = quality

    /** Listet die gespeicherten Schlüssel-Referenzen (nicht die Geheimnisse). */
    fun aliases(): List<String> = keyStore.aliases().toList()

    private fun persist() {
        storeFile.parentFile?.mkdirs()
        try {
            // Rechte VOR dem Schreiben der Geheimnisse einschränken — sonst entsteht ein
            // kurzes Fenster, in dem die Keystore-Datei für andere Nutzer lesbar ist.
            // (POSIX greift sofort; auf Windows zusätzlich über ACLs.)
            if (!storeFile.exists()) {
                storeFile.createNewFile()
                storeFile.setReadable(false, false)
                storeFile.setReadable(true, true)
                storeFile.setWritable(false, false)
                storeFile.setWritable(true, true)
                storeFile.setExecutable(false, false)
            }
            storeFile.outputStream().use { keyStore.store(it, password) }
            quality = QualityState.STABLE
        } catch (e: Exception) {
            quality = QualityState.FAILED
            throw e
        }
    }
}
