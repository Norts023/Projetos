package com.skytycoon.app.domain.repository

import com.skytycoon.app.domain.model.Achievement
import com.skytycoon.app.domain.model.AchievementCategory
import kotlinx.coroutines.flow.Flow

enum class AchievementProgressField {
    FLIGHTS_SCHEDULED, FLIGHTS_COMPLETED, AIRCRAFT_PURCHASED, ROUTES_OPERATED, PROFIT_COLLECTED
}

interface AchievementRepository {
    fun getAll(): Flow<List<Achievement>>
    fun getByCategory(category: AchievementCategory): Flow<List<Achievement>>
    suspend fun getById(id: Long): Achievement?
    suspend fun markClaimed(id: Long)
    suspend fun seedIfEmpty()
    suspend fun incrementProgress(field: AchievementProgressField, amount: Long = 1)
    suspend fun incrementAll(field: AchievementProgressField, amount: Long) =
        incrementProgress(field, amount)
}
