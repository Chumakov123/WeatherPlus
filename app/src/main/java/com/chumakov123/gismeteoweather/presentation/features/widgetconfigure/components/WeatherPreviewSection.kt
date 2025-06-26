package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WidgetState
import com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components.preview.WeatherWidgetPreview

@Composable
fun WeatherPreviewSection(
    previewState: WidgetState,
    previewSizeDp: DpSize,
    screenHeightDp: Dp,
    modifier: Modifier = Modifier
) {
    val previewBoxHeight = screenHeightDp / 3f

    if (previewState.weatherInfo is WeatherInfo.Available) {
        Box(
            modifier = modifier
                .padding(0.dp)
                .height(previewBoxHeight),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.wallpapper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopStart,
                modifier = Modifier.fillMaxWidth(),
            )
            WeatherWidgetPreview(
                weatherInfo = previewState.weatherInfo,
                appearance = previewState.appearance,
                previewSizeDp = previewSizeDp,
                previewState.forecastMode,
            )
        }
    }
}
