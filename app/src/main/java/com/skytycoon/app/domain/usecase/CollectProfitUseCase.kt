package com.skytycoon.app.domain.usecase

import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.repository.AchievementRepository
import com.skytycoon.app.domain.repository.AchievementProgressField
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CollectProfitUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(): UseCaseResult<GameState> {
        val gameState = gameStateRepository.get().first() ?: return UseCaseResult.Failure("No active game")
        val uncollected = gameState.uncollectedProfitCoins
        if (uncollected <= 0L) return UseCaseResult.Failure("No profit to collect")
        val updated = gameState.copy(
            balanceCoins = gameState.balanceCoins + uncollected,
            uncollectedProfitCoins = 0L
        )
        gameStateRepository.update(updated)
        achievementRepository.incrementAll(AchievementProgressField.PROFIT_COLLECTED, uncollected)
        return UseCaseResult.Success(updated)
    }
}
