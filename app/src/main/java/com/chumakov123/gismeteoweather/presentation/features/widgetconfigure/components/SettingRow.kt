package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val rowHeight = 48.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier
            .fillMaxWidth()
            .height(rowHeight) // гарантирует, что клик захватывает всю строку
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp),
    ) {
        Text(title)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier =
            Modifier
                .size(width = 32.dp, height = 16.dp)
                .scale(0.7f),
            colors =
            SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1a6fda),
                checkedTrackColor = Color(0xFFc5daf7),
            ),
        )
    }
}