package com.chumakov123.gismeteoweather.data.city

import android.content.Context
import android.content.SharedPreferences
import com.chumakov123.gismeteoweather.domain.model.LocationInfo

object RecentCitiesRepository {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_LIST = "recent_cities_ordered"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun save(city: LocationInfo.CityInfo) {
        if (city.cityCode == LocationInfo.Auto.cityCode) return

        val current = prefs.getString(KEY_LIST, "")?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
        current.remove(city.cityCode)
        current.add(0, city.cityCode)

        prefs.edit()
            .putString(KEY_LIST, current.joinToString(","))
            .putString("info_${city.cityCode}", "${city.title}|${city.subtitle.orEmpty()}|${city.cityKind}")
            .apply()
    }

    fun loadRecent(ipCity: LocationInfo.CityInfo?): List<LocationInfo.CityInfo> {
        val orderedCodes = prefs.getString(KEY_LIST, "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        return orderedCodes.mapNotNull { code ->
            if (code == ipCity?.cityCode) return@mapNotNull null

            prefs.getString("info_$code", null)
                ?.split("|", limit = 3)
                ?.takeIf { it.size == 3 }
                ?.let { (title, subtitle, kind) ->
                    LocationInfo.CityInfo(
                        code = code,
                        name = title,
                        info = subtitle.takeIf { it.isNotBlank() },
                        kind = kind
                    )
                }
        }
    }
}