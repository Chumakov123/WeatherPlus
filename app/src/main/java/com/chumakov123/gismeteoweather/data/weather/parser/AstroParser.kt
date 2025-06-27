package com.chumakov123.gismeteoweather.data.weather.parser

import com.chumakov123.gismeteoweather.domain.model.AstroTimes
import org.jsoup.nodes.Document

object AstroParser {
    fun parse(doc: Document): AstroTimes? {
        val sunriseEl = doc.selectFirst(".now-astro-sunrise")
        val sunsetEl = doc.selectFirst(".now-astro-sunset")
        val lineEl = doc.selectFirst(".now-astro-line")

        val sunriseTime = sunriseEl?.selectFirst(".time")?.text() ?: return null
        val sunsetTime = sunsetEl?.selectFirst(".time")?.text() ?: return null
        val sunriseCaption = sunriseEl.selectFirst(".caption")?.text() ?: "Восход"
        val sunsetCaption = sunsetEl.selectFirst(".caption")?.text() ?: "Заход"

        val rotationStyle = lineEl?.attr("style")
        val rotationDegrees = extractRotation(rotationStyle)

        return AstroTimes(
            sunriseTime = sunriseTime,
            sunsetTime = sunsetTime,
            sunriseCaption = sunriseCaption,
            sunsetCaption = sunsetCaption,
            rotationDegrees = rotationDegrees
        )
    }

    private fun extractRotation(style: String?): Double {
        if (style == null) return 0.0
        val match = Regex("""rotate\((-?[\d.]+)deg\)""").find(style)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }
}
