package io.anvil.modules.forge.knight

import io.anvil.core.contracts.QualityState

interface KnightContract {
    suspend fun readFile(path: String): FileContent
    suspend fun writeFile(path: String, content: String): WriteResult
    suspend fun diff(original: String, modified: String): UnifiedDiff
    suspend fun applyPatch(path: String, diff: UnifiedDiff): PatchResult
    fun qualityState(): QualityState
}
