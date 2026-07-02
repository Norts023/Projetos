package com.skytycoon.app.domain.usecase

import android.content.Context
import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.AirportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SeedDataUseCase @Inject constructor(
    private val airportRepository: AirportRepository,
    private val aircraftModelRepository: AircraftModelRepository
) {
    suspend operator fun invoke(context: Context) = withContext(Dispatchers.IO) {
        airportRepository.seedIfEmpty(context)
        aircraftModelRepository.seedIfEmpty(context)
    }
}
