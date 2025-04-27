package com.chumakov123.gismeteoweather.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RuTranslation(
    val city: NameField,
    val country: NameField,
    val district: NameField? = null,      // nullable
    val subdistrict: NameField? = null    // nullable
)