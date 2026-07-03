package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.Achievement
import com.skytycoon.app.domain.model.AchievementCategory

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: Long,
    val category: String,
    val title: String,
    val description: String,
    val targetValue: Long,
    val currentValue: Long = 0L,
    val rewardCoins: Long,
    val rewardReputation: Int,
    val isClaimed: Boolean = false,
    val unlocksContentId: String? = null
) {
    fun toDomain() = Achievement(
        id = id,
        category = runCatching { AchievementCategory.valueOf(category) }.getOrDefault(AchievementCategory.GETTING_STARTED),
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        rewardCoins = rewardCoins,
        rewardReputation = rewardReputation,
        isClaimed = isClaimed,
        unlocksContentId = unlocksContentId
    )
}
