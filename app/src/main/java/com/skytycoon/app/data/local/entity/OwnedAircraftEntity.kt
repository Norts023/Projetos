package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.domain.model.OwnedAircraft

@Entity(tableName = "owned_aircraft")
data class OwnedAircraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelId: Int,
    val registrationCode: String,
    val acquisitionType: String,
    val condition: Int,
    val totalFlightHours: Double,
    val nextMaintenanceHours: Double
) {
    /**
     * Requires the resolved [AircraftModel] because the entity stores only [modelId].
     * The repository is responsible for loading and supplying the model.
     */
    fun toDomain(model: AircraftModel): OwnedAircraft = OwnedAircraft(
        id = id,
        modelId = modelId,
        model = model,
        registrationCode = registrationCode,
        acquisitionType = AcquisitionType.valueOf(acquisitionType),
        condition = condition,
        totalFlightHours = totalFlightHours,
        nextMaintenanceHours = nextMaintenanceHours
    )

    companion object {
        fun fromDomain(ownedAircraft: OwnedAircraft): OwnedAircraftEntity = OwnedAircraftEntity(
            id = ownedAircraft.id,
            modelId = ownedAircraft.modelId,
            registrationCode = ownedAircraft.registrationCode,
            acquisitionType = ownedAircraft.acquisitionType.name,
            condition = ownedAircraft.condition,
            totalFlightHours = ownedAircraft.totalFlightHours,
            nextMaintenanceHours = ownedAircraft.nextMaintenanceHours
        )
    }
}
