package io.anvil.core.contracts

interface ModuleSlotContract {
    val name: String
    val purpose: String
    fun qualityState(): QualityState
}
