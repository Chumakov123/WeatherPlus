package com.chumakov123.weatherplus.domain.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

object WindSpeedGradation {
    data class SpeedColorPoint(val speed: Int, val color: Color)

    private val speedGradient = listOf(
        SpeedColorPoint(0, Color.LightGray),
        SpeedColorPoint(5, Color.White),
        SpeedColorPoint(10, Color(0xFFFFF59D)),
        SpeedColorPoint(18, Color(0xFFEF9A9A)),
        SpeedColorPoint(30, Color(0xFFEF5350))
    )

    fun interpolateWindColor(speed: Int): Color {
        val s = speed.coerceIn(speedGradient.first().speed, speedGradient.last().speed)

        speedGradient.zipWithNext().forEach { (start, end) ->
            if (s in start.speed..end.speed) {
                val fraction = (s - start.speed).toFloat() / (end.speed - start.speed)
                return lerp(start.color, end.color, fraction)
            }
        }

        return if (s < speedGradient.first().speed) {
            speedGradient.first().color
        } else {
            speedGradient.last().color
        }
    }
}
