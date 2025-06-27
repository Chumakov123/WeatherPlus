package com.chumakov123.gismeteoweather.data.weather.parser

import com.chumakov123.gismeteoweather.domain.model.WeatherIconInfo
import org.jsoup.nodes.Document

object IconParser {
    fun parse(doc: Document): List<WeatherIconInfo> {
        val result = mutableListOf<WeatherIconInfo>()

        val items = doc.select(".widget-row-icon .row-item")
        for (item in items) {
            val tooltip = item.attr("data-tooltip")
            val topUse = item.selectFirst("svg.top-layer use")
            val bottomUse = item.selectFirst("svg.bottom-layer use")

            val topLayer = topUse?.attr("href")?.removePrefix("#")
            val bottomLayer = bottomUse?.attr("href")?.removePrefix("#")

            result.add(
                WeatherIconInfo(
                    tooltip = tooltip,
                    topLayer = topLayer,
                    bottomLayer = bottomLayer
                )
            )
        }
        return result
    }
}
