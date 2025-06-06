package com.chumakov123.gismeteoweather.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.presentation.ui.components.application.PreviewWeatherTable
import com.chumakov123.gismeteoweather.presentation.ui.components.application.SlideUpPanelContinuous
import com.chumakov123.gismeteoweather.presentation.ui.components.application.WeatherContent
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherUiState
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel

@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    onAddCityClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("По часам", "По дням")

    when (state) {
        is WeatherUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WeatherUiState.Success -> {
            val data = (state as WeatherUiState.Success).rawData
            SlideUpPanelContinuous(
                overlay = {
                    WeatherContent(
                        weather = data,
                        onRefresh = { viewModel.loadWeather(data.placeCode) },
                        modifier = modifier,
                    )
                    Box(Modifier.fillMaxSize()) {
                        IconButton(
                            onClick = onAddCityClick,
                            modifier = modifier.align(Alignment.TopStart).padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        }
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = modifier.align(Alignment.TopEnd).padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                    }
                },
                headerContent = {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                },
                panelContent = {
                    val s = (state as WeatherUiState.Success)
                    if (selectedTab == 0 && s.hourlyPreprocessedData != null) {
                        PreviewWeatherTable(
                            weatherRows = s.hourlyPreprocessedData,
                            displaySettings = settings)
                    }
                    if (selectedTab == 1 && s.dailyPreprocessedData != null) {
                        PreviewWeatherTable(
                            weatherRows = s.dailyPreprocessedData,
                            displaySettings = settings)
                    }
                }
            )


        }
        is WeatherUiState.Error -> {
            val msg = (state as WeatherUiState.Error).message
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ошибка: $msg")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadWeather("auto") }) {
                        Text("Повторить")
                    }
                }
            }
        }
    }
}
