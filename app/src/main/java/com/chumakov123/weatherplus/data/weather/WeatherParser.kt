package com.chumakov123.weatherplus.data.weather

import com.chumakov123.weatherplus.data.weather.parser.GeomagneticParser
import com.chumakov123.weatherplus.data.weather.parser.HeatIndexParser
import com.chumakov123.weatherplus.data.weather.parser.HumidityParser
import com.chumakov123.weatherplus.data.weather.parser.IconParser
import com.chumakov123.weatherplus.data.weather.parser.PollenParser
import com.chumakov123.weatherplus.data.weather.parser.PrecipitationParser
import com.chumakov123.weatherplus.data.weather.parser.PressureParser
import com.chumakov123.weatherplus.data.weather.parser.RadiationParser
import com.chumakov123.weatherplus.data.weather.parser.SnowParser
import com.chumakov123.weatherplus.data.weather.parser.TemperatureAvgParser
import com.chumakov123.weatherplus.data.weather.parser.TemperatureParser
import com.chumakov123.weatherplus.data.weather.parser.WindParser
import com.chumakov123.weatherplus.domain.model.WeatherData
import com.chumakov123.weatherplus.domain.model.WeatherIconInfo
import com.chumakov123.weatherplus.domain.model.WindData
import com.chumakov123.weatherplus.domain.util.Utils.normalizeIconString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.jsoup.nodes.Document

object WeatherParser {
    fun parseWeatherData(doc: Document, hasMinValues: Boolean = false): List<WeatherData> {
        val temperatureList = TemperatureParser.parse(doc)
        val temperatureHeatIndexList = HeatIndexParser.parse(doc)
        val temperatureAvgList = TemperatureAvgParser.parse(doc)
        val windDataList = WindParser.parse(doc)
        val precipitationList = PrecipitationParser.parse(doc)
        val iconList = IconParser.parse(doc)
        val pressureList = PressureParser.parse(doc)
        val geomagneticList = GeomagneticParser.parse(doc)
        val radiationList = RadiationParser.parse(doc)
        val humidityList = HumidityParser.parse(doc)
        val pollenGrassList = PollenParser.parseGrass(doc)
        val pollenBirchList = PollenParser.parseBirch(doc)
        val snowHeightList = SnowParser.parseHeight(doc)
        val fallingSnowList = SnowParser.parseFalling(doc)

        val weatherDataList = mutableListOf<WeatherData>()

        val size = if (!hasMinValues) temperatureList.size else temperatureList.size / 2

        for (i in 0 until size) {
            val tMax = if (!hasMinValues) temperatureList[i] else temperatureList[i * 2]
            val tMin = if (!hasMinValues) null else temperatureList[i * 2 + 1]
            val tAvg = temperatureAvgList.getOrElse(i) { 0 }
            val tHeatIndex = if (!hasMinValues) temperatureHeatIndexList[i] else temperatureHeatIndexList[i * 2]
            val tHeatIndexMin = if (!hasMinValues) null else temperatureHeatIndexList[i * 2 + 1]
            val windData = windDataList.getOrElse(i) { WindData(0, "Неизвестно", 0) }
            val precipitation = precipitationList.getOrElse(i) { 0.0 }
            val humidity = humidityList.getOrElse(i) { -1 }
            val geomagnetic = geomagneticList.getOrElse(i) { -1 }
            val radiation = radiationList.getOrElse(i) { -1 }
            val pollenBirch = pollenBirchList.getOrElse(i) { -1 }
            val pollenGrass = pollenGrassList.getOrElse(i) { -1 }
            val snowHeight = snowHeightList.getOrElse(i) { -1.0 }
            val fallingSnow = fallingSnowList.getOrElse(i) { -1.0 }
            val pressure = if (!hasMinValues) pressureList[i] else pressureList[i * 2]
            val pressureMin = if (!hasMinValues) null else pressureList[i * 2 + 1]
            val icon = iconList.getOrElse(i) { WeatherIconInfo("Неизвестно", null, null) }

            var iconString =
                if (icon.bottomLayer != null) {
                    "${icon.topLayer}_${icon.bottomLayer}"
                } else {
                    "${icon.topLayer}"
                }

            iconString = normalizeIconString(iconString)

            val weatherData = WeatherData(
                description = icon.tooltip,
                icon = iconString,
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

    fun parseWeatherNowFromHtml(doc: Document): WeatherRawDTO? {
        val regex = Regex("""window\.M\.state\s*=\s*(\{.*?\})(?=\s*</script>)""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = regex.find(doc.outerHtml()) ?: return null
        val jsonString = matchResult.groupValues[1]
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(jsonString).jsonObject
        val cwJson = root["weather"]?.jsonObject?.get("cw") ?: return null
        return json.decodeFromJsonElement<WeatherRawDTO>(cwJson)
    }
}
