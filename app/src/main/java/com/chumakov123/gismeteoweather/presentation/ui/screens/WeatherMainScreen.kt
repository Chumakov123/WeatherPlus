package com.chumakov123.gismeteoweather.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.presentation.ui.components.application.PreviewWeatherTable
import com.chumakov123.gismeteoweather.presentation.ui.components.application.SlideUpPanelContinuous
import com.chumakov123.gismeteoweather.presentation.ui.components.application.WeatherContent
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.CityWeatherUiState
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

    val cityCodes = state.cityStates.keys.toList()

    if (cityCodes.isEmpty()) {
        LaunchedEffect(Unit) {
            onAddCityClick()
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = cityCodes.indexOf(state.selectedCityCode).coerceAtLeast(0),
        pageCount = { cityCodes.size }
    )


    LaunchedEffect(pagerState.currentPage) {
        val newSelected = cityCodes.getOrNull(pagerState.currentPage)
        if (newSelected != null && newSelected != state.selectedCityCode) {
            viewModel.selectCity(newSelected)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("По часам", "По дням")

    val selectedCityState = state.cityStates[state.selectedCityCode]

    SlideUpPanelContinuous(
        overlay = {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                ) { pageIndex ->
                    val cityCode = cityCodes[pageIndex]
                    val cityState = state.cityStates[cityCode]

                    when (cityState) {
                        is CityWeatherUiState.Loading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is CityWeatherUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ошибка: ${cityState.message}")
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = { viewModel.retryCity(cityCode) }) {
                                        Text("Повторить")
                                    }
                                }
                            }
                        }

                        is CityWeatherUiState.Success -> {
                            WeatherContent(
                                weather = cityState.rawData,
                                onRefresh = { viewModel.retryCity(cityCode) },
                                modifier = modifier.fillMaxSize()
                            )
                        }

                        null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                Box(modifier = modifier.fillMaxSize()){
                    IconButton(
                        onClick = onAddCityClick,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }

                    var expanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Настройки") },
                                onClick = {
                                    expanded = false
                                    onSettingsClick()
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Black
                                )
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить") },
                                onClick = {
                                    expanded = false
                                    viewModel.removeCity(state.selectedCityCode)
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Black
                                )
                            )
                        }
                    }
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
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        },
        panelContent = {
            when (selectedCityState) {
                is CityWeatherUiState.Success -> {
                    when (selectedTab) {
                        0 -> selectedCityState.hourlyPreprocessedData?.let {
                            PreviewWeatherTable(it, settings)
                        }

                        1 -> selectedCityState.dailyPreprocessedData?.let {
                            PreviewWeatherTable(it, settings)
                        }
                    }
                }

                else -> {
                    // Пустое место или сообщение
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Нет данных")
                    }
                }
            }
        }
    )
}