package com.chumakov123.gismeteoweather.domain.model

sealed class OptionItem(
    val cityCode: String,
    val title: String,
    val subtitle: String?,
    val cityKind: String,
) {
    data object Auto : OptionItem("auto", "Автоопределение", null, cityKind = "T")

    data class CityInfo(
        val code: String,
        val kind: String,
        val name: String,
        val info: String?,
    ) : OptionItem(code, name, info, kind)
}
