package com.chumakov123.gismeteoweather

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import com.chumakov123.gismeteoweather.data.storage.DataStoreProvider
import com.chumakov123.gismeteoweather.data.city.RecentCitiesRepository
import com.chumakov123.gismeteoweather.data.city.WeatherCityRepository
import com.chumakov123.gismeteoweather.data.weather.WeatherRepository
import com.chumakov123.gismeteoweather.data.storage.SettingsRepository
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val diskCacheDirectory = File(cacheDir, "image_cache")
        val imageLoader = ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(diskCacheDirectory)
                    .maxSizeBytes(5L * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        Coil.setImageLoader(imageLoader)

        val dataStore = DataStoreProvider.createWeatherCache(this)
        WeatherRepository.init(dataStore)
        SettingsRepository.init(this)
        WeatherCityRepository.init(this)
        RecentCitiesRepository.init(this)
    }
}
