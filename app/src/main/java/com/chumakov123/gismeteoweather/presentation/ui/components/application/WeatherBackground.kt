package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.Image
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
import com.chumakov123.gismeteoweather.R

@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    iconWeather: String? = null,
    defaultResId: Int = R.drawable.d_c3,
    alpha: Float = 1f,
) {
    val bgUrl = remember(iconWeather) {
        if (iconWeather != null) {
            "https://st.gismeteo.st/assets/bg-desktop-now/$iconWeather.webp"
        } else {
            null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (bgUrl != null) {
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
        } else {
            Image(
                painter = painterResource(id = defaultResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha },
                contentScale = ContentScale.Crop
            )
        }
    }
}