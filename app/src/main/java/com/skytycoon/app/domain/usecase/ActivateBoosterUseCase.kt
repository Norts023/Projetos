package com.skytycoon.app.domain.usecase

import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.repository.BoosterRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ActivateBoosterUseCase @Inject constructor(
    private val boosterRepository: BoosterRepository,
    private val gameStateRepository: GameStateRepository
) {
    suspend operator fun invoke(inventoryId: Long): UseCaseResult<Unit> {
        val gameState = gameStateRepository.get().first() ?: return UseCaseResult.Failure("No active game")
        val activated = boosterRepository.activateBooster(inventoryId, gameState.currentGameMinutes)
        return if (activated) UseCaseResult.Success(Unit) else UseCaseResult.Failure("Booster not available")
    }
}
