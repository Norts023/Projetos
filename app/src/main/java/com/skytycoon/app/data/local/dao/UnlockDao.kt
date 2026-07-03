package com.skytycoon.app.data.local.dao

import androidx.room.*
import com.skytycoon.app.data.local.entity.UnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {
    @Query("SELECT * FROM unlocks")
    fun getAll(): Flow<List<UnlockEntity>>

    @Query("SELECT COUNT(*) FROM unlocks WHERE contentId = :contentId")
    suspend fun isUnlocked(contentId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: UnlockEntity)
}
