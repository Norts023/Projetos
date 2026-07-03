package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.ActiveBooster
import com.skytycoon.app.domain.model.BoosterType

@Entity(tableName = "active_boosters")
data class ActiveBoosterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boosterType: String,
    val name: String,
    val multiplier: Double,
    val expiresAtGameMinutes: Long
) {
    fun toDomain() = ActiveBooster(
        id = id,
        boosterType = runCatching { BoosterType.valueOf(boosterType) }.getOrDefault(BoosterType.TIME_BOOST),
        name = name,
        multiplier = multiplier,
        expiresAtGameMinutes = expiresAtGameMinutes
    )
}
