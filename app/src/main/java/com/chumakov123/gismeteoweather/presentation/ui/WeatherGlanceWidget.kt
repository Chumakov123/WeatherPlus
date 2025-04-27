package com.chumakov123.gismeteoweather.presentation.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.util.TemperatureGradation.interpolateTemperatureColor
import com.chumakov123.gismeteoweather.domain.util.Utils
import com.chumakov123.gismeteoweather.domain.util.Utils.plusCalendarDays
import com.chumakov123.gismeteoweather.domain.util.Utils.toWeekdayDayString
import com.chumakov123.gismeteoweather.domain.util.WindSpeedGradation.interpolateWindColor
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherAlarmScheduler
import com.chumakov123.gismeteoweather.domain.model.WeatherData
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.domain.model.WidgetState
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherUpdateReceiver
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
                when (widgetState.weatherInfo) {
                    WeatherInfo.Loading -> {
                        AppWidgetBox(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is WeatherInfo.Available -> {
                        // Based on the size render different UI
                        WeatherMedium(widgetState.weatherInfo, widgetState.forecastMode)
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
                            horizontalAlignment = Alignment.CenterHorizontally
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
            modifier = GlanceModifier.fillMaxSize(),
            Alignment.CenterHorizontally
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
            PlaceWeather(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
        }
    }
}

@Composable
fun WeatherMedium(weatherInfo: WeatherInfo.Available, forecastMode: ForecastMode) {
    AppWidgetColumn(GlanceModifier.clickable(actionRunCallback<UpdateWeatherAction>())) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(Color.Black))
                .padding(start = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = "${Utils.formatDateTime(weatherInfo.updateTime)}, ${weatherInfo.placeName}",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .height(12.dp)
                        .width(24.dp)
                        .clickable(actionRunCallback<OpenConfigCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_settings),
                        contentDescription = "Настройки",
                        modifier = GlanceModifier.size(12.dp)
                    )
                }
                Box(
                    modifier = GlanceModifier
                        .height(12.dp)
                        .width(24.dp)
                        .clickable(actionRunCallback<SwitchForecastModeAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_calendar),
                        contentDescription = "Переключить режим",
                        modifier = GlanceModifier.size(12.dp)
                    )
                }
                Box(
                    modifier = GlanceModifier
                        .height(12.dp)
                        .width(24.dp)
                        .clickable(actionRunCallback<UpdateWeatherAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "Обновить",
                        modifier = GlanceModifier.size(12.dp)
                    )
                }
            }
        }
        CurrentWeather(weatherInfo, GlanceModifier.fillMaxWidth())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (forecastMode == ForecastMode.ByHours) {
                HourlyForecast(weatherInfo, modifier = GlanceModifier.fillMaxSize())
            } else {
                DailyForecast(weatherInfo, modifier = GlanceModifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun WeatherLarge(weatherInfo: WeatherInfo.Available, forecastMode: ForecastMode) {
    AppWidgetColumn(GlanceModifier.clickable(actionRunCallback<UpdateWeatherAction>())) {
        Row(
            modifier = GlanceModifier.wrapContentHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            WeatherIcon(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
            PlaceWeather(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
        }
        Row(
            modifier = GlanceModifier.wrapContentHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            CurrentWeather(
                weatherInfo,
                modifier = GlanceModifier.wrapContentHeight(),
                Alignment.Start
            )
            HourlyForecast(weatherInfo, modifier = GlanceModifier.fillMaxWidth())
        }
        Spacer(GlanceModifier.size(8.dp))
        //DailyForecast(weatherInfo)
    }
}

@Composable
fun WeatherIcon(weatherInfo: WeatherInfo.Available, modifier: GlanceModifier = GlanceModifier) {
    Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
        Image(
            provider = ImageProvider(weatherInfo.now.icon),
            contentDescription = weatherInfo.now.description,
            modifier = GlanceModifier.size(36.dp),
        )
    }
}

@Composable
fun CurrentWeather(
    weatherInfo: WeatherInfo.Available,
    modifier: GlanceModifier = GlanceModifier,
    horizontal: Alignment.Horizontal = Alignment.Start
) {
    Row(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = modifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val defaultWeight = GlanceModifier.wrapContentSize()
            Row(
                modifier = modifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,) {
                Text(
                    text = "${if (weatherInfo.now.temperature > 0) "+" else ""}${weatherInfo.now.temperature}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 24.sp
                    )
                )
                if (weatherInfo.now.temperatureMin != null) {
                    Row(modifier = modifier.defaultWeight()) {
                        Text(
                            text = "${weatherInfo.now.temperatureMin}°",
                            style = TextStyle(
                                color = ColorProvider(Color.LightGray),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                WeatherIcon(weatherInfo)
            }
            Text(
                text = weatherInfo.now.description,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
        Spacer(modifier = GlanceModifier.width(4.dp))
        Spacer(
            modifier = GlanceModifier
                .height(48.dp)
                .width(1.dp)
                .background(ColorProvider(Color.Gray))
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Column(
            modifier = modifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row{
                Text(
                    text = "Осадки: ",
                    style = TextStyle(
                        color = ColorProvider(Color.LightGray),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = if (weatherInfo.now.precipitation != 0.0)
                        "${weatherInfo.now.precipitation} мм"
                    else
                        "Нет",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp
                    )
                )
            }

            Row(modifier = modifier.defaultWeight(),){
                Text(
                    text = "Ветер: ",
                    style = TextStyle(
                        color = ColorProvider(Color.LightGray),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = if (weatherInfo.now.windDirection != "—")
                        "${weatherInfo.now.windSpeed}—${weatherInfo.now.windGust} м/c, ${weatherInfo.now.windDirection}"
                    else
                        "Нет",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp
                    )
                )
            }

            Row{
                Text(
                    text = "Давление: ",
                    style = TextStyle(
                        color = ColorProvider(Color.LightGray),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "${weatherInfo.now.pressure} мм рт. ст.",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PlaceWeather(
    weatherInfo: WeatherInfo.Available,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.End
    ) {
        val defaultWeight = GlanceModifier.defaultWeight()
        Text(
            text = weatherInfo.placeName,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 18.sp,
                textAlign = TextAlign.End
            ),
            modifier = defaultWeight
        )
        Text(
            text = weatherInfo.now.description,
            style = TextStyle(
                color = ColorProvider(Color.LightGray),
                fontSize = 12.sp,
                textAlign = TextAlign.End
            ),
            modifier = defaultWeight
        )
    }
}

@Composable
fun HourlyForecast(
    weatherInfo: WeatherInfo.Available,
    modifier: GlanceModifier = GlanceModifier
) {
    val startIndex = Utils.getIntervalIndexByHour(weatherInfo.localTime.hour)
    val visibleCount = 6

    val displayList = weatherInfo.hourly
        .drop(startIndex)
        .take(visibleCount)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayList.forEachIndexed { index, item ->
            val hourLabel = Utils.getIntervalStartTime(startIndex + index)
            ForecastColumn(weatherData = item, date = hourLabel, modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
fun DailyForecast(
    weatherInfo: WeatherInfo.Available,
    modifier: GlanceModifier = GlanceModifier
) {
    val visibleCount = 6

    val displayList = weatherInfo.daily
        .take(visibleCount)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayList.forEachIndexed { index, item ->
            val dayLabel = weatherInfo.localTime.plusCalendarDays(index).toWeekdayDayString()
            ForecastColumn(weatherData = item, date = dayLabel, modifier = GlanceModifier.defaultWeight())
        }
    }
}


@Composable
fun ForecastColumn(
    weatherData: WeatherData,
    date: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 12.sp
            )
        )
        Image(
            provider = ImageProvider(weatherData.icon),
            contentDescription = weatherData.description,
            modifier = GlanceModifier.size(32.dp),
        )
        val temperatureColor = interpolateTemperatureColor(weatherData.temperature)
        if (weatherData.temperatureMin == null) {
            Text(
                text = "${if (weatherData.temperature > 0) "+" else ""}${weatherData.temperature}°",
                style = TextStyle(
                    color = ColorProvider(temperatureColor),
                    fontSize = 14.sp
                ),
            )
        } else {
            val temperatureColorMin = interpolateTemperatureColor(weatherData.temperatureMin)
            Text(
                text = "${if (weatherData.temperature > 0) "+" else ""}${weatherData.temperature}°",
                style = TextStyle(
                    color = ColorProvider(temperatureColor),
                    fontSize = 12.sp
                ),
            )
            Text(
                text = "${if (weatherData.temperatureMin > 0) "+" else ""}${weatherData.temperatureMin}°",
                style = TextStyle(
                    color = ColorProvider(temperatureColorMin),
                    fontSize = 10.sp
                ),
            )
        }
        val windColor = interpolateWindColor(weatherData.windGust)
        Text(
            text = if (weatherData.windDirection != "—")
                "${weatherData.windSpeed}—${weatherData.windGust}, ${weatherData.windDirection}"
            else
                "—",
            style = TextStyle(
                color = ColorProvider(windColor),
                fontSize = 10.sp
            ),
        )
        Text(
            text = if (weatherData.precipitation != 0.0)
                "${weatherData.precipitation} мм"
            else
                "—",
            style = TextStyle(
                color = if (weatherData.precipitation != 0.0)
                    ColorProvider(Color(66, 165, 245, 255))
                else ColorProvider(Color.LightGray),
                fontSize = 10.sp
            ),
        )
    }
}

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