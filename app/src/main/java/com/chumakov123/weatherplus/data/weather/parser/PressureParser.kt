package com.chumakov123.weatherplus.data.weather.parser

import org.jsoup.nodes.Document

object PressureParser {
    fun parse(doc: Document): List<Int> {
        val pressures = mutableListOf<Int>()

        val section = doc.selectFirst(".widget-row-chart.widget-row-chart-pressure")
            ?: return pressures

        val hasMaxElements = section.selectFirst(".maxt pressure-value") != null

        if (hasMaxElements) {
            val valueContainers = section.select(".values .value")

            for (vc in valueContainers) {
                val maxVal = vc
                    .selectFirst(".maxt pressure-value")
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: continue

                val minVal = vc
                    .selectFirst(".mint pressure-value")
                    ?.attr("value")
                    ?.toIntOrNull()
                    ?: maxVal

                pressures.add(maxVal)
                pressures.add(minVal)
            }
        } else {
            val elems = section.select("pressure-value")
            for (el in elems) {
                el.attr("value")
                    .toIntOrNull()
                    ?.let { pressures.add(it) }
            }
        }

        return pressures
    }
}
