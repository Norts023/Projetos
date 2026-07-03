package com.skytycoon.app.domain.usecase

import android.content.Context
import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.domain.repository.AchievementRepository
import com.skytycoon.app.domain.repository.BoosterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SeedDataUseCase @Inject constructor(
    private val airportRepository: AirportRepository,
    private val aircraftModelRepository: AircraftModelRepository,
    private val achievementRepository: AchievementRepository,
    private val boosterRepository: BoosterRepository
) {
    suspend operator fun invoke(context: Context) = withContext(Dispatchers.IO) {
        airportRepository.seedIfEmpty(context)
        aircraftModelRepository.seedIfEmpty(context)
        achievementRepository.seedIfEmpty()
        boosterRepository.seedDefaultBoosters()
    }
}
