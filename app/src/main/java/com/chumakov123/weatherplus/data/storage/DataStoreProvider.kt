package com.chumakov123.weatherplus.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.chumakov123.weatherplus.WeatherCacheOuterClass
import com.chumakov123.weatherplus.WeatherCacheOuterClass.WeatherCache.getDefaultInstance
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

    fun createWeatherCache(context: Context): DataStore<WeatherCacheOuterClass.WeatherCache> {
        return DataStoreFactory.create(
            serializer = WeatherCacheSerializer,
            produceFile = { context.dataStoreFile(WEATHER_CACHE_FILE) }
        )
    }
}
