package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.BoosterInventoryItem
import com.skytycoon.app.domain.model.BoosterType

@Entity(tableName = "booster_inventory")
data class BoosterInventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boosterType: String,
    val name: String,
    val description: String,
    val durationGameMinutes: Int,
    val multiplier: Double,
    val quantity: Int
) {
    fun toDomain() = BoosterInventoryItem(
        id = id,
        boosterType = runCatching { BoosterType.valueOf(boosterType) }.getOrDefault(BoosterType.TIME_BOOST),
        name = name,
        description = description,
        durationGameMinutes = durationGameMinutes,
        multiplier = multiplier,
        quantity = quantity
    )
}
