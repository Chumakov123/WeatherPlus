package com.chumakov123.gismeteoweather.data.remote

import android.util.Log
import com.chumakov123.gismeteoweather.data.dto.CityByIpResponse
import com.chumakov123.gismeteoweather.data.mapper.parseCityJsonKxSafely
import com.chumakov123.gismeteoweather.data.mapper.toCityInfo
import com.chumakov123.gismeteoweather.domain.model.CityInfo
import com.chumakov123.gismeteoweather.domain.util.JsonConfig.AppJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object GismeteoApi {
    private const val TAG = "GismeteoApi"
    private const val BASE_URL = "https://www.gismeteo.ru"
    private const val CITY_BY_IP_URL = "$BASE_URL/mq/city/ip/"
    private const val CITY_SEARCH_URL = "$BASE_URL/mq/city/q"

    suspend fun fetchCityByIp(): CityInfo = withContext(Dispatchers.IO) {
        val response = Jsoup.connect(CITY_BY_IP_URL)
            .ignoreContentType(true)
            .execute()

        val body = response.body()
        parseCityJsonKxSafely(body)
    }

    suspend fun searchCitiesByName(
        query: String,
        limit: Int = 10
    ): List<CityInfo> = withContext(Dispatchers.IO) {
        val url = "$CITY_SEARCH_URL/${query.trim().replace(' ', '+')}/?limit=$limit"
        val raw = try {
            Jsoup.connect(url)
                .ignoreContentType(true)
                .execute()
                .body()
        } catch (e: Exception) {
            Log.e(TAG, "searchCitiesByName: network error", e)
            return@withContext emptyList()
        }

        Log.d(TAG, "raw city search response: $raw")

        return@withContext runCatching {
            val listDto: List<CityByIpResponse> =
                AppJson.decodeFromString(raw)
            listDto.map { it.toCityInfo() }
        }.getOrElse { e ->
            Log.e(TAG, "searchCitiesByName: JSON parse error", e)
            emptyList()
        }
    }
}
