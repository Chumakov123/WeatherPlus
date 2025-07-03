package com.chumakov123.weatherplus.domain.model

sealed class LocationInfo(
    val cityCode: String,
    val title: String,
    val subtitle: String?,
    val cityKind: String,
) {
    data object Auto : LocationInfo("auto", "Автоопределение", null, cityKind = "T")

    data class CityInfo(
        val code: String,
        val kind: String,
        val name: String,
        val info: String?,
    ) : LocationInfo(code, name, info, kind)
}
