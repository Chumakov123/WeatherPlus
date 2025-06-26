package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance

@Composable
fun AppearanceSettings(
    appearance: WidgetAppearance,
    onAppearanceChange: (WidgetAppearance) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(0.dp)) {
        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingRow(
            title = "Время обновления",
            checked = appearance.showUpdateTime,
            onCheckedChange = { newValue ->
                onAppearanceChange(appearance.copy(showUpdateTime = newValue))
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingRow(
            title = "Текущая погода",
            checked = appearance.showCurrentWeather,
            onCheckedChange = { newValue ->
                onAppearanceChange(appearance.copy(showCurrentWeather = newValue))
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingRow(
            title = "Цветовая индикация",
            checked = appearance.useColorIndicators,
            onCheckedChange = { newValue ->
                onAppearanceChange(appearance.copy(useColorIndicators = newValue))
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingRow(
            title = "Ветер",
            checked = appearance.showWind,
            onCheckedChange = { newValue ->
                onAppearanceChange(appearance.copy(showWind = newValue))
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        SettingRow(
            title = "Осадки",
            checked = appearance.showPrecipitation,
            onCheckedChange = { newValue ->
                onAppearanceChange(appearance.copy(showPrecipitation = newValue))
            },
        )

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(12.dp))

        TransparencySetting(
            backgroundTransparency = appearance.backgroundTransparencyPercent.toFloat(),
            onTransparencyChange = { newVal ->
                onAppearanceChange(
                    appearance.copy(
                        backgroundTransparencyPercent = newVal.toInt()
                    )
                )
            },
        )
    }
}
