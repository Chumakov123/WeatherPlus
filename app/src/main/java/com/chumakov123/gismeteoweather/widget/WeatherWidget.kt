package com.chumakov123.gismeteoweather.widget

import android.content.Context
import androidx.annotation.DrawableRes
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
import com.chumakov123.gismeteoweather.callback.RefreshCallback
import com.chumakov123.gismeteoweather.data.model.ForecastMode
import com.chumakov123.gismeteoweather.data.model.WeatherDTO
import com.chumakov123.gismeteoweather.data.model.WeatherRawDTO
import com.chumakov123.gismeteoweather.data.model.toWeatherDTO
import com.chumakov123.gismeteoweather.data.model.toWeatherData
import com.chumakov123.gismeteoweather.prefs.WeatherPrefs
import com.chumakov123.gismeteoweather.utils.WeatherDrawables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var weatherNow = WeatherPrefs.loadWeatherNow(context)
        if (weatherNow == null) {
            updateWeather(context)
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
                            .clickable(actionRunCallback<RefreshCallback>()),
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

@Serializable
sealed interface MyWeatherInfo {
    @Serializable
    object MyLoading : MyWeatherInfo

    @Serializable
    data class MyAvailable(
        val mode: ForecastMode,
        val placeName: String,
        val placeCode: String,
        val now: MyWeatherData,
        val hourly: Map<String, MyWeatherData>,
        val daily: Map<String, MyWeatherData>
    ) : MyWeatherInfo

    @Serializable
    data class MyUnavailable(val message: String) : MyWeatherInfo
}

suspend fun fetchWeatherNow(cityCode: String): WeatherDTO? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://www.gismeteo.ru/weather-$cityCode/now/"
            val html = Jsoup.connect(url).get().outerHtml()
            parseWeatherNowFromHtml(html)?.toWeatherDTO()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun fetchWeatherHourly(cityCode: String): List<MyWeatherData> {
    return withContext(Dispatchers.IO) {
        val url = "https://www.gismeteo.ru/weather-$cityCode/"
        val html = Jsoup.connect(url).get().outerHtml()
        parseWeatherData(html)
    }
}

suspend fun fetchWeatherTomorrowHourly(cityCode: String): List<MyWeatherData> {
    return withContext(Dispatchers.IO) {
        val url = "https://www.gismeteo.ru/weather-$cityCode/tomorrow/"
        val html = Jsoup.connect(url).get().outerHtml()
        parseWeatherData(html)
    }
}

suspend fun fetchWeather3Days(cityCode: String): List<MyWeatherData> {
    return withContext(Dispatchers.IO) {
        val url = "https://www.gismeteo.ru/weather-$cityCode/3-days/"
        val html = Jsoup.connect(url).get().outerHtml()
        parseWeatherData(html)
    }
}
suspend fun fetchWeather10Days(cityCode: String): List<MyWeatherData> {
    return withContext(Dispatchers.IO) {
        val url = "https://www.gismeteo.ru/weather-$cityCode/10-days/"
        val html = Jsoup.connect(url).get().outerHtml()
        parseWeatherData(html, hasMinT = true)
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

@Serializable
data class MyWeatherData(
    val description: String,
    @DrawableRes val icon: Int,
    val temperature: Int,
    val temperatureMin: Int?,
    val windSpeed: Int,
    val windDirection: String,
    val windGust: Int,
    val precipitation: Double,
)

fun parseWeatherData(html: String, hasMinT: Boolean = false): List<MyWeatherData> {
    val temperatures = parseTemperatureData(html)
    val windDataList = parseWindData(html)
    val precipitations = parsePrecipitationData(html)
    val icons = parseWeatherIcons(html)

    val weatherDataList = mutableListOf<MyWeatherData>()

    val size = if (!hasMinT) temperatures.size else temperatures.size / 2

    for (i in 0 until size) {
        // Получаем все данные для текущей позиции
        val tMax = if (!hasMinT) temperatures[i] else temperatures[i*2]
        val tMin = if (!hasMinT) null else temperatures[i*2+1]
        val windData = windDataList.getOrElse(i) { WindData(0, "Неизвестно", 0) }
        val precipitation = precipitations.getOrElse(i) { 0.0 }
        val icon = icons.getOrElse(i) { WeatherIconInfo("Неизвестно", null, null) }

        var iconString =
            if (icon.bottomLayer != null)
                "${icon.topLayer}_${icon.bottomLayer}"
            else
                "${icon.topLayer}"

        iconString = iconString.replace("_c0","")
        if (!WeatherDrawables.drawableMap.containsKey(iconString)) {
            val underscoreIndex = iconString.indexOf('_')
            if (underscoreIndex != -1) {
                iconString = iconString.substring(underscoreIndex + 1)
            }
        }

        val iconDrawable = WeatherDrawables.drawableMap[iconString] ?: R.drawable.c3

        val weatherData = MyWeatherData(
            description = icon.tooltip,
            icon = iconDrawable,
            temperature = tMax.toInt(),
            temperatureMin = tMin?.toInt(),
            windSpeed = windData.speed,
            windDirection = windData.direction,
            windGust = windData.gust,
            precipitation = precipitation
        )

        weatherDataList.add(weatherData)
    }

    return weatherDataList
}

fun fahrenheitToCelsius(fahrenheit: Double): Double {
    return (fahrenheit - 32) * 5 / 9
}

fun readHtmlFromInternalStorage(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)

    if (file.exists()) {
        return file.readText(Charsets.UTF_8)
    } else {
        throw Exception("Файл не найден: ${file.absolutePath}")
    }
}

fun parsePrecipitationData(html: String): List<Double> {
    val document = Jsoup.parse(html) // Парсим HTML
    val precipitations = mutableListOf<Double>()
    val precipitationSection = document.select(".widget-row-precipitation-bars")
    val precipitationElements = precipitationSection.select(".item-unit")
    for (element in precipitationElements) {
        val precipitationText = element.text().trim().replace(",", ".")
        val precipitationValue = precipitationText.toDoubleOrNull()
        if (precipitationValue != null) {
            precipitations.add(precipitationValue)
        }
    }

    return precipitations
}
fun parseWindData(html: String): List<WindData> {
    val document: Document = Jsoup.parse(html) // Парсим HTML
    val windDataList = mutableListOf<WindData>()

    val windRows = document.select(".widget-row-wind .row-item")

    for (row in windRows) {
        val windSpeedElement = row.select(".wind-speed").first() // Скорость ветра
        val windDirectionElement = row.select(".wind-direction").first() // Направление ветра
        val windGustElement = row.select(".wind-gust").first() // Порывы ветра

        val windSpeed = windSpeedElement?.select("speed-value")?.attr("value")?.toIntOrNull() ?: 0
        val windDirection = windDirectionElement?.text()?.trim() ?: "Неизвестно"
        val windGust = windGustElement?.select("speed-value")?.attr("value")?.toIntOrNull() ?: 0

        windDataList.add(WindData(windSpeed, windDirection, windGust))
    }

    return windDataList
}

// Пример вызова функции для парсинга и вывода
fun parseAndDisplayHtml(context: Context) {
    val html = readHtmlFromInternalStorage(context, "10days.html")  // Прочитать файл hourly.html
    displayWeatherData(parseWeatherData(html, hasMinT = true))
}
fun displayWeatherData(weatherDataList: List<MyWeatherData>) {
    // Выводим данные
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
// Модель для данных о ветре
data class WindData(
    val speed: Int,
    val direction: String,
    val gust: Int
)
data class WeatherIconInfo(
    val tooltip: String,
    val topLayer: String?,
    val bottomLayer: String?
)

fun parseWeatherIcons(html: String): List<WeatherIconInfo> {
    val doc: Document = Jsoup.parse(html)
    val result = mutableListOf<WeatherIconInfo>()

    val items = doc.select(".widget-row-icon .row-item")
    for (item in items) {
        val tooltip = item.attr("data-tooltip")
        val topUse = item.selectFirst("svg.top-layer use")
        val bottomUse = item.selectFirst("svg.bottom-layer use")

        val topLayer = topUse?.attr("href")?.removePrefix("#")
        val bottomLayer = bottomUse?.attr("href")?.removePrefix("#")

        result.add(
            WeatherIconInfo(
                tooltip = tooltip,
                topLayer = topLayer,
                bottomLayer = bottomLayer
            )
        )
    }

    return result
}

fun parseTemperatureData(html: String): List<Double> {
    val document = Jsoup.parse(html)
    val temperatures = mutableListOf<Double>()
    val temperatureSection = document.select(".widget-row-chart.widget-row-chart-temperature-air")
    val temperatureElements = temperatureSection.select("temperature-value")
    for (element in temperatureElements) {
        val temperatureValue = element.attr("value").toDoubleOrNull() ?: continue
        val fromUnit = element.attr("from-unit")
        if (fromUnit == "f") {
            val celsius = fahrenheitToCelsius(temperatureValue)
            temperatures.add(celsius)
        } else {
            temperatures.add(temperatureValue)
        }
    }

    return temperatures
}

fun parseWeatherNowFromHtml(html: String): WeatherRawDTO? {
    val regex = Regex("""window\.M\.state\s*=\s*(\{.*?\})(?=\s*</script>)""", RegexOption.DOT_MATCHES_ALL)
    val matchResult = regex.find(html) ?: return null
    val jsonString = matchResult.groupValues[1]
    val json = Json { ignoreUnknownKeys = true }
    val root = json.parseToJsonElement(jsonString).jsonObject
    val cwJson = root["weather"]?.jsonObject?.get("cw") ?: return null
    return json.decodeFromJsonElement<WeatherRawDTO>(cwJson)
}

suspend fun updateWeather(context: Context) {
    WeatherPrefs.incDebugCounter(context)
    val settings = WeatherPrefs.loadCitySettings(context)
    val weatherNow = fetchWeatherNow(settings.city)
    if (weatherNow != null) {
        WeatherPrefs.saveWeatherNow(context, weatherNow)
        WeatherPrefs.saveLastUpdateTime(context)
    }
}
