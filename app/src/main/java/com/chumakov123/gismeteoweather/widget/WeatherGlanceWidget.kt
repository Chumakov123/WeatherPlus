package com.chumakov123.gismeteoweather.widget

import android.content.Context
import androidx.compose.material3.MaterialTheme
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
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
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
import com.chumakov123.gismeteoweather.AppWidgetBox
import com.chumakov123.gismeteoweather.AppWidgetColumn
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.utils.Utils
import com.chumakov123.gismeteoweather.worker.WeatherWorker
import kotlinx.datetime.LocalDateTime

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
        CurrentTemperature(
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
            CurrentTemperature(weatherInfo, modifier = GlanceModifier.fillMaxSize().defaultWeight())
            WeatherIcon(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
            PlaceWeather(weatherInfo, modifier = GlanceModifier.fillMaxWidth().defaultWeight())
        }
    }
}

@Composable
fun WeatherMedium(weatherInfo: WeatherInfo.Available, forecastMode: ForecastMode) {
    AppWidgetColumn(GlanceModifier.clickable(actionRunCallback<UpdateWeatherAction>())) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.End
        ) {

            // Дата последнего обновления
            Text(
                text = "${Utils.formatDateTime(weatherInfo.updateTime)}, Ростов-на-дону",
                style = TextStyle(
                    color = ColorProvider(Color.LightGray),
                    fontSize = 10.sp
                )
            )
            Box(
                modifier = GlanceModifier
                    .height(10.dp)
                    .width(24.dp)
                    .clickable(actionRunCallback<UpdateWeatherAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "Обновить",
                    modifier = GlanceModifier.size(10.dp) // сама иконка — маленькая
                )
            }
        }
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {

            CurrentTemperature(
                weatherInfo,
            )
            WeatherIcon(weatherInfo)
            //PlaceWeather(weatherInfo)
        }
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (forecastMode == ForecastMode.ByHours) {
                HourlyForecast(weatherInfo, modifier = GlanceModifier.fillMaxSize())
            } else {
                //TODO Прогноз на неделю
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
            CurrentTemperature(
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
    // TODO missing tint
    Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
        Image(
            provider = ImageProvider(weatherInfo.now.icon),
            contentDescription = weatherInfo.now.description,
            modifier = GlanceModifier.size(48.dp),
        )
    }
}

@Composable
fun CurrentTemperature(
    weatherInfo: WeatherInfo.Available,
    modifier: GlanceModifier = GlanceModifier,
    horizontal: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = horizontal
    ) {
        val defaultWeight = GlanceModifier.wrapContentSize()
        Row(modifier = defaultWeight) {
            Text(
                text = "${if (weatherInfo.now.temperature > 0) "+" else ""}${weatherInfo.now.temperature}°",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 24.sp
                )
            )
            if (weatherInfo.now.temperatureMin != null) {
                Row(modifier = defaultWeight) {
                    Text(
                        text = "${weatherInfo.now.temperatureMin}°",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
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
            HourForecast(weatherData = item, date = hourLabel)
        }
    }
}

@Composable
fun HourForecast(
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
        Text(
            text = "${weatherData.temperature}º",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 14.sp
            ),
        )
    }
}

//@Composable
//fun DailyForecast(
//    weatherInfo: WeatherInfo.Available,
//    modifier: GlanceModifier = GlanceModifier
//) {
//    LazyColumn(
//        modifier = GlanceModifier
//            .background(GlanceTheme.colors.surfaceVariant)
//            .appWidgetInnerCornerRadius()
//            .then(modifier)
//    ) {
//        items(weatherInfo.dailyForecast) { dayForecast ->
//            Row(
//                modifier = GlanceModifier.fillMaxWidth().padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = dayForecast.toDayString(),
//                    style = TextStyle(
//                        color = ColorProvider(Color.Black),
//                        fontSize = 14.sp
//                    )
//                )
//                Row(
//                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
//                    horizontalAlignment = Alignment.End,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    val textStyle = TextStyle(
//                        color = ColorProvider(Color.Black),
//                        fontSize = 14.sp
//                    )
//                    Image(
//                        provider = ImageProvider(dayForecast.icon),
//                        contentDescription = dayForecast.status.toString(),
//                        modifier = GlanceModifier.size(24.dp).padding(4.dp),
//                    )
//                    Text(
//                        text = "${dayForecast.minTemp}º",
//                        style = textStyle,
//                        modifier = GlanceModifier.padding(4.dp)
//                    )
//                    Text(
//                        text = "${dayForecast.maxTemp}º",
//                        style = textStyle,
//                        modifier = GlanceModifier.padding(4.dp)
//                    )
//                }
//            }
//        }
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
        // Force the worker to refresh
        WeatherWorker.enqueue(context = context, force = true)
    }
}

class SwitchForecastModeAction: ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Force the worker to refresh
        WeatherWorker.enqueue(context = context, force = true, switchMode = true)
    }
}

//@Composable
//private fun WeatherData.toDayString() = day.lowercase(Locale.getDefault()).replaceFirstChar {
//    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
//}