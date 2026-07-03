package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.MapSettingsDataStore
import com.skytycoon.app.domain.model.MapSettings
import com.skytycoon.app.domain.repository.MapSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapSettingsRepositoryImpl @Inject constructor(
    private val dataStore: MapSettingsDataStore
) : MapSettingsRepository {

    override fun getSettings(): Flow<MapSettings> = dataStore.settings

    override suspend fun update(settings: MapSettings) = dataStore.update(settings)
}
