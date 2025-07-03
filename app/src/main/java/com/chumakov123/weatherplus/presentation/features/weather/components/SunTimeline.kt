package com.chumakov123.weatherplus.presentation.features.weather.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.chumakov123.weatherplus.R
import kotlin.math.roundToInt

@Composable
fun SunTimeline(
    rotationDegrees: Double,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    lineHeight: Dp = 2.dp
) {
    val pos = ((rotationDegrees + 35.0) / 70.0).toFloat().coerceIn(0f, 1f)

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
}
