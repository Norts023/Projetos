package com.skytycoon.app.domain.usecase

import com.skytycoon.app.config.GameBalanceConfig
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.Contract
import com.skytycoon.app.domain.model.ContractStatus
import com.skytycoon.app.domain.model.OperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class GenerateContractsUseCase @Inject constructor(
    private val contractRepository: ContractRepository,
    private val airportRepository: AirportRepository,
    private val gameStateRepository: GameStateRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val currentAvailable = contractRepository.getAvailable().first().size
        val toGenerate = GameBalanceConfig.MAX_AVAILABLE_CONTRACTS - currentAvailable
        if (toGenerate <= 0) return@withContext

        val state = gameStateRepository.get().first() ?: return@withContext
        val allAirports = airportRepository.getAll().first()
        if (allAirports.size < 2) return@withContext

        val helipads = allAirports.filter { it.hasHelipad }

        repeat(toGenerate) {
            // 60% charter, 40% helicopter
            val operationType = if (Random.nextFloat() < 0.6f) OperationType.CHARTER else OperationType.HELICOPTER

            val eligibleAirports = if (operationType == OperationType.HELICOPTER && helipads.size >= 2) {
                helipads
            } else {
                allAirports
            }

            val shuffled = eligibleAirports.shuffled(Random)
            val origin = shuffled[0]
            val destination = shuffled[1]

            val distanceKm = origin.distanceKmTo(destination)

            val passengerCount = when (operationType) {
                OperationType.CHARTER -> Random.nextInt(1, 9)     // 1–8
                OperationType.HELICOPTER -> Random.nextInt(1, 5)  // 1–4
                else -> 1
            }

            val rawValue = when (operationType) {
                OperationType.CHARTER -> (distanceKm * passengerCount * 15.0).toLong()
                    .coerceIn(
                        GameBalanceConfig.CONTRACT_MIN_VALUE_CHARTER,
                        GameBalanceConfig.CONTRACT_MAX_VALUE_CHARTER
                    )
                OperationType.HELICOPTER -> (distanceKm * passengerCount * 25.0).toLong()
                    .coerceIn(
                        GameBalanceConfig.CONTRACT_MIN_VALUE_HELICOPTER,
                        GameBalanceConfig.CONTRACT_MAX_VALUE_HELICOPTER
                    )
                else -> GameBalanceConfig.CONTRACT_MIN_VALUE_CHARTER
            }

            // ±20% variance
            val variance = 0.8 + Random.nextDouble() * 0.4
            val totalValueCoins = (rawValue * variance).toLong()
            val bonusOnTimeCoins = (totalValueCoins * GameBalanceConfig.CONTRACT_BONUS_RATE).toLong()

            val contract = Contract(
                id = 0L,
                operationType = operationType,
                originIata = origin.iata,
                destinationIata = destination.iata,
                passengerCount = passengerCount,
                totalValueCoins = totalValueCoins,
                bonusOnTimeCoins = bonusOnTimeCoins,
                deadlineGameMinutes = state.currentGameMinutes + GameBalanceConfig.CONTRACT_EXPIRY_GAME_HOURS * 60L,
                status = ContractStatus.AVAILABLE,
                assignedFlightId = null
            )
            contractRepository.insert(contract)
        }
    }
}
