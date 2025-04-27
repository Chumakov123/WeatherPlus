package com.chumakov123.gismeteoweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherUpdateReceiver
import com.chumakov123.gismeteoweather.presentation.ui.WeatherGlanceWidget
import kotlinx.coroutines.runBlocking

class WeatherWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent
            .getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.activity_widget_config)
        setResult(RESULT_CANCELED)

        val spinner: Spinner = findViewById(R.id.city_spinner)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("auto", "moscow-4368", "rostov-na-donu-5110")
        )

        val btnConfirm: Button = findViewById(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            val selectedCityCode = spinner.selectedItem as String

            runBlocking {
                val manager   = GlanceAppWidgetManager(this@WeatherWidgetConfigureActivity)
                val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)
                val glanceId  = glanceIds.first { gid ->
                    manager.getAppWidgetId(gid) == appWidgetId
                }

                updateAppWidgetState(
                    context    = this@WeatherWidgetConfigureActivity,
                    definition = WeatherStateDefinition,
                    glanceId   = glanceId
                ) { old ->
                    old.copy(cityCode = selectedCityCode)
                }
            }

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