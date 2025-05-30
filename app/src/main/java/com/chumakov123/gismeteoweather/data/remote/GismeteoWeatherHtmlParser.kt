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
import kotlin.math.roundToInt

object GismeteoWeatherHtmlParser {
    fun parseWeatherData(doc: Document, hasMinValues: Boolean = false): List<WeatherData> {
        val temperatureList = parseTemperatureData(doc)
        val temperatureHeatIndexList = parseHeatIndexData(doc)
        val temperatureAvgList = parseTemperatureAvgData(doc)
        val windDataList = parseWindData(doc)
        val precipitationList = parsePrecipitationData(doc)
        val iconList = parseWeatherIcons(doc)
        val pressureList = parsePressureData(doc)
        val geomagneticList = parseGeomagneticData(doc)
        val radiationList = parseRadiationData(doc)
        val humidityList = parseHumidityData(doc)
        val pollenGrassList = parsePollenGrassData(doc)
        val pollenBirchList = parsePollenBirchData(doc)
        val snowHeightList = parseSnowHeightData(doc)
        val fallingSnowList = parseFallingSnowData(doc)

        val weatherDataList = mutableListOf<WeatherData>()

        val size = if (!hasMinValues) temperatureList.size else temperatureList.size / 2

        for (i in 0 until size) {
            val tMax = if (!hasMinValues) temperatureList[i] else temperatureList[i*2]
            val tMin = if (!hasMinValues) null else temperatureList[i*2+1]
            val tAvg = temperatureAvgList.getOrElse(i) { 0 }
            val tHeatIndex = if (!hasMinValues) temperatureHeatIndexList[i] else temperatureHeatIndexList[i*2]
            val tHeatIndexMin = if (!hasMinValues) null else temperatureHeatIndexList[i*2+1]
            val windData = windDataList.getOrElse(i) { WindData(0, "Неизвестно", 0) }
            val precipitation = precipitationList.getOrElse(i) { 0.0 }
            val humidity = humidityList.getOrElse(i) { -1 }
            val geomagnetic = geomagneticList.getOrElse(i) { -1 }
            val radiation = radiationList.getOrElse(i) { -1 }
            val pollenBirch = pollenBirchList.getOrElse(i) { -1 }
            val pollenGrass = pollenGrassList.getOrElse(i) { -1 }
            val snowHeight = snowHeightList.getOrElse(i) { -1.0 }
            val fallingSnow = fallingSnowList.getOrElse(i) { -1.0 }
            val pressure = if (!hasMinValues) pressureList[i] else pressureList[i*2]
            val pressureMin = if (!hasMinValues) null else pressureList[i*2+1]
            val icon = iconList.getOrElse(i) { WeatherIconInfo("Неизвестно", null, null) }

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
                temperatureAvg = tAvg,
                temperatureHeatIndex = tHeatIndex,
                temperatureHeatIndexMin = tHeatIndexMin,
                windSpeed = windData.speed,
                windDirection = windData.direction,
                windGust = windData.gust,
                precipitation = precipitation,
                pressure = pressure,
                pressureMin = pressureMin,
                humidity = humidity,
                radiation = radiation,
                geomagnetic = geomagnetic,
                pollenGrass = pollenGrass,
                pollenBirch = pollenBirch,
                snowHeight = snowHeight,
                fallingSnow = fallingSnow,
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
        val section = doc.selectFirst(".widget-row-chart.widget-row-chart-temperature-air")
            ?: return temperatures

        val hasMaxElements = section.selectFirst(".maxt temperature-value") != null

        if (hasMaxElements) {
            val valueContainers = section.select(".values .value")
            for (vc in valueContainers) {
                val maxElem = vc.selectFirst(".maxt temperature-value")
                val rawMax = maxElem
                    ?.attr("value")
                    ?.toDoubleOrNull()
                    ?: continue
                val unitMax = maxElem.attr("from-unit")

                val minElem = vc.selectFirst(".mint temperature-value")
                val rawMin = minElem
                    ?.attr("value")
                    ?.toDoubleOrNull()
                    ?: rawMax
                val unitMin = minElem?.attr("from-unit") ?: unitMax

                val maxC = if (unitMax == "f") fahrenheitToCelsius(rawMax) else rawMax
                val minC = if (unitMin == "f") fahrenheitToCelsius(rawMin) else rawMin

                temperatures.add(maxC)
                temperatures.add(minC)
            }
        } else {
            val elems = section.select("temperature-value")
            for (el in elems) {
                val raw = el.attr("value").toDoubleOrNull() ?: continue
                val unit = el.attr("from-unit")
                val celsius = if (unit == "f") fahrenheitToCelsius(raw) else raw
                temperatures.add(celsius)
            }
        }

        return temperatures
    }

    private fun parseHeatIndexData(doc: Document): List<Int> {
        val heatIndices = mutableListOf<Int>()
        // Секция с индексом жары
        val section = doc.selectFirst(
            ".widget-row-chart" +
                    ".widget-row-chart-temperature-heat-index" +
                    ".row-with-caption"
        ) ?: return heatIndices

        val hasMaxElements = section.selectFirst(".maxt temperature-value") != null

        if (hasMaxElements) {
            val valueContainers = section.select(".values .value")
            for (vc in valueContainers) {
                val maxElem = vc.selectFirst(".maxt temperature-value")
                val rawMax = maxElem
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: continue
                val unitMax = maxElem.attr("from-unit")

                val minElem = vc.selectFirst(".mint temperature-value")
                val rawMin = minElem
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: rawMax
                val unitMin = minElem?.attr("from-unit") ?: unitMax

                val maxC = if (unitMax == "f") fahrenheitToCelsius(rawMax.toDouble()).roundToInt() else rawMax
                val minC = if (unitMin == "f") fahrenheitToCelsius(rawMin.toDouble()).roundToInt() else rawMin

                heatIndices.add(maxC)
                heatIndices.add(minC)
            }
        } else {
            val elems = section.select("temperature-value")
            for (el in elems) {
                val raw = el.attr("value").toIntOrNull() ?: continue
                val unit = el.attr("from-unit")
                val celsiusIndex = if (unit == "f") {
                    fahrenheitToCelsius(raw.toDouble()).roundToInt()
                } else {
                    raw
                }
                heatIndices.add(celsiusIndex)
            }
        }

        return heatIndices
    }

    private fun parseTemperatureAvgData(doc: Document): List<Int> {
        val avgTemperatures = mutableListOf<Int>()

        val avgTempSection = doc.select(
            ".widget-row-chart" +
                    ".widget-row-chart-temperature-avg" +
                    ".row-with-caption"
        )

        val avgTempElements = avgTempSection.select("temperature-value")

        for (element in avgTempElements) {
            val rawValue = element.attr("value").toDoubleOrNull() ?: continue
            val fromUnit = element.attr("from-unit")

            val celsiusValue = if (fromUnit == "f") {
                fahrenheitToCelsius(rawValue)
            } else {
                rawValue
            }

            avgTemperatures.add(celsiusValue.roundToInt())
        }

        return avgTemperatures
    }

    private fun parseGeomagneticData(doc: Document): List<Int> {
        val geomagneticValues = mutableListOf<Int>()
        val geomagneticSection = doc.select(
            ".widget-row" +
                    ".widget-row-geomagnetic" +
                    ".row-with-caption"
        )
        val geomagneticElements = geomagneticSection.select("div.row-item")

        for (element in geomagneticElements) {
            element.text()
                .trim()
                .toIntOrNull()
                ?.let { geomagneticValues.add(it) }
        }

        return geomagneticValues
    }

    private fun parseRadiationData(doc: Document): List<Int> {
        val radiationValues = mutableListOf<Int>()
        val radiationSection = doc.select(
            ".widget-row" +
                    ".widget-row-radiation" +
                    ".row-with-caption"
        )
        val radiationElements = radiationSection.select("div.row-item")

        for (element in radiationElements) {
            element.text()
                .trim()
                .toIntOrNull()
                ?.let { radiationValues.add(it) }
        }

        return radiationValues
    }

    private fun parseHumidityData(doc: Document): List<Int> {
        val humidities = mutableListOf<Int>()
        val humiditySection = doc.select(".widget-row.widget-row-humidity.row-with-caption")
        val humidityElements = humiditySection.select("div.row-item")
        for (element in humidityElements) {
            element.text()
                .trim()
                .toIntOrNull()
                ?.let { humidities.add(it) }
        }

        return humidities
    }

    private fun parsePollenGrassData(doc: Document): List<Int> {
        val pollenValues = mutableListOf<Int>()
        val pollenSection = doc.select(
            ".widget-row" +
                    ".widget-row-pollen" +
                    ".row-pollen-grass" +
                    ".row-with-caption"
        )
        val pollenElements = pollenSection.select("div.row-item")

        for (element in pollenElements) {
            val isNoData = element.selectFirst(".nodata") != null

            if (isNoData) {
                pollenValues.add(-1)
            } else {
                val value = element.text().trim().toIntOrNull() ?: -1
                pollenValues.add(value)
            }
        }

        return pollenValues
    }

    private fun parsePollenBirchData(doc: Document): List<Int> {
        val pollenValues = mutableListOf<Int>()
        val birchSection = doc.select(
            ".widget-row" +
                    ".widget-row-pollen" +
                    ".row-pollen-birch" +
                    ".row-with-caption"
        )
        val pollenElements = birchSection.select("div.row-item")

        for (element in pollenElements) {
            val itemElement = element.selectFirst("div.item")

            val value = itemElement?.text()?.trim()?.toIntOrNull() ?: -1
            pollenValues.add(value)
        }

        return pollenValues
    }

    private fun parseSnowHeightData(doc: Document): List<Double> {
        val snowHeights = mutableListOf<Double>()
        val snowSection = doc.select(
            ".widget-row" +
                    ".widget-row-icon-snow" +
                    ".row-with-caption"
        )
        val snowElements = snowSection.select("div.row-item")

        for (element in snowElements) {
            val valueElement = element.selectFirst("div.value")
            val textValue = valueElement?.text()?.replace(",", ".")?.trim()
            val parsedValue = textValue?.toDoubleOrNull() ?: 0.0
            snowHeights.add(parsedValue)
        }

        return snowHeights
    }

    private fun parseFallingSnowData(doc: Document): List<Double> {
        val res = mutableListOf<Double>()
        val chartElement = doc.selectFirst(".chart.chart-snow.js-chart-snow")

        val dataSnow = chartElement?.attr("data-snow") ?: return res
        val values = dataSnow
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
        for (value in values) {
            val snowValue = value.trim().toDoubleOrNull() ?: continue
            res.add(snowValue)
        }
        return res
    }


    private fun parsePressureData(doc: Document): List<Int> {
        val pressures = mutableListOf<Int>()

        val section = doc.selectFirst(".widget-row-chart.widget-row-chart-pressure")
            ?: return pressures

        val hasMaxElements = section.selectFirst(".maxt pressure-value") != null

        if (hasMaxElements) {
            val valueContainers = section.select(".values .value")

            for (vc in valueContainers) {
                val maxVal = vc
                    .selectFirst(".maxt pressure-value")
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: continue

                val minVal = vc
                    .selectFirst(".mint pressure-value")
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: maxVal

                pressures.add(maxVal)
                pressures.add(minVal)
            }
        } else {
            val elems = section.select("pressure-value")
            for (el in elems) {
                el.attr("value")
                    .toIntOrNull()
                    ?.let { pressures.add(it) }
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