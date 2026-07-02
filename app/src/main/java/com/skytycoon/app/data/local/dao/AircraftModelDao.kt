package com.skytycoon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skytycoon.app.data.local.entity.AircraftModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AircraftModelDao {

    @Query("SELECT * FROM aircraft_models")
    fun getAll(): Flow<List<AircraftModelEntity>>

    @Query("SELECT * FROM aircraft_models WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AircraftModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<AircraftModelEntity>)

    @Query("SELECT COUNT(*) FROM aircraft_models")
    suspend fun count(): Int
}
