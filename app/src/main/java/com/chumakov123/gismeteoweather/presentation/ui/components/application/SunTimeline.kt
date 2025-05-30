package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.model.AstroTimes
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.math.roundToInt

@Composable
fun SunTimeline(
    currentTime: LocalDateTime,
    astroTimes: AstroTimes,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    lineHeight: Dp = 2.dp
) {
    val timeZone = TimeZone.currentSystemDefault()
    val today = currentTime.date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val tomorrow = today.plus(1, DateTimeUnit.DAY)

    val nowMs = currentTime.toInstant(timeZone).toEpochMilliseconds()

    val (sunrise, sunset) = if (astroTimes.sunrise.hour < astroTimes.sunset.hour)
        astroTimes.sunrise to astroTimes.sunset
    else
        astroTimes.sunset to astroTimes.sunrise

    val candidates = listOf(
        Pair(yesterday.atTime(sunset), today.atTime(sunrise)),
        Pair(today.atTime(sunrise), today.atTime(sunset)),
        Pair(today.atTime(sunset), tomorrow.atTime(sunrise))
    )

    val (startTime, endTime) = candidates.find { (start, end) ->
        val startMs = start.toInstant(timeZone).toEpochMilliseconds()
        val endMs = end.toInstant(timeZone).toEpochMilliseconds()
        nowMs in startMs..endMs
    } ?: run {
        Pair(today.atTime(astroTimes.sunrise), today.atTime(astroTimes.sunset))
    }

    val startMs = startTime.toInstant(timeZone).toEpochMilliseconds()
    val endMs = endTime.toInstant(timeZone).toEpochMilliseconds()
    val pos = ((nowMs - startMs).toFloat() / (endMs - startMs).toFloat()).coerceIn(0f, 1f)

    val sunriseLabel = if (astroTimes.sunrise.hour < 12) "Восход" else "Заход"
    val sunsetLabel  = if (astroTimes.sunset.hour < 12) "Восход" else "Заход"

    Column(modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(iconSize)
        ) {
            val density = LocalDensity.current
            val fullWidthPx = with(density) { maxWidth.toPx() }
            val iconOffsetPx = with(density) {
                (fullWidthPx * pos - iconSize.toPx() / 2f).roundToInt()
            }

            // Линия
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
                    end = Offset(size.width * pos, y),
                    strokeWidth = lineHeight.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(size.width * pos, y),
                    end = Offset(size.width, y),
                    strokeWidth = lineHeight.toPx(),
                    pathEffect = dash
                )
            }

            // Солнце
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.sun),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(iconSize)
                    .offset { IntOffset(iconOffsetPx, 0) }
            )
        }

        // Подписи: "Восход" / "Заход"
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = sunriseLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = sunsetLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}