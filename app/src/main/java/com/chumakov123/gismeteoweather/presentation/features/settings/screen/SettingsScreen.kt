package com.chumakov123.gismeteoweather.presentation.features.settings.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val canPinWidget by viewModel.canPinWidgets.collectAsState()

    Scaffold(
        topBar = {
            SettingsTopBar(onBackClick)
        },
        modifier = modifier
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            if (canPinWidget) {
                Button(
                    onClick = viewModel::requestWidgetPinning,
                    modifier.wrapContentSize().padding(top = 8.dp).align(Alignment.CenterHorizontally)
                ) {
                    Text("Добавить виджет на экран")
                }
            }

            SettingsList(
                settings,
                viewModel::onSettingsChanged,
            )
        }
    }
}

