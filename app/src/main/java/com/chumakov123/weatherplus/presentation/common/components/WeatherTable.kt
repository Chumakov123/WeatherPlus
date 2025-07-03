package com.chumakov123.weatherplus.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chumakov123.weatherplus.domain.model.WeatherCell
import com.chumakov123.weatherplus.domain.model.WeatherRow
import com.chumakov123.weatherplus.domain.util.WeatherDrawables

@Composable
fun WeatherTable(
    rows: List<WeatherRow>,
    cellWidth: Dp = 64.dp
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            when (row) {
                is WeatherRow.DataRow -> {
                    DataRowContent(
                        row = row,
                        scrollState = scrollState,
                        cellWidth = cellWidth
                    )
                }
                is WeatherRow.ChartRow -> {
                    ChartRowContent(
                        row = row,
                        scrollState = scrollState
                    )
                }
            }
        }
    }
}

@Composable
private fun DataRowContent(
    row: WeatherRow.DataRow,
    scrollState: ScrollState,
    cellWidth: Dp
) {
    val labelModifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState)
        .padding(start = 8.dp, top = 8.dp, bottom = 4.dp)

    val rowModifier = Modifier
        .horizontalScroll(scrollState)
        .padding(bottom = 4.dp)

    val cellSizeModifier = Modifier.width(cellWidth).wrapContentHeight()

    row.label?.let { label ->
        Box(labelModifier) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }
    }

    Row(rowModifier) {
        row.values.forEach { cell ->
            if (row.useSurface) {
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = cellSizeModifier
                ) {
                    WeatherCellContent(cell, cellWidth)
                }
            } else {
                Box(cellSizeModifier) {
                    WeatherCellContent(cell, cellWidth)
                }
            }
        }
    }
}

@Composable
private fun WeatherCellContent(cell: WeatherCell, cellWidth: Dp) {
    when (cell) {
        is WeatherCell.Text -> {
            Text(
                text = cell.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
        is WeatherCell.WeatherIcon -> {
            Image(
                imageVector = ImageVector.vectorResource(WeatherDrawables.getWeatherIcon(cell.icon)),
                contentDescription = cell.contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        }
        is WeatherCell.IconWithCenterText -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    imageVector = ImageVector.vectorResource(cell.iconRes),
                    contentDescription = cell.contentDescription,
                    modifier = Modifier.size(36.dp).align(Alignment.Center)
                )
                Text(
                    text = cell.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is WeatherCell.IconAboveText -> {
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
                    modifier = Modifier.size(18.dp)
                        .graphicsLayer {
                            cell.iconRotation?.let { rotationZ = it }
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = cell.text,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                )
            }
        }
        is WeatherCell.IconBelowText -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cell.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Image(
                    imageVector = ImageVector.vectorResource(cell.iconRes),
                    contentDescription = cell.contentDescription,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        is WeatherCell.ColumnBackground -> {
            val contentWidth = cellWidth * 0.4f
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(64.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0B4059),
                                    Color(0xFF093549)
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        text = cell.text,
                        style = MaterialTheme.typography.bodySmall.copy(color = cell.textColor),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = -cell.textOffsetFromBottom)
                            .width(contentWidth)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(contentWidth)
                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(cell.iconRes),
                        contentDescription = cell.contentDescription,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartRowContent(
    row: WeatherRow.ChartRow,
    scrollState: ScrollState
) {
    val labelModifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState)
        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)

    row.label?.let { label ->
        Box(labelModifier) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
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
