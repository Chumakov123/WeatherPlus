package com.chumakov123.gismeteoweather.presentation.features.settings.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.chumakov123.gismeteoweather.presentation.features.settings.components.SettingsList
import com.chumakov123.gismeteoweather.presentation.features.settings.components.SettingsTopBar
import com.chumakov123.gismeteoweather.presentation.viewModel.WeatherViewModel

@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    Scaffold(
        topBar = {
            SettingsTopBar(onBackClick)
        },
        modifier = modifier
    ) { innerPadding ->
        SettingsList(
            settings,
            viewModel::onSettingsChanged,
            Modifier.padding(innerPadding)
        )
    }
}

