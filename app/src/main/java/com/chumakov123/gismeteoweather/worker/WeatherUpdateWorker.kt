package com.chumakov123.gismeteoweather.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.gismeteoweather.widget.WeatherWidget
import com.chumakov123.gismeteoweather.widget.updateWeather

class WeatherUpdateWorker(
    private val context: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        updateWeather(context)
        WeatherWidget().updateAll(context)
        return Result.success()
    }
}