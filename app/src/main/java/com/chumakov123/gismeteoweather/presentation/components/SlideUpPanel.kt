package com.chumakov123.gismeteoweather.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideUpPanelContinuous(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    overlay: (@Composable () -> Unit)? = null,
    headerContent: (@Composable RowScope.() -> Unit)? = null,
    panelContent: @Composable ColumnScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()

    var panelHeightPx by remember { mutableFloatStateOf(0f) }
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var contentHeightPx by remember { mutableFloatStateOf(0f) }
    val maxOffset = remember(panelHeightPx, headerHeightPx) {
        (panelHeightPx - headerHeightPx).coerceAtLeast(0f)
    }

    val offsetY = remember { Animatable(0f, Float.VectorConverter) }
    val decay = rememberSplineBasedDecay<Float>()
    val scrollDecay = rememberSplineBasedDecay<Float>()

    var isDragging by remember { mutableStateOf(false) }
    var scrollAnimationJob by remember { mutableStateOf<Job?>(null) }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(maxOffset) {
        if (maxOffset > 0f) {
            if (!isInitialized) {
                offsetY.updateBounds(lowerBound = 0f, upperBound = maxOffset)
                offsetY.snapTo(maxOffset)
                isInitialized = true
            } else {
                offsetY.updateBounds(lowerBound = 0f, upperBound = maxOffset)
                val currentUpperBound = offsetY.upperBound ?: maxOffset
                val currentProgress = if (currentUpperBound > 0f) offsetY.value / currentUpperBound else 1f
                offsetY.snapTo(maxOffset * currentProgress)
            }
        }
    }

    val isFullyOpen = offsetY.value == 0f
    val canScrollContent = remember(contentHeightPx, panelHeightPx, headerHeightPx, isFullyOpen) {
        isFullyOpen && contentHeightPx > (panelHeightPx - headerHeightPx)
    }
    val scrollState = rememberScrollState()

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            isDragging = true
            if (!isFullyOpen) {
                offsetY.stop()
                offsetY.snapTo((offsetY.value + delta).coerceIn(0f, maxOffset))
            } else if (canScrollContent) {
                when {
                    delta < 0 -> {
                        val newValue = (scrollState.value - delta).coerceAtLeast(0f)
                        scrollState.scrollTo(newValue.toInt())
                    }
                    delta > 0 && scrollState.value == 0 -> {
                        offsetY.stop()
                        offsetY.snapTo((offsetY.value + delta).coerceIn(0f, maxOffset))
                    }
                    delta > 0 -> {
                        val newValue = (scrollState.value - delta).coerceAtMost(scrollState.maxValue.toFloat())
                        scrollState.scrollTo(newValue.toInt())
                    }
                }
            } else {
                offsetY.stop()
                offsetY.snapTo((offsetY.value + delta).coerceIn(0f, maxOffset))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStarted = {
                    isDragging = true
                    scrollAnimationJob?.cancel()
                },
                onDragStopped = { velocity ->
                    scope.launch {
                        isDragging = false
                        if (!isFullyOpen) {
                            offsetY.animateDecay(velocity, decay)
                        } else if (canScrollContent) {
                            if (scrollState.value == 0 && velocity > 0) {
                                offsetY.animateDecay(velocity, decay)
                            } else if (scrollState.value > 0) {
                                scrollAnimationJob = scope.launch {
                                    val scrollAnimatable = Animatable(scrollState.value.toFloat())
                                    scrollAnimatable.animateDecay(
                                        initialVelocity = -velocity,
                                        animationSpec = scrollDecay
                                    ) {
                                        scope.launch {
                                            scrollState.scrollTo(value.toInt())
                                        }
                                    }
                                }
                            }
                        } else {
                            offsetY.animateDecay(velocity, decay)
                        }
                    }
                }
            )
    ) {
        overlay?.invoke()
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .graphicsLayer {
                    alpha = if (isInitialized) 1f else 0f
                }
                .onGloballyPositioned { coords ->
                    panelHeightPx = coords.size.height.toFloat()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = backgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            headerHeightPx = coords.size.height.toFloat()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (isFullyOpen) {
                                    offsetY.animateTo(maxOffset)
                                } else {
                                    offsetY.animateTo(0f)
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        val icon = if (isFullyOpen) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowUp
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = if (isFullyOpen) "Свернуть панель" else "Развернуть панель",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    headerContent?.invoke(this)
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            enabled = false,
                            state = scrollState
                        )
                        .onGloballyPositioned { coords ->
                            contentHeightPx = coords.size.height.toFloat()
                        }
                ) {
                    panelContent()
                }
            }
        }
    }
}
