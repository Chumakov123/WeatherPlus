package com.chumakov123.gismeteoweather.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

sealed class WeatherCell {
    data class Text(val text: String) : WeatherCell()
    data class WeatherIcon(val icon: String, val contentDescription: String? = null) : WeatherCell()

    data class IconWithCenterText(
        @DrawableRes val iconRes: Int,
        val text: String,
        val contentDescription: String? = null
    ) : WeatherCell()

    data class IconAboveText(
        @DrawableRes val iconRes: Int,
        val text: String,
        val contentDescription: String? = null,
        val iconRotation: Float? = null
    ) : WeatherCell()

    data class IconBelowText(
        @DrawableRes val iconRes: Int,
        val text: String,
        val contentDescription: String? = null
    ) : WeatherCell()

    data class ColumnBackground(
        @DrawableRes val backgroundRes: Int,
        @DrawableRes val iconRes: Int,
        val text: String,
        val textOffsetFromBottom: Dp,
        val contentDescription: String? = null,
        val textColor: Color = Color.White
    ) : WeatherCell()
}