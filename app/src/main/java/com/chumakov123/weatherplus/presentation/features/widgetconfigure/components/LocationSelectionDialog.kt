package com.chumakov123.weatherplus.presentation.features.widgetconfigure.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.weatherplus.domain.model.LocationInfo
import com.chumakov123.weatherplus.presentation.common.components.SearchBar
import com.chumakov123.weatherplus.presentation.common.components.SearchResultRow

@Composable
fun LocationSelectionDialog(
    showDialog: Boolean,
    query: String,
    options: List<LocationInfo>,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onLocationSelected: (LocationInfo) -> Unit,
) {
    val displayedOptions = remember(options) {
        options.take(10)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Выберите пункт") },
            text = {
                Column {
                    SearchBar(
                        query = query,
                        isSearchVisible = true,
                        onQueryChange = onQueryChange,
                        label = "Поиск города",
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(displayedOptions) { item ->
                            SearchResultRow(item) {
                                onLocationSelected(item)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            },
        )
    }
}
