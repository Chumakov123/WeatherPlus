package com.chumakov123.gismeteoweather.data.weather.parser

import com.chumakov123.gismeteoweather.domain.util.Utils.fahrenheitToCelsius
import org.jsoup.nodes.Document
import kotlin.math.roundToInt

object TemperatureAvgParser {
    fun parse(doc: Document): List<Int> {
        val avgTemperatures = mutableListOf<Int>()

        val avgTempSection = doc.select(
            ".widget-row-chart" +
                    ".widget-row-chart-temperature-avg" +
                    ".row-with-caption"
        )

        val avgTempElements = avgTempSection.select("temperature-value")

        for (element in avgTempElements) {
            val rawValue = element.attr("value").toDoubleOrNull() ?: continue
            val fromUnit = element.attr("from-unit")

            val celsiusValue = if (fromUnit == "f") {
                fahrenheitToCelsius(rawValue)
            } else {
                rawValue
            }

            avgTemperatures.add(celsiusValue.roundToInt())
        }

        return avgTemperatures
    }
}
