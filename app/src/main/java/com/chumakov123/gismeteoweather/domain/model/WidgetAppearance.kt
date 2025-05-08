package com.chumakov123.gismeteoweather.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetAppearance(
    val showUpdateTime: Boolean = true,
    val showCurrentWeather: Boolean = true,
    val useColorIndicators: Boolean = true,
    val backgroundTransparencyPercent: Int = 50,
    val showPrecipitation: Boolean = true,
    val showWind: Boolean = true
)