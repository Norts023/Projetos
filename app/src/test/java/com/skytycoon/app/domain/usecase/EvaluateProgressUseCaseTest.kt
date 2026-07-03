package com.skytycoon.app.domain.usecase

import com.skytycoon.app.domain.model.Achievement
import com.skytycoon.app.domain.model.AchievementCategory
import com.skytycoon.app.domain.repository.AchievementProgressField
import com.skytycoon.app.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EvaluateProgressUseCaseTest {

    private class FakeAchievementRepository : AchievementRepository {
        val calls = mutableListOf<Pair<AchievementProgressField, Long>>()

        override fun getAll(): Flow<List<Achievement>> = flowOf(emptyList())
        override fun getByCategory(category: AchievementCategory): Flow<List<Achievement>> = flowOf(emptyList())
        override suspend fun getById(id: Long): Achievement? = null
        override suspend fun markClaimed(id: Long) {}
        override suspend fun seedIfEmpty() {}
        override suspend fun incrementProgress(field: AchievementProgressField, amount: Long) {
            calls.add(field to amount)
        }
    }

    private lateinit var achievementRepo: FakeAchievementRepository
    private lateinit var useCase: EvaluateProgressUseCase

    @Before
    fun setUp() {
        achievementRepo = FakeAchievementRepository()
        useCase = EvaluateProgressUseCase(achievementRepo)
    }

    @Test
    fun `invoke with default amount calls incrementAll with 1`() = runTest {
        useCase(AchievementProgressField.FLIGHTS_COMPLETED)

        assertEquals(1, achievementRepo.calls.size)
        assertEquals(AchievementProgressField.FLIGHTS_COMPLETED to 1L, achievementRepo.calls.first())
    }

    @Test
    fun `invoke with custom amount passes it through`() = runTest {
        useCase(AchievementProgressField.PROFIT_COLLECTED, 10_000L)

        assertEquals(1, achievementRepo.calls.size)
        assertEquals(AchievementProgressField.PROFIT_COLLECTED to 10_000L, achievementRepo.calls.first())
    }

    @Test
    fun `invoke for different fields records correct field`() = runTest {
        useCase(AchievementProgressField.AIRCRAFT_PURCHASED)

        assertEquals(AchievementProgressField.AIRCRAFT_PURCHASED, achievementRepo.calls.first().first)
    }
}
