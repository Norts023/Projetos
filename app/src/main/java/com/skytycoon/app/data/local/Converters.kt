package com.skytycoon.app.data.local

import androidx.room.TypeConverter
import com.skytycoon.app.domain.model.AircraftCategory

class Converters {

    @TypeConverter
    fun fromAircraftCategory(value: AircraftCategory): String = value.name

    @TypeConverter
    fun toAircraftCategory(value: String): AircraftCategory =
        AircraftCategory.entries.firstOrNull { it.name == value } ?: AircraftCategory.AIRLINER
}
