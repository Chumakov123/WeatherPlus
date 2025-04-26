package com.chumakov123.gismeteoweather.domain.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
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

    private val russianWeekdays = mapOf(
        DayOfWeek.MONDAY    to "пн",
        DayOfWeek.TUESDAY   to "вт",
        DayOfWeek.WEDNESDAY to "ср",
        DayOfWeek.THURSDAY  to "чт",
        DayOfWeek.FRIDAY    to "пт",
        DayOfWeek.SATURDAY  to "сб",
        DayOfWeek.SUNDAY    to "вс"
    )

    fun LocalDateTime.toWeekdayDayString(): String {
        val wd = russianWeekdays[this.date.dayOfWeek]
            ?: error("Неизвестный день недели")
        val dayOfMonth = this.date.dayOfMonth
        return "$wd, $dayOfMonth"
    }

    fun LocalDateTime.plusCalendarDays(days: Int): LocalDateTime {
        val newDate = this.date.plus(DatePeriod(days = days))
        return LocalDateTime(newDate, this.time)
    }
}