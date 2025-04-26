package com.chumakov123.gismeteoweather.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.chumakov123.gismeteoweather.data.repo.WeatherRepo
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.domain.model.WidgetState
import com.chumakov123.gismeteoweather.presentation.ui.WeatherGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Default).launch {
            val manager   = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)

            val prevStates: Map<GlanceId, WidgetState> = glanceIds.associateWith { glanceId ->
                getAppWidgetState(context, WeatherStateDefinition, glanceId)
            }

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, WeatherStateDefinition, glanceId) { old ->
                    old.copy(weatherInfo = WeatherInfo.Loading)
                }
            }
            WeatherGlanceWidget().updateAll(context)

            val uniqueCities: Set<String> = prevStates.values
                .map { it.cityCode }
                .toSet()

            val fetched: Map<String, WeatherInfo> = uniqueCities.associateWith { city ->
                runCatching { WeatherRepo.getWeatherInfo(city) }
                    .getOrElse {
                        prevStates.values
                            .first { it.cityCode == city }
                            .weatherInfo
                    }
            }
            glanceIds.forEach { glanceId ->
                val oldState = prevStates[glanceId]!!
                val newInfo  = fetched[oldState.cityCode]!!
                updateAppWidgetState(context, WeatherStateDefinition, glanceId) { old ->
                    old.copy(weatherInfo = newInfo)
                }
            }
            WeatherGlanceWidget().updateAll(context)
            WeatherAlarmScheduler.scheduleNext(context)
        }
    }
}