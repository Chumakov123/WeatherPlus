package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.WeatherCell
import com.chumakov123.gismeteoweather.domain.model.WeatherRow
import kotlinx.coroutines.launch

@Composable
fun WeatherTable(rows: List<WeatherRow>) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val scrollableState = rememberScrollableState { delta ->
        coroutineScope.launch { scrollState.scrollBy(-delta) }
        delta
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            when (row) {
                is WeatherRow.DataRow -> {
                    row.label?.let { label ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scrollable(scrollableState, Orientation.Horizontal)
                                .padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        row.values.forEach { cell ->
                            val cellModifier = Modifier
                                .size(width = row.cellWidth, height = row.cellHeight)

                            val content = @Composable {
                                when (cell) {
                                    is WeatherCell.Text -> Box(cellModifier, Alignment.Center) {
                                        Text(cell.text, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    is WeatherCell.Icon -> Box(cellModifier, Alignment.Center) {
                                        Image(
                                            imageVector = ImageVector.vectorResource(cell.resId),
                                            contentDescription = cell.contentDescription
                                        )
                                    }

                                    is WeatherCell.IconWithCenterText -> Box(cellModifier, Alignment.Center) {
                                        Image(
                                            imageVector = ImageVector.vectorResource(cell.iconRes),
                                            contentDescription = cell.contentDescription
                                        )
                                        Text(
                                            cell.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }

                                    is WeatherCell.IconAboveText -> Box(cellModifier) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Image(
                                                imageVector = ImageVector.vectorResource(cell.iconRes),
                                                contentDescription = cell.contentDescription,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(0.33f)
                                                    .graphicsLayer {
                                                        cell.iconRotation?.let { rotationZ = it }
                                                    }
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                cell.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }

                                    is WeatherCell.IconBelowText -> Column(
                                        modifier = cellModifier,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(cell.text, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Image(
                                            imageVector = ImageVector.vectorResource(cell.iconRes),
                                            contentDescription = cell.contentDescription
                                        )
                                    }

                                    is WeatherCell.ColumnBackground -> {
                                        val contentWidth = row.cellWidth * 0.4f

                                        Box(
                                            modifier = cellModifier,
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(contentWidth)
                                                    .fillMaxHeight()
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFF29343A),
                                                                Color(0xFF1B2023)
                                                            )
                                                        ),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                            ) {
                                                Text(
                                                    text = cell.text,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = cell.textColor),
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .offset(y = -cell.textOffsetFromBottom)
                                                        .width(contentWidth)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .width(contentWidth)
                                                    .clip(
                                                        RoundedCornerShape(
                                                            bottomStart = 4.dp,
                                                            bottomEnd = 4.dp
                                                        )
                                                    )
                                            ) {
                                                Image(
                                                    imageVector = ImageVector.vectorResource(cell.iconRes),
                                                    contentDescription = cell.contentDescription,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.Center),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (row.useSurface) {
                                Surface(
                                    tonalElevation = 2.dp,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = cellModifier
                                ) {
                                    content()
                                }
                            } else {
                                content()
                            }
                        }
                    }
                }

                is WeatherRow.ChartRow -> {
                    row.label?.let { label ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .scrollable(scrollableState, Orientation.Horizontal)
                                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    SteppedChart(
                        values = row.values,
                        baseline = row.baseline,
                        cellWidth = row.cellWidth,
                        rowHeight = row.rowHeight,
                        colorForValue = row.colorForValue,
                        fillColorForPair = row.fillColorForPair,
                        labelFormatter = row.labelFormatter,
                        labelColor = row.labelColor,
                        labelTextSize = row.labelTextSize,
                        scrollState = scrollState
                    )
                }
            }
        }
    }
}