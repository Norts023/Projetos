package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionStatus
import com.skytycoon.app.domain.model.MissionType
import com.skytycoon.app.domain.model.OperationType

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val title: String,
    val description: String,
    val rewardMoneyCoins: Long,
    val rewardReputation: Int,
    val rewardResearchPoints: Int,
    val targetValue: Long,
    val currentValue: Long,
    val status: String,
    val operationType: String?,
    val expiresAtGameDay: Int?
) {
    fun toDomain(): Mission = Mission(
        id = id,
        type = MissionType.entries.firstOrNull { it.name == type } ?: MissionType.DAILY,
        title = title,
        description = description,
        rewardMoneyCoins = rewardMoneyCoins,
        rewardReputation = rewardReputation,
        rewardResearchPoints = rewardResearchPoints,
        targetValue = targetValue,
        currentValue = currentValue,
        status = MissionStatus.entries.firstOrNull { it.name == status } ?: MissionStatus.ACTIVE,
        operationType = operationType?.let { op -> OperationType.entries.firstOrNull { it.name == op } },
        expiresAtGameDay = expiresAtGameDay
    )

    companion object {
        fun fromDomain(mission: Mission): MissionEntity = MissionEntity(
            id = mission.id,
            type = mission.type.name,
            title = mission.title,
            description = mission.description,
            rewardMoneyCoins = mission.rewardMoneyCoins,
            rewardReputation = mission.rewardReputation,
            rewardResearchPoints = mission.rewardResearchPoints,
            targetValue = mission.targetValue,
            currentValue = mission.currentValue,
            status = mission.status.name,
            operationType = mission.operationType?.name,
            expiresAtGameDay = mission.expiresAtGameDay
        )
    }
}
