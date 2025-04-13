package com.chumakov123.gismeteoweather.utils

import kotlinx.serialization.json.Json

object JsonProvider {
    val json: Json = Json {
        ignoreUnknownKeys = true
    }
}