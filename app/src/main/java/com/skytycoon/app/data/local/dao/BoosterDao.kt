package com.skytycoon.app.data.local.dao

import androidx.room.*
import com.skytycoon.app.data.local.entity.ActiveBoosterEntity
import com.skytycoon.app.data.local.entity.BoosterInventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoosterDao {
    @Query("SELECT * FROM booster_inventory WHERE quantity > 0")
    fun getInventory(): Flow<List<BoosterInventoryEntity>>

    @Query("SELECT * FROM active_boosters WHERE expiresAtGameMinutes > :currentMinutes")
    fun getActiveBoosters(currentMinutes: Long): Flow<List<ActiveBoosterEntity>>

    @Query("SELECT * FROM booster_inventory WHERE id = :id")
    suspend fun getInventoryItemById(id: Long): BoosterInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: BoosterInventoryEntity)

    @Update
    suspend fun updateInventoryItem(item: BoosterInventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActiveBooster(booster: ActiveBoosterEntity)

    @Query("DELETE FROM active_boosters WHERE expiresAtGameMinutes <= :currentMinutes")
    suspend fun deleteExpiredBoosters(currentMinutes: Long)

    @Query("SELECT COUNT(*) FROM booster_inventory")
    suspend fun inventoryCount(): Int
}
