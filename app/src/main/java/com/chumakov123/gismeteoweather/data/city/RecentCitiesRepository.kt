package com.chumakov123.gismeteoweather.data.city

import android.content.Context
import android.content.SharedPreferences
import com.chumakov123.gismeteoweather.domain.model.LocationInfo

object RecentCitiesRepository {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_LIST = "recent_cities"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun save(city: LocationInfo.CityInfo) {
        if (city.cityCode == LocationInfo.Auto.cityCode) return

        val current = prefs
            .getStringSet(KEY_LIST, emptySet())!!
            .toMutableList()

        current.remove(city.cityCode)
        current.add(0, city.cityCode)

        prefs.edit()
            .putStringSet(KEY_LIST, current.take(5).toSet())
            .putString("info_${city.cityCode}", "${city.title}|${city.subtitle.orEmpty()}|${city.cityKind}")
            .apply()
    }

    fun loadRecent(ipCity: LocationInfo.CityInfo?): List<LocationInfo.CityInfo> {
        return prefs.getStringSet(KEY_LIST, emptySet())!!
            .mapNotNull { code ->
                if (code == ipCity?.cityCode) return@mapNotNull null

                prefs.getString("info_$code", null)
                    ?.split("|", limit = 3)
                    ?.takeIf { it.size == 3 }
                    ?.let { parts ->
                        val (title, subtitle, kind) = parts
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
