package com.chumakov123.weatherplus.data.weather.parser

import com.chumakov123.weatherplus.domain.model.WindData
import org.jsoup.nodes.Document

object WindParser {
    fun parse(doc: Document): List<WindData> {
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
