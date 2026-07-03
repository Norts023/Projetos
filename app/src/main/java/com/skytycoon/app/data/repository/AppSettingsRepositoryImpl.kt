package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.AppSettingsDataStore
import com.skytycoon.app.domain.model.AppSettings
import com.skytycoon.app.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    private val dataStore: AppSettingsDataStore
) : AppSettingsRepository {

    override fun getSettings(): Flow<AppSettings> = dataStore.settings

    override suspend fun update(settings: AppSettings) = dataStore.update(settings)
}
