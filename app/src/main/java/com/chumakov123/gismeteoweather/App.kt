package com.chumakov123.gismeteoweather

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import com.chumakov123.gismeteoweather.data.repo.WeatherRepo
import com.chumakov123.gismeteoweather.data.local.DataStoreProvider
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
            .diskCachePolicy(  CachePolicy.ENABLED )
            .build()

        Coil.setImageLoader(imageLoader)

        val dataStore = DataStoreProvider.create(this)
        WeatherRepo.init(dataStore)
    }
}