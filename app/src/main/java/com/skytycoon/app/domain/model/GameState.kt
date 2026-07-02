package com.skytycoon.app.domain.model

data class GameState(
    val id: Long = 1L,
    val companyName: String,
    val gameMode: GameMode,
    val balanceCoins: Long,
    val reputation: Int,
    val researchPoints: Int,
    val currentGameMinutes: Long,
    val dayNumber: Int
) {
    val currentHour: Int get() = ((currentGameMinutes % 1440) / 60).toInt()
    val currentMinute: Int get() = (currentGameMinutes % 60).toInt()
    val timeDisplay: String get() = "%02d:%02d".format(currentHour, currentMinute)
    val reputationLabel: String get() = when {
        reputation >= 90 -> "World Class"
        reputation >= 75 -> "Excellent"
        reputation >= 60 -> "Good"
        reputation >= 40 -> "Average"
        reputation >= 20 -> "Poor"
        else -> "Terrible"
    }
    val balanceDisplay: String get() = when {
        balanceCoins >= 1_000_000 -> "${"%.1f".format(balanceCoins / 1_000_000.0)}M"
        balanceCoins >= 1_000 -> "${"%.0f".format(balanceCoins / 1_000.0)}K"
        else -> balanceCoins.toString()
    }
}
