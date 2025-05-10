package com.chumakov123.gismeteoweather.domain.model

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
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
    private const val FILE_PREFIX = "widgetState_"

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<WidgetState> = DataStoreFactory.create(
        serializer = WidgetStateSerializer,
        produceFile = { context.dataStoreFile("$FILE_PREFIX$fileKey") }
    )

    override fun getLocation(context: Context, fileKey: String): File =
        context.dataStoreFile("$FILE_PREFIX$fileKey")

    object WidgetStateSerializer : Serializer<WidgetState> {
        override val defaultValue = WidgetState()

        override suspend fun readFrom(input: InputStream): WidgetState = try {
            Json.decodeFromString(
                WidgetState.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            throw CorruptionException("Could not read widget state", e)
        }

        override suspend fun writeTo(t: WidgetState, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(WidgetState.serializer(), t)
                        .encodeToByteArray()
                )
            }
        }
    }
}

@Serializable
data class WidgetState(
    val cityCode: String = "auto",
    val forecastMode: ForecastMode = ForecastMode.ByHours,
    val weatherInfo: WeatherInfo = WeatherInfo.Unavailable("Нет данных"),
    val appearance: WidgetAppearance = WidgetAppearance(),
    val lastAvailable: WeatherInfo.Available? = null,
    val forecastColumns: Int = 6,
    val forecastRows: Int = 2
)