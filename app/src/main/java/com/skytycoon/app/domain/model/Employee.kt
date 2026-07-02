package com.skytycoon.app.domain.model

data class Employee(
    val id: Long,
    val name: String,
    val type: EmployeeType,
    val level: Int,
    val dailySalaryCoins: Long,
    val fatigue: Int,
    val currentFlightId: Long? = null
) {
    val isAvailable: Boolean get() = currentFlightId == null && fatigue < 80
    val fatigueLabel: String get() = when {
        fatigue < 30 -> "Rested"
        fatigue < 60 -> "Tired"
        fatigue < 80 -> "Fatigued"
        else -> "Exhausted"
    }

    fun canFlyAircraftCategory(category: AircraftCategory): Boolean = when (type) {
        EmployeeType.PILOT -> category == AircraftCategory.AIRLINER || category == AircraftCategory.CHARTER
        EmployeeType.HELICOPTER_PILOT -> category == AircraftCategory.HELICOPTER
        EmployeeType.COPILOT -> category == AircraftCategory.AIRLINER || category == AircraftCategory.CHARTER
        else -> false
    }
}
