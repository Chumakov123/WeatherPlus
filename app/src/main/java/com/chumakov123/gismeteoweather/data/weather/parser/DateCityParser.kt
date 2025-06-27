package com.chumakov123.gismeteoweather.data.weather.parser

import com.chumakov123.gismeteoweather.data.city.DateAndCityDTO
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.nodes.Document

object DateCityParser {
    fun parse(doc: Document): DateAndCityDTO? {
        val tsRegex = Regex(
            """window\.M\.state\.app\.timestamps\s*=\s*\{[^}]*?now\s*:\s*new Date\(\s*'([^']+)'\s*\)""",
            RegexOption.DOT_MATCHES_ALL
        )
        val outerHtml = doc.outerHtml()
        val tsMatch = tsRegex.find(outerHtml) ?: return null
        val rawDate = tsMatch.groupValues[1] // "2025/04/10 08:04:18"

        // Разбиваем на дату и время
        val (datePart, timePart) = rawDate.split(' ')
        val (yearStr, monthStr, dayStr) = datePart.split('/')
        val (hourStr, minuteStr, secondStr) = timePart.split(':')

        val dateTime = LocalDateTime(
            year = yearStr.toInt(),
            monthNumber = monthStr.toInt(),
            dayOfMonth = dayStr.toInt(),
            hour = hourStr.toInt(),
            minute = minuteStr.toInt(),
            second = secondStr.toInt(),
            nanosecond = 0
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

        val cityKind = root["city"]?.jsonObject
            ?.get("kind")?.jsonPrimitive?.content ?: "T"

        // Преобразуем UTC LocalDateTime в Instant
        val utcInstant = dateTime.toInstant(TimeZone.UTC)
        // Прибавляем смещение в минутах к Instant
        val localInstant = utcInstant.plus(timeZoneOffsetMinutes.toLong(), DateTimeUnit.MINUTE)
        // Преобразуем обратно в LocalDateTime (можно указать TimeZone.UTC, т.к. смещение уже учтено)
        val localDateTime = localInstant.toLocalDateTime(TimeZone.UTC)

        return DateAndCityDTO(localDateTime, cityName, cityKind)
    }
}
