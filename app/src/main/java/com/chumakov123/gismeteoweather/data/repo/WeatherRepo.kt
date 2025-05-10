package com.chumakov123.gismeteoweather.data.repo

import com.chumakov123.gismeteoweather.data.dto.toCurrentWeatherData
import com.chumakov123.gismeteoweather.data.dto.toWeatherDTO
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.data.remote.GismeteoHtmlFetcher
import com.chumakov123.gismeteoweather.data.remote.GismeteoWeatherHtmlParser
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo

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

        val nowHtml = GismeteoHtmlFetcher.getNowHtml(cityCode)
        val todayHtml = GismeteoHtmlFetcher.getTodayHtml(cityCode)
        val tomorrowHtml = GismeteoHtmlFetcher.getTomorrowHtml(cityCode)
        val teenDaysHtml = GismeteoHtmlFetcher.get10DaysHtml(cityCode)
        val now = GismeteoWeatherHtmlParser.parseWeatherNowFromHtml(todayHtml)?.toWeatherDTO()
        val nowWeather = now?.toCurrentWeatherData()
        val hourly = GismeteoWeatherHtmlParser.parseWeatherData(todayHtml) +
                GismeteoWeatherHtmlParser.parseWeatherData(tomorrowHtml)
        val teenDays = GismeteoWeatherHtmlParser.parseWeatherData(teenDaysHtml, hasMinT = true)
        val astroTimes = GismeteoWeatherHtmlParser.parseAstroTimes(nowHtml)
        val dateAndCity = GismeteoWeatherHtmlParser.parseDateAndCityFromHtml(todayHtml)

        return WeatherInfo.Available(
            placeCode = cityCode,
            placeName = dateAndCity!!.cityName,
            localTime = dateAndCity.localDateTime,
            now = nowWeather!!,
            hourly = hourly,
            daily = teenDays,
            updateTime = System.currentTimeMillis(),
            astroTimes = astroTimes!!
        )
    }

    private data class Cached<T>(val value: T, val timestamp: Long)
}