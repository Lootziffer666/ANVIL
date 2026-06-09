package io.anvil.modules.bellows

import io.anvil.core.contracts.BellowsExhaustedException
import io.anvil.core.contracts.ChatMessage
import io.anvil.core.contracts.ModelRequest
import io.anvil.core.contracts.ModelResponse
import io.anvil.core.contracts.PrivacyMode
import io.anvil.core.contracts.QualityState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private class FakeAdapter(
    override val id: String,
    override val isLocal: Boolean,
    override val models: List<String>,
    /** `null` ⇒ Adapter wirft (simuliert Ausfall). */
    private val reply: String? = "ok",
    private val state: QualityState = QualityState.STABLE,
) : ProviderAdapter {
    var calls = 0
        private set

    override fun handles(model: String?): Boolean =
        model == null || models.isEmpty() || models.any { it.equals(model, ignoreCase = true) }

    override suspend fun route(request: ModelRequest): ModelResponse {
        calls++
        if (reply == null) throw ProviderException(id, 500, "simulierter Ausfall")
        return ModelResponse(content = reply, modelUsed = "$id:test")
    }

    override fun qualityState(): QualityState = state
}

private fun req(model: String? = null, privacy: PrivacyMode = PrivacyMode.OPEN) =
    ModelRequest(messages = listOf(ChatMessage("user", "hi")), model = model, privacyMode = privacy)

class BellowsRouterTest {

    @Test
    fun localOnly_withoutLocalAdapter_throws() = runTest {
        val cloud = FakeAdapter("cloud", isLocal = false, models = listOf("gpt"))
        val router = BellowsRouter(listOf(cloud))
        assertFailsWith<BellowsExhaustedException> {
            router.route(req(privacy = PrivacyMode.LOCAL_ONLY))
        }
        assertEquals(0, cloud.calls, "Cloud-Adapter darf bei LOCAL_ONLY nie aufgerufen werden")
    }

    @Test
    fun localOnly_neverFallsBackToCloud() = runTest {
        val cloud = FakeAdapter("cloud", isLocal = false, models = listOf("gpt"), reply = "cloud")
        val local = FakeAdapter("local", isLocal = true, models = listOf("hermes"), reply = "local")
        val router = BellowsRouter(listOf(cloud, local))
        val res = router.route(req(privacy = PrivacyMode.LOCAL_ONLY))
        assertEquals("local", res.content)
        assertEquals(0, cloud.calls)
    }

    @Test
    fun routesToMatchingModelFirst() = runTest {
        val a = FakeAdapter("a", isLocal = false, models = listOf("model-a"), reply = "from-a")
        val b = FakeAdapter("b", isLocal = false, models = listOf("model-b"), reply = "from-b")
        val router = BellowsRouter(listOf(a, b))
        assertEquals("from-b", router.route(req(model = "model-b")).content)
        assertEquals(0, a.calls)
    }

    @Test
    fun fallsBackToNextProviderOnFailure() = runTest {
        val broken = FakeAdapter("broken", isLocal = false, models = listOf("m"), reply = null)
        val good = FakeAdapter("good", isLocal = false, models = listOf("m"), reply = "recovered")
        val router = BellowsRouter(listOf(broken, good))
        val res = router.route(req(model = "m"))
        assertEquals("recovered", res.content)
        assertEquals(1, broken.calls)
        assertEquals(1, good.calls)
    }

    @Test
    fun skipsFailedAdapters() = runTest {
        val dead = FakeAdapter("dead", isLocal = false, models = listOf("m"), reply = "never", state = QualityState.FAILED)
        val alive = FakeAdapter("alive", isLocal = false, models = listOf("m"), reply = "alive")
        val router = BellowsRouter(listOf(dead, alive))
        assertEquals("alive", router.route(req(model = "m")).content)
        assertEquals(0, dead.calls)
    }

    @Test
    fun allProvidersFailing_throws() = runTest {
        val a = FakeAdapter("a", isLocal = false, models = listOf("m"), reply = null)
        val b = FakeAdapter("b", isLocal = false, models = listOf("m"), reply = null)
        val router = BellowsRouter(listOf(a, b))
        assertFailsWith<BellowsExhaustedException> { router.route(req(model = "m")) }
    }

    @Test
    fun noProviders_throws() = runTest {
        assertFailsWith<BellowsExhaustedException> { BellowsRouter(emptyList()).route(req()) }
    }

    @Test
    fun listModels_dedupesAndSorts() {
        val router = BellowsRouter(
            listOf(
                FakeAdapter("a", false, listOf("b-model", "a-model")),
                FakeAdapter("b", true, listOf("a-model", "c-model")),
            ),
        )
        assertEquals(listOf("a-model", "b-model", "c-model"), router.listModels())
    }

    @Test
    fun qualityState_aggregates() {
        assertEquals(QualityState.BLOCKED, BellowsRouter(emptyList()).qualityState())
        assertEquals(
            QualityState.STABLE,
            BellowsRouter(listOf(FakeAdapter("a", false, listOf("m")))).qualityState(),
        )
        assertEquals(
            QualityState.DEGRADED,
            BellowsRouter(
                listOf(
                    FakeAdapter("a", false, listOf("m"), state = QualityState.STABLE),
                    FakeAdapter("b", false, listOf("m"), state = QualityState.FAILED),
                ),
            ).qualityState(),
        )
        assertTrue(
            BellowsRouter(
                listOf(FakeAdapter("a", false, listOf("m"), state = QualityState.FAILED)),
            ).qualityState() == QualityState.FAILED,
        )
    }
}
