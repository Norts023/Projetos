package com.skytycoon.app.domain.usecase

import com.skytycoon.app.config.GameBalanceConfig
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.MissionRepository
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionStatus
import com.skytycoon.app.domain.model.MissionType
import com.skytycoon.app.domain.model.OperationType
import com.skytycoon.app.domain.model.UseCaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StartNewGameUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val missionRepository: MissionRepository
) {
    suspend operator fun invoke(
        companyName: String,
        mode: GameMode
    ): UseCaseResult<GameState> = withContext(Dispatchers.IO) {
        try {
            val startingBalance = when (mode) {
                GameMode.REALISTIC -> GameBalanceConfig.STARTING_BALANCE_REALISTIC
                GameMode.FICTIONAL -> GameBalanceConfig.STARTING_BALANCE_FICTIONAL
            }
            val gameState = GameState(
                id = 1L,
                companyName = companyName,
                gameMode = mode,
                balanceCoins = startingBalance,
                reputation = GameBalanceConfig.STARTING_REPUTATION,
                researchPoints = 0,
                currentGameMinutes = 480L, // 08:00 on day 1
                dayNumber = 1
            )
            gameStateRepository.insert(gameState)
            seedInitialMissions(dayNumber = 1)
            UseCaseResult.Success(gameState)
        } catch (e: Exception) {
            UseCaseResult.Failure(e.message ?: "Failed to start new game")
        }
    }

    private suspend fun seedInitialMissions(dayNumber: Int) {
        val primaryMissions = listOf(
            Mission(
                id = 0L,
                type = MissionType.PRIMARY,
                title = "First Flight",
                description = "Complete your first airline flight",
                rewardMoneyCoins = 10_000L,
                rewardReputation = 5,
                rewardResearchPoints = 10,
                targetValue = 1L,
                currentValue = 0L,
                status = MissionStatus.ACTIVE,
                operationType = OperationType.AIRLINE,
                expiresAtGameDay = null
            ),
            Mission(
                id = 0L,
                type = MissionType.PRIMARY,
                title = "Charter Starter",
                description = "Complete 3 charter contracts",
                rewardMoneyCoins = 25_000L,
                rewardReputation = 10,
                rewardResearchPoints = 15,
                targetValue = 3L,
                currentValue = 0L,
                status = MissionStatus.ACTIVE,
                operationType = OperationType.CHARTER,
                expiresAtGameDay = null
            ),
            Mission(
                id = 0L,
                type = MissionType.PRIMARY,
                title = "Helicopter Service",
                description = "Complete 5 helicopter operations",
                rewardMoneyCoins = 15_000L,
                rewardReputation = 8,
                rewardResearchPoints = 12,
                targetValue = 5L,
                currentValue = 0L,
                status = MissionStatus.ACTIVE,
                operationType = OperationType.HELICOPTER,
                expiresAtGameDay = null
            )
        )

        val secondaryMissions = listOf(
            Mission(
                id = 0L,
                type = MissionType.SECONDARY,
                title = "Punctual",
                description = "Complete 10 on-time flights",
                rewardMoneyCoins = 5_000L,
                rewardReputation = 3,
                rewardResearchPoints = 5,
                targetValue = 10L,
                currentValue = 0L,
                status = MissionStatus.ACTIVE,
                operationType = null,
                expiresAtGameDay = null
            ),
            Mission(
                id = 0L,
                type = MissionType.SECONDARY,
                title = "Fleet Builder",
                description = "Own 3 aircraft simultaneously",
                rewardMoneyCoins = 20_000L,
                rewardReputation = 8,
                rewardResearchPoints = 10,
                targetValue = 3L,
                currentValue = 0L,
                status = MissionStatus.ACTIVE,
                operationType = null,
                expiresAtGameDay = null
            ),
            Mission(
                id = 0L,
                type = MissionType.SECONDARY,
                title = "Full House",
                description = "Fill a flight to 100% passenger capacity",
                rewardMoneyCoins = 8_000L,
                rewardReputation = 5,
                rewardResearchPoints = 8,
                targetValue = 1L,
                currentValue = 0L,
                status = MissionStatus.ACTIVE,
                operationType = null,
                expiresAtGameDay = null
            )
        )

        val dailyMissions = buildDailyMissions(dayNumber)

        (primaryMissions + secondaryMissions + dailyMissions).forEach { mission ->
            missionRepository.insert(mission)
        }
    }

    internal fun buildDailyMissions(dayNumber: Int): List<Mission> = listOf(
        Mission(
            id = 0L,
            type = MissionType.DAILY,
            title = "Daily Charter",
            description = "Complete 2 charter contracts today",
            rewardMoneyCoins = 2_000L,
            rewardReputation = 2,
            rewardResearchPoints = 3,
            targetValue = 2L,
            currentValue = 0L,
            status = MissionStatus.ACTIVE,
            operationType = OperationType.CHARTER,
            expiresAtGameDay = dayNumber
        ),
        Mission(
            id = 0L,
            type = MissionType.DAILY,
            title = "Helicopter Day",
            description = "Complete 1 helicopter operation today",
            rewardMoneyCoins = 1_500L,
            rewardReputation = 1,
            rewardResearchPoints = 2,
            targetValue = 1L,
            currentValue = 0L,
            status = MissionStatus.ACTIVE,
            operationType = OperationType.HELICOPTER,
            expiresAtGameDay = dayNumber
        ),
        Mission(
            id = 0L,
            type = MissionType.DAILY,
            title = "Punctual Today",
            description = "Complete 1 on-time landing today",
            rewardMoneyCoins = 1_000L,
            rewardReputation = 1,
            rewardResearchPoints = 1,
            targetValue = 1L,
            currentValue = 0L,
            status = MissionStatus.ACTIVE,
            operationType = null,
            expiresAtGameDay = dayNumber
        )
    )
}
