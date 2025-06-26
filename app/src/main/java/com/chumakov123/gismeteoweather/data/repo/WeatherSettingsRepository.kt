package com.chumakov123.gismeteoweather.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.chumakov123.gismeteoweather.domain.model.WeatherDisplaySettings
import com.chumakov123.gismeteoweather.domain.model.WeatherRowType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

object WeatherSettingsRepository {
    private lateinit var dataStore: DataStore<Preferences>

    private object Keys {
        val ENABLED_ROWS = stringSetPreferencesKey("enabled_rows")
        val ROW_ORDER = stringPreferencesKey("row_order")
    }

    fun init(context: Context) {
        dataStore = PreferenceDataStoreFactory.create {
            context.dataStoreFile("settings.preferences_pb")
        }
    }

    val settingsFlow: Flow<WeatherDisplaySettings> by lazy {
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                val enabled = prefs[Keys.ENABLED_ROWS]
                    ?.mapNotNull { runCatching { WeatherRowType.valueOf(it) }.getOrNull() }
                    ?.toSet()
                    ?: WeatherDisplaySettings().enabledRows

                val order = prefs[Keys.ROW_ORDER]
                    ?.split(",")
                    ?.mapNotNull { runCatching { WeatherRowType.valueOf(it) }.getOrNull() }
                    ?: WeatherDisplaySettings().rowOrder
                WeatherDisplaySettings(enabledRows = enabled, rowOrder = order)
            }
    }

    suspend fun updateSettings(settings: WeatherDisplaySettings) {
        dataStore.edit { prefs ->
            prefs[Keys.ENABLED_ROWS] = settings.enabledRows.map { it.name }.toSet()
            prefs[Keys.ROW_ORDER] = settings.rowOrder.joinToString(",") { it.name }
        }
    }
}
