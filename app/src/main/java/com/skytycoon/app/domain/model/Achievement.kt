package com.skytycoon.app.domain.model

data class Achievement(
    val id: Long,
    val category: AchievementCategory,
    val title: String,
    val description: String,
    val targetValue: Long,
    val currentValue: Long,
    val rewardCoins: Long,
    val rewardReputation: Int,
    val isClaimed: Boolean,
    val unlocksContentId: String? = null
) {
    val progressPercent: Float get() = if (targetValue == 0L) 0f else (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
    val isCompleted: Boolean get() = currentValue >= targetValue
    val canClaim: Boolean get() = isCompleted && !isClaimed
}
