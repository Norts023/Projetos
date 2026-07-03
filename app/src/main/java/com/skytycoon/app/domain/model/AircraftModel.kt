package com.skytycoon.app.domain.model

data class AircraftModel(
    val id: Int,
    val manufacturer: String,
    val model: String,
    val category: AircraftCategory,
    val passengerCapacity: Int,
    val rangeKm: Int,
    val cruiseSpeedKmh: Int,
    val fuelBurnLph: Double,
    val purchasePriceCoins: Long,
    val leasingCostPerHourCoins: Long,
    val maintenanceCostPerHourCoins: Long,
    val imageResName: String = "",
    val family: String = "",
    val variantCode: String = ""
) {
    val displayName: String get() = "$manufacturer $model"

    fun flightDurationHours(distanceKm: Double): Double =
        distanceKm / cruiseSpeedKmh.toDouble()

    fun isCapableOfRoute(distanceKm: Double): Boolean = distanceKm <= rangeKm
}
