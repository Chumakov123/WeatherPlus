package com.chumakov123.gismeteoweather.presentation.features.cities.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.presentation.components.ShimmerPlaceholder

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
