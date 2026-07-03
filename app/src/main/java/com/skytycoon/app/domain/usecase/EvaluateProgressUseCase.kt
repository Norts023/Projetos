package com.skytycoon.app.domain.usecase

import com.skytycoon.app.domain.repository.AchievementProgressField
import com.skytycoon.app.domain.repository.AchievementRepository
import javax.inject.Inject

class EvaluateProgressUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(field: AchievementProgressField, amount: Long = 1) {
        achievementRepository.incrementAll(field, amount)
    }
}
