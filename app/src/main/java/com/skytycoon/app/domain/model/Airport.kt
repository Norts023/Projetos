package com.skytycoon.app.domain.model

import kotlin.math.*

data class Airport(
    val id: Int,
    val iata: String,
    val icao: String,
    val name: String,
    val city: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val region: String,
    val hasHelipad: Boolean = false
) {
    fun distanceKmTo(other: Airport): Double {
        val r = 6371.0
        val dLat = Math.toRadians(other.lat - lat)
        val dLon = Math.toRadians(other.lon - lon)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(other.lat)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
