package com.chumakov123.gismeteoweather.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Translations(
    @SerialName("ru")
    val ru: RuTranslation
)
