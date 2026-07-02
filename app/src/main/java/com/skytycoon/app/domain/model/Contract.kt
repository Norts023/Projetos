package com.skytycoon.app.domain.model

data class Contract(
    val id: Long,
    val operationType: OperationType,
    val originIata: String,
    val destinationIata: String,
    val passengerCount: Int,
    val totalValueCoins: Long,
    val bonusOnTimeCoins: Long,
    val deadlineGameMinutes: Long,
    val status: ContractStatus,
    val assignedFlightId: Long? = null
) {
    val maxEarnings: Long get() = totalValueCoins + bonusOnTimeCoins
    val isAvailable: Boolean get() = status == ContractStatus.AVAILABLE
}
