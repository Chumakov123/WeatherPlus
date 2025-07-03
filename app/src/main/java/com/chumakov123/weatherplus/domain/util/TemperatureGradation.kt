package com.chumakov123.weatherplus.domain.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

object TemperatureGradation {
    data class TempColorPoint(val temp: Int, val color: Color)

    private val tempGradientDark = listOf(
        TempColorPoint(-40, Color(0xFF1565C0)),
        TempColorPoint(-30, Color(0xFF2196F3)),
        TempColorPoint(-20, Color(0xFF64B5F6)),
        TempColorPoint(-10, Color(0xFF81D4FA)),
        TempColorPoint(0, Color.White),
        TempColorPoint(10, Color(0xFFA5D6A7)),
        TempColorPoint(20, Color(0xFFE6EE9C)),
        TempColorPoint(30, Color(0xFFE57373)),
        TempColorPoint(40, Color(0xFFE53935))
    )

    private val tempGradientLight = listOf(
        TempColorPoint(-40, Color(0xFF0D47A1)),
        TempColorPoint(-30, Color(0xFF1976D2)),
        TempColorPoint(-20, Color(0xFF42A5F5)),
        TempColorPoint(-10, Color(0xFF4FC3F7)),
        TempColorPoint(0, Color(0xFF60B704)), // тёмно-серый вместо белого
        TempColorPoint(10, Color(0xFF388E3C)),
        TempColorPoint(20, Color(0xFFFBC02D)),
        TempColorPoint(30, Color(0xFFEF5350)),
        TempColorPoint(40, Color(0xFFD32F2F))
    )

    fun interpolateTemperatureColor(
        temp: Int,
        isDarkTheme: Boolean = true
    ): Color {
        val gradient = if (isDarkTheme) tempGradientDark else tempGradientLight
        val t = temp.coerceIn(gradient.first().temp, gradient.last().temp)

        gradient.zipWithNext().forEach { (start, end) ->
            if (t in start.temp..end.temp) {
                val fraction = (t - start.temp).toFloat() / (end.temp - start.temp)
                return lerp(start.color, end.color, fraction)
            }
        }

        return if (t < gradient.first().temp) {
            gradient.first().color
        } else {
            gradient.last().color
        }
    }
}
