package io.anvil.core.artifacts

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JvmFileArtifactStoreTest {

    private lateinit var root: java.io.File

    @BeforeTest
    fun setUp() {
        root = Files.createTempDirectory("anvil-artifact-store-test").toFile()
    }

    @AfterTest
    fun tearDown() {
        root.deleteRecursively()
    }

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
    fun write_thenRead_roundtripsFromDisk() = runTest {
        val store = JvmFileArtifactStore(root)
        val env = envelope("A1", "{\"hello\":true}")
        store.write(env)
        assertEquals(env, store.read(ArtifactId("A1")))
        assertTrue(java.io.File(root, "A1.json").exists())
    }

    @Test
    fun write_samePayloadTwice_isIdempotent() = runTest {
        val store = JvmFileArtifactStore(root)
        val env = envelope("A1", "{\"x\":1}")
        store.write(env)
        store.write(env)
        assertEquals(env, store.read(ArtifactId("A1")))
    }

    @Test
    fun write_differentPayloadSameId_throwsConflict() = runTest {
        val store = JvmFileArtifactStore(root)
        store.write(envelope("A1", "{\"x\":1}"))
        assertFailsWith<ArtifactStoreConflictException> {
            store.write(envelope("A1", "{\"x\":2}"))
        }
    }

    @Test
    fun write_tamperedChecksum_rejectedBeforeTouchingDisk() = runTest {
        val store = JvmFileArtifactStore(root)
        val bad = envelope("A1", "{\"x\":1}").let { it.copy(manifest = it.manifest.copy(checksumSha256 = "sha256:" + "0".repeat(64))) }
        assertFailsWith<IllegalArgumentException> { store.write(bad) }
        assertNull(store.read(ArtifactId("A1")))
    }

    @Test
    fun artifactId_withPathTraversal_isRejected() = runTest {
        val store = JvmFileArtifactStore(root)
        assertFailsWith<IllegalArgumentException> {
            store.write(envelope("../escape", "{\"x\":1}"))
        }
    }

    @Test
    fun artifactId_withPathSeparator_isRejected() = runTest {
        val store = JvmFileArtifactStore(root)
        assertFailsWith<IllegalArgumentException> {
            store.write(envelope("sub/dir", "{\"x\":1}"))
        }
    }

    @Test
    fun symlinkedArtifactRoot_stillConfinesWritesToRealRoot() = runTest {
        val outside = Files.createTempDirectory("anvil-artifact-store-outside").toFile()
        try {
            val symlinkRoot = Files.createTempDirectory("anvil-artifact-store-linkparent").resolve("link")
            Files.createSymbolicLink(symlinkRoot, outside.toPath())
            val store = JvmFileArtifactStore(symlinkRoot.toFile())
            store.write(envelope("A1", "{\"x\":1}"))
            // The artifact must land in the canonical (real) target of the symlink, not create
            // a duplicate elsewhere, and must be readable back through the store.
            assertTrue(java.io.File(outside, "A1.json").exists(), "expected artifact at ${outside.path}, symlink was $symlinkRoot")
        } finally {
            outside.deleteRecursively()
        }
    }

    @Test
    fun read_missing_returnsNull() = runTest {
        val store = JvmFileArtifactStore(root)
        assertNull(store.read(ArtifactId("GHOST")))
    }

    @Test
    fun verify_afterWrite_verified() = runTest {
        val store = JvmFileArtifactStore(root)
        store.write(envelope("A1", "{\"x\":1}"))
        assertEquals(ArtifactVerificationResult.Verified(ArtifactId("A1")), store.verify(ArtifactId("A1")))
    }
}
