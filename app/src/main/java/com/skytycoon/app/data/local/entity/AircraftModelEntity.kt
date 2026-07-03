package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.AircraftCategory
import com.skytycoon.app.domain.model.AircraftModel

/**
 * AircraftCategory is persisted as a String via [com.skytycoon.app.data.local.Converters].
 * All other enums in this entity are inline String columns handled by fromDomain/toDomain directly.
 */
@Entity(tableName = "aircraft_models")
data class AircraftModelEntity(
    @PrimaryKey val id: Int,
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
    val imageResName: String,
    val family: String = "",
    val variantCode: String = ""
) {
    fun toDomain(): AircraftModel = AircraftModel(
        id = id,
        manufacturer = manufacturer,
        model = model,
        category = category,
        passengerCapacity = passengerCapacity,
        rangeKm = rangeKm,
        cruiseSpeedKmh = cruiseSpeedKmh,
        fuelBurnLph = fuelBurnLph,
        purchasePriceCoins = purchasePriceCoins,
        leasingCostPerHourCoins = leasingCostPerHourCoins,
        maintenanceCostPerHourCoins = maintenanceCostPerHourCoins,
        imageResName = imageResName,
        family = family,
        variantCode = variantCode
    )

    companion object {
        fun fromDomain(aircraftModel: AircraftModel): AircraftModelEntity = AircraftModelEntity(
            id = aircraftModel.id,
            manufacturer = aircraftModel.manufacturer,
            model = aircraftModel.model,
            category = aircraftModel.category,
            passengerCapacity = aircraftModel.passengerCapacity,
            rangeKm = aircraftModel.rangeKm,
            cruiseSpeedKmh = aircraftModel.cruiseSpeedKmh,
            fuelBurnLph = aircraftModel.fuelBurnLph,
            purchasePriceCoins = aircraftModel.purchasePriceCoins,
            leasingCostPerHourCoins = aircraftModel.leasingCostPerHourCoins,
            maintenanceCostPerHourCoins = aircraftModel.maintenanceCostPerHourCoins,
            imageResName = aircraftModel.imageResName,
            family = aircraftModel.family,
            variantCode = aircraftModel.variantCode
        )
    }
}
