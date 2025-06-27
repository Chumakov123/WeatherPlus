package com.chumakov123.gismeteoweather.data.weather.parser

import com.chumakov123.gismeteoweather.domain.util.Utils.fahrenheitToCelsius
import org.jsoup.nodes.Document

object TemperatureParser {
    fun parse(doc: Document): List<Double> {
        val temperatures = mutableListOf<Double>()
        val section = doc.selectFirst(".widget-row-chart.widget-row-chart-temperature-air")
            ?: return temperatures

        val hasMaxElements = section.selectFirst(".maxt temperature-value") != null

        if (hasMaxElements) {
            val valueContainers = section.select(".values .value")
            for (vc in valueContainers) {
                val maxElem = vc.selectFirst(".maxt temperature-value")
                val rawMax = maxElem
                    ?.attr("value")
                    ?.toDoubleOrNull()
                    ?: continue
                val unitMax = maxElem.attr("from-unit")

                val minElem = vc.selectFirst(".mint temperature-value")
                val rawMin = minElem
                    ?.attr("value")
                    ?.toDoubleOrNull()
                    ?: rawMax
                val unitMin = minElem?.attr("from-unit") ?: unitMax

                val maxC = if (unitMax == "f") fahrenheitToCelsius(rawMax) else rawMax
                val minC = if (unitMin == "f") fahrenheitToCelsius(rawMin) else rawMin

                temperatures.add(maxC)
                temperatures.add(minC)
            }
        } else {
            val elems = section.select("temperature-value")
            for (el in elems) {
                val raw = el.attr("value").toDoubleOrNull() ?: continue
                val unit = el.attr("from-unit")
                val celsius = if (unit == "f") fahrenheitToCelsius(raw) else raw
                temperatures.add(celsius)
            }
        }

        return temperatures
    }
}
