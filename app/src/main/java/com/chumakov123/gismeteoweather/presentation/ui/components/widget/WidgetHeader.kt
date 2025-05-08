package com.chumakov123.gismeteoweather.presentation.ui.components.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.util.Utils
import com.chumakov123.gismeteoweather.presentation.ui.OpenConfigCallback
import com.chumakov123.gismeteoweather.presentation.ui.SwitchForecastModeAction
import com.chumakov123.gismeteoweather.presentation.ui.UpdateWeatherAction

@Composable
fun WidgetHeader(
    placeName: String,
    updateTimeText: String?,
    isLoading: Boolean,
    forecastColumns: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorProvider(Color.Black))
            .padding(start = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (forecastColumns > 1) {
            Text(
                text = buildString {
                    updateTimeText?.let { append("$it, ") }
                    if (forecastColumns >= 3) {
                        append(placeName)
                    }
                },
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp
                ),
                modifier = GlanceModifier.defaultWeight(),
                maxLines = 1,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .height(12.dp)
                    .width(24.dp)
                    .clickable(actionRunCallback<OpenConfigCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_settings),
                    contentDescription = "Настройки",
                    modifier = GlanceModifier.size(12.dp)
                )
            }
            if (forecastColumns > 1) {
                Box(
                    modifier = GlanceModifier
                        .height(12.dp)
                        .width(24.dp)
                        .clickable(actionRunCallback<SwitchForecastModeAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_calendar),
                        contentDescription = "Переключить режим",
                        modifier = GlanceModifier.size(12.dp)
                    )
                }
            }
            if (!isLoading) {
                Box(
                    modifier = GlanceModifier
                        .height(12.dp)
                        .width(24.dp)
                        .clickable(actionRunCallback<UpdateWeatherAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "Обновить",
                        modifier = GlanceModifier.size(12.dp)
                    )
                }
            } else {
                CircularProgressIndicator(
                    modifier = GlanceModifier
                        .height(12.dp)
                        .width(24.dp)
                )
            }
        }
    }
}