package com.chumakov123.gismeteoweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.domain.model.CityInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherUpdateReceiver
import com.chumakov123.gismeteoweather.presentation.ui.OptionAdapter
import com.chumakov123.gismeteoweather.presentation.ui.OptionItem
import com.chumakov123.gismeteoweather.presentation.ui.WeatherGlanceWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WeatherWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var adapter: OptionAdapter

    private val prefs by lazy {
        getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetId = intent
            .getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish(); return
        }
        setContentView(R.layout.activity_widget_config)
        setResult(RESULT_CANCELED)

        adapter = OptionAdapter { item ->
            selectedItem = item
            findViewById<Button>(R.id.btn_confirm).isEnabled = true
        }
        findViewById<RecyclerView>(R.id.rv_options).apply {
            layoutManager = LinearLayoutManager(this@WeatherWidgetConfigureActivity)
            adapter = this@WeatherWidgetConfigureActivity.adapter
        }

        findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            applySelectionAndFinish(selectedItem!!)
        }

        val etQuery = findViewById<EditText>(R.id.et_query)
        etQuery.addTextChangedListener {
            searchJob?.cancel()
            val q = it.toString().trim()
            searchJob = lifecycleScope.launch {
                delay(300)
                if (q.isEmpty()) showDefaultOptions()
                else showSearchResults(q)
            }
        }

        showDefaultOptions()
    }

    private var selectedItem: OptionItem? = null
    private var ipCity: CityInfo?         = null
    private var searchJob: Job?           = null

    private fun showDefaultOptions() {
        adapter.submitList(buildList {
            add(OptionItem.Auto)

            if (ipCity == null) {
                lifecycleScope.launch {
                    try {
                        val city = GismeteoApi.fetchCityByIp()
                        ipCity = city
                        runOnUiThread { showDefaultOptions() }
                    } catch (_: Exception) { /* silent */ }
                }
                add(OptionItem.City("auto", "Автоопределение", null))
            } else {
                add(OptionItem.City(
                    ipCity!!.id.let { "${ipCity!!.slug}-$it" },
                    ipCity!!.cityName,
                    listOfNotNull(
                        ipCity!!.countryName,
                        ipCity!!.districtName
                    ).joinToString(", ")
                ))
            }

            val recents = prefs
                .getStringSet("recent_cities", emptySet())
                ?.toList()
                ?.takeLast(5)    // последние 5
                ?.mapNotNull { code ->
                    prefs.getString("info_$code", null)?.let { infoStr ->
                        val (title, subtitle) = infoStr.split("|", limit = 2)
                        OptionItem.City(code, title, subtitle)
                    }
                }
                .orEmpty()

            addAll(recents)
        })
    }

    private suspend fun showSearchResults(query: String) {
        val results = GismeteoApi.searchCitiesByName(query, limit = 10)
        adapter.submitList(results.map { ci ->
            OptionItem.City(
                cityCode = "${ci.slug}-${ci.id}",
                title    = ci.cityName,
                subtitle = listOfNotNull(
                    ci.countryName,
                    ci.districtName
                ).joinToString(", ")
            )
        })
    }

    private fun applySelectionAndFinish(item: OptionItem) {
        lifecycleScope.launch {
            val manager  = GlanceAppWidgetManager(this@WeatherWidgetConfigureActivity)
            val glanceId = manager.getGlanceIds(WeatherGlanceWidget::class.java)
                .first { manager.getAppWidgetId(it) == appWidgetId }

            updateAppWidgetState(
                context    = this@WeatherWidgetConfigureActivity,
                definition = WeatherStateDefinition,
                glanceId   = glanceId
            ) { old ->
                old.copy(cityCode = item.cityCode)
            }

            val recents = prefs
                .getStringSet("recent_cities", emptySet())
                ?.toMutableSet()
                ?: mutableSetOf()

            recents.add(item.cityCode)
            if (recents.size > 5) {
                recents.remove(recents.first())
            }

            prefs.edit()
                .putStringSet("recent_cities", recents)
                .putString(
                    "info_${item.cityCode}",
                    "${item.title}|${item.subtitle.orEmpty()}"
                )
                .apply()

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)

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

fun Context.startWidgetConfigure(appWidgetId: Int) {
    Intent(this, WeatherWidgetConfigureActivity::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.also(::startActivity)
}