package com.chumakov123.gismeteoweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chumakov123.gismeteoweather.domain.model.AstroTimes
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.util.Utils.plusMillis
import com.chumakov123.gismeteoweather.domain.util.Utils.toDayDateTimeString
import com.chumakov123.gismeteoweather.presentation.ui.components.application.viewModel.WeatherUiState
import com.chumakov123.gismeteoweather.presentation.ui.components.application.viewModel.WeatherViewModel
import com.chumakov123.gismeteoweather.ui.theme.GismeteoWeatherTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val weatherViewModel : WeatherViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GismeteoWeatherTheme {
                Scaffold { inner ->
                    WeatherScreen(modifier = Modifier.padding(inner), viewModel = weatherViewModel)
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    when (state) {
        is WeatherUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WeatherUiState.Success -> {
            val data = (state as WeatherUiState.Success).data
            WeatherContent(
                weather = data,
                onRefresh = { viewModel.loadWeather(data.placeCode) },
                modifier = modifier
            )
        }
        is WeatherUiState.Error -> {
            val msg = (state as WeatherUiState.Error).message
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ошибка: $msg")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadWeather("auto") }) {
                        Text("Повторить")
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherContent(
    weather: WeatherInfo.Available,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nowMillis by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = Unit
    ) {
        while (true) {
            value = System.currentTimeMillis()
            delay(2_500L)
        }
    }

    val localDateTime = weather.localTime.plusMillis(nowMillis-weather.updateTime)

    val bgUrl = remember(weather.now.iconWeather) {
        "https://st.gismeteo.st/assets/bg-desktop-now/${weather.now.iconWeather}.webp"
    }

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(bgUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.1f))
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            val shadowStyle = Shadow(
                color = Color.Black,
                offset = Offset(1f, 1f),
                blurRadius = 4f
            )

//            Text(
//                text = "Обновлено ${formatRelativeTime(weather.updateTime, nowMillis)}",
//                color = Color.White,
//                style = MaterialTheme.typography.bodyMedium.copy(shadow = shadowStyle)
//            )
//            Spacer(Modifier.height(8.dp))

            Text(
                text = weather.placeName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(shadow = shadowStyle)
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = localDateTime.toDayDateTimeString(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(shadow = shadowStyle)
            )

            Spacer(Modifier.height(8.dp))

            SunTimelineWithLabels(
                currentTime = localDateTime,
                astroTimes = weather.astroTimes,
                shadowStyle = shadowStyle
            )

            val tempSign = if (weather.now.temperature >= 0) "+" else "-"
            Text(
                text = "$tempSign${weather.now.temperature}°",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge.copy(shadow = shadowStyle)
            )

            Spacer(Modifier.height(8.dp))

            val heatSign = if (weather.now.temperatureHeatIndex >= 0) "+" else "-"
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "По ощущению $heatSign${weather.now.temperatureHeatIndex}°",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle)
                )
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = weather.now.description,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(shadow = shadowStyle)
            )
            Spacer(Modifier.height(8.dp))

            WeatherParamsRow(weather = weather)

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun SunTimelineWithLabels(
    currentTime: LocalDateTime,
    astroTimes: AstroTimes,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 2.dp,
    iconSize: Dp = 24.dp,
    shadowStyle: Shadow
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = astroTimes.sunrise.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle)
            )
            Text(
                text = "Восход",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle)
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(iconSize)
        ) {
            SunTimeline(
                currentTime = currentTime,
                astroTimes  = astroTimes,
                modifier    = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                iconSize    = iconSize,
                lineHeight  = lineHeight
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = astroTimes.sunset.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle)
            )
            Text(
                text = "Заход",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle)
            )
        }
    }
}

@Composable
fun SunTimeline(
    currentTime: LocalDateTime,
    astroTimes: AstroTimes,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    lineHeight: Dp = 2.dp
) {

    val today       = currentTime.date
    val sunriseMs   = today.atTime(astroTimes.sunrise)
        .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    val sunsetMs    = today.atTime(astroTimes.sunset)
        .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    val nowMs       = currentTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    val pos = when {
        nowMs <= sunriseMs -> 0f
        nowMs >= sunsetMs  -> 1f
        else               -> (nowMs - sunriseMs).toFloat() / (sunsetMs - sunriseMs).toFloat()
    }.coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(iconSize)
    ) {
        val density = LocalDensity.current
        val fullWidthPx = with(density) { maxWidth.toPx() }

        val iconOffsetPx = with(density) {
            (fullWidthPx * pos - iconSize.toPx() / 2f).roundToInt()
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(lineHeight)
                .align(Alignment.CenterStart)
        ) {
            val y = size.height / 2f
            val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            drawLine(
                color = Color.White,
                start = Offset(0f, y),
                end   = Offset(size.width * pos, y),
                strokeWidth = lineHeight.toPx(),
                cap = Stroke.DefaultCap
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(size.width * pos, y),
                end   = Offset(size.width, y),
                strokeWidth = lineHeight.toPx(),
                pathEffect = dash,
                cap = Stroke.DefaultCap
            )
        }

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.sun),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(iconSize)
                .offset { IntOffset(iconOffsetPx, 0) }
        )
    }
}

@Composable
private fun WeatherParamItem(
    modifier: Modifier = Modifier,
    title: String,
    mainValue: String,
    unit: String? = null,
    shadowStyle: Shadow
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = mainValue,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(shadow = shadowStyle),
            )
            unit?.let {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = it,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle)
                )
            }
        }
    }
}

@Composable
fun WeatherParamsRow(
    weather: WeatherInfo.Available,
    modifier: Modifier = Modifier
) {
    val shadowStyle = Shadow(
        color = Color.Black,
        offset = Offset(1f, 1f),
        blurRadius = 2f
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WeatherParamItem(
            title = "Ветер",
            mainValue = if (weather.now.windDirection == "—")
                "—"
            else
                if(weather.now.windSpeed==weather.now.windGust)
                    "${weather.now.windSpeed}"
                else
                    "${weather.now.windSpeed}-${weather.now.windGust}",
            unit = if (weather.now.windDirection == "—")
                null
            else
                weather.now.windDirection,
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Давление",
            mainValue = "${weather.now.pressure}",
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Влажность",
            mainValue = "${weather.now.humidity}",
            unit = "%",
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Осадки",
            mainValue = if (weather.now.precipitation != 0.0)
                "${weather.now.precipitation}"
            else
                "—",
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Вода",
            mainValue = "${if (weather.now.temperatureWater >= 0) "+" else "-"}${weather.now.temperatureWater}°",
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
    }
}