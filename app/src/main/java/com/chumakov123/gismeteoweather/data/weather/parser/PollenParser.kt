package com.chumakov123.gismeteoweather.data.weather.parser

import org.jsoup.nodes.Document

object PollenParser {
    fun parseGrass(doc: Document): List<Int> {
        val pollenValues = mutableListOf<Int>()
        val pollenSection = doc.select(
            ".widget-row" +
                    ".widget-row-pollen" +
                    ".row-pollen-grass" +
                    ".row-with-caption"
        )
        val pollenElements = pollenSection.select("div.row-item")

        for (element in pollenElements) {
            val isNoData = element.selectFirst(".nodata") != null

            if (isNoData) {
                pollenValues.add(-1)
            } else {
                val value = element.text().trim().toIntOrNull() ?: -1
                pollenValues.add(value)
            }
        }

        return pollenValues
    }

    fun parseBirch(doc: Document): List<Int> {
        val pollenValues = mutableListOf<Int>()
        val birchSection = doc.select(
            ".widget-row" +
                    ".widget-row-pollen" +
                    ".row-pollen-birch" +
                    ".row-with-caption"
        )
        val pollenElements = birchSection.select("div.row-item")

        for (element in pollenElements) {
            val itemElement = element.selectFirst("div.item")

            val value = itemElement?.text()?.trim()?.toIntOrNull() ?: -1
            pollenValues.add(value)
        }

        return pollenValues
    }
}
