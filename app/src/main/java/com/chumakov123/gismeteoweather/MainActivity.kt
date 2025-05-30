package com.chumakov123.gismeteoweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.chumakov123.gismeteoweather.presentation.ui.screens.WeatherMainScreen
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel
import com.chumakov123.gismeteoweather.ui.theme.GismeteoWeatherTheme

class MainActivity : ComponentActivity() {
    private val weatherViewModel : WeatherViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GismeteoWeatherTheme(darkTheme = true) {
                Scaffold { inner ->
                    WeatherMainScreen(modifier = Modifier.padding(inner), viewModel = weatherViewModel)
                }
            }
        }
    }
}


