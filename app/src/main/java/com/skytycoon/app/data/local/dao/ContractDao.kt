package com.skytycoon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skytycoon.app.data.local.entity.ContractEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractDao {

    @Query("SELECT * FROM contracts WHERE status = 'AVAILABLE'")
    fun getAvailable(): Flow<List<ContractEntity>>

    @Query("SELECT * FROM contracts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ContractEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contract: ContractEntity): Long

    @Update
    suspend fun update(contract: ContractEntity)

    @Query("DELETE FROM contracts WHERE deadlineGameMinutes < :currentGameMinutes AND status = 'AVAILABLE'")
    suspend fun deleteExpired(currentGameMinutes: Long)
}
