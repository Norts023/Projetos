package com.skytycoon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skytycoon.app.data.local.entity.FlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    @Query("SELECT * FROM flights")
    fun getAll(): Flow<List<FlightEntity>>

    @Query("SELECT * FROM flights WHERE status = :status")
    fun getByStatus(status: String): Flow<List<FlightEntity>>

    @Query("SELECT * FROM flights WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FlightEntity?

    @Query("SELECT * FROM flights WHERE aircraftId = :aircraftId AND status IN (:statuses)")
    suspend fun getActiveForAircraft(aircraftId: Long, statuses: List<String>): List<FlightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flight: FlightEntity): Long

    @Update
    suspend fun update(flight: FlightEntity)

    @Query("DELETE FROM flights WHERE id = :id")
    suspend fun deleteById(id: Long)
}
