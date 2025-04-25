package com.chumakov123.gismeteoweather.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

object TemperatureGradation {
    data class TempColorPoint(val temp: Int, val color: Color)

    val tempGradient = listOf(
        TempColorPoint(-40, Color(0xFF1565C0)),
        TempColorPoint(-30, Color(0xFF2196F3)),
        TempColorPoint(-20, Color(0xFF64B5F6)),
        TempColorPoint(-10, Color(0xFF81D4FA)),
        TempColorPoint(0,   Color.White),
        TempColorPoint(10,  Color(0xFFA5D6A7)),
        TempColorPoint(20,  Color(0xFFE6EE9C)),
        TempColorPoint(30,  Color(0xFFE57373)),
        TempColorPoint(40,  Color(0xFFE53935))
    )

    fun interpolateTemperatureColor(temp: Int): Color {
        val t = temp.coerceIn(tempGradient.first().temp, tempGradient.last().temp)

        tempGradient.zipWithNext().forEach { (start, end) ->
            if (t in start.temp..end.temp) {
                val fraction = (t - start.temp).toFloat() / (end.temp - start.temp)
                return lerp(start.color, end.color, fraction)
            }
        }

        return if (t < tempGradient.first().temp) tempGradient.first().color
        else tempGradient.last().color
    }
}