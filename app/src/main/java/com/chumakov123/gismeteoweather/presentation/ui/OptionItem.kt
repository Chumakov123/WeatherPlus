package com.chumakov123.gismeteoweather.presentation.ui

sealed class OptionItem {
    abstract val cityCode: String
    abstract val title: String
    abstract val subtitle: String?

    object Auto : OptionItem() {
        override val cityCode = "auto"
        override val title    = "Автоопределение"
        override val subtitle = ""
    }

    data class City(
        override val cityCode: String,
        override val title: String,
        override val subtitle: String? = null
    ) : OptionItem()
}