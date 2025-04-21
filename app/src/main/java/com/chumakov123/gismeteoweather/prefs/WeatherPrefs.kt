package com.chumakov123.gismeteoweather.prefs

import android.content.Context
import com.chumakov123.gismeteoweather.data.model.WeatherDTO
import com.chumakov123.gismeteoweather.utils.JsonProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeatherPrefs {

    //region Constants
    private const val PREFS_NAME = "weather_settings"
    private const val WEATHER_NOW_KEY = "weather_now"
    private const val LAST_UPDATE_KEY = "last_update"
    private const val FORECAST_MODE_KEY = "forecast_mode"
    private const val DEBUG_COUNTER_KEY = "debug_counter_mode"
    private const val CITY_KEY = "city"
    private const val UPDATING_STATE_KEY = "is_updating"
    //endregion

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    //region WeatherNow
    fun saveWeatherNow(context: Context, weather: WeatherDTO) {
        val prefs = getPrefs(context)
        val weatherJson = JsonProvider.json.encodeToString(weather)
        prefs.edit().putString(WEATHER_NOW_KEY, weatherJson).apply()
    }

    fun loadWeatherNow(context: Context): WeatherDTO? {
        val prefs = getPrefs(context)
        val weatherJson = prefs.getString(WEATHER_NOW_KEY, null) ?: return null
        return try {
            JsonProvider.json.decodeFromString(weatherJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    //endregion

    //region LastUpdateTime
    fun saveLastUpdateTime(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().putLong(LAST_UPDATE_KEY, System.currentTimeMillis()).apply()
    }

    fun loadLastUpdateTime(context: Context): String? {
        val prefs = getPrefs(context)
        val timeMillis = prefs.getLong(LAST_UPDATE_KEY, 0L)
        return if (timeMillis != 0L) {
            val date = Date(timeMillis)
            val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            dateFormat.format(date)
        } else {
            null
        }
    }
    //endregion

    //region DebugCounter
    fun loadDebugCounter(context: Context): Int {
        val prefs = getPrefs(context)
        return prefs.getInt(DEBUG_COUNTER_KEY, 0)
    }
    fun incDebugCounter(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().putInt(DEBUG_COUNTER_KEY, prefs.getInt(DEBUG_COUNTER_KEY, 0)+1).apply()
    }
    //endregion
}