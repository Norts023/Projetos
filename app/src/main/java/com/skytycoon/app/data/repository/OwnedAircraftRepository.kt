package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.AircraftModelDao
import com.skytycoon.app.data.local.dao.OwnedAircraftDao
import com.skytycoon.app.data.local.entity.OwnedAircraftEntity
import com.skytycoon.app.domain.model.OwnedAircraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface OwnedAircraftRepository {
    fun getAll(): Flow<List<OwnedAircraft>>
    suspend fun getById(id: Long): OwnedAircraft?
    suspend fun insert(aircraft: OwnedAircraft): Long
    suspend fun update(aircraft: OwnedAircraft)
    suspend fun delete(id: Long)
}

@Singleton
class OwnedAircraftRepositoryImpl @Inject constructor(
    private val ownedAircraftDao: OwnedAircraftDao,
    private val aircraftModelDao: AircraftModelDao
) : OwnedAircraftRepository {

    override fun getAll(): Flow<List<OwnedAircraft>> =
        ownedAircraftDao.getAll().map { entities ->
            entities.mapNotNull { entity ->
                val model = aircraftModelDao.getById(entity.modelId)?.toDomain()
                    ?: return@mapNotNull null
                entity.toDomain(model)
            }
        }

    override suspend fun getById(id: Long): OwnedAircraft? = withContext(Dispatchers.IO) {
        val entity = ownedAircraftDao.getById(id) ?: return@withContext null
        val model = aircraftModelDao.getById(entity.modelId)?.toDomain()
            ?: return@withContext null
        entity.toDomain(model)
    }

    override suspend fun insert(aircraft: OwnedAircraft): Long = withContext(Dispatchers.IO) {
        ownedAircraftDao.insert(OwnedAircraftEntity.fromDomain(aircraft))
    }

    override suspend fun update(aircraft: OwnedAircraft) = withContext(Dispatchers.IO) {
        ownedAircraftDao.update(OwnedAircraftEntity.fromDomain(aircraft))
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        ownedAircraftDao.deleteById(id)
    }
}
