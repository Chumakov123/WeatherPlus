package com.chumakov123.gismeteoweather.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.data.model.WeatherDTO
import com.chumakov123.gismeteoweather.data.parser.GismeteoWeatherHtmlParser
import com.chumakov123.gismeteoweather.prefs.WeatherPrefs
import java.io.File

class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var weatherNow = WeatherPrefs.loadWeatherNow(context)
        if (weatherNow == null) {
            //updateWeather(context)
            weatherNow = WeatherPrefs.loadWeatherNow(context)
        }
        val lastUpdateTime = WeatherPrefs.loadLastUpdateTime(context)
        val debugCount = WeatherPrefs.loadDebugCounter(context)
        //scheduleWeatherUpdateWork(context)
        provideContent {
            MyContent(weatherNow, lastUpdateTime, debugCount)
        }
    }

    @Composable
    private fun MyContent(
        weather: WeatherDTO?,
        lastUpdateTime: String?,
        debugCount: Int,
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0x80000000)) // полупрозрачный черный фон
        ) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Row(
                    modifier = GlanceModifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Дата последнего обновления
                    if (lastUpdateTime != null) {
                        Text(
                            text = "$lastUpdateTime, Ростов-на-дону",
                        style = TextStyle(
                            color = ColorProvider(Color.LightGray),
                            fontSize = 10.sp
                        )
                        )
                    }
                    // Кнопка-иконка для обновления
                    Box(
                        modifier = GlanceModifier
                            .height(10.dp)
                            .width(24.dp)
                            .clickable(actionRunCallback<UpdateWeatherAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_refresh),
                            contentDescription = "Обновить",
                            modifier = GlanceModifier.size(10.dp) // сама иконка — маленькая
                        )
                    }
                }
            }
            Column(modifier = GlanceModifier.padding(8.dp)) {
                // Если данные успешно получены, отображаем температуру и описание
                if (weather != null) {
                    Text(
                        text = "${if (weather.temperatureAir>0) "+" else ""}${weather.temperatureAir}° $debugCount",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 24.sp
                        )
                    )
                    Text(
                        text = weather.description,
                        style = TextStyle(
                            color = ColorProvider(Color.LightGray),
                            fontSize = 12.sp
                        )
                    )
                } else {
                    // Если данных нет, показываем сообщение об ошибке
                    Text(
                        text = "Нет данных",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }
}

suspend fun saveHtmlToFile(context: Context) {
//    val htmlHourly = fetchWeatherHourly("rostov-na-donu-5110")
//    val htmlTomorrowHourly = fetchWeatherTomorrowHourly("rostov-na-donu-5110")
//    val html3Days = fetchWeather3Days("rostov-na-donu-5110")
//    val html10Days = fetchWeather10Days("rostov-na-donu-5110")
//
//    // Список пар: имя файла и содержимое
//    val htmlFiles = listOf(
//        "hourly.html" to htmlHourly,
//        "tomorrow_hourly.html" to htmlTomorrowHourly,
//        "3days.html" to html3Days,
//        "10days.html" to html10Days
//    )
//
//    // Сохранение каждого файла
//    htmlFiles.forEach { (filename, content) ->
//        val file = File(context.filesDir, filename)
//        file.writeText(content)
//        Log.d("WeatherDebug", "HTML saved to: ${file.absolutePath}")
//    }
    parseAndDisplayHtml(context)
}
fun readHtmlFromInternalStorage(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)

    if (file.exists()) {
        return file.readText(Charsets.UTF_8)
    } else {
        throw Exception("Файл не найден: ${file.absolutePath}")
    }
}

// Пример вызова функции для парсинга и вывода
fun parseAndDisplayHtml(context: Context) {
    val html = readHtmlFromInternalStorage(context, "10days.html")  // Прочитать файл hourly.html
    displayWeatherData(GismeteoWeatherHtmlParser.parseWeatherData(html, hasMinT = true))
}
fun displayWeatherData(weatherDataList: List<WeatherData>) {
    weatherDataList.forEachIndexed { _, weatherData ->
        println("Описание: ${weatherData.description}")
        println("Иконка: ${weatherData.icon}")
        println("Температура: ${weatherData.temperature}°C")
        if (weatherData.temperatureMin != null)
            println("Температура ночью: ${weatherData.temperatureMin}°C")
        println("Ветер: ${weatherData.windSpeed} м/с, Направление: ${weatherData.windDirection}, Порывы: ${weatherData.windGust} м/с")
        println("Осадки: ${weatherData.precipitation} мм")
        println("----------------------------------")
    }
}
