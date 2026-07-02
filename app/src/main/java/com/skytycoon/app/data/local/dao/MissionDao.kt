package com.skytycoon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skytycoon.app.data.local.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    @Query("SELECT * FROM missions")
    fun getAll(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE type = :type")
    fun getByType(type: String): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: MissionEntity): Long

    @Update
    suspend fun update(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE type = 'DAILY' AND expiresAtGameDay < :currentDay")
    suspend fun deleteExpired(currentDay: Int)
}
