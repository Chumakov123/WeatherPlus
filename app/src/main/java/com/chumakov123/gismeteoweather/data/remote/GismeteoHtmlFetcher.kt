package com.chumakov123.gismeteoweather.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object GismeteoHtmlFetcher {
    suspend fun getNowHtml(cityCode: String): String =
        fetchHtml(cityCode, "now/")

    suspend fun getTodayHtml(cityCode: String): String =
        fetchHtml(cityCode)

    suspend fun getTomorrowHtml(cityCode: String): String =
        fetchHtml(cityCode, "tomorrow/")

    suspend fun get3DaysHtml(cityCode: String): String =
        fetchHtml(cityCode, "3-days/")

    suspend fun get10DaysHtml(cityCode: String): String =
        fetchHtml(cityCode, "10-days/")

    private suspend fun fetchHtml(cityCode: String, path: String = ""): String {
        val url = "https://www.gismeteo.ru/weather-$cityCode/$path"
        return withContext(Dispatchers.IO) {
            Jsoup.connect(url).get().outerHtml()
        }
    }
}
