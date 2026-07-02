package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.EmployeeDao
import com.skytycoon.app.data.local.entity.EmployeeEntity
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface EmployeeRepository {
    fun getAll(): Flow<List<Employee>>
    suspend fun getById(id: Long): Employee?
    fun getByType(type: EmployeeType): Flow<List<Employee>>
    suspend fun insert(employee: Employee): Long
    suspend fun update(employee: Employee)
    suspend fun delete(id: Long)
}

@Singleton
class EmployeeRepositoryImpl @Inject constructor(
    private val employeeDao: EmployeeDao
) : EmployeeRepository {

    override fun getAll(): Flow<List<Employee>> =
        employeeDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Employee? = withContext(Dispatchers.IO) {
        employeeDao.getById(id)?.toDomain()
    }

    override fun getByType(type: EmployeeType): Flow<List<Employee>> =
        employeeDao.getByType(type.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(employee: Employee): Long = withContext(Dispatchers.IO) {
        employeeDao.insert(EmployeeEntity.fromDomain(employee))
    }

    override suspend fun update(employee: Employee) = withContext(Dispatchers.IO) {
        employeeDao.update(EmployeeEntity.fromDomain(employee))
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        employeeDao.deleteById(id)
    }
}
