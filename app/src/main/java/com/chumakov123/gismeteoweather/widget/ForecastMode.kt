package com.chumakov123.gismeteoweather.widget

import kotlinx.serialization.Serializable

/**
 * Enum class representing the forecast mode (By hours / By days).
 */
@Serializable
enum class ForecastMode {
    ByHours,  // Прогноз по часам
    ByDays    // Прогноз по дням
}