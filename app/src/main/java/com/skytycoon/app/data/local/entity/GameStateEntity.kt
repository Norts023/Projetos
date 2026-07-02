package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.domain.model.GameState

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Long = 1L,
    val companyName: String,
    val gameMode: String,
    val balanceCoins: Long,
    val reputation: Int,
    val researchPoints: Int,
    val currentGameMinutes: Long,
    val dayNumber: Int
) {
    fun toDomain(): GameState = GameState(
        id = id,
        companyName = companyName,
        gameMode = GameMode.valueOf(gameMode),
        balanceCoins = balanceCoins,
        reputation = reputation,
        researchPoints = researchPoints,
        currentGameMinutes = currentGameMinutes,
        dayNumber = dayNumber
    )

    companion object {
        fun fromDomain(gameState: GameState): GameStateEntity = GameStateEntity(
            id = gameState.id,
            companyName = gameState.companyName,
            gameMode = gameState.gameMode.name,
            balanceCoins = gameState.balanceCoins,
            reputation = gameState.reputation,
            researchPoints = gameState.researchPoints,
            currentGameMinutes = gameState.currentGameMinutes,
            dayNumber = gameState.dayNumber
        )
    }
}
