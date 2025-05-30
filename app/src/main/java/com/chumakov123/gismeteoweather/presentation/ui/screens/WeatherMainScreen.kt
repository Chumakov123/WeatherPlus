package com.chumakov123.gismeteoweather.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.presentation.ui.components.application.PreviewWeatherTable
import com.chumakov123.gismeteoweather.presentation.ui.components.application.SlideUpPanelContinuous
import com.chumakov123.gismeteoweather.presentation.ui.components.application.WeatherContent
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherUiState
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel

@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("По часам", "По дням")

    when (state) {
        is WeatherUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WeatherUiState.Success -> {
            val data = (state as WeatherUiState.Success).data
            WeatherContent(
                weather = data,
                onRefresh = { viewModel.loadWeather(data.placeCode) },
                modifier = modifier
            )

            SlideUpPanelContinuous(
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
                    when (selectedTab) {
                        0 -> PreviewWeatherTable(
                            weather = s.data.hourly,
                            forecastMode = ForecastMode.ByHours,
                            localDateTime = s.data.localTime)
                        1 -> PreviewWeatherTable(
                            weather = s.data.daily,
                            forecastMode = ForecastMode.ByDays,
                            localDateTime = s.data.localTime)
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
