package com.chumakov123.weatherplus.presentation.widget

import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider

/**
 * Provide a Box composable using the system parameters for app widgets background with rounded
 * corners and background color.
 */
@Composable
fun AppWidgetBox(
    modifier: GlanceModifier = GlanceModifier,
    contentAlignment: Alignment = Alignment.TopStart,
    transparencyPercent: Int = 50,
    content: @Composable () -> Unit
) {
    Box(
        modifier = appWidgetBackgroundModifier(transparencyPercent).then(modifier),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Provide a Column composable using the system parameters for app widgets background with rounded
 * corners and background color.
 */
@Composable
fun AppWidgetColumn(
    modifier: GlanceModifier = GlanceModifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    transparencyPercent: Int = 50,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = appWidgetBackgroundModifier(transparencyPercent).then(modifier),
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun appWidgetBackgroundModifier(transparencyPercent: Int = 50) = GlanceModifier
    .fillMaxSize()
    .appWidgetBackground()
    .background(ColorProvider(Color(0, 0, 0, (255 * (100 - transparencyPercent) / 100).coerceIn(0, 255))))
    .appWidgetBackgroundCornerRadius()

fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        cornerRadius(16.dp)
    }
    return this
}

fun GlanceModifier.appWidgetInnerCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_inner_radius)
    } else {
        cornerRadius(8.dp)
    }
    return this
}

val Float.toPx get() = this * Resources.getSystem().displayMetrics.density
