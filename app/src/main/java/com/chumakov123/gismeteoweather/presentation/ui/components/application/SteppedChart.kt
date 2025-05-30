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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        val wCell   = cellWidth.toPx()
        val padH    = 8.dp.toPx()
        val stepX   = wCell + padH * 2f
        val half    = stepX / 2f
        val h       = size.height
        val padV    = 16.dp.toPx()
        val usableH = h - padV * 2f

        val all = values + (baseline ?: emptyList())
        val gMin  = all.minOrNull() ?: 0f
        val gMax  = all.maxOrNull() ?: gMin + 1f
        val scale = usableH / (gMax - gMin).coerceAtLeast(1f)

        fun point(i: Int, v: Float) = Offset(
            x = i*stepX + padH + wCell/2,
            y = h - padV - (v - gMin)*scale
        )
        val ptsMain = List(values.size) { i -> point(i, values[i]) }
        val ptsBase = baseline
            ?.takeIf { it.size == values.size }
            ?.mapIndexed { i, v -> point(i, v) }

        ptsBase?.let { basePts ->
            val fillFn = fillColorForPair
            if (fillFn != null) {
                for (i in values.indices) {
                    val vTop = values[i]
                    val vBot = baseline!![i]
                    if (vTop == vBot) continue

                    val xL = ptsMain[i].x - half
                    val xR = ptsMain[i].x + half
                    val yT = ptsMain[i].y
                    val yB = basePts[i].y

                    drawPath(
                        Path().apply {
                            moveTo(xL, yT)
                            lineTo(xR, yT)
                            lineTo(xR, yB)
                            lineTo(xL, yB)
                            close()
                        },
                        color = fillFn(vTop, vBot)
                    )
                }
            }
        }

        fun drawSeries(pts: List<Offset>, vals: List<Float>) {
            if (pts.isEmpty()) return
            fun seg(a: Offset, b: Offset, v: Float) =
                drawLine(
                    start = a, end = b,
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                    color = colorForValue(v)
                )

            seg(pts[0] - Offset(half,0f), pts[0], vals[0])
            for (i in 0 until pts.lastIndex) {
                val p1  = pts[i]
                val p2  = pts[i+1]
                val mid = p1.copy(x = p1.x + half)
                seg(p1,             mid,   vals[i])
                seg(mid, mid.copy(y = p2.y), vals[i])
                seg(mid.copy(y = p2.y), p2, vals[i+1])
            }
            seg(pts.last(), pts.last() + Offset(half,0f), vals.last())
        }

        if (ptsBase != null && baseline.anyIndexed { i, v -> v != values[i] }) {
            drawSeries(ptsBase, baseline)
        }
        drawSeries(ptsMain, values)

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

            ptsBase?.forEachIndexed { i, p ->
                val vBot = baseline[i]
                if (vBot == values[i]) return@forEachIndexed
                dc.nativeCanvas.drawText(
                    labelFormatter(vBot),
                    p.x,
                    p.y + dy + paintBase.textSize,
                    paintBase
                )
            }

            ptsMain.forEachIndexed { i, p ->
                dc.nativeCanvas.drawText(
                    labelFormatter(values[i]),
                    p.x,
                    p.y - dy,
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