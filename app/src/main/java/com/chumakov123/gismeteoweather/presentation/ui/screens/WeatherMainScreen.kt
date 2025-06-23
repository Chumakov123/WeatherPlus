package com.chumakov123.gismeteoweather.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.data.repo.WeatherRepo
import com.chumakov123.gismeteoweather.presentation.ui.components.application.PreviewWeatherTable
import com.chumakov123.gismeteoweather.presentation.ui.components.application.SlideUpPanelContinuous
import com.chumakov123.gismeteoweather.presentation.ui.components.application.WeatherBackground
import com.chumakov123.gismeteoweather.presentation.ui.components.application.WeatherContent
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.CityWeatherUiState
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel
import kotlin.math.abs

@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    onAddCityClick: () -> Unit,
    previewCityCode: String? = null,
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val isPreviewMode = previewCityCode != null

    val currentCity = previewCityCode ?: state.selectedCityCode
    val cityCodes = if (isPreviewMode) listOf(currentCity) else state.citiesOrder

    val isCityAdded = currentCity in state.citiesOrder

    if (cityCodes.isEmpty()) {
        LaunchedEffect(Unit) {
            onAddCityClick()
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = cityCodes.indexOf(currentCity).coerceAtLeast(0),
        pageCount = { cityCodes.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        val newSelected = cityCodes.getOrNull(pagerState.currentPage)
        if (newSelected != null && newSelected != currentCity) {
            viewModel.selectCity(newSelected)
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("По часам", "По дням")

    val selectedCityState = state.cityStates[currentCity]

    val updatingCities by viewModel.updatingCities.collectAsState()

    val currentPage = pagerState.currentPage
    val currentPageOffset = pagerState.currentPageOffsetFraction
    val nextPage = if (currentPageOffset > 0) currentPage + 1 else currentPage - 1

    val currentBg = cityCodes.getOrNull(currentPage)?.let { code ->
        state.cityStates[code]?.let {
            when (it) {
                is CityWeatherUiState.Success -> it.rawData.now.iconWeather
                else -> null
            }
        }
    }

    val nextBg = if (nextPage in cityCodes.indices) {
        cityCodes[nextPage].let { code ->
            state.cityStates[code]?.let {
                when (it) {
                    is CityWeatherUiState.Success -> it.rawData.now.iconWeather
                    else -> null
                }
            }
        }
    } else null

    if (currentPageOffset != 0f) {
        WeatherBackground(
            modifier = Modifier.fillMaxSize(),
            iconWeather = nextBg,
        )
    }

    WeatherBackground(
        modifier = Modifier.fillMaxSize(),
        iconWeather = currentBg,
        alpha = 1f - abs(currentPageOffset)
    )

    SlideUpPanelContinuous(
        overlay = {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                ) { pageIndex ->
                    val cityCode = cityCodes[pageIndex]
                    when (val cityState = state.cityStates[cityCode]) {
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
                                    Text(cityState.message)
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
                    if (cityCodes.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 38.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            cityCodes.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == index)
                                                Color.White
                                            else
                                                Color.White.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (pagerState.currentPage == index)
                                                Color.White
                                            else
                                                Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

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
                                text = {
                                    Text(if (isCityAdded) "Удалить" else "Сохранить")
                                },
                                onClick = {
                                    expanded = false
                                    if (isCityAdded) {
                                        viewModel.removeCity(currentCity)
                                    } else {
                                        viewModel.addCity(currentCity)
                                    }
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Black
                                )
                            )
                        }
                    }
                }
                if (selectedCityState is CityWeatherUiState.Success) {
                    if (!WeatherRepo.isActual(selectedCityState.rawData.updateTime)) {
                        if (updatingCities.contains(selectedCityState.rawData.placeCode)) {
                            Text("Обновление...", modifier = Modifier.align(Alignment.TopCenter).padding(16.dp))
                        } else {
                            Text(
                                "Обновить",
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp)
                                    .clickable(onClick = { viewModel.loadWeatherForAllCities()})
                            )
                        }
                    }
                }
            }
        },
        headerContent = {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                divider = {},
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.height(36.dp),
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
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