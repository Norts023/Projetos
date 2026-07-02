package com.skytycoon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skytycoon.app.data.local.entity.OwnedAircraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnedAircraftDao {

    @Query("SELECT * FROM owned_aircraft")
    fun getAll(): Flow<List<OwnedAircraftEntity>>

    @Query("SELECT * FROM owned_aircraft WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OwnedAircraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(aircraft: OwnedAircraftEntity): Long

    @Update
    suspend fun update(aircraft: OwnedAircraftEntity)

    @Query("DELETE FROM owned_aircraft WHERE id = :id")
    suspend fun deleteById(id: Long)
}
