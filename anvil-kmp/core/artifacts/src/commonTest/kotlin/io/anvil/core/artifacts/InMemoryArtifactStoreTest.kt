package io.anvil.core.artifacts

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryArtifactStoreTest {

    private fun envelope(id: String, payload: String) = ArtifactEnvelope(
        manifest = ArtifactManifest(
            artifactId = ArtifactId(id),
            createdAt = "2026-07-11T00:00:00Z",
            origin = ArtifactOrigin("test-module", WorkspaceId("ws"), RunId("r1")),
            type = "test.type/v1",
            uri = "artifact://test/$id.json",
            sizeBytes = payload.encodeToByteArray().size,
            checksumSha256 = Sha256.digestPrefixed(payload),
        ),
        payload = payload,
    )

    @Test
    fun write_thenRead_roundtrips() = runTest {
        val store = InMemoryArtifactStore()
        val env = envelope("A1", "{\"hello\":true}")
        store.write(env)
        assertEquals(env, store.read(ArtifactId("A1")))
        assertTrue(store.exists(ArtifactId("A1")))
    }

    @Test
    fun read_missing_returnsNull() = runTest {
        val store = InMemoryArtifactStore()
        assertNull(store.read(ArtifactId("GHOST")))
        assertFalse(store.exists(ArtifactId("GHOST")))
    }

    @Test
    fun write_samePayloadTwice_isIdempotent() = runTest {
        val store = InMemoryArtifactStore()
        val env = envelope("A1", "{\"x\":1}")
        store.write(env)
        store.write(env)
        assertEquals(env, store.read(ArtifactId("A1")))
    }

    @Test
    fun write_differentPayloadSameId_throws() = runTest {
        val store = InMemoryArtifactStore()
        store.write(envelope("A1", "{\"x\":1}"))
        assertFailsWith<ArtifactStoreConflictException> {
            store.write(envelope("A1", "{\"x\":2}"))
        }
    }

    @Test
    fun write_tamperedChecksum_rejected() = runTest {
        val store = InMemoryArtifactStore()
        val bad = envelope("A1", "{\"x\":1}").let { it.copy(manifest = it.manifest.copy(checksumSha256 = "sha256:" + "0".repeat(64))) }
        assertFailsWith<IllegalArgumentException> { store.write(bad) }
    }

    @Test
    fun verify_matchingChecksum_verified() = runTest {
        val store = InMemoryArtifactStore()
        store.write(envelope("A1", "{\"x\":1}"))
        assertEquals(ArtifactVerificationResult.Verified(ArtifactId("A1")), store.verify(ArtifactId("A1")))
    }

    @Test
    fun verify_missing_notFound() = runTest {
        val store = InMemoryArtifactStore()
        assertEquals(ArtifactVerificationResult.NotFound(ArtifactId("GHOST")), store.verify(ArtifactId("GHOST")))
    }
}
