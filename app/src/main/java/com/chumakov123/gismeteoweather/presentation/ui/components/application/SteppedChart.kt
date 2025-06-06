package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun SteppedChart(
    values: List<Float>,
    baseline: List<Float>? = null,
    modifier: Modifier = Modifier,
    cellWidth: Dp,
    rowHeight: Dp,
    colorForValue: (Float) -> Color,
    fillColorForPair: ((Float, Float) -> Color)? = null,
    labelFormatter: (Float) -> String,
    labelColor: Color,
    labelTextSize: TextUnit,
    scrollState: ScrollState
) {
    if (values.isEmpty()) return

    Canvas(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp)
            .width(cellWidth * values.size + 16.dp * values.size)
            .height(rowHeight)
    ) {
        // Precompute all pixel values once
        val wCell = cellWidth.toPx()
        val padH = 8.dp.toPx()
        val stepX = wCell + padH * 2f
        val half = stepX / 2f
        val h = size.height
        val padV = 16.dp.toPx()
        val usableH = h - padV * 2f

        // Compute scale once
        val all = values + (baseline ?: emptyList())
        val gMin = all.minOrNull() ?: 0f
        val gMax = all.maxOrNull() ?: (gMin + 1f)
        val scale = usableH / (gMax - gMin).coerceAtLeast(1f)

        // Precompute points arrays
        val ptsMain = FloatArray(values.size * 2)
        val ptsBase = baseline?.let { FloatArray(it.size * 2) }

        // Precompute all points
        for (i in values.indices) {
            val x = i * stepX + padH + wCell / 2
            val yMain = h - padV - (values[i] - gMin) * scale
            ptsMain[i * 2] = x
            ptsMain[i * 2 + 1] = yMain

            baseline?.let { base ->
                if (i < base.size) {
                    val yBase = h - padV - (base[i] - gMin) * scale
                    ptsBase!![i * 2] = x
                    ptsBase[i * 2 + 1] = yBase
                }
            }
        }

        // Draw filled areas if needed
        fillColorForPair?.let { fillFn ->
            baseline?.let { base ->
                if (base.size == values.size) {
                    for (i in values.indices) {
                        val vTop = values[i]
                        val vBot = base[i]
                        if (vTop == vBot) continue

                        val xL = ptsMain[i * 2] - half
                        val xR = ptsMain[i * 2] + half
                        val yT = ptsMain[i * 2 + 1]
                        val yB = ptsBase!![i * 2 + 1]

                        val left = xL
                        val top = minOf(yT, yB)
                        val width = xR - xL
                        val height = abs(yB - yT)

                        drawRect(
                            color = fillFn(vTop, vBot),
                            topLeft = Offset(left, top),
                            size = Size(width, height)
                        )
                    }
                }
            }
        }

        // Optimized drawing functions
        fun drawSeries(pts: FloatArray, vals: List<Float>) {
            if (pts.isEmpty()) return

            // Precompute stroke width once
            val strokeWidth = 1.dp.toPx()

            // First segment
            drawLine(
                start = Offset(pts[0] - half, pts[1]),
                end = Offset(pts[0], pts[1]),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                color = colorForValue(vals[0])
            )

            // Middle segments
            for (i in 0 until vals.size - 1) {
                val x1 = pts[i * 2]
                val y1 = pts[i * 2 + 1]
                val x2 = pts[(i + 1) * 2]
                val y2 = pts[(i + 1) * 2 + 1]
                val midX = x1 + half

                // Horizontal segment
                drawLine(
                    start = Offset(x1, y1),
                    end = Offset(midX, y1),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    color = colorForValue(vals[i])
                )

                // Vertical segment
                drawLine(
                    start = Offset(midX, y1),
                    end = Offset(midX, y2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    color = colorForValue(vals[i])
                )

                // Last horizontal segment
                drawLine(
                    start = Offset(midX, y2),
                    end = Offset(x2, y2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                    color = colorForValue(vals[i + 1])
                )
            }

            // Last segment
            val lastIdx = (vals.size - 1) * 2
            drawLine(
                start = Offset(pts[lastIdx], pts[lastIdx + 1]),
                end = Offset(pts[lastIdx] + half, pts[lastIdx + 1]),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                color = colorForValue(vals.last())
            )
        }

        // Draw series
        if (ptsBase != null && baseline.anyIndexed { i, v -> v != values[i] }) {
            drawSeries(ptsBase, baseline)
        }
        drawSeries(ptsMain, values)

        // Draw labels
        drawIntoCanvas { dc ->
            val paintMain = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = labelTextSize.toPx()
                color = labelColor.toArgb()
            }
            val paintBase = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = (labelTextSize.value - 2).sp.toPx()
                color = labelColor.toArgb()
            }
            val dy = 6.dp.toPx()

            ptsBase?.let { base ->
                for (i in baseline.indices) {
                    val vBot = baseline[i]
                    if (vBot == values[i]) continue

                    dc.nativeCanvas.drawText(
                        labelFormatter(vBot),
                        base[i * 2],
                        base[i * 2 + 1] + dy + paintBase.textSize,
                        paintBase
                    )
                }
            }

            for (i in values.indices) {
                dc.nativeCanvas.drawText(
                    labelFormatter(values[i]),
                    ptsMain[i * 2],
                    ptsMain[i * 2 + 1] - dy,
                    paintMain
                )
            }
        }
    }
}

private inline fun <T> Iterable<T>.anyIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
    for ((i, v) in this.withIndex()) if (predicate(i, v)) return true
    return false
}