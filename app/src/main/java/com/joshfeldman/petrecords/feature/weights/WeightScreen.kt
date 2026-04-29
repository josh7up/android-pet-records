package com.joshfeldman.petrecords.feature.weights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshfeldman.petrecords.core.model.WeightPoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun WeightRoute(
    onBack: () -> Unit,
    viewModel: WeightViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text("Weight trend", style = MaterialTheme.typography.headlineMedium)
        Text("Pet: ${uiState.petName.ifBlank { uiState.petId }}")
        Card(modifier = Modifier.fillMaxWidth()) {
            if (uiState.points.isEmpty()) {
                Text("No weight measurements yet.", modifier = Modifier.padding(16.dp))
            } else {
                WeightTrendChart(
                    points = uiState.points,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun WeightTrendChart(
    points: List<WeightPoint>,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val axisColor = MaterialTheme.colorScheme.outline
    val lineColor = Color(0xFF006C4C)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    var selectedIndex by remember(points) { mutableStateOf(points.lastIndex) }
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val yAxisLabelWidthPx = with(density) { 44.dp.toPx() }
        val xAxisLabelHeightPx = with(density) { 24.dp.toPx() }
        val xAxisLabelWidthPx = with(density) { 56.dp.toPx() }
        val topPaddingPx = with(density) { 12.dp.toPx() }
        val rightPaddingPx = with(density) { 12.dp.toPx() }
        val bottomPaddingPx = with(density) { 8.dp.toPx() }
        val selectionThresholdPx = with(density) { 28.dp.toPx() }

        val chart = remember(points, widthPx, heightPx) {
            buildChartState(
                points = points,
                widthPx = widthPx,
                heightPx = heightPx,
                yAxisLabelWidthPx = yAxisLabelWidthPx,
                xAxisLabelHeightPx = xAxisLabelHeightPx,
                xAxisLabelWidthPx = xAxisLabelWidthPx,
                topPaddingPx = topPaddingPx,
                rightPaddingPx = rightPaddingPx,
                bottomPaddingPx = bottomPaddingPx,
            )
        }

        val safeSelectedIndex = selectedIndex.coerceIn(points.indices)
        val selectedPoint = chart.positions[safeSelectedIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(chart.positions) {
                    detectTapGestures { tapOffset ->
                        val nearest = chart.positions.minByOrNull { pointOffset ->
                            distanceSquared(tapOffset, pointOffset)
                        } ?: return@detectTapGestures
                        val nearestIndex = chart.positions.indexOf(nearest)
                        val isNearPoint = distanceSquared(tapOffset, nearest) <= selectionThresholdPx * selectionThresholdPx
                        if (isNearPoint) {
                            selectedIndex = nearestIndex
                        }
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = axisColor,
                    start = Offset(chart.plotLeft, chart.plotTop),
                    end = Offset(chart.plotLeft, chart.plotBottom),
                    strokeWidth = 2f,
                )
                drawLine(
                    color = axisColor,
                    start = Offset(chart.plotLeft, chart.plotBottom),
                    end = Offset(chart.plotRight, chart.plotBottom),
                    strokeWidth = 2f,
                )

                chart.yTickOffsets.forEach { y ->
                    drawLine(
                        color = axisColor.copy(alpha = 0.2f),
                        start = Offset(chart.plotLeft, y),
                        end = Offset(chart.plotRight, y),
                        strokeWidth = 1f,
                    )
                }

                val linePath = Path().apply {
                    chart.positions.forEachIndexed { index, offset ->
                        if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 5f, cap = StrokeCap.Round),
                )

                chart.positions.forEachIndexed { index, offset ->
                    val isSelected = index == safeSelectedIndex
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 10f else 7f,
                        center = offset,
                    )
                    drawCircle(
                        color = lineColor,
                        radius = if (isSelected) 6f else 4f,
                        center = offset,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        top = with(density) { chart.plotTop.toDp() - 8.dp },
                        bottom = with(density) { (heightPx - chart.plotBottom).toDp() - 8.dp },
                    )
                    .width(44.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                chart.yAxisLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            chart.xAxisLabels.forEach { label ->
                Text(
                    text = label.text,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    textAlign = when (label.alignment) {
                        LabelAlignment.Start -> TextAlign.Start
                        LabelAlignment.Center -> TextAlign.Center
                        LabelAlignment.End -> TextAlign.End
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .width(56.dp)
                        .offset(
                            x = with(density) { label.startX.toDp() },
                            y = with(density) { chart.plotBottom.toDp() + 4.dp },
                        ),
                )
            }

            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .onSizeChanged { tooltipSize = it }
                    .offset {
                        val margin = with(density) { 8.dp.roundToPx() }
                        val desiredX = selectedPoint.x.roundToInt() - (tooltipSize.width / 2)
                        val maxX = (widthPx.roundToInt() - tooltipSize.width - margin).coerceAtLeast(margin)
                        val clampedX = desiredX.coerceIn(margin, maxX)
                        val desiredY = selectedPoint.y.roundToInt() - tooltipSize.height - margin
                        val fallbackY = selectedPoint.y.roundToInt() + margin
                        val clampedY = if (desiredY >= margin) desiredY else fallbackY
                        IntOffset(clampedX, clampedY)
                    },
            ) {
                Text(
                    text = formatWeight(points[safeSelectedIndex]),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private data class ChartState(
    val plotLeft: Float,
    val plotTop: Float,
    val plotRight: Float,
    val plotBottom: Float,
    val positions: List<Offset>,
    val yAxisLabels: List<String>,
    val yTickOffsets: List<Float>,
    val xAxisLabels: List<XAxisLabel>,
)

private data class XAxisLabel(
    val text: String,
    val startX: Float,
    val alignment: LabelAlignment,
)

private enum class LabelAlignment {
    Start,
    Center,
    End,
}

private fun buildChartState(
    points: List<WeightPoint>,
    widthPx: Float,
    heightPx: Float,
    yAxisLabelWidthPx: Float,
    xAxisLabelHeightPx: Float,
    xAxisLabelWidthPx: Float,
    topPaddingPx: Float,
    rightPaddingPx: Float,
    bottomPaddingPx: Float,
): ChartState {
    val plotLeft = yAxisLabelWidthPx + 8f
    val plotTop = topPaddingPx
    val plotRight = widthPx - rightPaddingPx
    val plotBottom = heightPx - xAxisLabelHeightPx - bottomPaddingPx
    val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
    val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)

    val values = points.map { it.weightValue }
    val rawMin = values.minOrNull() ?: 0.0
    val rawMax = values.maxOrNull() ?: 1.0
    val rangePadding = max((rawMax - rawMin) * 0.1, 0.5)
    val minValue = rawMin - rangePadding
    val maxValue = rawMax + rangePadding
    val range = (maxValue - minValue).coerceAtLeast(1.0)
    val stepX = if (points.size == 1) 0f else plotWidth / (points.lastIndex)

    val positions = points.mapIndexed { index, point ->
        val normalized = ((point.weightValue - minValue) / range).toFloat()
        val x = plotLeft + (stepX * index)
        val y = plotBottom - (normalized * plotHeight)
        Offset(x, y)
    }

    val yAxisLabels = listOf(maxValue, (maxValue + minValue) / 2.0, minValue)
        .map(::formatAxisValue)
    val yTickOffsets = listOf(plotTop, plotTop + (plotHeight / 2f), plotBottom)

    val xAxisLabels = listOf(
        Triple(0, LabelAlignment.Start, plotLeft),
        Triple(points.lastIndex / 2, LabelAlignment.Center, (plotLeft + (plotWidth / 2f)) - (xAxisLabelWidthPx / 2f)),
        Triple(points.lastIndex, LabelAlignment.End, plotRight - xAxisLabelWidthPx),
    ).distinctBy { it.first }.map { (index, alignment, startX) ->
        XAxisLabel(
            text = abbreviateDate(points[index].measuredAt),
            startX = startX.coerceIn(plotLeft, plotRight - xAxisLabelWidthPx),
            alignment = alignment,
        )
    }

    return ChartState(
        plotLeft = plotLeft,
        plotTop = plotTop,
        plotRight = plotRight,
        plotBottom = plotBottom,
        positions = positions,
        yAxisLabels = yAxisLabels,
        yTickOffsets = yTickOffsets,
        xAxisLabels = xAxisLabels,
    )
}

private fun formatAxisValue(value: Double): String {
    val rounded = value.roundToInt()
    return if (abs(value - rounded) < 0.1) rounded.toString() else String.format("%.1f", value)
}

private fun formatWeight(point: WeightPoint): String =
    "${formatAxisValue(point.weightValue)} ${point.weightUnit}"

private fun abbreviateDate(value: String): String {
    val datePart = value.substringBefore('T')
    val parts = datePart.split("-")
    if (parts.size != 3) return value.take(6)
    val month = parts[1].toIntOrNull() ?: return value.take(6)
    val day = parts[2].toIntOrNull() ?: return value.take(6)
    val monthLabel = listOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
    ).getOrNull(month - 1) ?: return value.take(6)
    return "$monthLabel $day"
}

private fun distanceSquared(first: Offset, second: Offset): Float {
    val deltaX = first.x - second.x
    val deltaY = first.y - second.y
    return (deltaX * deltaX) + (deltaY * deltaY)
}
