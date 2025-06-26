package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure

import WeatherAppTheme
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.chumakov123.gismeteoweather.data.repo.RecentCitiesRepository
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.domain.model.OptionItem
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.model.WidgetState
import com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.screen.WeatherWidgetConfigureScreen
import com.chumakov123.gismeteoweather.presentation.widget.WeatherGlanceWidget
import com.chumakov123.gismeteoweather.presentation.widget.receiver.WeatherUpdateReceiver
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

const val previewString = "{\"cityCode\":\"sankt-peterburg-4079\",\"weatherInfo\":{\"type\":\"com.chumakov123.gismeteoweather.domain.model.WeatherInfo.Available\",\"placeName\":\"Санкт-Петербург\",\"placeCode\":\"sankt-peterburg-4079\",\"placeKind\":\"M\",\"now\":{\"date\":\"2025-06-16T09:00:00.000Z\",\"colorBackground\":\"d-c1\",\"description\":\"Малооблачно\",\"iconWeather\":\"d_c1\",\"icon\":\"d_c1\",\"temperature\":21,\"humidity\":66,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":757,\"radiation\":4,\"temperatureAir\":21,\"temperatureHeatIndex\":21,\"temperatureWater\":16,\"geomagnetic\":0},\"hourly\":[{\"description\":\"Облачно\",\"icon\":\"n_c2\",\"temperature\":17,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":17,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":null,\"humidity\":72,\"radiation\":0,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":\"c3\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":76,\"radiation\":0,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":3,\"windDirection\":\"СЗ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":80,\"radiation\":0,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"СЗ\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":79,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":21,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":21,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":3,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":66,\"radiation\":4,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":22,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":22,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":55,\"radiation\":5,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":21,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":21,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":51,\"radiation\":4,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":756,\"pressureMin\":null,\"humidity\":56,\"radiation\":1,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно\",\"icon\":\"n_c2\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":0,\"windDirection\":\"—\",\"windGust\":2,\"precipitation\":0.0,\"pressure\":756,\"pressureMin\":null,\"humidity\":66,\"radiation\":0,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":\"c3\",\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":0,\"windDirection\":\"—\",\"windGust\":2,\"precipitation\":0.0,\"pressure\":755,\"pressureMin\":null,\"humidity\":70,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":14,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":14,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":4,\"precipitation\":0.1,\"pressure\":754,\"pressureMin\":null,\"humidity\":78,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":4,\"precipitation\":0.3,\"pressure\":753,\"pressureMin\":null,\"humidity\":82,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":17,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":0,\"windDirection\":\"—\",\"windGust\":1,\"precipitation\":0.7,\"pressure\":753,\"pressureMin\":null,\"humidity\":79,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":2,\"precipitation\":2.1,\"pressure\":753,\"pressureMin\":null,\"humidity\":82,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":5,\"precipitation\":1.7,\"pressure\":753,\"pressureMin\":null,\"humidity\":81,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":5,\"precipitation\":0.1,\"pressure\":754,\"pressureMin\":null,\"humidity\":88,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"daily\":[{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":22,\"temperatureMin\":16,\"temperatureAvg\":19,\"temperatureHeatIndex\":22,\"temperatureHeatIndexMin\":16,\"windSpeed\":3,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":756,\"humidity\":67,\"radiation\":5,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":17,\"temperatureMin\":14,\"temperatureAvg\":16,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":14,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":5,\"precipitation\":5.0,\"pressure\":756,\"pressureMin\":753,\"humidity\":78,\"radiation\":1,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":\"d_c2_r2\",\"temperature\":19,\"temperatureMin\":14,\"temperatureAvg\":16,\"temperatureHeatIndex\":19,\"temperatureHeatIndexMin\":14,\"windSpeed\":2,\"windDirection\":\"ЮЗ\",\"windGust\":7,\"precipitation\":3.8,\"pressure\":754,\"pressureMin\":751,\"humidity\":79,\"radiation\":3,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":15,\"temperatureMin\":14,\"temperatureAvg\":14,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":13,\"windSpeed\":4,\"windDirection\":\"З\",\"windGust\":10,\"precipitation\":9.7,\"pressure\":751,\"pressureMin\":748,\"humidity\":87,\"radiation\":1,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь\",\"icon\":\"d_c1_r1\",\"temperature\":15,\"temperatureMin\":9,\"temperatureAvg\":12,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":5,\"windSpeed\":5,\"windDirection\":\"С\",\"windGust\":13,\"precipitation\":1.6,\"pressure\":761,\"pressureMin\":752,\"humidity\":66,\"radiation\":7,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":17,\"temperatureMin\":9,\"temperatureAvg\":14,\"temperatureHeatIndex\":17,\"temperatureHeatIndexMin\":7,\"windSpeed\":3,\"windDirection\":\"С\",\"windGust\":8,\"precipitation\":0.0,\"pressure\":763,\"pressureMin\":762,\"humidity\":58,\"radiation\":6,\"geomagnetic\":2,\"pollenBirch\":1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь\",\"icon\":\"d_c1_r1\",\"temperature\":19,\"temperatureMin\":11,\"temperatureAvg\":15,\"temperatureHeatIndex\":19,\"temperatureHeatIndexMin\":9,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":8,\"precipitation\":0.1,\"pressure\":763,\"pressureMin\":757,\"humidity\":65,\"radiation\":6,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":\"d_c2_r2\",\"temperature\":20,\"temperatureMin\":14,\"temperatureAvg\":17,\"temperatureHeatIndex\":20,\"temperatureHeatIndexMin\":14,\"windSpeed\":3,\"windDirection\":\"З\",\"windGust\":7,\"precipitation\":4.6,\"pressure\":756,\"pressureMin\":751,\"humidity\":71,\"radiation\":5,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":22,\"temperatureMin\":14,\"temperatureAvg\":18,\"temperatureHeatIndex\":22,\"temperatureHeatIndexMin\":14,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":755,\"pressureMin\":751,\"humidity\":67,\"radiation\":4,\"geomagnetic\":4,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":18,\"temperatureMin\":14,\"temperatureAvg\":16,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":13,\"windSpeed\":2,\"windDirection\":\"СЗ\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":756,\"humidity\":70,\"radiation\":5,\"geomagnetic\":5,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"updateTime\":1750065688663,\"localTime\":\"2025-06-16T12:21:28\",\"astroTimes\":{\"sunriseTime\":\"3:35\",\"sunsetTime\":\"22:24\",\"sunriseCaption\":\"Восход\",\"sunsetCaption\":\"Заход\",\"rotationDegrees\":-2.164922440904192}},\"lastAvailable\":{\"placeName\":\"Санкт-Петербург\",\"placeCode\":\"sankt-peterburg-4079\",\"placeKind\":\"M\",\"now\":{\"date\":\"2025-06-16T09:00:00.000Z\",\"colorBackground\":\"d-c1\",\"description\":\"Малооблачно\",\"iconWeather\":\"d_c1\",\"icon\":\"d_c1\",\"temperature\":21,\"humidity\":66,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":757,\"radiation\":4,\"temperatureAir\":21,\"temperatureHeatIndex\":21,\"temperatureWater\":16,\"geomagnetic\":0},\"hourly\":[{\"description\":\"Облачно\",\"icon\":\"n_c2\",\"temperature\":17,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":17,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":null,\"humidity\":72,\"radiation\":0,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":\"c3\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":76,\"radiation\":0,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":3,\"windDirection\":\"СЗ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":80,\"radiation\":0,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"СЗ\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":79,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":21,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":21,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":3,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":66,\"radiation\":4,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":22,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":22,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":55,\"radiation\":5,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":21,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":21,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":757,\"pressureMin\":null,\"humidity\":51,\"radiation\":4,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":4,\"precipitation\":0.0,\"pressure\":756,\"pressureMin\":null,\"humidity\":56,\"radiation\":1,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно\",\"icon\":\"n_c2\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":0,\"windDirection\":\"—\",\"windGust\":2,\"precipitation\":0.0,\"pressure\":756,\"pressureMin\":null,\"humidity\":66,\"radiation\":0,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":\"c3\",\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":0,\"windDirection\":\"—\",\"windGust\":2,\"precipitation\":0.0,\"pressure\":755,\"pressureMin\":null,\"humidity\":70,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":14,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":14,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":4,\"precipitation\":0.1,\"pressure\":754,\"pressureMin\":null,\"humidity\":78,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":4,\"precipitation\":0.3,\"pressure\":753,\"pressureMin\":null,\"humidity\":82,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":17,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":0,\"windDirection\":\"—\",\"windGust\":1,\"precipitation\":0.7,\"pressure\":753,\"pressureMin\":null,\"humidity\":79,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":2,\"precipitation\":2.1,\"pressure\":753,\"pressureMin\":null,\"humidity\":82,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":5,\"precipitation\":1.7,\"pressure\":753,\"pressureMin\":null,\"humidity\":81,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":\"c3_r1\",\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":5,\"precipitation\":0.1,\"pressure\":754,\"pressureMin\":null,\"humidity\":88,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"daily\":[{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":22,\"temperatureMin\":16,\"temperatureAvg\":19,\"temperatureHeatIndex\":22,\"temperatureHeatIndexMin\":16,\"windSpeed\":3,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":756,\"humidity\":67,\"radiation\":5,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":17,\"temperatureMin\":14,\"temperatureAvg\":16,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":14,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":5,\"precipitation\":5.0,\"pressure\":756,\"pressureMin\":753,\"humidity\":78,\"radiation\":1,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":\"d_c2_r2\",\"temperature\":19,\"temperatureMin\":14,\"temperatureAvg\":16,\"temperatureHeatIndex\":19,\"temperatureHeatIndexMin\":14,\"windSpeed\":2,\"windDirection\":\"ЮЗ\",\"windGust\":7,\"precipitation\":3.8,\"pressure\":754,\"pressureMin\":751,\"humidity\":79,\"radiation\":3,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":\"c3_r2\",\"temperature\":15,\"temperatureMin\":14,\"temperatureAvg\":14,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":13,\"windSpeed\":4,\"windDirection\":\"З\",\"windGust\":10,\"precipitation\":9.7,\"pressure\":751,\"pressureMin\":748,\"humidity\":87,\"radiation\":1,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь\",\"icon\":\"d_c1_r1\",\"temperature\":15,\"temperatureMin\":9,\"temperatureAvg\":12,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":5,\"windSpeed\":5,\"windDirection\":\"С\",\"windGust\":13,\"precipitation\":1.6,\"pressure\":761,\"pressureMin\":752,\"humidity\":66,\"radiation\":7,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":17,\"temperatureMin\":9,\"temperatureAvg\":14,\"temperatureHeatIndex\":17,\"temperatureHeatIndexMin\":7,\"windSpeed\":3,\"windDirection\":\"С\",\"windGust\":8,\"precipitation\":0.0,\"pressure\":763,\"pressureMin\":762,\"humidity\":58,\"radiation\":6,\"geomagnetic\":2,\"pollenBirch\":1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь\",\"icon\":\"d_c1_r1\",\"temperature\":19,\"temperatureMin\":11,\"temperatureAvg\":15,\"temperatureHeatIndex\":19,\"temperatureHeatIndexMin\":9,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":8,\"precipitation\":0.1,\"pressure\":763,\"pressureMin\":757,\"humidity\":65,\"radiation\":6,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":\"d_c2_r2\",\"temperature\":20,\"temperatureMin\":14,\"temperatureAvg\":17,\"temperatureHeatIndex\":20,\"temperatureHeatIndexMin\":14,\"windSpeed\":3,\"windDirection\":\"З\",\"windGust\":7,\"precipitation\":4.6,\"pressure\":756,\"pressureMin\":751,\"humidity\":71,\"radiation\":5,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":22,\"temperatureMin\":14,\"temperatureAvg\":18,\"temperatureHeatIndex\":22,\"temperatureHeatIndexMin\":14,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":755,\"pressureMin\":751,\"humidity\":67,\"radiation\":4,\"geomagnetic\":4,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":\"d_c1\",\"temperature\":18,\"temperatureMin\":14,\"temperatureAvg\":16,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":13,\"windSpeed\":2,\"windDirection\":\"СЗ\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":756,\"humidity\":70,\"radiation\":5,\"geomagnetic\":5,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"updateTime\":1750065688663,\"localTime\":\"2025-06-16T12:21:28\",\"astroTimes\":{\"sunriseTime\":\"3:35\",\"sunsetTime\":\"22:24\",\"sunriseCaption\":\"Восход\",\"sunsetCaption\":\"Заход\",\"rotationDegrees\":-2.164922440904192}}}"

class WeatherWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent
            .getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ).takeIf { it != AppWidgetManager.INVALID_APPWIDGET_ID }
            ?: return finish()

        val manager = GlanceAppWidgetManager(this)
        val glanceId =
            runBlocking {
                manager
                    .getGlanceIds(WeatherGlanceWidget::class.java)
                    .first { manager.getAppWidgetId(it) == appWidgetId }
            }

        val currentState =
            runBlocking {
                getAppWidgetState(
                    context = this@WeatherWidgetConfigureActivity,
                    definition = WeatherStateDefinition,
                    glanceId = glanceId,
                )
            }

        setResult(RESULT_CANCELED)

        // WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color(0xFF17629F).toArgb()
        window.navigationBarColor = Color(0xFF2196F3).toArgb()

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        setContent {
            WeatherAppTheme(isMainScreen = false) {
                Scaffold { innerPadding ->
                    WeatherWidgetConfigureScreen(
                        initialState = currentState,
                        onConfirm = { city, appearance, forecastMode ->
                            applySelectionAndFinish(city, appearance, forecastMode)
                        },
                        previewWeatherState =
                        if (currentState.weatherInfo !is WeatherInfo.Available) {
                            Json.decodeFromString(
                                WidgetState.serializer(),
                                previewString,
                            )
                        } else {
                            currentState
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    private fun applySelectionAndFinish(
        item: OptionItem,
        appearance: WidgetAppearance,
        forecastMode: ForecastMode,
    ) {
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@WeatherWidgetConfigureActivity)
            val glanceId =
                manager
                    .getGlanceIds(WeatherGlanceWidget::class.java)
                    .first { manager.getAppWidgetId(it) == appWidgetId }

            updateAppWidgetState(
                context = this@WeatherWidgetConfigureActivity,
                definition = WeatherStateDefinition,
                glanceId = glanceId,
            ) { old -> old.copy(cityCode = item.cityCode, appearance = appearance, forecastMode = forecastMode) }

            if (item != OptionItem.Auto) saveRecentCity(item)

            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                },
            )

            sendBroadcast(
                Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component =
                        ComponentName(
                            this@WeatherWidgetConfigureActivity,
                            WeatherUpdateReceiver::class.java,
                        )
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                },
            )

            if (item is OptionItem.CityInfo) {
                RecentCitiesRepository.save(item)
            }

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
        prefs
            .edit()
            .putStringSet(key, current.take(5).toSet())
            .putString("info_${item.cityCode}", "${item.title}|${item.subtitle.orEmpty()}|${item.cityKind}")
            .apply()
    }
}

fun Context.startWidgetConfigure(appWidgetId: Int) {
    Intent(this, WeatherWidgetConfigureActivity::class.java)
        .apply {
            action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also(::startActivity)
}
