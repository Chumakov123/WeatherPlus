package com.chumakov123.gismeteoweather.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.presentation.ui.components.application.WeatherContent
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherUiState
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel

@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

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
