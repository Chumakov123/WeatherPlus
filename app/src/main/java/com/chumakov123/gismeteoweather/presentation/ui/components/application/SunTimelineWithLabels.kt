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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.AstroTimes

// Таймлайн с метками, автоматически меняющими порядок
@Composable
fun SunTimelineWithLabels(
    astroTimes: AstroTimes,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 2.dp,
    iconSize: Dp = 24.dp,
    shadowStyle: Shadow
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = astroTimes.sunriseTime.replace(", ", "\n"),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle),
                textAlign = TextAlign.Center
            )
            Text(
                text = astroTimes.sunriseCaption,
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
                rotationDegrees = astroTimes.rotationDegrees,
                modifier = Modifier.fillMaxWidth(),
                iconSize = iconSize,
                lineHeight = lineHeight
            )
        }

        Spacer(Modifier.width(8.dp))

        // Правая метка — восход
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = astroTimes.sunsetTime.replace(", ", "\n"),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle),
                textAlign = TextAlign.Center
            )
            Text(
                text = astroTimes.sunsetCaption,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle)
            )
        }
    }
}