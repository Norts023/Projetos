package com.skytycoon.app.domain.usecase

import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.repository.AchievementProgressField
import com.skytycoon.app.domain.repository.AchievementRepository
import com.skytycoon.app.domain.model.Achievement
import com.skytycoon.app.domain.model.AchievementCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CollectProfitUseCaseTest {

    private class FakeGameStateRepository : GameStateRepository {
        var state: GameState? = null
        var updatedState: GameState? = null

        override fun get(): Flow<GameState?> = flowOf(state)
        override suspend fun insert(s: GameState) { state = s }
        override suspend fun update(s: GameState) {
            state = s
            updatedState = s
        }
    }

    private class FakeAchievementRepository : AchievementRepository {
        var lastField: AchievementProgressField? = null
        var lastAmount: Long = 0L

        override fun getAll(): Flow<List<Achievement>> = flowOf(emptyList())
        override fun getByCategory(category: AchievementCategory): Flow<List<Achievement>> = flowOf(emptyList())
        override suspend fun getById(id: Long): Achievement? = null
        override suspend fun markClaimed(id: Long) {}
        override suspend fun seedIfEmpty() {}
        override suspend fun incrementProgress(field: AchievementProgressField, amount: Long) {
            lastField = field
            lastAmount = amount
        }
    }

    private fun buildGameState(
        balance: Long = 1_000L,
        uncollected: Long = 5_000L
    ) = GameState(
        id = 1L,
        companyName = "TestAir",
        gameMode = GameMode.FICTIONAL,
        balanceCoins = balance,
        reputation = 50,
        researchPoints = 0,
        currentGameMinutes = 480L,
        dayNumber = 1,
        uncollectedProfitCoins = uncollected
    )

    private lateinit var gameStateRepo: FakeGameStateRepository
    private lateinit var achievementRepo: FakeAchievementRepository
    private lateinit var useCase: CollectProfitUseCase

    @Before
    fun setUp() {
        gameStateRepo = FakeGameStateRepository()
        achievementRepo = FakeAchievementRepository()
        useCase = CollectProfitUseCase(gameStateRepo, achievementRepo)
    }

    @Test
    fun `collect profit transfers uncollected to balance`() = runTest {
        gameStateRepo.state = buildGameState(balance = 1_000L, uncollected = 5_000L)

        val result = useCase()

        assertTrue("Expected Success", result is UseCaseResult.Success)
        val updated = (result as UseCaseResult.Success).data
        assertEquals(6_000L, updated.balanceCoins)
        assertEquals(0L, updated.uncollectedProfitCoins)
    }

    @Test
    fun `collect profit increments achievement progress by collected amount`() = runTest {
        gameStateRepo.state = buildGameState(balance = 0L, uncollected = 3_500L)

        useCase()

        assertEquals(AchievementProgressField.PROFIT_COLLECTED, achievementRepo.lastField)
        assertEquals(3_500L, achievementRepo.lastAmount)
    }

    @Test
    fun `collect profit returns Failure when no game state`() = runTest {
        gameStateRepo.state = null

        val result = useCase()

        assertTrue("Expected Failure", result is UseCaseResult.Failure)
    }

    @Test
    fun `collect profit returns Failure when nothing to collect`() = runTest {
        gameStateRepo.state = buildGameState(balance = 1_000L, uncollected = 0L)

        val result = useCase()

        assertTrue("Expected Failure when uncollected == 0", result is UseCaseResult.Failure)
    }

    @Test
    fun `collect profit persists updated state to repository`() = runTest {
        gameStateRepo.state = buildGameState(balance = 200L, uncollected = 800L)

        useCase()

        assertEquals(1_000L, gameStateRepo.updatedState?.balanceCoins)
        assertEquals(0L, gameStateRepo.updatedState?.uncollectedProfitCoins)
    }
}
