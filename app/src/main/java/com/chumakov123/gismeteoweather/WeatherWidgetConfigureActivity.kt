package com.chumakov123.gismeteoweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.chumakov123.gismeteoweather.data.remote.GismeteoApi
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherStateDefinition
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.model.WidgetState
import com.chumakov123.gismeteoweather.presentation.receiver.WeatherUpdateReceiver
import com.chumakov123.gismeteoweather.presentation.ui.WeatherGlanceWidget
import com.chumakov123.gismeteoweather.presentation.ui.WeatherWidgetPreview
import com.chumakov123.gismeteoweather.ui.theme.GismeteoWeatherTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

const val previewString = "{\"cityCode\":\"sankt-peterburg-4079\",\"weatherInfo\":{\"type\":\"com.chumakov123.gismeteoweather.domain.model.WeatherInfo.Available\",\"placeName\":\"Санкт-Петербург\",\"placeCode\":\"sankt-peterburg-4079\",\"now\":{\"date\":\"2025-05-24T10:00:00.000Z\",\"colorBackground\":\"d-c3\",\"description\":\"Пасмурно\",\"iconWeather\":\"d_c3\",\"icon\":2131165285,\"temperature\":14,\"humidity\":77,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":2,\"precipitation\":0.04,\"pressure\":762,\"radiation\":1,\"temperatureAir\":14,\"temperatureHeatIndex\":14,\"temperatureWater\":13,\"geomagnetic\":0},\"hourly\":[{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":10,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":8,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":6,\"precipitation\":0.3,\"pressure\":761,\"pressureMin\":null,\"humidity\":91,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, сильный  дождь\",\"icon\":2131165290,\"temperature\":10,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":8,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СЗ\",\"windGust\":4,\"precipitation\":5.9,\"pressure\":761,\"pressureMin\":null,\"humidity\":91,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":10,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":9,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СЗ\",\"windGust\":2,\"precipitation\":1.9,\"pressure\":762,\"pressureMin\":null,\"humidity\":92,\"radiation\":0,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":11,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":11,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СЗ\",\"windGust\":3,\"precipitation\":0.3,\"pressure\":762,\"pressureMin\":null,\"humidity\":91,\"radiation\":0,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":14,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":14,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"С\",\"windGust\":2,\"precipitation\":0.1,\"pressure\":762,\"pressureMin\":null,\"humidity\":78,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":2131165285,\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":2,\"precipitation\":0.0,\"pressure\":761,\"pressureMin\":null,\"humidity\":74,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165308,\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":760,\"pressureMin\":null,\"humidity\":69,\"radiation\":3,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно,  дождь, гроза\",\"icon\":2131165312,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":5,\"precipitation\":1.1,\"pressure\":760,\"pressureMin\":null,\"humidity\":75,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165391,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":759,\"pressureMin\":null,\"humidity\":82,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165391,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":null,\"humidity\":78,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Ясно\",\"icon\":2131165307,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"Ю\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":null,\"humidity\":80,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165308,\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"Ю\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":759,\"pressureMin\":null,\"humidity\":73,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":2131165331,\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"Ю\",\"windGust\":7,\"precipitation\":1.0,\"pressure\":759,\"pressureMin\":null,\"humidity\":74,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":3,\"windDirection\":\"ЮЗ\",\"windGust\":9,\"precipitation\":2.5,\"pressure\":760,\"pressureMin\":null,\"humidity\":85,\"radiation\":1,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":13,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":13,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"СЗ\",\"windGust\":8,\"precipitation\":0.1,\"pressure\":761,\"pressureMin\":null,\"humidity\":83,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":2131165285,\"temperature\":12,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":11,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":762,\"pressureMin\":null,\"humidity\":76,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"daily\":[{\"description\":\"Облачно,  дождь, гроза\",\"icon\":2131165332,\"temperature\":18,\"temperatureMin\":10,\"temperatureAvg\":13,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":8,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":9.6,\"pressure\":762,\"pressureMin\":760,\"humidity\":83,\"radiation\":3,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":2131165331,\"temperature\":18,\"temperatureMin\":12,\"temperatureAvg\":15,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":11,\"windSpeed\":3,\"windDirection\":\"Ю\",\"windGust\":9,\"precipitation\":3.6,\"pressure\":762,\"pressureMin\":758,\"humidity\":79,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, сильный  дождь\",\"icon\":2131165290,\"temperature\":14,\"temperatureMin\":10,\"temperatureAvg\":12,\"temperatureHeatIndex\":14,\"temperatureHeatIndexMin\":9,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":5,\"precipitation\":20.4,\"pressure\":762,\"pressureMin\":757,\"humidity\":83,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":18,\"temperatureMin\":11,\"temperatureAvg\":15,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":10,\"windSpeed\":3,\"windDirection\":\"ЮЗ\",\"windGust\":9,\"precipitation\":6.0,\"pressure\":761,\"pressureMin\":757,\"humidity\":76,\"radiation\":2,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь\",\"icon\":2131165309,\"temperature\":19,\"temperatureMin\":11,\"temperatureAvg\":15,\"temperatureHeatIndex\":19,\"temperatureHeatIndexMin\":10,\"windSpeed\":4,\"windDirection\":\"Ю\",\"windGust\":10,\"precipitation\":0.1,\"pressure\":762,\"pressureMin\":760,\"humidity\":75,\"radiation\":4,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно\",\"icon\":2131165328,\"temperature\":20,\"temperatureMin\":13,\"temperatureAvg\":17,\"temperatureHeatIndex\":20,\"temperatureHeatIndexMin\":13,\"windSpeed\":4,\"windDirection\":\"Ю\",\"windGust\":10,\"precipitation\":0.0,\"pressure\":759,\"pressureMin\":755,\"humidity\":78,\"radiation\":4,\"geomagnetic\":6,\"pollenBirch\":1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно, небольшой  дождь\",\"icon\":2131165329,\"temperature\":17,\"temperatureMin\":13,\"temperatureAvg\":15,\"temperatureHeatIndex\":17,\"temperatureHeatIndexMin\":13,\"windSpeed\":3,\"windDirection\":\"З\",\"windGust\":7,\"precipitation\":0.3,\"pressure\":758,\"pressureMin\":755,\"humidity\":77,\"radiation\":4,\"geomagnetic\":6,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь, гроза\",\"icon\":2131165332,\"temperature\":16,\"temperatureMin\":10,\"temperatureAvg\":14,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":8,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":9.2,\"pressure\":764,\"pressureMin\":759,\"humidity\":72,\"radiation\":3,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":15,\"temperatureMin\":9,\"temperatureAvg\":12,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":8,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":3,\"precipitation\":3.2,\"pressure\":765,\"pressureMin\":762,\"humidity\":66,\"radiation\":1,\"geomagnetic\":4,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь, гроза\",\"icon\":2131165310,\"temperature\":16,\"temperatureMin\":9,\"temperatureAvg\":13,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":9,\"windSpeed\":1,\"windDirection\":\"С\",\"windGust\":3,\"precipitation\":1.8,\"pressure\":763,\"pressureMin\":760,\"humidity\":60,\"radiation\":5,\"geomagnetic\":4,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"updateTime\":1748081500833,\"localTime\":\"2025-05-24T13:11:40\",\"astroTimes\":{\"sunset\":\"21:50\",\"sunrise\":\"04:02\"}},\"lastAvailable\":{\"placeName\":\"Санкт-Петербург\",\"placeCode\":\"sankt-peterburg-4079\",\"now\":{\"date\":\"2025-05-24T10:00:00.000Z\",\"colorBackground\":\"d-c3\",\"description\":\"Пасмурно\",\"iconWeather\":\"d_c3\",\"icon\":2131165285,\"temperature\":14,\"humidity\":77,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":2,\"precipitation\":0.04,\"pressure\":762,\"radiation\":1,\"temperatureAir\":14,\"temperatureHeatIndex\":14,\"temperatureWater\":13,\"geomagnetic\":0},\"hourly\":[{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":10,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":8,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"З\",\"windGust\":6,\"precipitation\":0.3,\"pressure\":761,\"pressureMin\":null,\"humidity\":91,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, сильный  дождь\",\"icon\":2131165290,\"temperature\":10,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":8,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СЗ\",\"windGust\":4,\"precipitation\":5.9,\"pressure\":761,\"pressureMin\":null,\"humidity\":91,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":10,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":9,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СЗ\",\"windGust\":2,\"precipitation\":1.9,\"pressure\":762,\"pressureMin\":null,\"humidity\":92,\"radiation\":0,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":11,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":11,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СЗ\",\"windGust\":3,\"precipitation\":0.3,\"pressure\":762,\"pressureMin\":null,\"humidity\":91,\"radiation\":0,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":14,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":14,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"С\",\"windGust\":2,\"precipitation\":0.1,\"pressure\":762,\"pressureMin\":null,\"humidity\":78,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":2131165285,\"temperature\":16,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":2,\"precipitation\":0.0,\"pressure\":761,\"pressureMin\":null,\"humidity\":74,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165308,\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":760,\"pressureMin\":null,\"humidity\":69,\"radiation\":3,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно,  дождь, гроза\",\"icon\":2131165312,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"СВ\",\"windGust\":5,\"precipitation\":1.1,\"pressure\":760,\"pressureMin\":null,\"humidity\":75,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165391,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":759,\"pressureMin\":null,\"humidity\":82,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165391,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"ЮВ\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":null,\"humidity\":78,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Ясно\",\"icon\":2131165307,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"Ю\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":758,\"pressureMin\":null,\"humidity\":80,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно\",\"icon\":2131165308,\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"Ю\",\"windGust\":5,\"precipitation\":0.0,\"pressure\":759,\"pressureMin\":null,\"humidity\":73,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":2131165331,\"temperature\":18,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"Ю\",\"windGust\":7,\"precipitation\":1.0,\"pressure\":759,\"pressureMin\":null,\"humidity\":74,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":15,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":null,\"windSpeed\":3,\"windDirection\":\"ЮЗ\",\"windGust\":9,\"precipitation\":2.5,\"pressure\":760,\"pressureMin\":null,\"humidity\":85,\"radiation\":1,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, небольшой  дождь\",\"icon\":2131165286,\"temperature\":13,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":13,\"temperatureHeatIndexMin\":null,\"windSpeed\":2,\"windDirection\":\"СЗ\",\"windGust\":8,\"precipitation\":0.1,\"pressure\":761,\"pressureMin\":null,\"humidity\":83,\"radiation\":1,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно\",\"icon\":2131165285,\"temperature\":12,\"temperatureMin\":null,\"temperatureAvg\":0,\"temperatureHeatIndex\":11,\"temperatureHeatIndexMin\":null,\"windSpeed\":1,\"windDirection\":\"З\",\"windGust\":6,\"precipitation\":0.0,\"pressure\":762,\"pressureMin\":null,\"humidity\":76,\"radiation\":0,\"geomagnetic\":2,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"daily\":[{\"description\":\"Облачно,  дождь, гроза\",\"icon\":2131165332,\"temperature\":18,\"temperatureMin\":10,\"temperatureAvg\":13,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":8,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":9.6,\"pressure\":762,\"pressureMin\":760,\"humidity\":83,\"radiation\":3,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь\",\"icon\":2131165331,\"temperature\":18,\"temperatureMin\":12,\"temperatureAvg\":15,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":11,\"windSpeed\":3,\"windDirection\":\"Ю\",\"windGust\":9,\"precipitation\":3.6,\"pressure\":762,\"pressureMin\":758,\"humidity\":79,\"radiation\":2,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно, сильный  дождь\",\"icon\":2131165290,\"temperature\":14,\"temperatureMin\":10,\"temperatureAvg\":12,\"temperatureHeatIndex\":14,\"temperatureHeatIndexMin\":9,\"windSpeed\":1,\"windDirection\":\"В\",\"windGust\":5,\"precipitation\":20.4,\"pressure\":762,\"pressureMin\":757,\"humidity\":83,\"radiation\":0,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":18,\"temperatureMin\":11,\"temperatureAvg\":15,\"temperatureHeatIndex\":18,\"temperatureHeatIndexMin\":10,\"windSpeed\":3,\"windDirection\":\"ЮЗ\",\"windGust\":9,\"precipitation\":6.0,\"pressure\":761,\"pressureMin\":757,\"humidity\":76,\"radiation\":2,\"geomagnetic\":3,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь\",\"icon\":2131165309,\"temperature\":19,\"temperatureMin\":11,\"temperatureAvg\":15,\"temperatureHeatIndex\":19,\"temperatureHeatIndexMin\":10,\"windSpeed\":4,\"windDirection\":\"Ю\",\"windGust\":10,\"precipitation\":0.1,\"pressure\":762,\"pressureMin\":760,\"humidity\":75,\"radiation\":4,\"geomagnetic\":5,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно\",\"icon\":2131165328,\"temperature\":20,\"temperatureMin\":13,\"temperatureAvg\":17,\"temperatureHeatIndex\":20,\"temperatureHeatIndexMin\":13,\"windSpeed\":4,\"windDirection\":\"Ю\",\"windGust\":10,\"precipitation\":0.0,\"pressure\":759,\"pressureMin\":755,\"humidity\":78,\"radiation\":4,\"geomagnetic\":6,\"pollenBirch\":1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно, небольшой  дождь\",\"icon\":2131165329,\"temperature\":17,\"temperatureMin\":13,\"temperatureAvg\":15,\"temperatureHeatIndex\":17,\"temperatureHeatIndexMin\":13,\"windSpeed\":3,\"windDirection\":\"З\",\"windGust\":7,\"precipitation\":0.3,\"pressure\":758,\"pressureMin\":755,\"humidity\":77,\"radiation\":4,\"geomagnetic\":6,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Облачно,  дождь, гроза\",\"icon\":2131165332,\"temperature\":16,\"temperatureMin\":10,\"temperatureAvg\":14,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":8,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":6,\"precipitation\":9.2,\"pressure\":764,\"pressureMin\":759,\"humidity\":72,\"radiation\":3,\"geomagnetic\":4,\"pollenBirch\":0,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Пасмурно,  дождь\",\"icon\":2131165288,\"temperature\":15,\"temperatureMin\":9,\"temperatureAvg\":12,\"temperatureHeatIndex\":15,\"temperatureHeatIndexMin\":8,\"windSpeed\":2,\"windDirection\":\"С\",\"windGust\":3,\"precipitation\":3.2,\"pressure\":765,\"pressureMin\":762,\"humidity\":66,\"radiation\":1,\"geomagnetic\":4,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0},{\"description\":\"Малооблачно, небольшой  дождь, гроза\",\"icon\":2131165310,\"temperature\":16,\"temperatureMin\":9,\"temperatureAvg\":13,\"temperatureHeatIndex\":16,\"temperatureHeatIndexMin\":9,\"windSpeed\":1,\"windDirection\":\"С\",\"windGust\":3,\"precipitation\":1.8,\"pressure\":763,\"pressureMin\":760,\"humidity\":60,\"radiation\":5,\"geomagnetic\":4,\"pollenBirch\":-1,\"pollenGrass\":-1,\"snowHeight\":0.0,\"fallingSnow\":0.0}],\"updateTime\":1748081500833,\"localTime\":\"2025-05-24T13:11:40\",\"astroTimes\":{\"sunset\":\"21:50\",\"sunrise\":\"04:02\"}}}"
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
                        onConfirm    = { city, appearance, forecastMode ->
                            applySelectionAndFinish(city, appearance, forecastMode)
                        },
                        previewWeatherState = if (currentState.weatherInfo !is WeatherInfo.Available)
                            Json.decodeFromString(
                                WidgetState.serializer(),
                                previewString
                            )
                        else
                            currentState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun applySelectionAndFinish(item: OptionItem, appearance: WidgetAppearance, forecastMode: ForecastMode) {
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@WeatherWidgetConfigureActivity)
            val glanceId = manager.getGlanceIds(WeatherGlanceWidget::class.java)
                .first { manager.getAppWidgetId(it) == appWidgetId }

            updateAppWidgetState(
                context = this@WeatherWidgetConfigureActivity,
                definition = WeatherStateDefinition,
                glanceId = glanceId
            ) { old -> old.copy(cityCode = item.cityCode, appearance = appearance, forecastMode = forecastMode) }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherWidgetConfigureScreen(
    initialState: WidgetState,
    onConfirm: (OptionItem, WidgetAppearance, ForecastMode) -> Unit,
    modifier: Modifier = Modifier,
    previewWeatherState: WidgetState
) {
    var previewState by remember { mutableStateOf(previewWeatherState) }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenWidthDp = configuration.screenWidthDp.dp

    val previewBoxHeight = screenHeightDp / 3f

    val previewPadding = 32.dp
    val previewRatio = 0.64f
    val previewWidth = screenWidthDp - previewPadding
    val previewHeight = previewWidth * previewRatio

    val previewSizeDp = DpSize(previewWidth, previewHeight)


    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var showLocationDialog by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<List<OptionItem>>(emptyList()) }
    var selected by remember { mutableStateOf<OptionItem>(OptionItem.Auto) }
    var ipCity by remember { mutableStateOf<OptionItem.CityInfo?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    var initialized by remember { mutableStateOf(false) }

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

    val tabIndex = when (previewState.forecastMode) {
        ForecastMode.ByHours -> 0
        ForecastMode.ByDays -> 1
    }

    val tabs = listOf("По часам", "По дням")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Виджет погоды",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val appearance = previewState.appearance
                                val forecastMode = previewState.forecastMode
                                onConfirm(selected, appearance, forecastMode)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Подтвердить"
                            )
                        }
                    },
                )

                // TabRow под TopAppBar
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = {
                                previewState = previewState.copy(
                                    forecastMode = if (index == 0) ForecastMode.ByHours else ForecastMode.ByDays
                                )
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            //.padding(16.dp),
            //verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = innerPadding
        ) {
            if (previewState.weatherInfo is WeatherInfo.Available) {
                item {
                    Box(modifier = modifier
                        .padding(0.dp)
                        .height(previewBoxHeight),
                        contentAlignment = Alignment.Center)
                    {
                        Image(
                            painter = painterResource(id = R.drawable.wallpapper),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment    = Alignment.TopStart,
                            modifier     = Modifier.fillMaxWidth()
                        )
                        WeatherWidgetPreview(
                            weatherInfo = previewState.weatherInfo as WeatherInfo.Available,
                            appearance = previewState.appearance,
                            previewSizeDp = previewSizeDp,
                            previewState.forecastMode)
                    }
                }
            }

            // 2. Блок выбора города
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { showLocationDialog = true }
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Местоположение:")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selected.title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4. Блок настроек внешнего вида
            item {
                Column(
                    modifier = Modifier.padding(0.dp),
                ) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    SettingRow(
                        title = "Время обновления",
                        checked = previewState.appearance.showUpdateTime,
                        onCheckedChange = { newValue ->
                            previewState = previewState.copy(
                                appearance = previewState.appearance.copy(showUpdateTime = newValue)
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    SettingRow(
                        title = "Текущая погода",
                        checked = previewState.appearance.showCurrentWeather,
                        onCheckedChange = { newValue ->
                            previewState = previewState.copy(
                                appearance = previewState.appearance.copy(showCurrentWeather = newValue)
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    SettingRow(
                        title = "Цветовая индикация",
                        checked = previewState.appearance.useColorIndicators,
                        onCheckedChange = { newValue ->
                            previewState = previewState.copy(
                                appearance = previewState.appearance.copy(useColorIndicators = newValue)
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    SettingRow(
                        title = "Ветер",
                        checked = previewState.appearance.showWind,
                        onCheckedChange = { newValue ->
                            previewState = previewState.copy(
                                appearance = previewState.appearance.copy(showWind = newValue)
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    SettingRow(
                        title = "Осадки",
                        checked = previewState.appearance.showPrecipitation,
                        onCheckedChange = { newValue ->
                            previewState = previewState.copy(
                                appearance = previewState.appearance.copy(showPrecipitation = newValue)
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(12.dp))

                    TransparencySetting(
                        backgroundTransparency = previewState.appearance.backgroundTransparencyPercent.toFloat(),
                        onTransparencyChange = { newVal ->
                            previewState = previewState.copy(
                                appearance = previewState.appearance.copy(
                                    backgroundTransparencyPercent = newVal.toInt()
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Выберите город") },
            text = {
                Column {
                    // Поисковая строка
                    SearchBar(
                        query = query,
                        isSearchVisible = true,
                        onQueryChange = { query = it },
                        label = "Поиск города"
                    )
                    Spacer(Modifier.height(8.dp))
                    // Список результатов
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(options) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selected = item
                                        showLocationDialog = false
                                        query = ""           // сбросить запрос
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (item == selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    item.subtitle?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val rowHeight = 48.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight) // гарантирует, что клик захватывает всю строку
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp)
    ) {
        Text(title)
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .size(width = 32.dp, height = 16.dp)
                .scale(0.7f)
        )
    }
}

@Composable
fun TransparencySetting(
    backgroundTransparency: Float,
    onTransparencyChange: (Float) -> Unit
) {
    Text("Прозрачность", modifier = Modifier.padding(horizontal = 16.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Slider(
            value = backgroundTransparency,
            onValueChange = onTransparencyChange,
            valueRange = 0f..100f,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "${backgroundTransparency.toInt()}%",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
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