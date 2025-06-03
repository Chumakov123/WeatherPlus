package com.chumakov123.gismeteoweather.data.provider

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.chumakov123.gismeteoweather.WeatherCacheOuterClass
import com.chumakov123.gismeteoweather.WeatherCacheOuterClass.WeatherCache.getDefaultInstance
import java.io.InputStream
import java.io.OutputStream

object WeatherCacheSerializer : Serializer<WeatherCacheOuterClass.WeatherCache> {
    override val defaultValue: WeatherCacheOuterClass.WeatherCache = getDefaultInstance()

    override suspend fun readFrom(input: InputStream): WeatherCacheOuterClass.WeatherCache =
        WeatherCacheOuterClass.WeatherCache.parseFrom(input)

    override suspend fun writeTo(t: WeatherCacheOuterClass.WeatherCache, output: OutputStream) {
        t.writeTo(output)
    }
}

object DataStoreProvider {
    private const val WEATHER_CACHE_FILE = "weather_cache.pb"
    private const val SETTINGS_FILE = "weather_settings.preferences_pb"

    fun createWeatherCache(context: Context): DataStore<WeatherCacheOuterClass.WeatherCache> {
        return DataStoreFactory.create(
            serializer = WeatherCacheSerializer,
            produceFile = { context.dataStoreFile(WEATHER_CACHE_FILE) }
        )
    }

    fun createSettingsStore(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(SETTINGS_FILE) }
        )
    }
}