package com.chumakov123.gismeteoweather.widget

import com.chumakov123.gismeteoweather.data.fetcher.GismeteoHtmlFetcher
import com.chumakov123.gismeteoweather.data.model.toWeatherDTO
import com.chumakov123.gismeteoweather.data.model.toWeatherData
import com.chumakov123.gismeteoweather.data.parser.GismeteoWeatherHtmlParser
import com.chumakov123.gismeteoweather.utils.WeatherDrawables
import com.chumakov123.gismeteoweather.utils.WindDirections
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

object WeatherRepo {

    /**
     * Request the WeatherInfo of a given location
     */
    suspend fun getWeatherInfoFake(delay: Long = Random.nextInt(1, 3) * 1000L): WeatherInfo {
        // Simulate network loading
        if (delay > 0) {
            delay(delay)
        }

        val currentInstant = Clock.System.now()
        val currentDateTime = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        return WeatherInfo.Available(
            placeName = "Ростов-на-дону",
            placeCode = "rostov-na-donu-5110",
            updateTime = System.currentTimeMillis(),
            localTime = currentDateTime,
            now = getRandomWeatherData(),
            hourly = List(16) { getRandomWeatherData() },
            daily = List(10) { getRandomWeatherData() }
        )
    }

    suspend fun getWeatherInfo(cityCode: String): WeatherInfo {
        val todayHtml = GismeteoHtmlFetcher.getTodayHtml(cityCode)
        val tomorrowHtml = GismeteoHtmlFetcher.getTomorrowHtml(cityCode)
        val teenDaysHtml = GismeteoHtmlFetcher.get10DaysHtml(cityCode)
        val now = GismeteoWeatherHtmlParser.parseWeatherNowFromHtml(todayHtml)?.toWeatherDTO()
        val nowWeather = now?.toWeatherData()
        println(now?.windDirection)
        val dateAndCity = GismeteoWeatherHtmlParser.parseDateAndCityFromHtml(todayHtml)
        val hourly = GismeteoWeatherHtmlParser.parseWeatherData(todayHtml) +
                GismeteoWeatherHtmlParser.parseWeatherData(tomorrowHtml)
        val teenDays = GismeteoWeatherHtmlParser.parseWeatherData(teenDaysHtml, hasMinT = true)

        return WeatherInfo.Available(
            placeCode = cityCode,
            placeName = dateAndCity!!.cityName,
            localTime = dateAndCity.localDateTime,
            now = nowWeather!!,
            hourly = hourly,
            daily = teenDays,
            updateTime = System.currentTimeMillis()
        )
    }

    /**
     * Fake the weather data
     */
    private fun getRandomWeatherData(): WeatherData {
        return WeatherData(
            icon = WeatherDrawables.drawableMap.values.random(),
            description = "Облачно",
            temperature = Random.nextInt(20, 35),
            temperatureMin = Random.nextInt(5, 20),
            windSpeed = Random.nextInt(0, 5),
            windDirection = WindDirections.windDirections.values.random(),
            windGust = Random.nextInt(0, 15),
            precipitation = Random.nextDouble(0.0, 10.0),
            pressure = Random.nextInt(700, 800)
        )
    }
}