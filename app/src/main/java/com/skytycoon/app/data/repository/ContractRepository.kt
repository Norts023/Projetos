package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.ContractDao
import com.skytycoon.app.data.local.entity.ContractEntity
import com.skytycoon.app.domain.model.Contract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface ContractRepository {
    fun getAvailable(): Flow<List<Contract>>
    suspend fun getById(id: Long): Contract?
    suspend fun insert(contract: Contract): Long
    suspend fun update(contract: Contract)
    suspend fun expireOld(currentGameMinutes: Long)
}

@Singleton
class ContractRepositoryImpl @Inject constructor(
    private val contractDao: ContractDao
) : ContractRepository {

    override fun getAvailable(): Flow<List<Contract>> =
        contractDao.getAvailable().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Contract? = withContext(Dispatchers.IO) {
        contractDao.getById(id)?.toDomain()
    }

    override suspend fun insert(contract: Contract): Long = withContext(Dispatchers.IO) {
        contractDao.insert(ContractEntity.fromDomain(contract))
    }

    override suspend fun update(contract: Contract) = withContext(Dispatchers.IO) {
        contractDao.update(ContractEntity.fromDomain(contract))
    }

    override suspend fun expireOld(currentGameMinutes: Long) = withContext(Dispatchers.IO) {
        contractDao.deleteExpired(currentGameMinutes)
    }
}
