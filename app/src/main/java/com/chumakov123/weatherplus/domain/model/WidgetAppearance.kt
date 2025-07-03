package com.chumakov123.weatherplus.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetAppearance(
    val showUpdateTime: Boolean = true,
    val showCurrentWeather: Boolean = true,
    val useColorIndicators: Boolean = true,
    val backgroundTransparencyPercent: Int = 50,
    val showPrecipitation: Boolean = true,
    val showWind: Boolean = true,
    val textScale: Float = 1.0f
)
