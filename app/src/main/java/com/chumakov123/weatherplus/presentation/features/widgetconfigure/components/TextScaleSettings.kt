package com.chumakov123.weatherplus.presentation.features.widgetconfigure.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun TextScaleSetting(
    textScale: Float,
    onTextScaleChange: (Float) -> Unit,
) {
    val roundedScale = remember(textScale) {
        (textScale * 10).toInt() / 10f // округление до 0.1
    }

    Text("Масштаб текста", modifier = Modifier.padding(horizontal = 16.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Slider(
            value = roundedScale,
            onValueChange = {
                val stepped = (it * 10).roundToInt() / 10f
                onTextScaleChange(stepped.coerceIn(0.7f, 1.3f))
            },
            valueRange = 0.7f..1.3f,
            steps = 5, // (1.3 - 0.7) / 0.1 - 1 = 5 промежутков между 6 точками
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1a6fda),
                activeTrackColor = Color(0xFF1a6fda),
                inactiveTrackColor = Color(0xFFc5daf7),
            ),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = String.format("%.1f×", roundedScale),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}