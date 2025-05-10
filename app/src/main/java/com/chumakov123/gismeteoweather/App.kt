package com.chumakov123.gismeteoweather

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
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
    }
}