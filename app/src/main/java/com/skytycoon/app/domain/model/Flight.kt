package com.skytycoon.app.domain.model

data class Flight(
    val id: Long,
    val operationType: OperationType,
    val aircraftId: Long,
    val originIata: String,
    val destinationIata: String,
    val departureGameMinutes: Long,
    val arrivalGameMinutes: Long,
    val passengerCount: Int,
    val revenueCoins: Long,
    val status: FlightStatus,
    val assignedPilotId: Long? = null,
    val assignedCopilotId: Long? = null,
    val contractId: Long? = null
) {
    val durationMinutes: Long get() = arrivalGameMinutes - departureGameMinutes
    val durationHours: Double get() = durationMinutes / 60.0
    val isActive: Boolean get() = status == FlightStatus.SCHEDULED ||
            status == FlightStatus.BOARDING || status == FlightStatus.IN_FLIGHT ||
            status == FlightStatus.DELAYED
    val isFinished: Boolean get() = status == FlightStatus.COMPLETED ||
            status == FlightStatus.CANCELLED
}
