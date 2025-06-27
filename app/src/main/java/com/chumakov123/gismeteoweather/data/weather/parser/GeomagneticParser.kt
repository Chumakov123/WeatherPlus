package com.chumakov123.gismeteoweather.data.weather.parser

import org.jsoup.nodes.Document

object GeomagneticParser {
    fun parse(doc: Document): List<Int> {
        val geomagneticValues = mutableListOf<Int>()
        val geomagneticSection = doc.select(
            ".widget-row" +
                    ".widget-row-geomagnetic" +
                    ".row-with-caption"
        )
        val geomagneticElements = geomagneticSection.select("div.row-item")

        for (element in geomagneticElements) {
            element.text()
                .trim()
                .toIntOrNull()
                ?.let { geomagneticValues.add(it) }
        }

        return geomagneticValues
    }
}
