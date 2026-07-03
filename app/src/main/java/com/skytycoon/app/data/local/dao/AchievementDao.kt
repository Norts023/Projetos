package com.skytycoon.app.data.local.dao

import androidx.room.*
import com.skytycoon.app.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY category, id")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY id")
    fun getByCategory(category: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: Long): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<AchievementEntity>)

    @Update
    suspend fun update(item: AchievementEntity)

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun count(): Int
}
