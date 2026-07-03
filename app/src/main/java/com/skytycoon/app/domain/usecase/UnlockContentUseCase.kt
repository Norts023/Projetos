package com.skytycoon.app.domain.usecase

import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.repository.UnlockRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UnlockContentUseCase @Inject constructor(
    private val unlockRepository: UnlockRepository,
    private val gameStateRepository: GameStateRepository
) {
    suspend fun isUnlocked(contentId: String): Boolean = unlockRepository.isUnlocked(contentId)
    suspend fun unlock(contentId: String) {
        val minutes = gameStateRepository.get().first()?.currentGameMinutes ?: 0L
        unlockRepository.unlock(contentId, minutes)
    }
}
