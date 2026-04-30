package com.joshfeldman.petrecords.feature.weights

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshfeldman.petrecords.core.model.WeightPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
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
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = points.indices.toList(),
                    y = points.map(WeightPoint::weightValue),
                )
            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        val lineColor = MaterialTheme.colorScheme.primary
        val markerLabel = rememberTextComponent(
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.labelMedium.fontSize,
            ),
            lineCount = 2,
            padding = Insets(start = 8.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
            background = rememberShapeComponent(fill = Fill(MaterialTheme.colorScheme.surfaceVariant)),
        )
        val marker = rememberDefaultCartesianMarker(
            label = markerLabel,
            valueFormatter = remember(points) {
                DefaultCartesianMarker.ValueFormatter { _, targets ->
                    val point = points.getOrNull(targets.first().x.toInt())
                    if (point == null) "" else "${formatPointDate(point.measuredAt)}\n${formatWeight(point)}"
                }
            },
        )
        val bottomAxisFormatter = remember(points) {
            CartesianValueFormatter { _, x, _ ->
                points.getOrNull(x.toInt())?.let { formatAxisDate(it.measuredAt) }.orEmpty()
            }
        }
        val lineLayer = rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                listOf(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(
                                component = rememberShapeComponent(
                                    fill = Fill(lineColor),
                                    strokeFill = Fill(MaterialTheme.colorScheme.surface),
                                    strokeThickness = 2.dp,
                                ),
                                size = 10.dp,
                            ),
                        ),
                    ),
                ),
            ),
        )
        val chart = rememberCartesianChart(
            lineLayer,
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomAxisFormatter),
            marker = marker,
        )
        CartesianChartHost(
            chart = chart,
            modelProducer = modelProducer,
            modifier = modifier,
        )
    }
}

private fun formatWeight(point: WeightPoint): String =
    "${formatAxisValue(point.weightValue)} ${point.weightUnit}"

private fun formatAxisValue(value: Double): String {
    val rounded = value.roundToInt()
    return if (abs(value - rounded) < 0.1) rounded.toString() else String.format(Locale.getDefault(), "%.1f", value)
}

private fun formatPointDate(value: String, locale: Locale = Locale.getDefault()): String =
    parseMeasuredAtDate(value)?.let { formatDate(it, locale, "yyyyMd") } ?: value.substringBefore('T')

private fun formatAxisDate(value: String, locale: Locale = Locale.getDefault()): String =
    parseMeasuredAtDate(value)?.let { formatDate(it, locale, "Md") } ?: value.substringBefore('T').take(5)

private fun formatDate(date: Date, locale: Locale, skeleton: String): String {
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    return SimpleDateFormat(pattern, locale).format(date)
}

private fun parseMeasuredAtDate(value: String): Date? {
    val datePart = value.substringBefore('T')
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return runCatching { parser.parse(datePart) }.getOrNull()
}
