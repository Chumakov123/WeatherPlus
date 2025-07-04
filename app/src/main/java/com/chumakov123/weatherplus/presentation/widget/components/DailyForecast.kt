package com.chumakov123.weatherplus.presentation.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import com.chumakov123.weatherplus.domain.model.WeatherInfo
import com.chumakov123.weatherplus.domain.model.WidgetAppearance
import com.chumakov123.weatherplus.domain.util.Utils.plusCalendarDays
import com.chumakov123.weatherplus.domain.util.Utils.toWeekdayDayString
import kotlinx.datetime.DayOfWeek

@Composable
fun DailyForecast(
    weatherInfo: WeatherInfo.Available,
    appearance: WidgetAppearance,
    forecastColumns: Int,
    modifier: GlanceModifier = GlanceModifier
) {
    val displayList = weatherInfo.daily
        .take(forecastColumns)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayList.forEachIndexed { index, item ->
            val date = weatherInfo.localTime.plusCalendarDays(index)
            val dayLabel = date.toWeekdayDayString()
            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            val dateColor = if (isWeekend) Color(0xFFFF9FA6) else Color.White // Бледно-красный
            ForecastColumn(
                weatherData = item,
                date = dayLabel,
                dateColor = dateColor,
                modifier = GlanceModifier.defaultWeight(),
                appearance = appearance
            )
        }
    }
}
