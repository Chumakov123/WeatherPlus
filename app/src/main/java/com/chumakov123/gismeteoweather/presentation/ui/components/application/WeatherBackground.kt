package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    iconWeather: String,
    alpha: Float = 1f,
) {
    // URL фона
    val bgUrl = remember(iconWeather) {
        "https://st.gismeteo.st/assets/bg-desktop-now/$iconWeather.webp"
    }
    println(bgUrl)

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(bgUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha },
            contentScale = ContentScale.Crop
        )
    }
}