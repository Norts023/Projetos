package com.skytycoon.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.skytycoon.app.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.mapSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "map_settings")

@Singleton
class MapSettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val MAP_TYPE = stringPreferencesKey("map_type")
        val SHOW_FLIGHTS = stringPreferencesKey("show_flights")
        val SHOW_AIRPORTS = stringPreferencesKey("show_airports")
        val MARKER_SIZE = intPreferencesKey("marker_size")
        val LABEL_MODE = stringPreferencesKey("label_mode")
        val LABEL_SIZE = intPreferencesKey("label_size")
    }

    val settings: Flow<MapSettings> = context.mapSettingsDataStore.data.map { prefs ->
        MapSettings(
            mapType = prefs[Keys.MAP_TYPE]?.let { runCatching { MapType.valueOf(it) }.getOrNull() } ?: MapType.STANDARD,
            showFlights = prefs[Keys.SHOW_FLIGHTS]?.let { runCatching { ShowFlights.valueOf(it) }.getOrNull() } ?: ShowFlights.ALL,
            showAirports = prefs[Keys.SHOW_AIRPORTS]?.let { runCatching { ShowAirports.valueOf(it) }.getOrNull() } ?: ShowAirports.ALL,
            markerSizePercent = prefs[Keys.MARKER_SIZE] ?: 100,
            labelMode = prefs[Keys.LABEL_MODE]?.let { runCatching { LabelMode.valueOf(it) }.getOrNull() } ?: LabelMode.TEXT,
            labelSizePercent = prefs[Keys.LABEL_SIZE] ?: 100
        )
    }

    suspend fun update(settings: MapSettings) {
        context.mapSettingsDataStore.edit { prefs ->
            prefs[Keys.MAP_TYPE] = settings.mapType.name
            prefs[Keys.SHOW_FLIGHTS] = settings.showFlights.name
            prefs[Keys.SHOW_AIRPORTS] = settings.showAirports.name
            prefs[Keys.MARKER_SIZE] = settings.markerSizePercent
            prefs[Keys.LABEL_MODE] = settings.labelMode.name
            prefs[Keys.LABEL_SIZE] = settings.labelSizePercent
        }
    }
}
