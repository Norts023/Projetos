package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.AchievementDao
import com.skytycoon.app.data.local.entity.AchievementEntity
import com.skytycoon.app.domain.model.Achievement
import com.skytycoon.app.domain.model.AchievementCategory
import com.skytycoon.app.domain.repository.AchievementProgressField
import com.skytycoon.app.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao
) : AchievementRepository {

    override fun getAll(): Flow<List<Achievement>> =
        dao.getAll().map { it.map { e -> e.toDomain() } }

    override fun getByCategory(category: AchievementCategory): Flow<List<Achievement>> =
        dao.getByCategory(category.name).map { it.map { e -> e.toDomain() } }

    override suspend fun getById(id: Long): Achievement? = dao.getById(id)?.toDomain()

    override suspend fun markClaimed(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(isClaimed = true))
    }

    override suspend fun seedIfEmpty() {
        if (dao.count() > 0) return
        dao.insertAll(defaultAchievements())
    }

    override suspend fun incrementProgress(field: AchievementProgressField, amount: Long) {
        val fieldMap = mapOf(
            AchievementProgressField.FLIGHTS_SCHEDULED to listOf(1L, 2L, 3L),
            AchievementProgressField.FLIGHTS_COMPLETED to listOf(4L, 5L, 6L, 7L),
            AchievementProgressField.AIRCRAFT_PURCHASED to listOf(8L, 9L, 10L),
            AchievementProgressField.ROUTES_OPERATED to listOf(11L, 12L),
            AchievementProgressField.PROFIT_COLLECTED to listOf(13L, 14L)
        )
        fieldMap[field]?.forEach { id ->
            val entity = dao.getById(id) ?: return@forEach
            if (!entity.isClaimed) {
                dao.update(entity.copy(currentValue = (entity.currentValue + amount).coerceAtMost(entity.targetValue)))
            }
        }
    }
}

private fun defaultAchievements(): List<AchievementEntity> = listOf(
    AchievementEntity(1, "GETTING_STARTED", "First Takeoff", "Schedule your first flight", 1, rewardCoins = 1000, rewardReputation = 5),
    AchievementEntity(2, "GETTING_STARTED", "Busy Skies", "Schedule 10 flights", 10, rewardCoins = 5000, rewardReputation = 10),
    AchievementEntity(3, "GETTING_STARTED", "Regional Player", "Schedule 50 flights", 50, rewardCoins = 20000, rewardReputation = 25),
    AchievementEntity(4, "ROUTES_AND_FLIGHTS", "Safe Landing", "Complete 10 flights", 10, rewardCoins = 5000, rewardReputation = 10),
    AchievementEntity(5, "ROUTES_AND_FLIGHTS", "Frequent Flyer", "Complete 50 flights", 50, rewardCoins = 25000, rewardReputation = 30),
    AchievementEntity(6, "ROUTES_AND_FLIGHTS", "Century Club", "Complete 100 flights", 100, rewardCoins = 60000, rewardReputation = 50),
    AchievementEntity(7, "ROUTES_AND_FLIGHTS", "Sky Master", "Complete 500 flights", 500, rewardCoins = 500000, rewardReputation = 150),
    AchievementEntity(8, "AIRCRAFT", "First Purchase", "Buy your first aircraft", 1, rewardCoins = 2000, rewardReputation = 5),
    AchievementEntity(9, "AIRCRAFT", "Small Fleet", "Own 5 aircraft", 5, rewardCoins = 10000, rewardReputation = 15),
    AchievementEntity(10, "AIRCRAFT", "Major Airline", "Own 20 aircraft", 20, rewardCoins = 100000, rewardReputation = 50, unlocksContentId = "aircraft_a321neo"),
    AchievementEntity(11, "ROUTES_AND_FLIGHTS", "Route Pioneer", "Operate 10 different routes", 10, rewardCoins = 15000, rewardReputation = 20),
    AchievementEntity(12, "ROUTES_AND_FLIGHTS", "Network Builder", "Operate 30 different routes", 30, rewardCoins = 80000, rewardReputation = 60),
    AchievementEntity(13, "GETTING_STARTED", "First Profit", "Collect 100,000 coins in profit", 100000, rewardCoins = 10000, rewardReputation = 10),
    AchievementEntity(14, "GETTING_STARTED", "Millionaire", "Collect 1,000,000 coins in profit", 1000000, rewardCoins = 100000, rewardReputation = 50)
)
