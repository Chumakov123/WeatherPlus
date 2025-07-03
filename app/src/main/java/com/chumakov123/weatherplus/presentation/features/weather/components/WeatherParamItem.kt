package com.chumakov123.weatherplus.presentation.features.weather.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun WeatherParamItem(
    modifier: Modifier = Modifier,
    title: String,
    mainValue: String,
    unit: String? = null,
    shadowStyle: Shadow
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = mainValue,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(shadow = shadowStyle),
            )
            unit?.let {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = it,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(shadow = shadowStyle)
                )
            }
        }
    }
}
