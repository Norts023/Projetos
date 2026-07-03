package com.skytycoon.app.domain.repository

import com.skytycoon.app.domain.model.ActiveBooster
import com.skytycoon.app.domain.model.BoosterInventoryItem
import kotlinx.coroutines.flow.Flow

interface BoosterRepository {
    fun getInventory(): Flow<List<BoosterInventoryItem>>
    fun getActiveBoosters(currentGameMinutes: Long): Flow<List<ActiveBooster>>
    suspend fun activateBooster(inventoryId: Long, currentGameMinutes: Long): Boolean
    suspend fun deleteExpiredBoosters(currentGameMinutes: Long)
    suspend fun seedDefaultBoosters()
}
