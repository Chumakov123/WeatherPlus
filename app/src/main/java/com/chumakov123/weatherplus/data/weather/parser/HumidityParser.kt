package com.chumakov123.weatherplus.data.weather.parser

import org.jsoup.nodes.Document

object HumidityParser {
    fun parse(doc: Document): List<Int> {
        val humidities = mutableListOf<Int>()
        val humiditySection = doc.select(".widget-row.widget-row-humidity.row-with-caption")
        val humidityElements = humiditySection.select("div.row-item")
        for (element in humidityElements) {
            element.text()
                .trim()
                .toIntOrNull()
                ?.let { humidities.add(it) }
        }

        return humidities
    }
}
