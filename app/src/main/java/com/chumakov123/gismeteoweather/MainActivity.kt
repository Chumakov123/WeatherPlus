package com.chumakov123.gismeteoweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.chumakov123.gismeteoweather.presentation.ui.navigation.WeatherNavHost
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel
import com.chumakov123.gismeteoweather.ui.theme.GismeteoWeatherTheme

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GismeteoWeatherTheme(darkTheme = true) {
                WeatherNavHost(viewModel = weatherViewModel)
            }
        }
    }
}


