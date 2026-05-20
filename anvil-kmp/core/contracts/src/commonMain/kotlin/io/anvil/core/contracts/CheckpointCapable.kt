package io.anvil.core.contracts

import kotlinx.serialization.Serializable

interface CheckpointCapable {
    suspend fun checkpoint(): CheckpointData
    suspend fun restore(data: CheckpointData)
}

@Serializable
data class CheckpointData(
    val id: String,
    val payload: String,
)
