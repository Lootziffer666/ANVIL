package io.anvil.core.artifacts

import io.anvil.core.contracts.ModuleArtifactRef
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArtifactWriterStoreIntegrationTest {

    private fun request(id: String, payload: String) = ArtifactWriteRequest(
        artifactRef = ModuleArtifactRef(
            id = id,
            workspaceId = "ws",
            runId = "r1",
            moduleOrigin = "test-module",
            type = "test.type/v1",
            uri = "artifact://test/$id.json",
            sha256 = "sha256:placeholder",
            timestamp = "2026-07-11T00:00:00Z",
        ),
        payload = payload,
        createdAt = "2026-07-11T00:00:00Z",
    )

    @Test
    fun writeAndPersist_persistsToStoreBeforeReturningRegistry() = runTest {
        val writer = ArtifactWriter()
        val store = InMemoryArtifactStore()
        val result = writer.writeAndPersist(request("A1", "{\"x\":1}"), store)
        assertEquals(1, result.registry.artifacts.size)
        assertEquals(result.envelope, store.read(ArtifactId("A1")))
    }

    @Test
    fun writeAndPersist_conflictingPayload_throwsAndRegistryIsNeverObserved() = runTest {
        val writer = ArtifactWriter()
        val store = InMemoryArtifactStore()
        writer.writeAndPersist(request("A1", "{\"x\":1}"), store)
        assertFailsWith<ArtifactStoreConflictException> {
            writer.writeAndPersist(request("A1", "{\"x\":2}"), store)
        }
        // The store must still only contain the original payload — no phantom overwrite.
        assertEquals("{\"x\":1}", store.read(ArtifactId("A1"))?.payload)
    }
}
