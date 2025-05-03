package com.chumakov123.gismeteoweather.presentation.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
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
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.model.WidgetState
import com.chumakov123.gismeteoweather.domain.util.Utils
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherAlarmScheduler
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherUpdateReceiver
import com.chumakov123.gismeteoweather.presentation.ui.components.widget.CurrentWeather
import com.chumakov123.gismeteoweather.presentation.ui.components.widget.DailyForecast
import com.chumakov123.gismeteoweather.presentation.ui.components.widget.HourlyForecast
import com.chumakov123.gismeteoweather.presentation.ui.components.widget.WeatherIcon
import com.chumakov123.gismeteoweather.presentation.ui.components.widget.WidgetHeader
import com.chumakov123.gismeteoweather.startWidgetConfigure

class WeatherGlanceWidget : GlanceAppWidget() {

    companion object {
        private val thinMode = DpSize(120.dp, 120.dp)
        private val smallMode = DpSize(184.dp, 184.dp)
        private val mediumMode = DpSize(260.dp, 200.dp)
        private val largeMode = DpSize(260.dp, 280.dp)
    }

    // Override the state definition to use our custom one using Kotlin serialization
    override val stateDefinition = WeatherStateDefinition

    // Define the supported sizes for this widget.
    // The system will decide which one fits better based on the available space
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(thinMode, smallMode, mediumMode, largeMode)
    )



    override suspend fun provideGlance(context: Context, id: GlanceId) {
        //saveHtmlToFile(context)
        provideContent {
            // Get the stored stated based on our custom state definition.
            val widgetState = currentState<WidgetState>()
            // It will be one of the provided ones
            val size = LocalSize.current
            GlanceTheme {
                val transparency = widgetState.appearance.backgroundTransparencyPercent
                when (widgetState.weatherInfo) {
                    WeatherInfo.Loading -> {
                        if (widgetState.lastAvailable != null) {
                            WeatherMedium(widgetState.lastAvailable, widgetState.forecastMode, widgetState.appearance, isLoading = true)
                        } else {
                            AppWidgetBox(
                                contentAlignment = Alignment.Center,
                                transparencyPercent = transparency
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                    }
                    is WeatherInfo.Available -> {
                        // Based on the size render different UI
                        WeatherMedium(
                            widgetState.weatherInfo,
                            widgetState.forecastMode,
                            widgetState.appearance,
                        )
//                        when (size) {
//                            thinMode -> WeatherThin(widgetState.weatherInfo)
//                            smallMode -> WeatherSmall(widgetState.weatherInfo)
//                            mediumMode -> WeatherMedium(widgetState.weatherInfo, widgetState.forecastMode)
//                            largeMode -> WeatherLarge(widgetState.weatherInfo, widgetState.forecastMode)
//                        }
                    }
                    is WeatherInfo.Unavailable -> {
                        AppWidgetColumn(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            transparencyPercent = transparency
                        ) {
                            Text(
                                text = widgetState.weatherInfo.message,
                                style = TextStyle(
                                    color = ColorProvider(Color.White)
                                )
                            )
                            Button("Обновить", actionRunCallback<UpdateWeatherAction>())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherThin(weatherInfo: WeatherInfo.Available) {
    AppWidgetColumn(GlanceModifier.clickable(actionRunCallback<UpdateWeatherAction>())) { //TODO вместо обновления виджета, открывать приложение по нажатию
        CurrentWeather(
            weatherInfo,
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}

@Composable
fun WeatherSmall(weatherInfo: WeatherInfo.Available) {
    AppWidgetColumn(GlanceModifier.clickable(actionRunCallback<UpdateWeatherAction>())) {
        Row(
            modifier = GlanceModifier.wrapContentHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            CurrentWeather(weatherInfo, modifier = GlanceModifier.fillMaxSize().defaultWeight())
            WeatherIcon(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
            //PlaceWeather(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
        }
    }
}

@Composable
fun WeatherMedium(
    weatherInfo: WeatherInfo.Available,
    forecastMode: ForecastMode,
    appearance: WidgetAppearance,
    isLoading: Boolean = false
) {
    AppWidgetColumn(GlanceModifier.clickable(actionRunCallback<UpdateWeatherAction>()), transparencyPercent = appearance.backgroundTransparencyPercent) {
        WidgetHeader(
            placeName      = weatherInfo.placeName,
            updateTimeText = if (appearance.showUpdateTime) Utils.formatDateTime(weatherInfo.updateTime) else null,
            isLoading      = isLoading
        )
        CurrentWeather(weatherInfo, GlanceModifier.fillMaxWidth())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (forecastMode == ForecastMode.ByHours) {
                HourlyForecast(weatherInfo, modifier = GlanceModifier.fillMaxSize(), appearance = appearance)
            } else {
                DailyForecast(weatherInfo, modifier = GlanceModifier.fillMaxSize(), appearance = appearance)
            }
        }
    }
}

//@Composable
//fun WeatherLarge(weatherInfo: WeatherInfo.Available, forecastMode: ForecastMode) {
//    AppWidgetColumn(GlanceModifier.clickable(actionRunCallback<UpdateWeatherAction>())) {
//        Row(
//            modifier = GlanceModifier.wrapContentHeight().fillMaxWidth(),
//            horizontalAlignment = Alignment.Start
//        ) {
//            WeatherIcon(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
//            //PlaceWeather(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
//        }
//        Row(
//            modifier = GlanceModifier.wrapContentHeight().fillMaxWidth(),
//            horizontalAlignment = Alignment.Start
//        ) {
//            CurrentWeather(
//                weatherInfo,
//                modifier = GlanceModifier.wrapContentHeight()
//            )
//            HourlyForecast(weatherInfo, modifier = GlanceModifier.fillMaxWidth())
//        }
//        Spacer(GlanceModifier.size(8.dp))
//        //DailyForecast(weatherInfo)
//    }
//}

/**
 * Force update the weather info after user click
 */
class UpdateWeatherAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        context.sendBroadcast(Intent(context, WeatherUpdateReceiver::class.java))
        WeatherAlarmScheduler.scheduleNext(context)
    }
}

class SwitchForecastModeAction: ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(
            context = context,
            definition = WeatherStateDefinition,
            glanceId = glanceId
        ) { oldState ->
            oldState.copy(forecastMode = if (oldState.forecastMode == ForecastMode.ByHours)
                ForecastMode.ByDays
            else
                ForecastMode.ByHours
            )
        }
        WeatherGlanceWidget().updateAll(context)
    }
}

class OpenConfigCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val manager = GlanceAppWidgetManager(context)
        val appWidgetId = manager.getAppWidgetId(glanceId)

        context.startWidgetConfigure(appWidgetId)
    }
}