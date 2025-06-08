package com.chumakov123.gismeteoweather.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.OptionItem
import com.chumakov123.gismeteoweather.SearchBar
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//TODO Экран городов
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    viewModel: WeatherViewModel,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    var selected by remember { mutableStateOf<OptionItem?>(null) }
    var ipCity by remember { mutableStateOf<OptionItem.CityInfo?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun buildDefaultOptions(ipCity: OptionItem.CityInfo?): List<OptionItem> {
        return buildList {
            add(OptionItem.Auto)
            ipCity?.let { add(it) }
        }
    }

    // Получение города по IP
    LaunchedEffect(Unit) {
        try {
            val city = GismeteoApi.fetchCityByIp()
            ipCity = OptionItem.CityInfo(
                code = "${city.slug}-${city.id}",
                name = city.cityName,
                info = listOfNotNull(city.countryName, city.districtName).joinToString(", ")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        options = buildDefaultOptions(ipCity)
    }

    // Обработка ввода запроса
    LaunchedEffect(query) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(300)
            options = if (query.isBlank()) {
                buildDefaultOptions(ipCity)
            } else {
                runCatching {
                    GismeteoApi.searchCitiesByName(query.trim(), limit = 10)
                }.getOrDefault(emptyList())
                    .filter { ci -> "${ci.slug}-${ci.id}" != ipCity?.code }
                    .map {
                        OptionItem.CityInfo(
                            code = "${it.slug}-${it.id}",
                            name = it.cityName,
                            info = listOfNotNull(it.countryName, it.districtName).joinToString(", ")
                        )
                    }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Погода Gismeteo", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    isSearchVisible = true,
                    label = "Поиск города"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(options) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = item
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.title,
                                    fontWeight = if (item == selected) FontWeight.Bold else FontWeight.Normal
                                )
                                item.subtitle?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Плавающая кнопка добавления
            FloatingActionButton(
                onClick = {
                    selected?.let { city ->
                        viewModel.addCity(city.cityCode)
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить город")
            }
        }
    }
}