package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent(
    isSearchActive: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onCancel: () -> Unit,
    onClear: () -> Unit,
    onSearchActivate: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val textStyle = MaterialTheme.typography.bodyLarge
    TopAppBar(
        title = {
            if (!isSearchActive) {
                Text("Погода Gismeteo", color = MaterialTheme.colorScheme.onPrimary)
            } else {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Поиск местоположения", style = textStyle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = onClear) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    },
                    textStyle = textStyle,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedPlaceholderColor = Color(0xFFb1d8fe),
                        unfocusedPlaceholderColor = Color(0xFFb1d8fe),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                )
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(onClick = onSearchActivate) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}