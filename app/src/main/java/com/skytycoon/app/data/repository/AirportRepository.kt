package com.skytycoon.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skytycoon.app.data.local.dao.AirportDao
import com.skytycoon.app.data.local.entity.AirportEntity
import com.skytycoon.app.domain.model.Airport
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface AirportRepository {
    fun getAll(): Flow<List<Airport>>
    suspend fun getByIata(iata: String): Airport?
    fun search(query: String): Flow<List<Airport>>
    suspend fun seedIfEmpty(context: android.content.Context)
    suspend fun count(): Int
}

@Singleton
class AirportRepositoryImpl @Inject constructor(
    private val airportDao: AirportDao,
    @ApplicationContext private val appContext: Context
) : AirportRepository {

    override fun getAll(): Flow<List<Airport>> =
        airportDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getByIata(iata: String): Airport? = withContext(Dispatchers.IO) {
        airportDao.getByIata(iata)?.toDomain()
    }

    override fun search(query: String): Flow<List<Airport>> =
        airportDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun seedIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        if (airportDao.count() == 0) {
            val json = context.assets.open("airports.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<AirportJsonModel>>() {}.type
            val models: List<AirportJsonModel> = Gson().fromJson(json, listType)
            airportDao.insertAll(models.map { it.toEntity() })
        }
    }

    override suspend fun count(): Int = withContext(Dispatchers.IO) {
        airportDao.count()
    }

    private data class AirportJsonModel(
        val id: Int,
        val iata: String,
        val icao: String,
        val name: String,
        val city: String,
        val country: String,
        val lat: Double,
        val lon: Double,
        val region: String,
        val hasHelipad: Boolean
    ) {
        fun toEntity(): AirportEntity = AirportEntity(
            id = id,
            iata = iata,
            icao = icao,
            name = name,
            city = city,
            country = country,
            lat = lat,
            lon = lon,
            region = region,
            hasHelipad = hasHelipad
        )
    }
}
