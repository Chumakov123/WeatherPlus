package com.chumakov123.weatherplus.presentation.features.cities.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chumakov123.weatherplus.data.city.RecentCitiesRepository
import com.chumakov123.weatherplus.data.network.GismeteoApi
import com.chumakov123.weatherplus.domain.model.LocationInfo
import com.chumakov123.weatherplus.presentation.common.components.SearchResultRow
import com.chumakov123.weatherplus.presentation.features.cities.components.CityCard
import com.chumakov123.weatherplus.presentation.features.cities.components.CityCardShimmer
import com.chumakov123.weatherplus.presentation.features.cities.components.NormalTopBar
import com.chumakov123.weatherplus.presentation.features.cities.components.SearchTopBar
import com.chumakov123.weatherplus.presentation.features.cities.components.SelectionModeTopBar
import com.chumakov123.weatherplus.presentation.viewModel.CityWeatherUiState
import com.chumakov123.weatherplus.presentation.viewModel.WeatherViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private enum class SearchMode {
    ADD,
    PREVIEW
}

@Composable
fun CitiesScreen(
    viewModel: WeatherViewModel,
    onSettingsClick: () -> Unit,
    onCitySelect: () -> Unit,
    onCityPreview: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchMode by rememberSaveable { mutableStateOf(SearchMode.PREVIEW) }

    val uiState by viewModel.uiState.collectAsState()
    val addedCitiesFromVm = uiState.citiesOrder

    val localOrder = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { addedCitiesFromVm.toMutableStateList() }

    val nowMillis by produceState(initialValue = System.currentTimeMillis(), key1 = Unit) {
        while (true) {
            value = System.currentTimeMillis()
            delay(2_500L)
        }
    }

    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<List<LocationInfo>>(emptyList()) }
    var ipCity by remember { mutableStateOf<LocationInfo.CityInfo?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val searchTextFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchTextFieldFocusRequester.requestFocus()
        }
    }

    val displayedOptions = remember(options) {
        options.take(10)
    }

    fun buildDefaultOptions() = buildList {
        add(LocationInfo.Auto)
        ipCity?.let { add(it) }
        addAll(RecentCitiesRepository.loadRecent(ipCity))
    }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        query = ""
        options = buildDefaultOptions()
    }

    LaunchedEffect(addedCitiesFromVm) {
        localOrder.clear()
        localOrder.addAll(addedCitiesFromVm)
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            localOrder.add(to.index, localOrder.removeAt(from.index))
        },
    )

    LaunchedEffect(reorderState) {
        snapshotFlow { reorderState.isAnyItemDragging }
            .distinctUntilChanged()
            .filter { isDragging -> !isDragging }
            .collect {
                if (localOrder.isNotEmpty()) {
                    viewModel.updateCityOrder(localOrder.toList())
                }
            }
    }

    LaunchedEffect(Unit) {
        runCatching { GismeteoApi.fetchCityByIp() }
            .onSuccess { city ->
                ipCity = LocationInfo.CityInfo(
                    code = "${city.slug}-${city.id}",
                    name = city.cityName,
                    info = listOfNotNull(city.countryName, city.districtName).joinToString(", "),
                    kind = city.kind
                )
            }
        options = buildDefaultOptions()
    }

    LaunchedEffect(query) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(300)
            options = if (query.isBlank()) {
                buildDefaultOptions()
            } else {
                runCatching {
                    GismeteoApi.searchCitiesByName(query.trim(), limit = 10)
                }.getOrDefault(emptyList())
                    .filter { ci -> "${ci.slug}-${ci.id}" != ipCity?.code }
                    .map {
                        LocationInfo.CityInfo(
                            code = "${it.slug}-${it.id}",
                            name = it.cityName,
                            info = listOfNotNull(it.countryName, it.districtName).joinToString(", "),
                            kind = it.kind
                        )
                    }
            }
        }
    }

    var selectionMode by remember { mutableStateOf(false) }
    val selectedCities = remember { mutableStateSetOf<String>() }

    fun exitSelectionMode() {
        selectionMode = false
        selectedCities.clear()
    }

    fun activateAddMode() {
        exitSelectionMode()
        searchMode = SearchMode.ADD
        isSearchActive = true
    }

    fun activatePreviewMode() {
        exitSelectionMode()
        searchMode = SearchMode.PREVIEW
        isSearchActive = true
    }

    fun deleteSelectedCities() {
        viewModel.removeCities(selectedCities.toSet())
        exitSelectionMode()
    }

    fun onCityCardClick(cityCode: String) {
        if (selectionMode) {
            if (selectedCities.contains(cityCode)) {
                selectedCities.remove(cityCode)
                if (selectedCities.isEmpty()) {
                    exitSelectionMode()
                }
            } else {
                selectedCities.add(cityCode)
            }
        } else {
            viewModel.selectCity(cityCode)
            onCitySelect()
        }
    }

    fun onCityCardLongClick(cityCode: String) {
        if (!isSearchActive && !selectionMode) {
            selectionMode = true
            selectedCities.add(cityCode)
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            exitSelectionMode()
            searchTextFieldFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            when {
                selectionMode -> {
                    SelectionModeTopBar(
                        selectedCount = selectedCities.size,
                        onDeleteClick = { deleteSelectedCities() },
                        onCancelClick = { exitSelectionMode() }
                    )
                }
                isSearchActive -> {
                    SearchTopBar(
                        query = query,
                        onQueryChange = { query = it },
                        onCancel = {
                            isSearchActive = false
                            query = ""
                            options = buildDefaultOptions()
                        },
                        onClear = { query = "" },
                        focusRequester = searchTextFieldFocusRequester
                    )
                }
                else -> {
                    NormalTopBar(
                        onSearchActivate = { activatePreviewMode() },
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        },
        floatingActionButton = {
            if (!isSearchActive && !selectionMode) {
                FloatingActionButton(
                    onClick = { activateAddMode() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить пункт")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!isSearchActive && addedCitiesFromVm.isNotEmpty()) {
                Text(
                    text = "Мои пункты",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (!isSearchActive) {
                    if (addedCitiesFromVm.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Чтобы добавить сюда новый пункт, нажмите на плюс в правом нижнем углу",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(items = localOrder, key = { it }) { cityCode ->
                            ReorderableItem(reorderState, key = cityCode) {
                                when (val cityState = uiState.cityStates[cityCode]) {
                                    is CityWeatherUiState.Success -> {
                                        CityCard(
                                            cityState = cityState,
                                            nowMillis = nowMillis,
                                            isSelected = selectionMode && selectedCities.contains(cityCode),
                                            onClick = { onCityCardClick(cityCode) },
                                            onLongClick = { onCityCardLongClick(cityCode) },
                                            dragHandleModifier = Modifier.draggableHandle()
                                        )
                                    }

                                    is CityWeatherUiState.Loading -> {
                                        CityCardShimmer()
                                    }

                                    is CityWeatherUiState.Error -> {
                                        // Text("Ошибка загрузки города")
                                    }

                                    null -> {
                                        // Пока нет состояния — можно ничего не показывать или отобразить пустой spacer
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (options.isEmpty() && query.isNotBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ничего не найдено",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(displayedOptions) { item ->
                            if (item is LocationInfo.CityInfo) {
                                SearchResultRow(item) {
                                    isSearchActive = false
                                    query = ""
                                    when (searchMode) {
                                        SearchMode.ADD -> {
                                            viewModel.addCity(item)
                                        }
                                        SearchMode.PREVIEW -> {
                                            RecentCitiesRepository.save(item)
                                            onCityPreview(item.cityCode)
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
