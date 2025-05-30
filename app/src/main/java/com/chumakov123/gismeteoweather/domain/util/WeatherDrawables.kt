package com.chumakov123.gismeteoweather.domain.util

import com.chumakov123.gismeteoweather.R
import kotlin.math.ceil

object WeatherDrawables {
    val drawableMap: Map<String, Int> = mapOf(
        "c3" to R.drawable.c3,
        "c3_r1" to R.drawable.c3_r1,
        "c3_r1_st" to R.drawable.c3_r1_st,
        "c3_r2" to R.drawable.c3_r2,
        "c3_r2_st" to R.drawable.c3_r2_st,
        "c3_r3" to R.drawable.c3_r3,
        "c3_r3_st" to R.drawable.c3_r3_st,
        "c3_rs1" to R.drawable.c3_rs1,
        "c3_rs1_st" to R.drawable.c3_rs1_st,
        "c3_rs2" to R.drawable.c3_rs2,
        "c3_rs2_st" to R.drawable.c3_rs2_st,
        "c3_rs3" to R.drawable.c3_rs3,
        "c3_rs3_st" to R.drawable.c3_rs3_st,
        "c3_s1" to R.drawable.c3_s1,
        "c3_s1_st" to R.drawable.c3_s1_st,
        "c3_s2" to R.drawable.c3_s2,
        "c3_s2_st" to R.drawable.c3_s2_st,
        "c3_s3" to R.drawable.c3_s3,
        "c3_s3_st" to R.drawable.c3_s3_st,
        "c3_st" to R.drawable.c3_st,
        "d" to R.drawable.d,
        "d_c1" to R.drawable.d_c1,
        "d_c1_r1" to R.drawable.d_c1_r1,
        "d_c1_r1_st" to R.drawable.d_c1_r1_st,
        "d_c1_r2" to R.drawable.d_c1_r2,
        "d_c1_r2_st" to R.drawable.d_c1_r2_st,
        "d_c1_r3" to R.drawable.d_c1_r3,
        "d_c1_r3_st" to R.drawable.d_c1_r3_st,
        "d_c1_rs1" to R.drawable.d_c1_rs1,
        "d_c1_rs1_st" to R.drawable.d_c1_rs1_st,
        "d_c1_rs2" to R.drawable.d_c1_rs2,
        "d_c1_rs2_st" to R.drawable.d_c1_rs2_st,
        "d_c1_rs3" to R.drawable.d_c1_rs3,
        "d_c1_rs3_st" to R.drawable.d_c1_rs3_st,
        "d_c1_s1" to R.drawable.d_c1_s1,
        "d_c1_s1_st" to R.drawable.d_c1_s1_st,
        "d_c1_s2" to R.drawable.d_c1_s2,
        "d_c1_s2_st" to R.drawable.d_c1_s2_st,
        "d_c1_s3" to R.drawable.d_c1_s3,
        "d_c1_s3_st" to R.drawable.d_c1_s3_st,
        "d_c1_st" to R.drawable.d_c1_st,
        "d_c2" to R.drawable.d_c2,
        "d_c2_r1" to R.drawable.d_c2_r1,
        "d_c2_r1_st" to R.drawable.d_c2_r1_st,
        "d_c2_r2" to R.drawable.d_c2_r2,
        "d_c2_r2_st" to R.drawable.d_c2_r2_st,
        "d_c2_r3" to R.drawable.d_c2_r3,
        "d_c2_r3_st" to R.drawable.d_c2_r3_st,
        "d_c2_rs1" to R.drawable.d_c2_rs1,
        "d_c2_rs1_st" to R.drawable.d_c2_rs1_st,
        "d_c2_rs2" to R.drawable.d_c2_rs2,
        "d_c2_rs2_st" to R.drawable.d_c2_rs2_st,
        "d_c2_rs3" to R.drawable.d_c2_rs3,
        "d_c2_rs3_st" to R.drawable.d_c2_rs3_st,
        "d_c2_s1" to R.drawable.d_c2_s1,
        "d_c2_s1_st" to R.drawable.d_c2_s1_st,
        "d_c2_s2" to R.drawable.d_c2_s2,
        "d_c2_s2_st" to R.drawable.d_c2_s2_st,
        "d_c2_s3" to R.drawable.d_c2_s3,
        "d_c2_s3_st" to R.drawable.d_c2_s3_st,
        "d_c2_st" to R.drawable.d_c2_st,
        "d_st" to R.drawable.d_st,
        "mist" to R.drawable.mist,
        "n" to R.drawable.n,
        "n_c1" to R.drawable.n_c1,
        "n_c1_r1" to R.drawable.n_c1_r1,
        "n_c1_r1_st" to R.drawable.n_c1_r1_st,
        "n_c1_r2" to R.drawable.n_c1_r2,
        "n_c1_r2_st" to R.drawable.n_c1_r2_st,
        "n_c1_r3" to R.drawable.n_c1_r3,
        "n_c1_r3_st" to R.drawable.n_c1_r3_st,
        "n_c1_rs1" to R.drawable.n_c1_rs1,
        "n_c1_rs1_st" to R.drawable.n_c1_rs1_st,
        "n_c1_rs2" to R.drawable.n_c1_rs2,
        "n_c1_rs2_st" to R.drawable.n_c1_rs2_st,
        "n_c1_rs3" to R.drawable.n_c1_rs3,
        "n_c1_rs3_st" to R.drawable.n_c1_rs3_st,
        "n_c1_s1" to R.drawable.n_c1_s1,
        "n_c1_s1_st" to R.drawable.n_c1_s1_st,
        "n_c1_s2" to R.drawable.n_c1_s2,
        "n_c1_s2_st" to R.drawable.n_c1_s2_st,
        "n_c1_s3" to R.drawable.n_c1_s3,
        "n_c1_s3_st" to R.drawable.n_c1_s3_st,
        "n_c1_st" to R.drawable.n_c1_st,
        "n_c2" to R.drawable.n_c2,
        "n_c2_r1" to R.drawable.n_c2_r1,
        "n_c2_r1_st" to R.drawable.n_c2_r1_st,
        "n_c2_r2" to R.drawable.n_c2_r2,
        "n_c2_r2_st" to R.drawable.n_c2_r2_st,
        "n_c2_r3" to R.drawable.n_c2_r3,
        "n_c2_r3_st" to R.drawable.n_c2_r3_st,
        "n_c2_rs1" to R.drawable.n_c2_rs1,
        "n_c2_rs1_st" to R.drawable.n_c2_rs1_st,
        "n_c2_rs2" to R.drawable.n_c2_rs2,
        "n_c2_rs2_st" to R.drawable.n_c2_rs2_st,
        "n_c2_rs3" to R.drawable.n_c2_rs3,
        "n_c2_rs3_st" to R.drawable.n_c2_rs3_st,
        "n_c2_s1" to R.drawable.n_c2_s1,
        "n_c2_s1_st" to R.drawable.n_c2_s1_st,
        "n_c2_s2" to R.drawable.n_c2_s2,
        "n_c2_s2_st" to R.drawable.n_c2_s2_st,
        "n_c2_s3" to R.drawable.n_c2_s3,
        "n_c2_s3_st" to R.drawable.n_c2_s3_st,
        "n_c2_st" to R.drawable.n_c2_st,
        "n_st" to R.drawable.n_st,
        "r1_mist" to R.drawable.r1_mist,
        "r1_st_mist" to R.drawable.r1_st_mist,
        "r2_mist" to R.drawable.r2_mist,
        "r2_st_mist" to R.drawable.r2_st_mist,
        "r3_mist" to R.drawable.r3_mist,
        "r3_st_mist" to R.drawable.r3_st_mist,
        "s1_mist" to R.drawable.s1_mist,
        "s1_st_mist" to R.drawable.s1_st_mist,
        "s2_mist" to R.drawable.s2_mist,
        "s2_st_mist" to R.drawable.s2_st_mist,
        "s3_mist" to R.drawable.s3_mist,
        "s3_st_mist" to R.drawable.s3_st_mist
    )

    private val geomagneticDrawables = mapOf(
        0 to R.drawable.geomagnetic_0,
        1 to R.drawable.geomagnetic_1,
        2 to R.drawable.geomagnetic_2,
        3 to R.drawable.geomagnetic_3,
        4 to R.drawable.geomagnetic_4,
        5 to R.drawable.geomagnetic_5,
        6 to R.drawable.geomagnetic_6,
        7 to R.drawable.geomagnetic_7,
        8 to R.drawable.geomagnetic_8
    )

    private val scale5Drawables = mapOf(
        1 to R.drawable.scale5_1,
        2 to R.drawable.scale5_2,
        3 to R.drawable.scale5_3,
        4 to R.drawable.scale5_4,
        5 to R.drawable.scale5_5,
    )

    private val scale8Drawables = mapOf(
        1 to R.drawable.scale8_1,
        2 to R.drawable.scale8_2,
        3 to R.drawable.scale8_3,
        4 to R.drawable.scale8_4,
        5 to R.drawable.scale8_5,
        6 to R.drawable.scale8_6,
        7 to R.drawable.scale8_7,
        8 to R.drawable.scale8_8,
    )

    private val scale10Drawables = mapOf(
        1 to R.drawable.scale10_1,
        2 to R.drawable.scale10_2,
        3 to R.drawable.scale10_3,
        4 to R.drawable.scale10_4,
        5 to R.drawable.scale10_5,
        6 to R.drawable.scale10_6,
        7 to R.drawable.scale10_7,
        8 to R.drawable.scale10_8,
        9 to R.drawable.scale10_9,
        10 to R.drawable.scale10_10,
    )

    private val humidityDrawables = mapOf(
        0 to R.drawable.hum_0,
        1 to R.drawable.hum_1,
        2 to R.drawable.hum_2,
        3 to R.drawable.hum_3,
    )

    private val windDrawables = mapOf(
        0 to R.drawable.wind_zero,
        1 to R.drawable.wind_small,
        2 to R.drawable.wind_normal,
        3 to R.drawable.wind_big,
    )

    private val precipitationDrawables = mapOf(
        1 to R.drawable.precipitation_1,
        2 to R.drawable.precipitation_2,
        3 to R.drawable.precipitation_3,
        4 to R.drawable.precipitation_4,
        5 to R.drawable.precipitation_5,
        6 to R.drawable.precipitation_6,
        7 to R.drawable.precipitation_7,
        8 to R.drawable.precipitation_8,
        9 to R.drawable.precipitation_9,
        10 to R.drawable.precipitation_10,
        11 to R.drawable.precipitation_11,
        12 to R.drawable.precipitation_12
    )

    fun getGeomagneticDrawable(geomagnetic: Int) : Int {
        return geomagneticDrawables[geomagnetic.coerceIn(-1, 8)] ?: R.drawable.empty
    }

    fun getScale5Drawable(index: Int) : Int {
        return scale5Drawables[index.coerceIn(0, 5)] ?: R.drawable.empty
    }

    fun getScale8Drawable(index: Int) : Int {
        return scale8Drawables[index.coerceIn(0, 8)] ?: R.drawable.empty
    }

    fun getScale10Drawable(index: Int) : Int {
        return scale10Drawables[index.coerceIn(0, 10)] ?: R.drawable.empty
    }

    fun getHumidityDrawable(humidity: Int): Int {
        val validHumidity = humidity.coerceIn(0, 100)

        val index = when (validHumidity) {
            in 0..24 -> 0
            in 25..49 -> 1
            in 50..74 -> 2
            else -> 3 // 75..100
        }

        return humidityDrawables[index] ?: R.drawable.hum_0
    }

    fun getWindDrawable(windGust: Int): Int {
        val index = when {
            windGust <= 0 -> 0
            windGust in 1..5 -> 1
            windGust in 6..10 -> 2
            else -> 3
        }

        return windDrawables[index] ?: R.drawable.wind_zero
    }

    fun getWindAngle(windDirection: String): Int {
        return when (windDirection.uppercase()) {
            "Ю" -> 0
            "ЮЗ" -> 45
            "З" -> 90
            "СЗ" -> 135
            "С" -> 180
            "СВ" -> 225
            "В" -> 270
            "ЮВ" -> 315
            else -> 0
        }
    }

    fun getPrecipitationDrawable(precipitation: Double): Int {
        if (precipitation <= 0.0) {
            return R.drawable.empty
        }

        val index = ceil(precipitation).toInt().coerceIn(1, 12)
        return precipitationDrawables[index] ?: R.drawable.empty
    }
}