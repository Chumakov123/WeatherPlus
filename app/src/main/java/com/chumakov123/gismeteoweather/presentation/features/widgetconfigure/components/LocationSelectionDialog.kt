package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.OptionItem
import com.chumakov123.gismeteoweather.presentation.components.SearchBar
import com.chumakov123.gismeteoweather.presentation.components.SearchResultRow

@Composable
fun LocationSelectionDialog(
    showDialog: Boolean,
    query: String,
    options: List<OptionItem>,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onLocationSelected: (OptionItem) -> Unit,
) {
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
                        items(options) { item ->
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
