package com.chumakov123.gismeteoweather.widget

import androidx.glance.IconImageProvider
import com.chumakov123.gismeteoweather.R
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

object WeatherRepo {

    /**
     * Request the WeatherInfo of a given location
     */
    suspend fun getWeatherInfo(delay: Long = Random.nextInt(1, 3) * 1000L): WeatherInfo {
        // Simulate network loading
        if (delay > 0) {
            delay(delay)
        }
        return WeatherInfo.Available(
            placeName = "Tokyo",
            currentData = getRandomWeatherData(Instant.now()),
            hourlyForecast = (1..4).map {
                getRandomWeatherData(Instant.now().plusSeconds(it * 3600L))
            },
            dailyForecast = (1..4).map {
                getRandomWeatherData(Instant.now().plusSeconds(it * 86400L))
            }
        )
    }

    /**
     * Fake the weather data
     */
    private fun getRandomWeatherData(instant: Instant): WeatherData {
        val dateTime = instant.atZone(ZoneId.systemDefault())
        return WeatherData(
            icon = R.drawable.c3_rs1,
            status = R.string.app_name,
            temp = Random.nextInt(5, 35),
            maxTemp = Random.nextInt(5, 35),
            minTemp = Random.nextInt(5, 35),
            day = dateTime.dayOfWeek.name,
            hour = "${dateTime.hour % 12}:${if (dateTime.hour >= 12) "pm" else "am"}",
        )
    }
}