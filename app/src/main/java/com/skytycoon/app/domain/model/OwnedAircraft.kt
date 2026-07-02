package com.skytycoon.app.domain.model

data class OwnedAircraft(
    val id: Long,
    val modelId: Int,
    val model: AircraftModel,
    val registrationCode: String,
    val acquisitionType: AcquisitionType,
    val condition: Int,
    val totalFlightHours: Double,
    val nextMaintenanceHours: Double
) {
    val needsMaintenance: Boolean get() = totalFlightHours >= nextMaintenanceHours
    val conditionLabel: String get() = when {
        condition >= 80 -> "Excellent"
        condition >= 60 -> "Good"
        condition >= 40 -> "Fair"
        else -> "Poor"
    }
    val isOperational: Boolean get() = condition > 20 && !needsMaintenance
}
