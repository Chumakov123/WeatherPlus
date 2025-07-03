package com.chumakov123.weatherplus.presentation.widget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.chumakov123.weatherplus.data.weather.WeatherRepository
import com.chumakov123.weatherplus.domain.model.WeatherInfo
import com.chumakov123.weatherplus.domain.model.WeatherStateDefinition
import com.chumakov123.weatherplus.presentation.widget.WeatherGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Default).launch {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)

            val prevStates = glanceIds.associateWith { glanceId ->
                getAppWidgetState(context, WeatherStateDefinition, glanceId)
            }

            val cityToIds: Map<String, List<GlanceId>> = prevStates
                .entries
                .groupBy({ it.value.cityCode }, { it.key })

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, WeatherStateDefinition, glanceId) { old ->
                    old.copy(weatherInfo = WeatherInfo.Loading)
                }
            }
            WeatherGlanceWidget().updateAll(context)

            cityToIds.forEach { (city, idsForCity) ->
                launch {
                    val newInfo: WeatherInfo = runCatching { WeatherRepository.getWeatherInfo(city) }
                        .getOrElse { err ->
                            val last =
                                idsForCity.firstNotNullOfOrNull { prevStates[it]!!.lastAvailable }
                            last ?: WeatherInfo.Unavailable("Не удалось получить данные: ${err.message}")
                        }

                    idsForCity.forEach { glanceId ->
                        updateAppWidgetState(context, WeatherStateDefinition, glanceId) { old ->
                            old.copy(
                                weatherInfo = newInfo,
                                lastAvailable = (newInfo as? WeatherInfo.Available) ?: old.lastAvailable
                            )
                        }
                        WeatherGlanceWidget().update(context, glanceId)
                    }
                }
            }

            WeatherAlarmScheduler.scheduleNext(context)
        }
    }
}
