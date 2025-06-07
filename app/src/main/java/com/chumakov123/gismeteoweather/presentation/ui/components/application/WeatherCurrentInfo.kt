package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.AstroTimes
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.util.Utils.toDayDateTimeString
import kotlinx.datetime.LocalDateTime

@Composable
fun WeatherCurrentInfo(
    placeName: String,
    localDateTime: LocalDateTime,
    astroTimes: AstroTimes,
    temperature: Int,
    heatIndex: Int,
    description: String,
    weather: WeatherInfo.Available,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Top
    ) {
        val shadowStyle = remember {
            Shadow(
                color      = Color.Black,
                offset     = Offset(1f, 1f),
                blurRadius = 4f
            )
        }

        Text(
            text  = placeName,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(shadow = shadowStyle)
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text  = localDateTime.toDayDateTimeString(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(shadow = shadowStyle)
        )
        Spacer(Modifier.height(8.dp))

        SunTimelineWithLabels(
            currentTime = localDateTime,
            astroTimes  = astroTimes,
            shadowStyle = shadowStyle
        )
        val sign = if (temperature >= 0) "+" else "-"
        Text(
            text  = "$sign$temperature°",
            color = Color.White,
            style = MaterialTheme.typography.displayLarge.copy(shadow = shadowStyle)
        )

        val heatSign = if (heatIndex >= 0) "+" else "-"
        Box(
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text  = "По ощущению $heatSign$heatIndex°",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(shadow = shadowStyle)
            )
        }
        Spacer(Modifier.height(8.dp))

        Text(
            text  = description,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(shadow = shadowStyle)
        )
        Spacer(Modifier.height(8.dp))

        WeatherParamsRow(weather = weather)
    }
}