package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.Unlock

@Entity(tableName = "unlocks")
data class UnlockEntity(
    @PrimaryKey val contentId: String,
    val unlockedAt: Long
) {
    fun toDomain() = Unlock(contentId = contentId, unlockedAt = unlockedAt)
}
