package com.skytycoon.app.domain.model

data class MapSettings(
    val mapType: MapType = MapType.STANDARD,
    val showFlights: ShowFlights = ShowFlights.ALL,
    val showAirports: ShowAirports = ShowAirports.ALL,
    val markerSizePercent: Int = 100,
    val labelMode: LabelMode = LabelMode.TEXT,
    val labelSizePercent: Int = 100
)
