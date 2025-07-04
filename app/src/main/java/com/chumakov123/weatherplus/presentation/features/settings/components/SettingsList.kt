package com.chumakov123.weatherplus.presentation.features.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chumakov123.weatherplus.domain.model.WeatherDisplaySettings
import com.chumakov123.weatherplus.domain.model.displayName
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SettingsList(
    settings: WeatherDisplaySettings,
    onSettingsChange: (WeatherDisplaySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val localOrder = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { settings.rowOrder.toMutableStateList() }

    var localEnabledRows by rememberSaveable {
        mutableStateOf(settings.enabledRows)
    }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localOrder.add(to.index, localOrder.removeAt(from.index))
    }

    val currentOnSettingsChanged by rememberUpdatedState(onSettingsChange)

    DisposableEffect(Unit) {
        onDispose {
            if ((localOrder != settings.rowOrder) || (localEnabledRows != settings.enabledRows)) {
                currentOnSettingsChanged(
                    settings.copy(
                        rowOrder = localOrder.toList(),
                        enabledRows = localEnabledRows
                    )
                )
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxSize()
    ) {
        items(
            items = localOrder,
            key = { it }
        ) { row ->
            ReorderableItem(reorderableLazyListState, key = row) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .draggableHandle()
                                .padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DragHandle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = row.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Checkbox(
                            checked = row in localEnabledRows,
                            onCheckedChange = { checked ->
                                localEnabledRows = if (checked) {
                                    localEnabledRows + row
                                } else {
                                    localEnabledRows - row
                                }
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(48.dp),
                        )
                    }
                }
            }
        }
    }
}
