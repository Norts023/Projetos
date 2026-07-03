package com.skytycoon.app.domain.repository

import com.skytycoon.app.domain.model.Unlock
import kotlinx.coroutines.flow.Flow

interface UnlockRepository {
    fun getAll(): Flow<List<Unlock>>
    suspend fun isUnlocked(contentId: String): Boolean
    suspend fun unlock(contentId: String, currentGameMinutes: Long)
}
