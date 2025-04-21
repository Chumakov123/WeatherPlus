package com.chumakov123.gismeteoweather.utils

import kotlinx.datetime.LocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    fun fahrenheitToCelsius(fahrenheit: Double): Double {
        return (fahrenheit - 32) * 5 / 9
    }
    fun startOfDay(localTime: LocalDateTime): LocalDateTime {
        return LocalDateTime(localTime.date, kotlinx.datetime.LocalTime(0, 0))
    }
    fun getIntervalStartTime(intervalIndex: Int): String {
        val hour = (intervalIndex * 3) % 24
        return "${hour}⁰⁰"
    }
    fun getIntervalIndexByHour(hour: Int): Int {
        require(hour >= 0) { "Hour must be non-negative" }
        return (hour % 24) / 3
    }
    fun formatDateTime(timeMillis: Long): String {
        val date = Date(timeMillis)
        val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }
}