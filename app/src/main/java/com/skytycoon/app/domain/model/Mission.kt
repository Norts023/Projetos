package com.skytycoon.app.domain.model

data class Mission(
    val id: Long,
    val type: MissionType,
    val title: String,
    val description: String,
    val rewardMoneyCoins: Long,
    val rewardReputation: Int,
    val rewardResearchPoints: Int,
    val targetValue: Long,
    val currentValue: Long,
    val status: MissionStatus,
    val operationType: OperationType? = null,
    val expiresAtGameDay: Int? = null
) {
    val progressPercent: Float get() = if (targetValue == 0L) 1f
        else (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
    val isCompleted: Boolean get() = currentValue >= targetValue
}
