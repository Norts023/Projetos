package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_records")
data class PurchaseRecordEntity(
    @PrimaryKey val purchaseToken: String,
    val sku: String,
    val appliedAt: Long,
    val coinsGranted: Long
)
