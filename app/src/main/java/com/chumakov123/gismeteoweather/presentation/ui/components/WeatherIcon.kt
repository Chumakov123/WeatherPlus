package com.chumakov123.gismeteoweather.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo

@Composable
fun WeatherIcon(weatherInfo: WeatherInfo.Available, modifier: GlanceModifier = GlanceModifier) {
    Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
        Image(
            provider = ImageProvider(weatherInfo.now.icon),
            contentDescription = weatherInfo.now.description,
            modifier = GlanceModifier.size(36.dp),
        )
    }
}