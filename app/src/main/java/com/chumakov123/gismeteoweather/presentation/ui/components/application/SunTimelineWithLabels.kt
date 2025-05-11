package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.AstroTimes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

// Таймлайн с метками, автоматически меняющими порядок
@Composable
fun SunTimelineWithLabels(
    currentTime: LocalDateTime,
    astroTimes: AstroTimes,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 2.dp,
    iconSize: Dp = 24.dp,
    shadowStyle: Shadow
) {
    // Определяем, что слева, а что справа
    val afterSunset = currentTime.toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds() >
            currentTime.date.atTime(astroTimes.sunset)
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()

    // Восход и заход в нужном порядке
    val leftLabel  = if (afterSunset) "Заход" else "Восход"
    val rightLabel = if (afterSunset) "Восход" else "Заход"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Левая метка
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = astroTimes.sunrise.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle)
            )
            Text(
                text = leftLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Шкала
        Box(
            modifier = Modifier
                .weight(1f)
                .height(iconSize)
        ) {
            SunTimeline(
                currentTime = currentTime,
                astroTimes  = astroTimes,
                modifier    = Modifier.fillMaxWidth(),
                iconSize    = iconSize,
                lineHeight  = lineHeight
            )
        }

        Spacer(Modifier.width(8.dp))

        // Правая метка
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = astroTimes.sunset.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle)
            )
            Text(
                text = rightLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle)
            )
        }
    }
}