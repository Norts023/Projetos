package com.skytycoon.app.domain.model

data class BoosterDefinition(
    val id: Int,
    val type: BoosterType,
    val name: String,
    val description: String,
    val durationGameMinutes: Int,
    val multiplier: Double,
    val priceCostCoins: Long
)

data class ActiveBooster(
    val id: Long,
    val boosterType: BoosterType,
    val name: String,
    val multiplier: Double,
    val expiresAtGameMinutes: Long
) {
    val isExpired: Boolean get() = false // evaluated externally
}

data class BoosterInventoryItem(
    val id: Long,
    val boosterType: BoosterType,
    val name: String,
    val description: String,
    val durationGameMinutes: Int,
    val multiplier: Double,
    val quantity: Int
)
