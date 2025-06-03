package com.chumakov123.gismeteoweather.domain.model

data class WeatherDisplaySettings(
    val enabledRows: Set<WeatherRowType> = setOf(
        WeatherRowType.WIND,
        WeatherRowType.RADIATION,
        WeatherRowType.GEOMAGNETIC,
        WeatherRowType.HUMIDITY,
        WeatherRowType.PRECIP,
        WeatherRowType.PRESSURE
    ),
    val rowOrder: List<WeatherRowType> = listOf(
        WeatherRowType.TEMP_HEAT_INDEX,
        WeatherRowType.TEMP_AVG,
        WeatherRowType.WIND,
        WeatherRowType.RADIATION,
        WeatherRowType.GEOMAGNETIC,
        WeatherRowType.HUMIDITY,
        WeatherRowType.PRECIP,
        WeatherRowType.PRESSURE,
        WeatherRowType.POLLEN_BIRCH,
        WeatherRowType.POLLEN_GRASS,
        WeatherRowType.FALLING_SNOW,
        WeatherRowType.SNOW_HEIGHT,
    )
)

enum class WeatherRowType {
    TIME_LABELS,
    ICONS,
    TEMP,
    TEMP_HEAT_INDEX,
    TEMP_AVG,
    POLLEN_BIRCH,
    POLLEN_GRASS,
    RADIATION,
    GEOMAGNETIC,
    HUMIDITY,
    FALLING_SNOW,
    SNOW_HEIGHT,
    WIND,
    PRECIP,
    PRESSURE
}

val WeatherRowType.displayName: String
    get() = when (this) {
        WeatherRowType.TIME_LABELS -> "Время"
        WeatherRowType.ICONS -> "Иконки"
        WeatherRowType.TEMP -> "Температура"
        WeatherRowType.TEMP_HEAT_INDEX -> "Температура по ощущению"
        WeatherRowType.TEMP_AVG -> "Среднесуточная температура"
        WeatherRowType.POLLEN_BIRCH -> "Пыльца берёзы"
        WeatherRowType.POLLEN_GRASS -> "Пыльца злаковых трав"
        WeatherRowType.RADIATION -> "Солнечная активность"
        WeatherRowType.GEOMAGNETIC -> "Геомагнитная обстановка"
        WeatherRowType.HUMIDITY -> "Влажность"
        WeatherRowType.FALLING_SNOW -> "Выпадающий снег"
        WeatherRowType.SNOW_HEIGHT -> "Высота снежного покрова"
        WeatherRowType.WIND -> "Ветер"
        WeatherRowType.PRECIP -> "Осадки"
        WeatherRowType.PRESSURE -> "Давление"
    }