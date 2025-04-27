package com.chumakov123.gismeteoweather.domain.util

import kotlinx.serialization.json.Json

object JsonConfig {
    internal val AppJson: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint       = false
        isLenient         = true
    }
}