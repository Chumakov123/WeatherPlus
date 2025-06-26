package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TransparencySetting(
    backgroundTransparency: Float,
    onTransparencyChange: (Float) -> Unit,
) {
    Text("Прозрачность", modifier = Modifier.padding(horizontal = 16.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Slider(
            value = backgroundTransparency,
            onValueChange = onTransparencyChange,
            valueRange = 0f..100f,
            modifier = Modifier.weight(1f),
            colors =
            SliderDefaults.colors(
                thumbColor = Color(0xFF1a6fda),
                activeTrackColor = Color(0xFF1a6fda),
                inactiveTrackColor = Color(0xFFc5daf7),
            ),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = "${backgroundTransparency.toInt()}%",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}