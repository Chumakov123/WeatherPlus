package com.chumakov123.weatherplus.presentation.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.weatherplus.MainActivity
import com.chumakov123.weatherplus.domain.model.ForecastMode
import com.chumakov123.weatherplus.domain.model.WeatherInfo
import com.chumakov123.weatherplus.domain.model.WeatherStateDefinition
import com.chumakov123.weatherplus.domain.model.WidgetAppearance
import com.chumakov123.weatherplus.domain.model.WidgetState
import com.chumakov123.weatherplus.domain.util.Utils
import com.chumakov123.weatherplus.presentation.features.widgetconfigure.startWidgetConfigure
import com.chumakov123.weatherplus.presentation.widget.components.CurrentWeather
import com.chumakov123.weatherplus.presentation.widget.components.DailyForecast
import com.chumakov123.weatherplus.presentation.widget.components.HourlyForecast
import com.chumakov123.weatherplus.presentation.widget.components.WidgetHeader
import com.chumakov123.weatherplus.presentation.widget.receiver.WeatherAlarmScheduler
import com.chumakov123.weatherplus.presentation.widget.receiver.WeatherUpdateReceiver

class WeatherGlanceWidget : GlanceAppWidget() {
    // Override the state definition to use our custom one using Kotlin serialization
    override val stateDefinition = WeatherStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            // Get the stored stated based on our custom state definition.
            val widgetState = currentState<WidgetState>()
            GlanceTheme {
                val transparency = widgetState.appearance.backgroundTransparencyPercent
                when (widgetState.weatherInfo) {
                    WeatherInfo.Loading -> {
                        if (widgetState.lastAvailable != null) {
                            WeatherMedium(
                                weatherInfo = widgetState.lastAvailable,
                                forecastMode = widgetState.forecastMode,
                                forecastColumns = widgetState.forecastColumns,
                                forecastRows = widgetState.forecastRows,
                                appearance = widgetState.appearance,
                                isLoading = true,
                            )
                        } else {
                            AppWidgetBox(
                                contentAlignment = Alignment.Center,
                                transparencyPercent = transparency,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is WeatherInfo.Available -> {
                        WeatherMedium(
                            weatherInfo = widgetState.weatherInfo,
                            forecastMode = widgetState.forecastMode,
                            forecastColumns = widgetState.forecastColumns,
                            forecastRows = widgetState.forecastRows,
                            appearance = widgetState.appearance,
                        )
                    }
                    is WeatherInfo.Unavailable -> {
                        AppWidgetColumn(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            transparencyPercent = transparency,
                        ) {
                            Text(
                                text = "Погода Плюс",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 20.sp
                                ),
                            )
//                            Spacer(modifier = GlanceModifier.height(8.dp))
//                            Text(
//                                text = widgetState.weatherInfo.message,
//                                style =
//                                    TextStyle(
//                                        color = ColorProvider(Color.White),
//                                    ),
//                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Button("Обновить", actionRunCallback<UpdateWeatherAction>())
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Button("Настройки", actionRunCallback<OpenConfigCallback>())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMedium(
    weatherInfo: WeatherInfo.Available,
    forecastMode: ForecastMode,
    forecastColumns: Int,
    forecastRows: Int,
    appearance: WidgetAppearance,
    isLoading: Boolean = false,
) {
    val rows = if (appearance.showCurrentWeather) forecastRows - 1 else forecastRows
    val cityCodeKey = ActionParameters.Key<String>("cityCode")
    val parameters = actionParametersOf(cityCodeKey to weatherInfo.placeCode)
    AppWidgetColumn(
        modifier = GlanceModifier.clickable(actionRunCallback<OpenCityPreviewAction>(parameters)),
        transparencyPercent = appearance.backgroundTransparencyPercent,
    ) {
        WidgetHeader(
            placeName = weatherInfo.placeName,
            updateTimeText = if (appearance.showUpdateTime) Utils.formatDateTime(weatherInfo.updateTime) else null,
            isLoading = isLoading,
            forecastColumns = forecastColumns,
            appearance = appearance,
        )
        if (appearance.showCurrentWeather) {
            CurrentWeather(
                weatherInfo,
                GlanceModifier.fillMaxWidth(),
                forecastColumns = forecastColumns,
                forecastRows = forecastRows,
                appearance = appearance
            )
        }
        if (rows >= 1) {
            if (rows >= 2) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (forecastMode == ForecastMode.ByHours) {
                        HourlyForecast(
                            weatherInfo,
                            modifier = GlanceModifier.fillMaxSize(),
                            appearance = appearance,
                            forecastColumns = forecastColumns,
                        )
                    } else {
                        DailyForecast(
                            weatherInfo,
                            modifier = GlanceModifier.fillMaxSize(),
                            appearance = appearance,
                            forecastColumns = forecastColumns,
                        )
                    }
                }
                Column(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (forecastMode == ForecastMode.ByHours) {
                        DailyForecast(
                            weatherInfo,
                            modifier = GlanceModifier.fillMaxSize(),
                            appearance = appearance,
                            forecastColumns = forecastColumns,
                        )
                    } else {
                        HourlyForecast(
                            weatherInfo,
                            modifier = GlanceModifier.fillMaxSize(),
                            appearance = appearance,
                            forecastColumns = forecastColumns,
                        )
                    }
                }
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                ) {
                    if (forecastMode == ForecastMode.ByHours) {
                        HourlyForecast(
                            weatherInfo,
                            modifier = GlanceModifier.fillMaxSize(),
                            appearance = appearance,
                            forecastColumns = forecastColumns,
                        )
                    } else {
                        DailyForecast(
                            weatherInfo,
                            modifier = GlanceModifier.fillMaxSize(),
                            appearance = appearance,
                            forecastColumns = forecastColumns,
                        )
                    }
                }
            }
        }
    }
}

class OpenCityPreviewAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val cityCodeKey = ActionParameters.Key<String>("cityCode")
        val cityCode = parameters[cityCodeKey] ?: return

        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = Uri.parse("weatherapp://weather_preview/$cityCode")
            }

        context.startActivity(intent)
    }
}

/**
 * Force update the weather info after user click
 */
class UpdateWeatherAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        context.sendBroadcast(Intent(context, WeatherUpdateReceiver::class.java))
        WeatherAlarmScheduler.scheduleNext(context)
    }
}

class SwitchForecastModeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(
            context = context,
            definition = WeatherStateDefinition,
            glanceId = glanceId,
        ) { oldState ->
            oldState.copy(
                forecastMode =
                    if (oldState.forecastMode == ForecastMode.ByHours) {
                        ForecastMode.ByDays
                    } else {
                        ForecastMode.ByHours
                    },
            )
        }
        WeatherGlanceWidget().updateAll(context)
    }
}

class OpenConfigCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val manager = GlanceAppWidgetManager(context)
        val appWidgetId = manager.getAppWidgetId(glanceId)

        context.startWidgetConfigure(appWidgetId)
    }
}
