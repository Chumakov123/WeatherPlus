package com.chumakov123.gismeteoweather.presentation.features.cities.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTopBar(
    onSearchActivate: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text("Погода Gismeteo", color = MaterialTheme.colorScheme.onPrimary)
        },
        actions = {
            IconButton(onClick = onSearchActivate) {
                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier
    )
}
