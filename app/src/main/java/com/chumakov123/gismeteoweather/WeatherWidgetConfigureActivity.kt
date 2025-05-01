package com.chumakov123.gismeteoweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.model.WidgetState
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherUpdateReceiver
import com.chumakov123.gismeteoweather.presentation.ui.WeatherGlanceWidget
import com.chumakov123.gismeteoweather.ui.theme.GismeteoWeatherTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

sealed class OptionItem(val cityCode: String, val title: String, val subtitle: String?) {
    object Auto : OptionItem("auto", "Автоопределение", null)
    data class CityInfo(
        val code: String,
        val name: String,
        val info: String?
    ) : OptionItem(code, name, info)
}

class WeatherWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ).takeIf { it != AppWidgetManager.INVALID_APPWIDGET_ID }
            ?: return finish()

        val manager = GlanceAppWidgetManager(this)
        val glanceId = runBlocking {
            manager
                .getGlanceIds(WeatherGlanceWidget::class.java)
                .first { manager.getAppWidgetId(it) == appWidgetId }
        }

        val currentState = runBlocking {
            getAppWidgetState(
                context    = this@WeatherWidgetConfigureActivity,
                definition = WeatherStateDefinition,
                glanceId   = glanceId
            )
        }

        setResult(RESULT_CANCELED)

        setContent {
            GismeteoWeatherTheme {
                Scaffold { innerPadding ->
                    WeatherWidgetConfigureScreen(
                        initialState = currentState,
                        onConfirm    = { city, appearance ->
                            applySelectionAndFinish(city, appearance)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun applySelectionAndFinish(item: OptionItem, appearance: WidgetAppearance) {
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@WeatherWidgetConfigureActivity)
            val glanceId = manager.getGlanceIds(WeatherGlanceWidget::class.java)
                .first { manager.getAppWidgetId(it) == appWidgetId }

            updateAppWidgetState(
                context = this@WeatherWidgetConfigureActivity,
                definition = WeatherStateDefinition,
                glanceId = glanceId
            ) { old -> old.copy(cityCode = item.cityCode, appearance = appearance) }

            if (item != OptionItem.Auto) saveRecentCity(item)

            setResult(RESULT_OK, Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            })

            sendBroadcast(
                Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = ComponentName(
                        this@WeatherWidgetConfigureActivity,
                        WeatherUpdateReceiver::class.java
                    )
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                }
            )
            finish()
        }
    }

    private fun saveRecentCity(item: OptionItem) {
        if (item == OptionItem.Auto) return
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val key = "recent_cities"
        val current = prefs.getStringSet(key, emptySet())?.toMutableList() ?: mutableListOf()
        current.remove(item.cityCode)
        current.add(0, item.cityCode)
        prefs.edit()
            .putStringSet(key, current.take(5).toSet())
            .putString("info_${item.cityCode}", "${item.title}|${item.subtitle.orEmpty()}")
            .apply()
    }
}

@Composable
fun WeatherWidgetConfigureScreen(
    initialState: WidgetState,
    onConfirm: (OptionItem, WidgetAppearance) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    var selected by remember { mutableStateOf<OptionItem>(OptionItem.Auto) }
    var ipCity by remember { mutableStateOf<OptionItem.CityInfo?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Appearance state
    var showUpdateTime by rememberSaveable { mutableStateOf(initialState.appearance.showUpdateTime) }
    var useColorIndicators by rememberSaveable { mutableStateOf(initialState.appearance.useColorIndicators) }
    var backgroundTransparency by rememberSaveable {
        mutableStateOf(initialState.appearance.backgroundTransparencyPercent.toFloat())
    }
    var showPrecipitation by rememberSaveable { mutableStateOf(initialState.appearance.showPrecipitation) }
    var showWind by rememberSaveable { mutableStateOf(initialState.appearance.showWind) }

    var initialized by remember { mutableStateOf(false) }
    var showLocationEditor by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val city = GismeteoApi.fetchCityByIp()

            ipCity = OptionItem.CityInfo(
                code = "${city.slug}-${city.id}",
                name = city.cityName,
                info = listOfNotNull(city.countryName, city.districtName).joinToString(", ")
            )
        } catch (error: Exception) { error.printStackTrace() }
        options = buildDefaultOptions(prefs, ipCity)
    }

    LaunchedEffect(options, initialState.cityCode) {
        if (!initialized) {
            options
                .firstOrNull { it.cityCode == initialState.cityCode }
                ?.let {
                    selected = it
                    initialized = true
                }
        }
    }

    LaunchedEffect(query) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(300)
            options = if (query.isBlank()) {
                buildDefaultOptions(prefs, ipCity)
            } else {
                GismeteoApi.searchCitiesByName(query.trim(), limit = 10)
                    .filter { ci -> "${ci.slug}-${ci.id}" != ipCity?.code }
                    .map { ci ->
                        OptionItem.CityInfo(
                            code = "${ci.slug}-${ci.id}",
                            name = ci.cityName,
                            info = listOfNotNull(ci.countryName, ci.districtName).joinToString(", ")
                        )
                    }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Заголовок
        item {
            Text(
                text = "Настройка виджета",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // 2. Блок выбора города
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Местоположение:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Text(selected.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = if (showLocationEditor) "Скрыть" else "Изменить местоположение",
                modifier = Modifier
                    .clickable { showLocationEditor = !showLocationEditor },
                color = MaterialTheme.colorScheme.primary
            )
            if (showLocationEditor) {
                Spacer(Modifier.height(8.dp))
                SearchBar(
                    query = query,
                    isSearchVisible = true,
                    onQueryChange = { query = it },
                    label = "Поиск города"
                )
            }
        }
        if (showLocationEditor) {
            // 3. Список опций городов
            items(options) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = item
                            showLocationEditor = false },
                    shape = MaterialTheme.shapes.small,
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = item.title,
                            fontWeight = if (item == selected) FontWeight.Bold else FontWeight.Normal
                        )
                        item.subtitle?.let {
                            Text(text = it, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // 4. Блок настроек внешнего вида
        item {
            Text("Внешний вид", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Время обновления:")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = showUpdateTime, onCheckedChange = { showUpdateTime = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Цветовая индикация:")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = useColorIndicators, onCheckedChange = { useColorIndicators = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Показывать осадки:")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = showPrecipitation, onCheckedChange = { showPrecipitation = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Показывать ветер:")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = showWind, onCheckedChange = { showWind = it })
                    }

                    Text("Прозрачность фона: ${backgroundTransparency.toInt()}%")
                    Slider(
                        value = backgroundTransparency,
                        onValueChange = { backgroundTransparency = it },
                        valueRange = 0f..100f
                    )
                }
            }
        }

        // 5. Кнопка подтверждения
        item {
            Button(
                onClick = {
                    val appearance = WidgetAppearance(
                        showUpdateTime = showUpdateTime,
                        useColorIndicators = useColorIndicators,
                        backgroundTransparencyPercent = backgroundTransparency.toInt(),
                        showPrecipitation = showPrecipitation,
                        showWind = showWind
                    )
                    onConfirm(selected, appearance)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Подтвердить")
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    isSearchVisible: Boolean,
    onQueryChange: (String) -> Unit,
    label: String
) {
    if (isSearchVisible) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Очистить")
                    }
                } else {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск")
                }
            }
        )
    }
}

private fun buildDefaultOptions(
    prefs: SharedPreferences,
    ipCity: OptionItem.CityInfo?
): List<OptionItem> = buildList {
    add(OptionItem.Auto)
    ipCity?.let { add(it) }

    val recents = prefs
        .getStringSet("recent_cities", emptySet())
        .orEmpty()
        .mapNotNull { code ->
            if (code == ipCity?.cityCode) return@mapNotNull null

            prefs.getString("info_$code", null)?.let { infoStr ->
                val (title, subtitle) = infoStr.split("|", limit = 2)
                OptionItem.CityInfo(code, title, subtitle)
            }
        }

    addAll(recents)
}


fun Context.startWidgetConfigure(appWidgetId: Int) {
    Intent(this, WeatherWidgetConfigureActivity::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.also(::startActivity)
}