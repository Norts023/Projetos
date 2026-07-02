package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.Airport

@Entity(tableName = "airports")
data class AirportEntity(
    @PrimaryKey val id: Int,
    val iata: String,
    val icao: String,
    val name: String,
    val city: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val region: String,
    val hasHelipad: Boolean
) {
    fun toDomain(): Airport = Airport(
        id = id,
        iata = iata,
        icao = icao,
        name = name,
        city = city,
        country = country,
        lat = lat,
        lon = lon,
        region = region,
        hasHelipad = hasHelipad
    )

    companion object {
        fun fromDomain(airport: Airport): AirportEntity = AirportEntity(
            id = airport.id,
            iata = airport.iata,
            icao = airport.icao,
            name = airport.name,
            city = airport.city,
            country = airport.country,
            lat = airport.lat,
            lon = airport.lon,
            region = airport.region,
            hasHelipad = airport.hasHelipad
        )
    }
}
