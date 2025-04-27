package com.chumakov123.gismeteoweather.data.repo

import com.chumakov123.gismeteoweather.data.dto.toWeatherDTO
import com.chumakov123.gismeteoweather.data.dto.toWeatherData
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.data.remote.GismeteoHtmlFetcher
import com.chumakov123.gismeteoweather.data.remote.GismeteoWeatherHtmlParser
import com.chumakov123.gismeteoweather.domain.model.WeatherData
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables
import com.chumakov123.gismeteoweather.domain.util.WindDirections
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

object WeatherRepo {
    private val cache = mutableMapOf<String, Cached<WeatherInfo>>()
    private const val TTL_MS = 5 * 60 * 1000L

    suspend fun getWeatherInfo(cityCode: String): WeatherInfo {
        val now = System.currentTimeMillis()
        cache[cityCode]?.let { (info, ts) ->
            if (now - ts < TTL_MS) {
                return info
            }
        }

        val fresh = fetchFromGismeteo(cityCode)
        cache[cityCode] = Cached(fresh, now)
        return fresh
    }

    private suspend fun resolveCityCodeOrFallback(
        cityCode: String,
        previousCity: String?
    ): String {
        if (cityCode != "auto") return cityCode

        return try {
            val info = GismeteoApi.fetchCityByIp()

            "${info.slug}-${info.id}"
        } catch (e: Exception) {
            previousCity
                ?: throw IllegalStateException("Не удалось определить город", e)
        }
    }

    private suspend fun fetchFromGismeteo(city: String, previousCity: String? = null): WeatherInfo {
        val cityCode = resolveCityCodeOrFallback(city, previousCity)

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

    private data class Cached<T>(val value: T, val timestamp: Long)

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
}