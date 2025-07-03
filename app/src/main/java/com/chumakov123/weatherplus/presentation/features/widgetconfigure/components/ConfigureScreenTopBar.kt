package com.chumakov123.weatherplus.presentation.features.widgetconfigure.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chumakov123.weatherplus.domain.model.ForecastMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureScreenTopBar(
    forecastMode: ForecastMode,
    onConfirm: () -> Unit,
    onForecastModeChange: (ForecastMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = "Виджет погоды",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            },
            actions = {
                IconButton(onClick = onConfirm) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Подтвердить",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )

        val tabIndex = when (forecastMode) {
            ForecastMode.ByHours -> 0
            ForecastMode.ByDays -> 1
        }
        val tabs = listOf("По часам", "По дням")

        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = index == tabIndex,
                    onClick = { onForecastModeChange(if (index == 0) ForecastMode.ByHours else ForecastMode.ByDays) },
                    text = {
                        Text(
                            text = title,
                            color = if (index == tabIndex) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                Color(0xFFb1d8fe)
                            },
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
