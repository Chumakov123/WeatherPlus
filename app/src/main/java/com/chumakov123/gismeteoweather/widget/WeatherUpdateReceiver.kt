package com.chumakov123.gismeteoweather.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WeatherUpdateReceiver", "onReceive")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val glanceIds = GlanceAppWidgetManager(context)
                    .getGlanceIds(WeatherGlanceWidget::class.java)

                updateWeatherInfoState(context, glanceIds, WeatherInfo.Loading)

                val newWeatherInfo = WeatherRepo.getWeatherInfo("rostov-na-donu-5110")
                updateWeatherInfoState(context, glanceIds, newWeatherInfo)
            } catch (e: Exception) {
                Log.e("WeatherUpdateReceiver", "Ошибка обновления: ${e.message}", e)
                val glanceIds = GlanceAppWidgetManager(context)
                    .getGlanceIds(WeatherGlanceWidget::class.java)
                updateWeatherInfoState(context, glanceIds, WeatherInfo.Unavailable(e.message.orEmpty()))
            }
        }

        WeatherAlarmScheduler.scheduleNext(context)
    }

    private suspend fun updateWeatherInfoState(
        context: Context,
        glanceIds: List<GlanceId>,
        newWeatherInfo: WeatherInfo
    ) {
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = WeatherStateDefinition,
                glanceId = glanceId
            ) { currentState ->
                currentState.copy(weatherInfo = newWeatherInfo)
            }
        }
        WeatherGlanceWidget().updateAll(context)
    }
}