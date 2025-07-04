package com.chumakov123.weatherplus.domain.util

import kotlinx.serialization.json.Json

object JsonConfig {
    internal val AppJson: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
    }
}
