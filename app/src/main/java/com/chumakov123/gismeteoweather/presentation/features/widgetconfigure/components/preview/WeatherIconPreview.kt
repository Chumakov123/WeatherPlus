package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables

@Composable
fun WeatherIconPreview(weatherInfo: WeatherInfo.Available, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.TopStart) {
        Image(
            imageVector = ImageVector.vectorResource(WeatherDrawables.getWeatherIcon(weatherInfo.now.icon)),
            contentDescription = weatherInfo.now.description,
            modifier = Modifier.size(36.dp),
        )
    }
}
