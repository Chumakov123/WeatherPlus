package com.chumakov123.gismeteoweather.data.parser

import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.data.model.DateAndCityDTO
import com.chumakov123.gismeteoweather.data.model.WeatherRawDTO
import com.chumakov123.gismeteoweather.utils.Utils.fahrenheitToCelsius
import com.chumakov123.gismeteoweather.utils.WeatherDrawables
import com.chumakov123.gismeteoweather.widget.WeatherData
import com.chumakov123.gismeteoweather.widget.WeatherIconInfo
import com.chumakov123.gismeteoweather.widget.WindData
import kotlinx.datetime.DateTimeUnit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import java.time.format.DateTimeFormatter

object GismeteoWeatherHtmlParser {
    fun parseWeatherData(html: String, hasMinT: Boolean = false): List<WeatherData> {
        val temperatures = parseTemperatureData(html)
        val windDataList = parseWindData(html)
        val precipitations = parsePrecipitationData(html)
        val icons = parseWeatherIcons(html)
        val pressures = parsePressureData(html)

        val weatherDataList = mutableListOf<WeatherData>()

        val size = if (!hasMinT) temperatures.size else temperatures.size / 2

        for (i in 0 until size) {
            val tMax = if (!hasMinT) temperatures[i] else temperatures[i*2]
            val tMin = if (!hasMinT) null else temperatures[i*2+1]
            val windData = windDataList.getOrElse(i) { WindData(0, "Неизвестно", 0) }
            val precipitation = precipitations.getOrElse(i) { 0.0 }
            val pressure = pressures.getOrElse(i) { 0 }
            val icon = icons.getOrElse(i) { WeatherIconInfo("Неизвестно", null, null) }

            var iconString =
                if (icon.bottomLayer != null)
                    "${icon.topLayer}_${icon.bottomLayer}"
                else
                    "${icon.topLayer}"

            iconString = iconString.replace("_c0","")
            if (!WeatherDrawables.drawableMap.containsKey(iconString)) {
                val underscoreIndex = iconString.indexOf('_')
                if (underscoreIndex != -1) {
                    iconString = iconString.substring(underscoreIndex + 1)
                }
            }

            val iconDrawable = WeatherDrawables.drawableMap[iconString] ?: R.drawable.c3

            val weatherData = WeatherData(
                description = icon.tooltip,
                icon = iconDrawable,
                temperature = tMax.toInt(),
                temperatureMin = tMin?.toInt(),
                windSpeed = windData.speed,
                windDirection = windData.direction,
                windGust = windData.gust,
                precipitation = precipitation,
                pressure = pressure
            )

            weatherDataList.add(weatherData)
        }

        return weatherDataList
    }
    fun parseWeatherNowFromHtml(html: String): WeatherRawDTO? {
        val regex = Regex("""window\.M\.state\s*=\s*(\{.*?\})(?=\s*</script>)""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = regex.find(html) ?: return null
        val jsonString = matchResult.groupValues[1]
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(jsonString).jsonObject
        val cwJson = root["weather"]?.jsonObject?.get("cw") ?: return null
        return json.decodeFromJsonElement<WeatherRawDTO>(cwJson)
    }

    fun parseDateAndCityFromHtml(html: String): DateAndCityDTO? {
        // Извлекаем JSON из HTML
        val regex = Regex("""window\.M\.state\s*=\s*(\{.*?\})(?=\s*</script>)""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = regex.find(html) ?: return null
        val jsonString = matchResult.groupValues[1]

        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(jsonString).jsonObject

        // Извлекаем строку даты из app.cache.date
        val cacheDate = root["app"]?.jsonObject
            ?.get("cache")?.jsonObject
            ?.get("date")?.jsonPrimitive?.content
            ?: return null

        // Извлекаем смещение в минутах из city.timeZone
        val timeZoneOffsetMinutes = root["city"]?.jsonObject
            ?.get("timeZone")?.jsonPrimitive?.int ?: 0

        // Извлекаем название города из city.translations.ru.city.name
        val cityName = root["city"]?.jsonObject
            ?.get("translations")?.jsonObject
            ?.get("ru")?.jsonObject
            ?.get("city")?.jsonObject
            ?.get("name")?.jsonPrimitive?.content
            ?: return null

        // Обрезаем дату (например, "10.04.2025 08:03:37 (UTC)" -> "10.04.2025 08:03:37")
        val dateTimeString = cacheDate.substringBefore(" (")

        // Парсим строку с помощью java.time
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val javaUtcDateTime = java.time.LocalDateTime.parse(dateTimeString, formatter)

        // Конвертируем java.time.LocalDateTime в kotlinx.datetime.LocalDateTime
        val utcDateTime = LocalDateTime(
            year = javaUtcDateTime.year,
            monthNumber = javaUtcDateTime.monthValue,
            dayOfMonth = javaUtcDateTime.dayOfMonth,
            hour = javaUtcDateTime.hour,
            minute = javaUtcDateTime.minute,
            second = javaUtcDateTime.second,
            nanosecond = javaUtcDateTime.nano
        )

        // Преобразуем UTC LocalDateTime в Instant
        val utcInstant = utcDateTime.toInstant(TimeZone.UTC)
        // Прибавляем смещение в минутах к Instant
        val localInstant = utcInstant.plus(timeZoneOffsetMinutes.toLong(), DateTimeUnit.MINUTE)
        // Преобразуем обратно в LocalDateTime (можно указать TimeZone.UTC, т.к. смещение уже учтено)
        val localDateTime = localInstant.toLocalDateTime(TimeZone.UTC)

        return DateAndCityDTO(localDateTime, cityName)
    }

    private fun parseWeatherIcons(html: String): List<WeatherIconInfo> {
        val doc: Document = Jsoup.parse(html)
        val result = mutableListOf<WeatherIconInfo>()

        val items = doc.select(".widget-row-icon .row-item")
        for (item in items) {
            val tooltip = item.attr("data-tooltip")
            val topUse = item.selectFirst("svg.top-layer use")
            val bottomUse = item.selectFirst("svg.bottom-layer use")

            val topLayer = topUse?.attr("href")?.removePrefix("#")
            val bottomLayer = bottomUse?.attr("href")?.removePrefix("#")

            result.add(
                WeatherIconInfo(
                    tooltip = tooltip,
                    topLayer = topLayer,
                    bottomLayer = bottomLayer
                )
            )
        }
        return result
    }

    private fun parseTemperatureData(html: String): List<Double> {
        val document = Jsoup.parse(html)
        val temperatures = mutableListOf<Double>()
        val temperatureSection = document.select(".widget-row-chart.widget-row-chart-temperature-air")
        val temperatureElements = temperatureSection.select("temperature-value")
        for (element in temperatureElements) {
            val temperatureValue = element.attr("value").toDoubleOrNull() ?: continue
            val fromUnit = element.attr("from-unit")
            if (fromUnit == "f") {
                val celsius = fahrenheitToCelsius(temperatureValue)
                temperatures.add(celsius)
            } else {
                temperatures.add(temperatureValue)
            }
        }

        return temperatures
    }

    private fun parsePressureData(html: String): List<Int> {
        val document = Jsoup.parse(html)
        val pressures = mutableListOf<Int>()

        val pressureSection = document.select(".widget-row-chart.widget-row-chart-pressure")

        val pressureElements = pressureSection.select("pressure-value")

        for (element in pressureElements) {
            val pressureValue = element.attr("value").toIntOrNull() ?: continue
            val fromUnit = element.attr("from-unit")
            if (fromUnit == "mmhg") {
                pressures.add(pressureValue)
            } else {
                pressures.add(pressureValue) // временно оставляем как есть
            }
        }

        return pressures
    }

    private fun parsePrecipitationData(html: String): List<Double> {
        val document = Jsoup.parse(html) // Парсим HTML
        val precipitations = mutableListOf<Double>()
        val precipitationSection = document.select(".widget-row-precipitation-bars")
        val precipitationElements = precipitationSection.select(".item-unit")
        for (element in precipitationElements) {
            val precipitationText = element.text().trim().replace(",", ".")
            val precipitationValue = precipitationText.toDoubleOrNull()
            if (precipitationValue != null) {
                precipitations.add(precipitationValue)
            }
        }

        return precipitations
    }
    private fun parseWindData(html: String): List<WindData> {
        val document: Document = Jsoup.parse(html) // Парсим HTML
        val windDataList = mutableListOf<WindData>()

        val windRows = document.select(".widget-row-wind .row-item")

        for (row in windRows) {
            val windSpeedElement = row.select(".wind-speed").first() // Скорость ветра
            val windDirectionElement = row.select(".wind-direction").first() // Направление ветра
            val windGustElement = row.select(".wind-gust").first() // Порывы ветра

            val windSpeed = windSpeedElement?.select("speed-value")?.attr("value")?.toIntOrNull() ?: 0
            val windDirection = windDirectionElement?.text()?.trim() ?: "Неизвестно"
            val windGust = windGustElement?.select("speed-value")?.attr("value")?.toIntOrNull() ?: 0

            windDataList.add(WindData(windSpeed, windDirection, windGust))
        }

        return windDataList
    }
}