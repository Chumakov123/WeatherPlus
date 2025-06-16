package com.chumakov123.gismeteoweather.data.repo

import androidx.datastore.core.DataStore
import com.chumakov123.gismeteoweather.WeatherCacheOuterClass
import com.chumakov123.gismeteoweather.data.dto.toCurrentWeatherData
import com.chumakov123.gismeteoweather.data.dto.toWeatherDTO
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.data.remote.GismeteoHtmlFetcher
import com.chumakov123.gismeteoweather.data.remote.GismeteoWeatherHtmlParser
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

object WeatherRepo {
    private val cache = mutableMapOf<String, Cached<WeatherInfo>>()
    private const val TTL_MS = 5 * 60 * 1000L

    private lateinit var dataStore: DataStore<WeatherCacheOuterClass.WeatherCache>
    private val json = Json { encodeDefaults = true }

    fun init(dataStore: DataStore<WeatherCacheOuterClass.WeatherCache>) {
        this.dataStore = dataStore
    }

    suspend fun getWeatherInfo(
        cityCode: String,
        allowStale: Boolean = false
    ): WeatherInfo {
        val now = System.currentTimeMillis()

        cache[cityCode]?.let { (info, ts) ->
            if (allowStale || isActual(ts)) {
                return info
            }
        }

        val dsEntry = dataStore.data
            .map { it.entriesList.find { it.cityCode == cityCode } }
            .firstOrNull()

        if (dsEntry != null && (allowStale || now - dsEntry.timestamp < TTL_MS)) {
            runCatching {
                val avail = json.decodeFromString<WeatherInfo.Available>(
                    dsEntry.payload.toByteArray().decodeToString()
                )
                cache[cityCode] = Cached(avail, dsEntry.timestamp)
                return avail
            }.onFailure { throwable ->
                throwable.printStackTrace()
            }
        }

        val fresh = fetchFromGismeteo(cityCode)
        cache[cityCode] = Cached(fresh, now)

        if (fresh is WeatherInfo.Available) {
            saveToDataStore(cityCode, fresh, now)
        }

        return fresh
    }

    fun isActual(weatherUpdateTime: Long, referenceTime: Long = System.currentTimeMillis(), ttlMs: Long = 5 * 60 * 1000L): Boolean {
        return referenceTime - weatherUpdateTime < ttlMs
    }

    private suspend fun saveToDataStore(cityCode: String, info: WeatherInfo.Available, timestamp: Long) {
        val payload = json.encodeToString(info).encodeToByteArray()
        dataStore.updateData { cache ->
            val builder = cache.toBuilder().clearEntries()
            builder.addAllEntries(
                cache.entriesList.filter { it.cityCode != cityCode }
            )
            builder.addEntries(
                WeatherCacheOuterClass.WeatherCache.Entry.newBuilder()
                    .setCityCode(cityCode)
                    .setPayload(ByteString.copyFrom(payload))
                    .setTimestamp(timestamp)
                    .build()
            )
            builder.build()
        }
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
        val teenDays = GismeteoWeatherHtmlParser.parseWeatherData(teenDaysHtml, hasMinValues = true)
        val astroTimes = GismeteoWeatherHtmlParser.parseAstroTimes(nowHtml)
        val dateAndCity = GismeteoWeatherHtmlParser.parseDateAndCityFromHtml(todayHtml)

        return WeatherInfo.Available(
            placeCode = cityCode,
            placeName = dateAndCity!!.cityName,
            placeKind = dateAndCity.cityKind,
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