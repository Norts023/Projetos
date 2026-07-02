package com.skytycoon.app.domain.usecase

import com.skytycoon.app.config.GameBalanceConfig
import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.domain.model.UseCaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PurchaseAircraftUseCase @Inject constructor(
    private val aircraftModelRepository: AircraftModelRepository,
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val gameStateRepository: GameStateRepository
) {
    suspend operator fun invoke(
        modelId: Int,
        acquisitionType: AcquisitionType,
        registrationCode: String
    ): UseCaseResult<OwnedAircraft> = withContext(Dispatchers.IO) {
        val model = aircraftModelRepository.getById(modelId)
            ?: return@withContext UseCaseResult.Failure("Aircraft model not found")

        val state = gameStateRepository.get().first()
            ?: return@withContext UseCaseResult.Failure("No active game state found")

        var updatedBalance = state.balanceCoins

        if (acquisitionType == AcquisitionType.PURCHASED) {
            if (state.balanceCoins < model.purchasePriceCoins) {
                return@withContext UseCaseResult.Failure(
                    "Insufficient funds. Required: ${model.purchasePriceCoins}, Available: ${state.balanceCoins}"
                )
            }
            updatedBalance -= model.purchasePriceCoins
        }
        // LEASED: no upfront cost — hourly deduction is handled in AdvanceTimeUseCase

        val newAircraft = OwnedAircraft(
            id = 0L,
            modelId = modelId,
            model = model,
            registrationCode = registrationCode,
            acquisitionType = acquisitionType,
            condition = 100,
            totalFlightHours = 0.0,
            nextMaintenanceHours = GameBalanceConfig.MAINTENANCE_INTERVAL_HOURS
        )

        val newId = ownedAircraftRepository.insert(newAircraft)
        gameStateRepository.update(state.copy(balanceCoins = updatedBalance))

        UseCaseResult.Success(newAircraft.copy(id = newId))
    }
}
