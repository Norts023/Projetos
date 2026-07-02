package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.MissionDao
import com.skytycoon.app.data.local.entity.MissionEntity
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface MissionRepository {
    fun getAll(): Flow<List<Mission>>
    fun getByType(type: MissionType): Flow<List<Mission>>
    suspend fun insert(mission: Mission): Long
    suspend fun update(mission: Mission)
    suspend fun deleteExpiredDailies(currentGameDay: Int)
}

@Singleton
class MissionRepositoryImpl @Inject constructor(
    private val missionDao: MissionDao
) : MissionRepository {

    override fun getAll(): Flow<List<Mission>> =
        missionDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getByType(type: MissionType): Flow<List<Mission>> =
        missionDao.getByType(type.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(mission: Mission): Long = withContext(Dispatchers.IO) {
        missionDao.insert(MissionEntity.fromDomain(mission))
    }

    override suspend fun update(mission: Mission) = withContext(Dispatchers.IO) {
        missionDao.update(MissionEntity.fromDomain(mission))
    }

    override suspend fun deleteExpiredDailies(currentGameDay: Int) = withContext(Dispatchers.IO) {
        missionDao.deleteExpired(currentGameDay)
    }
}
