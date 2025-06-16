package com.chumakov123.gismeteoweather.domain.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
        val dateFormat = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }

     val russianWeekdays = mapOf(
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

    fun LocalDateTime.toDayDateTimeString(locale: Locale = Locale.getDefault()): String {
        val javaLDT = this.toJavaLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("EE, d MMMM, HH:mm", locale)
        return javaLDT.format(formatter)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    fun LocalDateTime.toTimeString(locale: Locale = Locale.getDefault()): String {
        val javaLDT = this.toJavaLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("HH:mm", locale)
        return javaLDT.format(formatter)
    }

    fun LocalDateTime.plusMillis(millis: Long): LocalDateTime {
        // 1) Конвертируем в Instant в UTC
        val instantUtc: Instant = this.toInstant(TimeZone.UTC)
        // 2) Вычитаем миллисекунды
        val newInstant = instantUtc + millis.toDuration(DurationUnit.MILLISECONDS)
        // 3) Возвращаем обратно в LocalDateTime в UTC
        return newInstant.toLocalDateTime(TimeZone.UTC)
    }


    fun LocalDateTime.plusCalendarDays(days: Int): LocalDateTime {
        val newDate = this.date.plus(DatePeriod(days = days))
        return LocalDateTime(newDate, this.time)
    }

    fun normalizeIconString(rawIconString: String): String {
        var iconString = rawIconString.replace("_c0", "")

        if (!WeatherDrawables.drawableMap.containsKey(iconString)) {
            val underscoreIndex = iconString.indexOf('_')
            if (underscoreIndex != -1 && underscoreIndex + 1 < iconString.length) {
                iconString = iconString.substring(underscoreIndex + 1)
            }
        }
        return iconString
    }

    fun formatTemperature(value: Int): String {
        return "${if (value >= 0) "+" else ""}$value°"
    }

    fun formatRelativeTime(
        timeMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        locale: Locale = Locale("ru")
    ): String {
        val zone = ZoneId.systemDefault()
        val now       = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDateTime()
        val then      = java.time.Instant.ofEpochMilli(timeMillis).atZone(zone).toLocalDateTime()
        val duration  = Duration.between(then, now)
        val seconds   = duration.seconds
        val minutes   = seconds / 60
        val hours     = minutes / 60
        val days      = hours / 24

        //  менее минуты
        if (seconds < 60) {
            return "только что"
        }
        // минуты
        if (minutes < 60) {
            val m = minutes.toInt().coerceAtLeast(1)
            return "${m} ${russianPlural(m, "минуту", "минуты", "минут")} назад"
        }
        // часы
        if (hours < 24) {
            val h = hours.toInt().coerceAtLeast(1)
            return "${h} ${russianPlural(h, "час", "часа", "часов")} назад"
        }
        // вчера / позавчера
        if (days == 1L) {
            val t = then.format(DateTimeFormatter.ofPattern("HH:mm", locale))
            return "вчера в $t"
        }
        if (days == 2L) {
            val t = then.format(DateTimeFormatter.ofPattern("HH:mm", locale))
            return "позавчера в $t"
        }
        // более двух дней
        val sameYear = then.year == now.year
        val fmt = if (sameYear) {
            DateTimeFormatter.ofPattern("d MMMM 'в' HH:mm", locale)
        } else {
            DateTimeFormatter.ofPattern("d MMMM yyyy 'в' HH:mm", locale)
        }
        return then.format(fmt)
    }

    private fun russianPlural(n: Int, form1: String, form2: String, form5: String): String {
        val nAbs = abs(n) % 100
        val n1   = nAbs % 10
        return if (nAbs in 11..19) {
            form5
        } else if (n1 == 1) {
            form1
        } else if (n1 in 2..4) {
            form2
        } else {
            form5
        }
    }
}