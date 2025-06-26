package com.chumakov123.gismeteoweather.presentation.widget.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.presentation.widget.WeatherGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Handle system events for AppWidgets with the provided GlanceAppWidget instance.
 *
 * Use this class to handle widget lifecycle specific events like onEnable/Disable.
 */
class WeatherGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WeatherGlanceWidget()

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        Log.d("WeatherGlanceWidgetReceiver", "onAppWidgetOptionsChanged")
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        val widthDp = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

        val columnMinWidthDp = 56
        val columns = max(1, widthDp / columnMinWidthDp)

        val heightDp = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val rowMinHeightDp = 65
        val rows = max(1, heightDp / rowMinHeightDp)

        CoroutineScope(Dispatchers.Default).launch {
            val glanceManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceManager.getGlanceIds(WeatherGlanceWidget::class.java)

            val glanceId = glanceIds.firstOrNull {
                glanceManager.getAppWidgetId(it) == appWidgetId
            } ?: return@launch

            updateAppWidgetState(context, WeatherStateDefinition, glanceId) { old ->
                old.copy(
                    forecastColumns = columns,
                    forecastRows = rows
                )
            }

            WeatherGlanceWidget().update(context, glanceId)
        }
    }

    /**
     * Called when the first instance of the widget is placed. Since all instances share the same
     * state, we don't need to enqueue a new one for subsequent instances.
     *
     * Note: if you would need to load different data for each instance you could enqueue onUpdate
     * method instead. It's safe to call multiple times because of the unique work + KEEP policy
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // context.sendBroadcast(Intent(context, WeatherUpdateReceiver::class.java))
        WeatherAlarmScheduler.scheduleNext(context)
    }

    /**
     * Called when the last instance of this widget is removed.
     * Make sure to cancel all ongoing workers when user remove all widget instances
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WeatherAlarmScheduler.cancel(context)
    }
}
