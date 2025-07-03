package com.chumakov123.weatherplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.chumakov123.weatherplus.presentation.navigation.WeatherNavHost
import com.chumakov123.weatherplus.presentation.viewModel.WeatherViewModel

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherNavHost(viewModel = weatherViewModel)
        }
    }
}
