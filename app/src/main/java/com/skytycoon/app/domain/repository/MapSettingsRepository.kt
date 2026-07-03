package com.skytycoon.app.domain.repository

import com.skytycoon.app.domain.model.MapSettings
import kotlinx.coroutines.flow.Flow

interface MapSettingsRepository {
    fun getSettings(): Flow<MapSettings>
    suspend fun update(settings: MapSettings)
}
