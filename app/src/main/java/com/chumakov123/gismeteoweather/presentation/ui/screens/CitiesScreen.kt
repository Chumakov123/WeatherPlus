package com.chumakov123.gismeteoweather.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.WeatherCell
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel

//TODO Экран городов
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    viewModel: WeatherViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cityName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { WeatherCell.Text("Добавить город") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = cityName,
                onValueChange = { cityName = it },
                label = { Text("Название города") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    //TODO Добавление города
                    //viewModel.addCity(cityName)
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cityName.isNotBlank()
            ) {
                Text("Добавить")
            }
        }
    }
}