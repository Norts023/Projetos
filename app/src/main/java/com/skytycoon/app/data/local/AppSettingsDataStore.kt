package com.skytycoon.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.skytycoon.app.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val DEFAULT_AUTO_TIME = booleanPreferencesKey("default_auto_time")
        val DEFAULT_SPEED = intPreferencesKey("default_speed")
    }

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map { prefs ->
        AppSettings(
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "auto",
            defaultAutoTime = prefs[Keys.DEFAULT_AUTO_TIME] ?: false,
            defaultSpeedMultiplier = prefs[Keys.DEFAULT_SPEED] ?: 1
        )
    }

    suspend fun update(settings: AppSettings) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = settings.languageCode
            prefs[Keys.DEFAULT_AUTO_TIME] = settings.defaultAutoTime
            prefs[Keys.DEFAULT_SPEED] = settings.defaultSpeedMultiplier
        }
    }
}
