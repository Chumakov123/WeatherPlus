package com.chumakov123.gismeteoweather.worker

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.chumakov123.gismeteoweather.widget.WeatherGlanceWidget
import com.chumakov123.gismeteoweather.widget.WeatherInfo
import com.chumakov123.gismeteoweather.widget.WeatherInfoStateDefinition
import com.chumakov123.gismeteoweather.widget.WeatherRepo
import java.time.Duration

class WeatherWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {

        private val uniqueWorkName = WeatherWorker::class.java.simpleName

        /**
         * Enqueues a new worker to refresh weather data only if not enqueued already
         *
         * Note: if you would like to have different workers per widget instance you could provide
         * the unique name based on some criteria (e.g selected weather location).
         *
         * @param force set to true to replace any ongoing work and expedite the request
         */
        fun enqueue(context: Context, force: Boolean = false, switchMode: Boolean = false) {
            val manager = WorkManager.getInstance(context)
            val inputData = workDataOf("switchMode" to switchMode)

            val requestBuilder = PeriodicWorkRequestBuilder<WeatherWorker>(
                Duration.ofMinutes(30)
            ).setInputData(inputData)

            val workPolicy = if (force) {
                ExistingPeriodicWorkPolicy.REPLACE
            } else {
                ExistingPeriodicWorkPolicy.KEEP
            }

            manager.enqueueUniquePeriodicWork(
                uniqueWorkName,
                workPolicy,
                requestBuilder.build()
            )
        }

        /**
         * Cancel any ongoing worker
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)

        val switchMode = inputData.getBoolean("switchMode", false)

        return try {
            setWidgetState(glanceIds, WeatherInfo.Loading)

            val weatherInfo =// if (switchMode) {
                //TODO

            //} else {
                WeatherRepo.getWeatherInfo()
            //}

            setWidgetState(glanceIds, weatherInfo)
            Result.success()
        } catch (e: Exception) {
            setWidgetState(glanceIds, WeatherInfo.Unavailable(e.message.orEmpty()))
            if (runAttemptCount < 10) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Update the state of all widgets and then force update UI
     */
    private suspend fun setWidgetState(glanceIds: List<GlanceId>, newState: WeatherInfo) {
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = WeatherInfoStateDefinition,
                glanceId = glanceId,
                updateState = { newState }
            )
        }
        WeatherGlanceWidget().updateAll(context)
    }
}