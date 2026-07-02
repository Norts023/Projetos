package com.skytycoon.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skytycoon.app.data.local.entity.GameStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {

    @Query("SELECT * FROM game_state LIMIT 1")
    fun get(): Flow<GameStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameState: GameStateEntity)

    @Update
    suspend fun update(gameState: GameStateEntity)
}
