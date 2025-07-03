package com.chumakov123.weatherplus

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import com.chumakov123.weatherplus.data.city.RecentCitiesRepository
import com.chumakov123.weatherplus.data.city.WeatherCityRepository
import com.chumakov123.weatherplus.data.storage.DataStoreProvider
import com.chumakov123.weatherplus.data.storage.SettingsRepository
import com.chumakov123.weatherplus.data.weather.WeatherRepository

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val imageLoader = ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(5L * 1024 * 1024)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()

        Coil.setImageLoader(imageLoader)

        val dataStore = DataStoreProvider.createWeatherCache(this)
        WeatherRepository.init(dataStore)
        SettingsRepository.init(this)
        WeatherCityRepository.init(this)
        RecentCitiesRepository.init(this)
    }
}
