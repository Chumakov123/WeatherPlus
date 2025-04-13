package com.chumakov123.gismeteoweather.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import com.chumakov123.gismeteoweather.prefs.WeatherPrefs
import com.chumakov123.gismeteoweather.widget.WeatherWidget
import com.chumakov123.gismeteoweather.widget.updateWeather

class RefreshCallback : androidx.glance.appwidget.action.ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {

        updateWeather(context)
        WeatherWidget().update(context, glanceId)
    }
}