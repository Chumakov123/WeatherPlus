package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.util.Utils.plusCalendarDays
import com.chumakov123.gismeteoweather.domain.util.Utils.toWeekdayDayString
import kotlinx.datetime.DayOfWeek

@Composable
fun DailyForecastPreview(
    weatherInfo: WeatherInfo.Available,
    appearance: WidgetAppearance,
    modifier: Modifier = Modifier
) {
    val visibleCount = 6
    val displayList = weatherInfo.daily.take(visibleCount)

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayList.forEachIndexed { index, item ->
            val date = weatherInfo.localTime.plusCalendarDays(index)
            val dayLabel = date.toWeekdayDayString()
            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            val dateColor = if (isWeekend) Color(0xFFFF9FA6) else Color.White // Бледно-красный

            ForecastColumnPreview(
                weatherData = item,
                date = dayLabel,
                dateColor = dateColor,
                appearance = appearance,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
        }
    }
}
