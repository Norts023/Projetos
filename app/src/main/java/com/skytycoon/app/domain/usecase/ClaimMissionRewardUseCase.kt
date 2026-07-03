package com.skytycoon.app.domain.usecase

import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.repository.AchievementRepository
import com.skytycoon.app.domain.repository.UnlockRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ClaimMissionRewardUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val gameStateRepository: GameStateRepository,
    private val unlockRepository: UnlockRepository
) {
    suspend operator fun invoke(achievementId: Long): UseCaseResult<Long> {
        val achievement = achievementRepository.getById(achievementId)
            ?: return UseCaseResult.Failure("Achievement not found")
        if (!achievement.canClaim) return UseCaseResult.Failure("Achievement not claimable")
        val gameState = gameStateRepository.get().first() ?: return UseCaseResult.Failure("No active game")
        val updatedState = gameState.copy(
            balanceCoins = gameState.balanceCoins + achievement.rewardCoins,
            reputation = gameState.reputation + achievement.rewardReputation
        )
        gameStateRepository.update(updatedState)
        achievementRepository.markClaimed(achievementId)
        achievement.unlocksContentId?.let {
            unlockRepository.unlock(it, gameState.currentGameMinutes)
        }
        return UseCaseResult.Success(achievement.rewardCoins)
    }
}
