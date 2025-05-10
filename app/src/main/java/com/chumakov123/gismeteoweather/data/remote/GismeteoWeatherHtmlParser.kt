package com.chumakov123.gismeteoweather.data.remote

import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.data.dto.DateAndCityDTO
import com.chumakov123.gismeteoweather.data.dto.WeatherRawDTO
import com.chumakov123.gismeteoweather.domain.model.AstroTimes
import com.chumakov123.gismeteoweather.domain.model.WeatherData
import com.chumakov123.gismeteoweather.domain.model.WeatherIconInfo
import com.chumakov123.gismeteoweather.domain.model.WindData
import com.chumakov123.gismeteoweather.domain.util.Utils.fahrenheitToCelsius
import com.chumakov123.gismeteoweather.domain.util.Utils.normalizeIconString
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.nodes.Document

object GismeteoWeatherHtmlParser {
    fun parseWeatherData(doc: Document, hasMinT: Boolean = false): List<WeatherData> {
        val temperatures = parseTemperatureData(doc)
        val windDataList = parseWindData(doc)
        val precipitations = parsePrecipitationData(doc)
        val icons = parseWeatherIcons(doc)
        val pressures = parsePressureData(doc)

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

            iconString = normalizeIconString(iconString)

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

    fun parseAstroTimes(doc: Document): AstroTimes? {
        val sunriseText = doc.selectFirst(".now-astro-sunrise .time")?.text()
        val sunsetText  = doc.selectFirst(".now-astro-sunset .time")?.text()

        if (sunriseText == null || sunsetText == null) return null

        val sunriseParts = sunriseText.split(":")
        val sunsetParts  = sunsetText.split(":")

        if (sunriseParts.size != 2 || sunsetParts.size != 2) return null

        val sunrise = LocalTime(
            sunriseParts[0].toIntOrNull() ?: return null,
            sunriseParts[1].toIntOrNull() ?: return null
        )

        val sunset = LocalTime(
            sunsetParts[0].toIntOrNull() ?: return null,
            sunsetParts[1].toIntOrNull() ?: return null
        )

        return AstroTimes(sunset = sunset, sunrise = sunrise)
    }

    fun parseWeatherNowFromHtml(doc: Document): WeatherRawDTO? {
        val regex = Regex("""window\.M\.state\s*=\s*(\{.*?\})(?=\s*</script>)""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = regex.find(doc.outerHtml()) ?: return null
        val jsonString = matchResult.groupValues[1]
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(jsonString).jsonObject
        val cwJson = root["weather"]?.jsonObject?.get("cw") ?: return null
        return json.decodeFromJsonElement<WeatherRawDTO>(cwJson)
    }

    fun parseDateAndCityFromHtml(doc: Document): DateAndCityDTO? {
        val tsRegex = Regex(
            """window\.M\.state\.app\.timestamps\s*=\s*\{[^}]*?now\s*:\s*new Date\(\s*'([^']+)'\s*\)""",
            RegexOption.DOT_MATCHES_ALL
        )
        val outerHtml = doc.outerHtml()
        val tsMatch = tsRegex.find(outerHtml) ?: return null
        val rawDate = tsMatch.groupValues[1]  // "2025/04/10 08:04:18"

        // Разбиваем на дату и время
        val (datePart, timePart) = rawDate.split(' ')
        val (yearStr, monthStr, dayStr) = datePart.split('/')
        val (hourStr, minuteStr, secondStr) = timePart.split(':')

        val dateTime = LocalDateTime(
            year        = yearStr.toInt(),
            monthNumber = monthStr.toInt(),
            dayOfMonth  = dayStr.toInt(),
            hour        = hourStr.toInt(),
            minute      = minuteStr.toInt(),
            second      = secondStr.toInt(),
            nanosecond  = 0
        )

        // Извлекаем JSON из HTML
        val regex = Regex("""window\.M\.state\s*=\s*(\{.*?\})(?=\s*</script>)""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = regex.find(outerHtml) ?: return null
        val jsonString = matchResult.groupValues[1]

        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(jsonString).jsonObject

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

        // Преобразуем UTC LocalDateTime в Instant
        val utcInstant = dateTime.toInstant(TimeZone.UTC)
        // Прибавляем смещение в минутах к Instant
        val localInstant = utcInstant.plus(timeZoneOffsetMinutes.toLong(), DateTimeUnit.MINUTE)
        // Преобразуем обратно в LocalDateTime (можно указать TimeZone.UTC, т.к. смещение уже учтено)
        val localDateTime = localInstant.toLocalDateTime(TimeZone.UTC)

        return DateAndCityDTO(localDateTime, cityName)
    }

    private fun parseWeatherIcons(doc: Document): List<WeatherIconInfo> {
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

    private fun parseTemperatureData(doc: Document): List<Double> {
        val temperatures = mutableListOf<Double>()
        val temperatureSection = doc.select(".widget-row-chart.widget-row-chart-temperature-air")
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

    private fun parsePressureData(doc: Document): List<Int> {
        val pressures = mutableListOf<Int>()

        val pressureSection = doc.select(".widget-row-chart.widget-row-chart-pressure")

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

    private fun parsePrecipitationData(doc: Document): List<Double> {
        val precipitations = mutableListOf<Double>()
        val precipitationSection = doc.select(".widget-row-precipitation-bars")
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
    private fun parseWindData(doc: Document): List<WindData> {
        val windDataList = mutableListOf<WindData>()

        val windRows = doc.select(".widget-row-wind .row-item")

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