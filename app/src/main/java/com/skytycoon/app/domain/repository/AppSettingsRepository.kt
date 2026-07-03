package com.skytycoon.app.domain.repository

import com.skytycoon.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun update(settings: AppSettings)
}
