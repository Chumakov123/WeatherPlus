package com.chumakov123.gismeteoweather.data.weather.parser

import org.jsoup.nodes.Document

object SnowParser {
    fun parseHeight(doc: Document): List<Double> {
        val snowHeights = mutableListOf<Double>()
        val snowSection = doc.select(
            ".widget-row" +
                    ".widget-row-icon-snow" +
                    ".row-with-caption"
        )
        val snowElements = snowSection.select("div.row-item")

        for (element in snowElements) {
            val valueElement = element.selectFirst("div.value")
            val textValue = valueElement?.text()?.replace(",", ".")?.trim()
            val parsedValue = textValue?.toDoubleOrNull() ?: 0.0
            snowHeights.add(parsedValue)
        }

        return snowHeights
    }
    fun parseFalling(doc: Document): List<Double> {
        val res = mutableListOf<Double>()
        val chartElement = doc.selectFirst(".chart.chart-snow.js-chart-snow")

        val dataSnow = chartElement?.attr("data-snow") ?: return res
        val values = dataSnow
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
        for (value in values) {
            val snowValue = value.trim().toDoubleOrNull() ?: continue
            res.add(snowValue)
        }
        return res
    }
}