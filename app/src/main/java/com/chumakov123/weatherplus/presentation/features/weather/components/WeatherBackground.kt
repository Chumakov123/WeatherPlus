package com.chumakov123.weatherplus.presentation.features.weather.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chumakov123.weatherplus.R

@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    iconWeather: String? = null,
    defaultResId: Int = R.drawable.d_c3,
    alpha: Float = 1f,
) {
    val context = LocalContext.current
    val bgUrl = remember(iconWeather) {
        iconWeather?.let {
            "https://st.gismeteo.st/assets/bg-desktop-now/$it.webp"
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(bgUrl)
                .diskCacheKey(bgUrl)
                .memoryCacheKey(bgUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = defaultResId),
            error = painterResource(id = defaultResId),
            fallback = painterResource(id = defaultResId), // если url == null
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha },
            contentScale = ContentScale.Crop,
        )
    }
}