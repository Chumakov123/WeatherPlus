package com.chumakov123.weatherplus.presentation.features.widgetconfigure.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.weatherplus.domain.model.LocationInfo


@Composable
fun LocationSelectionRow(
    selectedLocation: LocationInfo,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onLocationClick)
            .padding(horizontal = 16.dp),
    ) {
        Text("Местоположение:")
        Spacer(Modifier.width(8.dp))
        Text(
            text = selectedLocation.title,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
