package com.skytycoon.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skytycoon.app.data.local.dao.AircraftModelDao
import com.skytycoon.app.data.local.entity.AircraftModelEntity
import com.skytycoon.app.domain.model.AircraftCategory
import com.skytycoon.app.domain.model.AircraftModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface AircraftModelRepository {
    fun getAll(): Flow<List<AircraftModel>>
    suspend fun getById(id: Int): AircraftModel?
    suspend fun seedIfEmpty(context: Context)
}

@Singleton
class AircraftModelRepositoryImpl @Inject constructor(
    private val aircraftModelDao: AircraftModelDao,
    @ApplicationContext private val appContext: Context
) : AircraftModelRepository {

    override fun getAll(): Flow<List<AircraftModel>> =
        aircraftModelDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Int): AircraftModel? = withContext(Dispatchers.IO) {
        aircraftModelDao.getById(id)?.toDomain()
    }

    override suspend fun seedIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        if (aircraftModelDao.count() == 0) {
            val json = context.assets.open("aircraft_models.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<AircraftModelJsonModel>>() {}.type
            val models: List<AircraftModelJsonModel> = Gson().fromJson(json, listType)
            aircraftModelDao.insertAll(models.map { it.toEntity() })
        }
    }

    private data class AircraftModelJsonModel(
        val id: Int,
        val manufacturer: String,
        val model: String,
        val category: String,
        val passengerCapacity: Int,
        val rangeKm: Int,
        val cruiseSpeedKmh: Int,
        val fuelBurnLph: Double,
        val purchasePriceCoins: Long,
        val leasingCostPerHourCoins: Long,
        val maintenanceCostPerHourCoins: Long,
        val imageResName: String
    ) {
        fun toEntity(): AircraftModelEntity = AircraftModelEntity(
            id = id,
            manufacturer = manufacturer,
            model = model,
            category = AircraftCategory.entries.firstOrNull { it.name == category } ?: AircraftCategory.AIRLINER,
            passengerCapacity = passengerCapacity,
            rangeKm = rangeKm,
            cruiseSpeedKmh = cruiseSpeedKmh,
            fuelBurnLph = fuelBurnLph,
            purchasePriceCoins = purchasePriceCoins,
            leasingCostPerHourCoins = leasingCostPerHourCoins,
            maintenanceCostPerHourCoins = maintenanceCostPerHourCoins,
            imageResName = imageResName
        )
    }
}
