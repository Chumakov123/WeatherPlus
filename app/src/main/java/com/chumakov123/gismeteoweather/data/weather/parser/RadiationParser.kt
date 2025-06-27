package com.chumakov123.gismeteoweather.data.weather.parser

import org.jsoup.nodes.Document

object RadiationParser {
    fun parse(doc: Document): List<Int> {
        val radiationValues = mutableListOf<Int>()
        val radiationSection = doc.select(
            ".widget-row" +
                    ".widget-row-radiation" +
                    ".row-with-caption"
        )
        val radiationElements = radiationSection.select("div.row-item")

        for (element in radiationElements) {
            element.text()
                .trim()
                .toIntOrNull()
                ?.let { radiationValues.add(it) }
        }

        return radiationValues
    }
}