package com.chumakov123.gismeteoweather.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

object GismeteoHtmlFetcher {
    suspend fun getNowHtml(cityCode: String): Document =
        fetchHtmlWithGzip(cityCode, "now/")

    suspend fun getTodayHtml(cityCode: String): Document = // Интервал три часа
        fetchHtmlWithGzip(cityCode)

    suspend fun getHourlyHtml(cityCode: String): Document = // Интервал час
        fetchHtmlWithGzip(cityCode, "hourly/")

    suspend fun getTomorrowHtml(cityCode: String): Document =
        fetchHtmlWithGzip(cityCode, "tomorrow/")

    suspend fun get3DaysHtml(cityCode: String): Document =
        fetchHtmlWithGzip(cityCode, "3-days/")

    suspend fun get10DaysHtml(cityCode: String): Document =
        fetchHtmlWithGzip(cityCode, "10-days/")

    private suspend fun fetchHtmlWithGzip(cityCode: String, path: String = ""): Document {
        val urlString = "https://www.gismeteo.ru/weather-$cityCode/$path"
        return withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "gzip")

            connection.connect()

            val encoding = connection.contentEncoding?.lowercase()
            val rawBytes = connection.inputStream.use { it.readBytes() }

            val htmlBytes = if (encoding == "gzip") {
                GZIPInputStream(rawBytes.inputStream()).use { it.readBytes() }
            } else {
                rawBytes
            }

            Jsoup.parse(htmlBytes.inputStream(), null, urlString)
                .also { connection.disconnect() }
        }
    }
}
