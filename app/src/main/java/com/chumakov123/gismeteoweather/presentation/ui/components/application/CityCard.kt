package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.util.TemperatureGradation
import com.chumakov123.gismeteoweather.domain.util.Utils
import com.chumakov123.gismeteoweather.domain.util.Utils.plusMillis
import com.chumakov123.gismeteoweather.domain.util.Utils.toTimeString
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.CityWeatherUiState

@Composable
fun CityCard(
    cityState: CityWeatherUiState.Success,
    nowMillis: Long,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    dragHandleModifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = true }
            )
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = dragHandleModifier.size(48.dp),
                    onClick =  onClick,

                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(
                            WeatherDrawables.getWeatherIcon(cityState.rawData.now.icon)
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }


                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconRes = when (cityState.rawData.placeKind) {
                            "M" -> R.drawable.compound_station
                            "A" -> R.drawable.compound_airport
                            else -> null
                        }
                        iconRes?.let {
                            Image(
                                imageVector = ImageVector.vectorResource(it),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = cityState.rawData.placeName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    val localDateTime = cityState.rawData.localTime
                        .plusMillis(nowMillis - cityState.rawData.updateTime)
                    Text(
                        text = localDateTime.toTimeString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = Utils.formatTemperature(cityState.rawData.now.temperature),
                    style = MaterialTheme.typography.titleLarge,
                    color = TemperatureGradation.interpolateTemperatureColor(
                        cityState.rawData.now.temperature,
                        isDarkTheme = false
                    )
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = {
                    expanded = false
                    onRemove()
                }
            )
        }
    }
}

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    Spacer(
        modifier = modifier
            .background(brush = brush, shape = shape)
    )
}

@Composable
fun CityCardShimmer() {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                ShimmerPlaceholder(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp),
                        shape = CircleShape
                    )

                    ShimmerPlaceholder(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth(0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                ShimmerPlaceholder(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.15f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            ShimmerPlaceholder(
                modifier = Modifier
                    .height(28.dp)
                    .width(48.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}