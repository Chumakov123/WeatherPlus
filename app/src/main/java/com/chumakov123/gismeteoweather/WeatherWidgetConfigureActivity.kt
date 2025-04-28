package com.chumakov123.gismeteoweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherUpdateReceiver
import com.chumakov123.gismeteoweather.presentation.ui.WeatherGlanceWidget
import com.chumakov123.gismeteoweather.ui.theme.GismeteoWeatherTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()
        setResult(RESULT_CANCELED)

        setContent {
            GismeteoWeatherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherWidgetConfigureScreen(
                        onConfirm = { selected ->
                            applySelectionAndFinish(selected)
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    private fun applySelectionAndFinish(item: OptionItem) {
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@WeatherWidgetConfigureActivity)
            val glanceId = manager.getGlanceIds(WeatherGlanceWidget::class.java)
                .first { manager.getAppWidgetId(it) == appWidgetId }

            updateAppWidgetState(
                context = this@WeatherWidgetConfigureActivity,
                definition = WeatherStateDefinition,
                glanceId = glanceId
            ) { old -> old.copy(cityCode = item.cityCode) }

            val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            val recents = prefs.getStringSet("recent_cities", emptySet())
                ?.toMutableSet() ?: mutableSetOf()
            recents.add(item.cityCode)
            if (recents.size > 5) recents.remove(recents.first())
            prefs.edit()
                .putStringSet("recent_cities", recents)
                .putString("info_${item.cityCode}", "${item.title}|${item.subtitle.orEmpty()}")
                .apply()

            val resultIntent = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultIntent)

            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                component = ComponentName(
                    this@WeatherWidgetConfigureActivity,
                    WeatherUpdateReceiver::class.java
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            sendBroadcast(updateIntent)

            finish()
        }
    }
}

@Composable
fun WeatherWidgetConfigureScreen(
    onConfirm: (OptionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    var selected by remember { mutableStateOf<OptionItem?>(null) }
    var ipCity by remember { mutableStateOf<OptionItem.CityInfo?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        try {
            val city = GismeteoApi.fetchCityByIp()
            ipCity = OptionItem.CityInfo(
                code = "${city.slug}-${city.id}",
                name = city.cityName,
                info = listOfNotNull(city.countryName, city.districtName).joinToString(", ")
            )
        } catch (_: Exception) { }
        options = buildDefaultOptions(prefs, ipCity)
    }

    LaunchedEffect(query) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(300)
            options = if (query.isBlank()) {
                buildDefaultOptions(prefs, ipCity)
            } else {
                val results = GismeteoApi.searchCitiesByName(query.trim(), limit = 10)
                results.map { ci ->
                    OptionItem.CityInfo(
                        code = "${ci.slug}-${ci.id}",
                        name = ci.cityName,
                        info = listOfNotNull(ci.countryName, ci.districtName).joinToString(", ")
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Поиск города") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(options) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selected = item }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = item.title)
                        item.subtitle?.let { Text(text = it) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { selected?.let(onConfirm) },
            enabled = selected != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Подтвердить")
        }
    }
}

private fun buildDefaultOptions(
    prefs: android.content.SharedPreferences,
    ipCity: OptionItem.CityInfo?
): List<OptionItem> = buildList {
    add(OptionItem.Auto)
    if (ipCity == null) {
        add(OptionItem.CityInfo("auto", "Автоопределение", null))
    } else {
        add(ipCity)
    }
    val recents = prefs
        .getStringSet("recent_cities", emptySet())
        ?.toList()
        ?.takeLast(5)
        ?.mapNotNull { code ->
            prefs.getString("info_$code", null)?.let { infoStr ->
                val (title, subtitle) = infoStr.split("|", limit = 2)
                OptionItem.CityInfo(code, title, subtitle)
            }
        }.orEmpty()
    addAll(recents)
}

fun Context.startWidgetConfigure(appWidgetId: Int) {
    Intent(this, WeatherWidgetConfigureActivity::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.also(::startActivity)
}