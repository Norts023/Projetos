package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.Contract
import com.skytycoon.app.domain.model.ContractStatus
import com.skytycoon.app.domain.model.OperationType

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val originIata: String,
    val destinationIata: String,
    val passengerCount: Int,
    val totalValueCoins: Long,
    val bonusOnTimeCoins: Long,
    val deadlineGameMinutes: Long,
    val status: String,
    val assignedFlightId: Long?
) {
    fun toDomain(): Contract = Contract(
        id = id,
        operationType = OperationType.valueOf(operationType),
        originIata = originIata,
        destinationIata = destinationIata,
        passengerCount = passengerCount,
        totalValueCoins = totalValueCoins,
        bonusOnTimeCoins = bonusOnTimeCoins,
        deadlineGameMinutes = deadlineGameMinutes,
        status = ContractStatus.valueOf(status),
        assignedFlightId = assignedFlightId
    )

    companion object {
        fun fromDomain(contract: Contract): ContractEntity = ContractEntity(
            id = contract.id,
            operationType = contract.operationType.name,
            originIata = contract.originIata,
            destinationIata = contract.destinationIata,
            passengerCount = contract.passengerCount,
            totalValueCoins = contract.totalValueCoins,
            bonusOnTimeCoins = contract.bonusOnTimeCoins,
            deadlineGameMinutes = contract.deadlineGameMinutes,
            status = contract.status.name,
            assignedFlightId = contract.assignedFlightId
        )
    }
}
