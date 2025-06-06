package com.chumakov123.gismeteoweather.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.util.TemperatureGradation.interpolateTemperatureColor
import com.chumakov123.gismeteoweather.domain.util.Utils
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables.getGeomagneticDrawable
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables.getHumidityDrawable
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables.getPrecipitationDrawable
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables.getScale10Drawable
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables.getScale5Drawable
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables.getWindAngle
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables.getWindDrawable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlin.math.ceil

object WeatherDataPreprocessor {
    @Suppress("UNCHECKED_CAST")
    fun preprocess(
        weather: List<WeatherData>,
        forecastMode: ForecastMode,
        localDateTime: LocalDateTime
    ): Map<WeatherRowType, WeatherRow> {
        val startIndex = if (forecastMode == ForecastMode.ByDays) 0
        else Utils.getIntervalIndexByHour(localDateTime.hour)
        val slice = weather.drop(startIndex)

        return mapOf(
            WeatherRowType.TIME_LABELS to createTimeLabelsRow(slice, forecastMode, localDateTime,startIndex),
            WeatherRowType.ICONS to createIconsRow(slice),
            WeatherRowType.TEMP to createTemperatureChartRow(slice),
            WeatherRowType.TEMP_HEAT_INDEX to createTemperatureHeatIndexChartRow(slice),
            WeatherRowType.TEMP_AVG to createTemperatureAvgChartRow(slice).takeIf {
                forecastMode == ForecastMode.ByDays
            },
            WeatherRowType.RADIATION to createRadiationRow(slice),
            WeatherRowType.GEOMAGNETIC to createGeomagneticRow(slice),
            WeatherRowType.HUMIDITY to createHumidityRow(slice),
            WeatherRowType.POLLEN_BIRCH to createPollenBirchRow(slice),
            WeatherRowType.POLLEN_GRASS to createPollenGrassRow(slice),
            WeatherRowType.WIND to createWindRow(slice),
            WeatherRowType.PRECIP to createPrecipRow(slice),
            WeatherRowType.FALLING_SNOW to createFallingSnowRow(slice),
            WeatherRowType.SNOW_HEIGHT to createSnowHeightRow(slice),
            WeatherRowType.PRESSURE to createPressureChartRow(slice)
        ).filterValues { it != null } as Map<WeatherRowType, WeatherRow>
    }

    private fun createTimeLabelsRow(
        slice: List<WeatherData>,
        forecastMode: ForecastMode,
        localDateTime: LocalDateTime,
        startIndex: Int
    ): WeatherRow.DataRow {
        val timeLabels = List(slice.size) { idx ->
            if (forecastMode == ForecastMode.ByHours) {
                Utils.getIntervalStartTime(startIndex + idx)
            } else {
                val date = localDateTime.date.plus(idx, DateTimeUnit.DAY)
                val dayOfWeek = Utils.russianWeekdays[date.dayOfWeek]
                "$dayOfWeek, ${date.dayOfMonth}"
            }
        }
        return WeatherRow.DataRow(
            label = null,
            values = timeLabels.map { WeatherCell.Text(it) },
            useSurface = false,
            cellHeight = 24.dp
        )
    }

    private fun createIconsRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = null,
            values = slice.map { wd ->
                WeatherCell.Icon(wd.icon, contentDescription = "Погода")
            },
            useSurface = false,
            cellHeight = 48.dp
        )
    }

    private fun createTemperatureChartRow(slice: List<WeatherData>): WeatherRow.ChartRow {
        return WeatherRow.ChartRow(
            values = slice.map { it.temperature.toFloat() },
            baseline = slice.mapNotNull { it.temperatureMin?.toFloat() }
                .takeIf { it.size == slice.size },
            colorForValue = { t -> interpolateTemperatureColor(t.toInt()) },
            fillColorForPair = { max, min ->
                lerp(
                    interpolateTemperatureColor(max.toInt()),
                    interpolateTemperatureColor(min.toInt()),
                    0.5f
                ).copy(alpha = 0.3f)
            },
            labelFormatter = { "${if (it >= 0) "+" else ""}${it.toInt()}°" },
        )
    }

    private fun createTemperatureHeatIndexChartRow(slice: List<WeatherData>): WeatherRow.ChartRow {
        return WeatherRow.ChartRow(
            label = "Температура по ощущению, °C",
            values = slice.map { it.temperatureHeatIndex.toFloat() },
            baseline = slice.mapNotNull { it.temperatureHeatIndexMin?.toFloat() }
                .takeIf { it.size == slice.size },
            colorForValue = { t -> interpolateTemperatureColor(t.toInt()) },
            fillColorForPair = { max, min ->
                lerp(
                    interpolateTemperatureColor(max.toInt()),
                    interpolateTemperatureColor(min.toInt()),
                    0.5f
                ).copy(alpha = 0.3f)
            },
            labelFormatter = { "${if (it >= 0) "+" else ""}${it.toInt()}°" },
        )
    }

    private fun createTemperatureAvgChartRow(slice: List<WeatherData>): WeatherRow.ChartRow {
        return WeatherRow.ChartRow(
            label = "Среднесуточная температура, °C",
            values = slice.map { it.temperatureAvg.toFloat() },
            colorForValue = { t -> interpolateTemperatureColor(t.toInt()) },
            fillColorForPair = null,
            labelFormatter = { "${if (it >= 0) "+" else ""}${it.toInt()}°" },
        )
    }

    private fun createRadiationRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "УФ-индекс, баллы",
            values = slice.map {
                WeatherCell.IconWithCenterText(
                    text = "${if (it.radiation == -1) "—" else it.radiation}",
                    iconRes = getScale10Drawable(it.radiation)
                )
            },
            cellHeight = 36.dp
        )
    }

    private fun createGeomagneticRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Г/м активность, Кп-индекс",
            values = slice.map {
                WeatherCell.IconWithCenterText(
                    text = "${if (it.geomagnetic == -1) "—" else it.geomagnetic}",
                    iconRes = getGeomagneticDrawable(it.geomagnetic))
            },
            cellHeight = 36.dp
        )
    }

    private fun createHumidityRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Влажность, %",
            values = slice.map {
                WeatherCell.IconAboveText(
                    text = "${if (it.humidity == -1) "—" else it.humidity}",
                    iconRes = getHumidityDrawable(it.humidity))
            },
            cellHeight = 48.dp
        )
    }

    private fun createPollenBirchRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Пыльца берёзы, баллы",
            values = slice.map {
                WeatherCell.IconWithCenterText(
                    text = "${if (it.pollenBirch == -1) "—" else it.pollenBirch}",
                    iconRes = getScale5Drawable(it.pollenBirch))
            },
            cellHeight = 36.dp
        )
    }

    private fun createPollenGrassRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Пыльца злаковых трав, баллы",
            values = slice.map {
                WeatherCell.IconWithCenterText(
                    text = "${if (it.pollenGrass == -1) "—" else it.pollenGrass}",
                    iconRes = getScale5Drawable(it.pollenGrass))
            },
            cellHeight = 36.dp
        )
    }

    private fun createWindRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Скорость ветра, м/с",
            values = slice.map { wd ->
                val drawable = getWindDrawable(wd.windGust)
                val angle = getWindAngle(wd.windDirection)
                when {
                    wd.windDirection == "—" ->
                        WeatherCell.IconAboveText(text = "0", iconRes = R.drawable.wind_zero)
                    wd.windSpeed == wd.windGust ->
                        WeatherCell.IconAboveText(
                            text = "${wd.windGust} ${wd.windDirection}",
                            iconRes = drawable,
                            iconRotation = angle.toFloat())
                    else ->
                        WeatherCell.IconAboveText(
                            text = "${wd.windSpeed}-${wd.windGust} ${wd.windDirection}",
                            iconRes = drawable,
                            iconRotation = angle.toFloat())
                }
            },
            cellHeight = 48.dp
        )
    }

    private fun createPrecipRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Осадки, мм",
            values = slice.map { wd ->
                WeatherCell.ColumnBackground(
                    backgroundRes = R.drawable.empty,
                    iconRes = getPrecipitationDrawable(wd.precipitation),
                    text = if (wd.precipitation == 0.0) "0" else "%.1f".format(wd.precipitation),
                    textOffsetFromBottom = (2 + ceil(wd.precipitation).toInt().coerceIn(0, 12)*2.5).dp,
                    textColor = if (wd.precipitation == 0.0) Color.Gray else Color.White
                )
            },
            cellHeight = 64.dp,
        )
    }

    private fun createFallingSnowRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Выпадающий снег, см",
            values = slice.map { wd ->
                WeatherCell.Text(
                    if (wd.fallingSnow == 0.0) "0"
                    else "%.1f".format(wd.fallingSnow)
                )
            },
            cellHeight = 24.dp
        )
    }

    private fun createSnowHeightRow(slice: List<WeatherData>): WeatherRow.DataRow {
        return WeatherRow.DataRow(
            label = "Высота снежного покрова, см",
            values = slice.map { wd ->
                WeatherCell.Text(
                    if (wd.snowHeight == 0.0) "0"
                    else "%.1f".format(wd.snowHeight)
                )
            },
            cellHeight = 24.dp
        )
    }

    private fun createPressureChartRow(slice: List<WeatherData>): WeatherRow.ChartRow {
        return WeatherRow.ChartRow(
            label = "Давление, мм рт.ст.",
            values = slice.map { it.pressure.toFloat() },
            baseline = slice.mapNotNull { it.pressureMin?.toFloat() }
                .takeIf { it.size == slice.size },
            colorForValue = { p -> Color(0xFF9575CD) },
            fillColorForPair = { max, min -> Color(0x4D9575CD) },
            labelFormatter = { "${it.toInt()}" },
            labelTextSize = 16.sp
        )
    }
}