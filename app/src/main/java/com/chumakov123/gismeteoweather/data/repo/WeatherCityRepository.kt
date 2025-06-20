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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object WeatherCityRepository {
    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = PreferenceDataStoreFactory.create {
            context.dataStoreFile("cities.preferences_pb")
        }
    }

    private object Keys {
        val CITY_LIST = stringPreferencesKey("city_list")
        val SELECTED_CITY = stringPreferencesKey("selected_city")
    }

    val citySettingsFlow: Flow<CitySettings> by lazy {
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { prefs ->
                val cities = prefs[Keys.CITY_LIST]
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
                val selected = prefs[Keys.SELECTED_CITY] ?: cities.firstOrNull().orEmpty()
                CitySettings(cityList = cities, selectedCity = selected)
            }
    }

    suspend fun addCity(cityCode: String) {
        val current = dataStore.data.first()[Keys.CITY_LIST]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toMutableList() ?: mutableListOf()

        if (cityCode !in current) {
            current.add(cityCode)
            dataStore.edit {
                it[Keys.CITY_LIST] = current.joinToString(",")
                if (current.size == 1) {
                    it[Keys.SELECTED_CITY] = cityCode
                }
            }
        }
    }

    suspend fun removeCity(cityCode: String) {
        dataStore.edit { prefs ->
            val currentList = prefs[Keys.CITY_LIST]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toMutableList() ?: mutableListOf()

            if (cityCode in currentList) {
                currentList.remove(cityCode)
                prefs[Keys.CITY_LIST] = currentList.joinToString(",")

                val currentSelected = prefs[Keys.SELECTED_CITY]

                if (currentSelected == cityCode) {
                    prefs[Keys.SELECTED_CITY] = currentList.firstOrNull().orEmpty()
                }
            }
        }
    }

    suspend fun updateOrder(newOrder: List<String>) {
        dataStore.edit { prefs ->
            val filtered = newOrder.filter { it.isNotBlank() }

            if (filtered.isNotEmpty()) {
                prefs[Keys.CITY_LIST] = filtered.joinToString(",")
                val currentSelected = prefs[Keys.SELECTED_CITY]
                if (currentSelected !in filtered) {
                    prefs[Keys.SELECTED_CITY] = filtered.firstOrNull().orEmpty()
                }
            } else {
                prefs.remove(Keys.CITY_LIST)
                prefs.remove(Keys.SELECTED_CITY)
            }
        }
    }

    suspend fun updateSelectedCity(cityCode: String) {
        dataStore.edit { prefs ->
            val currentList = prefs[Keys.CITY_LIST]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toMutableList() ?: mutableListOf()

            if (cityCode in currentList) {
                prefs[Keys.SELECTED_CITY] = cityCode
            } else {
                currentList.add(cityCode)
                prefs[Keys.CITY_LIST] = currentList.joinToString(",")
                prefs[Keys.SELECTED_CITY] = cityCode
            }
        }
    }
}

data class CitySettings(
    val cityList: List<String>,
    val selectedCity: String
)