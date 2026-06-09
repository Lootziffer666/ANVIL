package io.anvil.app.bellows

import io.anvil.core.contracts.CredentialKey
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JvmCredentialVaultTest {

    private fun tempStore(): File =
        File.createTempFile("bellows-vault", ".jceks").also { it.delete() }

    @Test
    fun store_retrieve_persist_delete_roundtrip() {
        val store = tempStore()
        try {
            runBlocking {
                val vault = JvmCredentialVault(store, "master-pw".toCharArray())
                vault.store(CredentialKey("openrouter"), "sk-or-secret-123")

                assertEquals("sk-or-secret-123", vault.retrieve(CredentialKey("openrouter")))
                assertTrue(vault.aliases().contains("openrouter"))

                // Neu öffnen mit gleichem Passwort ⇒ Schlüssel ist persistiert.
                val reopened = JvmCredentialVault(store, "master-pw".toCharArray())
                assertEquals("sk-or-secret-123", reopened.retrieve(CredentialKey("openrouter")))

                reopened.delete(CredentialKey("openrouter"))
                assertNull(reopened.retrieve(CredentialKey("openrouter")))
            }
        } finally {
            store.delete()
        }
    }

    @Test
    fun unknownKey_returnsNull() {
        val store = tempStore()
        try {
            runBlocking {
                val vault = JvmCredentialVault(store, "pw".toCharArray())
                assertNull(vault.retrieve(CredentialKey("does-not-exist")))
            }
        } finally {
            store.delete()
        }
    }

    @Test
    fun wrongPassword_failsToOpen() {
        val store = tempStore()
        try {
            runBlocking { JvmCredentialVault(store, "right".toCharArray()).store(CredentialKey("k"), "v") }
            assertFailsWith<Exception> {
                JvmCredentialVault(store, "wrong".toCharArray())
            }
        } finally {
            store.delete()
        }
    }
}
