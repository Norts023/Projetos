package com.skytycoon.app.data.local.dao

import androidx.room.*
import com.skytycoon.app.data.local.entity.PurchaseRecordEntity

@Dao
interface PurchaseRecordDao {
    @Query("SELECT COUNT(*) FROM purchase_records WHERE purchaseToken = :token")
    suspend fun hasToken(token: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: PurchaseRecordEntity)

    @Query("SELECT * FROM purchase_records ORDER BY appliedAt DESC")
    suspend fun getAll(): List<PurchaseRecordEntity>
}
