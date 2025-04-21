package com.chumakov123.gismeteoweather.worker

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.*
import com.chumakov123.gismeteoweather.widget.ForecastMode
import com.chumakov123.gismeteoweather.widget.WeatherGlanceWidget
import com.chumakov123.gismeteoweather.widget.WeatherInfo
import com.chumakov123.gismeteoweather.widget.WeatherStateDefinition
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
         * the unique name based on some criteria (e.g. selected weather location).
         *
         * @param force set to true to replace any ongoing work and expedite the request
         */
        fun enqueue(context: Context, force: Boolean = false, switchMode: Boolean = false) {
            val manager = WorkManager.getInstance(context)
            val inputData = workDataOf("switchMode" to switchMode)

            val requestBuilder = PeriodicWorkRequestBuilder<WeatherWorker>(
                Duration.ofMinutes(30)
            )

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
        val glanceManager = GlanceAppWidgetManager(context)
        val glanceIds = glanceManager.getGlanceIds(WeatherGlanceWidget::class.java)
        val switchMode = inputData.getBoolean("switchMode", false)

        return try {
            updateWeatherInfo(glanceIds, WeatherInfo.Loading)

            if (switchMode) {
                updateForecastMode(glanceIds)
            } else {
                val newWeatherInfo = WeatherRepo.getWeatherInfo("rostov-na-donu-5110")
                updateWeatherInfo(glanceIds, newWeatherInfo)
                println("Успех ${(newWeatherInfo as WeatherInfo.Available).localTime}")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("WeatherWorker","Ошибка типа: ${e::class.qualifiedName}, сообщение: ${e.message}")
            e.printStackTrace()
            updateWeatherInfo(glanceIds, WeatherInfo.Unavailable(e.message.orEmpty()))
            if (runAttemptCount < 10) Result.retry() else Result.failure()
        }
    }

    /**
     * Обновляет информацию о погоде во всех виджетах, оставляя режим неизменным.
     */
    private suspend fun updateWeatherInfo(glanceIds: List<GlanceId>, newWeatherInfo: WeatherInfo) {
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = WeatherStateDefinition,
                glanceId = glanceId,
                updateState = { currentState -> currentState.copy(weatherInfo = newWeatherInfo) }
            )
        }
        println("updateWeatherInfo")
        println(newWeatherInfo.toString())
        WeatherGlanceWidget().updateAll(context)
    }

    /**
     * Переключает режим прогноза (ByHours -> ByDays или наоборот) во всех виджетах, сохраняя погоду без изменений.
     */
    private suspend fun updateForecastMode(glanceIds: List<GlanceId>) {
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = WeatherStateDefinition,
                glanceId = glanceId,
                updateState = { currentState ->
                    val newMode = when (currentState.forecastMode) {
                        ForecastMode.ByHours -> ForecastMode.ByDays
                        ForecastMode.ByDays  -> ForecastMode.ByHours
                    }
                    currentState.copy(forecastMode = newMode)
                }
            )
        }
        println("updateForecastMode")
        WeatherGlanceWidget().updateAll(context)
    }
}