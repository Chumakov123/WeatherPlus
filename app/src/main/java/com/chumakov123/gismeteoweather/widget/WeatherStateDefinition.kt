package com.chumakov123.gismeteoweather.widget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream


/**
 * Provides our own definition of "Glance state" using Kotlin serialization.
 */
object WeatherStateDefinition : GlanceStateDefinition<WidgetState> {
    private const val DATA_STORE_FILENAME = "widgetState"

    private val Context.datastore by dataStore(DATA_STORE_FILENAME, WidgetStateSerializer)

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<WidgetState> {
        return context.datastore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME)
    }

    // Сериализатор для состояния виджета
    object WidgetStateSerializer : Serializer<WidgetState> {
        override val defaultValue = WidgetState(ForecastMode.ByHours, WeatherInfo.Unavailable("no data"))

        override suspend fun readFrom(input: InputStream): WidgetState = try {
            Json.decodeFromString(WidgetState.serializer(), input.readBytes().decodeToString())
        } catch (exception: SerializationException) {
            throw CorruptionException("Could not read widget state: ${exception.message}")
        }

        override suspend fun writeTo(t: WidgetState, output: OutputStream) {
            output.use {
                it.write(Json.encodeToString(WidgetState.serializer(), t).encodeToByteArray())
            }
        }
    }
}
@Serializable
data class WidgetState(
    val forecastMode: ForecastMode,  // Режим прогноза (по дням или по часам)
    val weatherInfo: WeatherInfo    // Информация о погоде
)