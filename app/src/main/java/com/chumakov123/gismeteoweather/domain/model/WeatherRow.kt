package com.chumakov123.gismeteoweather.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class WeatherRow {
    data class DataRow(
        val label: String? = null,
        val values: List<WeatherCell>,
        val useSurface: Boolean = false,
        val cellHeight: Dp = 48.dp,
    ) : WeatherRow()

    data class ChartRow(
        val label: String? = null,
        val values: List<Float>,
        val baseline: List<Float>? = null,
        val cellWidth: Dp = 48.dp,
        val rowHeight: Dp = 48.dp,
        val colorForValue: (Float) -> Color,
        val fillColorForPair: ((Float, Float) -> Color)? = null,
        val labelFormatter: (Float) -> String = { it.toString() },
        val labelColor: Color = Color.White,
        val labelTextSize: TextUnit = 16.sp
    ) : WeatherRow()
}
