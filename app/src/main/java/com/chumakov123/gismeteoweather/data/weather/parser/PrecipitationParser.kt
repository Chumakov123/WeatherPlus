package com.chumakov123.gismeteoweather.data.weather.parser

import org.jsoup.nodes.Document

object PrecipitationParser {
    fun parse(doc: Document): List<Double> {
        val precipitations = mutableListOf<Double>()
        val precipitationSection = doc.select(".widget-row-precipitation-bars")
        val precipitationElements = precipitationSection.select(".item-unit")
        for (element in precipitationElements) {
            val precipitationText = element.text().trim().replace(",", ".")
            val precipitationValue = precipitationText.toDoubleOrNull()
            if (precipitationValue != null) {
                precipitations.add(precipitationValue)
            }
        }

        return precipitations
    }
}
