package com.chumakov123.gismeteoweather.domain.util

import kotlin.math.abs

object WindDirections {
    val windDirections = mapOf(
        0 to "—",
        1 to "С",
        2 to "СВ",
        3 to "В",
        4 to "ЮВ",
        5 to "Ю",
        6 to "ЮЗ",
        7 to "З",
        8 to "СЗ"
    )

    fun getWindDirectionIndex(angle: Int, speed: Int): Int {
        if (speed == 0) return 0 // Нет ветра

        val directionAngles = mapOf(
            1 to 0,    // С
            2 to 45,   // СВ
            3 to 90,   // В
            4 to 135,  // ЮВ
            5 to 180,  // Ю
            6 to 225,  // ЮЗ
            7 to 270,  // З
            8 to 315   // СЗ
        )

        val normalizedAngle = (angle % 360 + 360) % 360

        return directionAngles.minByOrNull { (_, dirAngle) ->
            val diff = abs(normalizedAngle - dirAngle)
            diff.coerceAtMost(360 - diff) // на случай перехода через 0°
        }?.key ?: 0
    }
}