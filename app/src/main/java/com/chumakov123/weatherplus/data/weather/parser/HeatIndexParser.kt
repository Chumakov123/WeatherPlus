package com.chumakov123.weatherplus.data.weather.parser

import com.chumakov123.weatherplus.domain.util.Utils.fahrenheitToCelsius
import org.jsoup.nodes.Document
import kotlin.math.roundToInt

object HeatIndexParser {
    fun parse(doc: Document): List<Int> {
        val heatIndices = mutableListOf<Int>()
        // Секция с индексом жары
        val section = doc.selectFirst(
            ".widget-row-chart" +
                    ".widget-row-chart-temperature-heat-index" +
                    ".row-with-caption"
        ) ?: return heatIndices

        val hasMaxElements = section.selectFirst(".maxt temperature-value") != null

        if (hasMaxElements) {
            val valueContainers = section.select(".values .value")
            for (vc in valueContainers) {
                val maxElem = vc.selectFirst(".maxt temperature-value")
                val rawMax = maxElem
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: continue
                val unitMax = maxElem.attr("from-unit")

                val minElem = vc.selectFirst(".mint temperature-value")
                val rawMin = minElem
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: rawMax
                val unitMin = minElem?.attr("from-unit") ?: unitMax

                val maxC = if (unitMax == "f") fahrenheitToCelsius(rawMax.toDouble()).roundToInt() else rawMax
                val minC = if (unitMin == "f") fahrenheitToCelsius(rawMin.toDouble()).roundToInt() else rawMin

                heatIndices.add(maxC)
                heatIndices.add(minC)
            }
        } else {
            val elems = section.select("temperature-value")
            for (el in elems) {
                val raw = el.attr("value").toIntOrNull() ?: continue
                val unit = el.attr("from-unit")
                val celsiusIndex = if (unit == "f") {
                    fahrenheitToCelsius(raw.toDouble()).roundToInt()
                } else {
                    raw
                }
                heatIndices.add(celsiusIndex)
            }
        }

        return heatIndices
    }
}
