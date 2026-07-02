package com.skytycoon.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.ui.theme.*

private data class ContinentRegion(
    val name: String,
    val latMin: Float, val latMax: Float,
    val lonMin: Float, val lonMax: Float,
    val color: Color
)

private val continents = listOf(
    ContinentRegion("North America", 15f, 72f, -168f, -52f, Color(0xFF0D2E2E)),
    ContinentRegion("South America", -56f, 13f, -82f, -34f, Color(0xFF0D2A1A)),
    ContinentRegion("Europe", 36f, 71f, -10f, 40f, Color(0xFF111840)),
    ContinentRegion("Africa", -35f, 37f, -18f, 52f, Color(0xFF1A1A0A)),
    ContinentRegion("Asia", 0f, 77f, 40f, 150f, Color(0xFF180D2E)),
    ContinentRegion("Oceania", -47f, 0f, 110f, 180f, Color(0xFF0D1A1A)),
)

private fun DrawScope.mercatorX(lon: Float): Float = (lon + 180f) / 360f * size.width
private fun DrawScope.mercatorY(lat: Float): Float = (1f - (lat + 90f) / 180f) * size.height

@Composable
fun RouteMapCanvas(
    airports: List<Airport>,
    routes: List<Pair<Airport, Airport>>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "route_anim")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )

    Canvas(modifier = modifier.background(SkyBlack)) {
        // Draw continent regions
        for (continent in continents) {
            val x1 = mercatorX(continent.lonMin)
            val y1 = mercatorY(continent.latMax)
            val x2 = mercatorX(continent.lonMax)
            val y2 = mercatorY(continent.latMin)
            drawRect(
                color = continent.color,
                topLeft = Offset(x1, y1),
                size = androidx.compose.ui.geometry.Size(x2 - x1, y2 - y1)
            )
        }

        // Draw routes
        for ((origin, dest) in routes) {
            val x1 = mercatorX(origin.lon.toFloat())
            val y1 = mercatorY(origin.lat.toFloat())
            val x2 = mercatorX(dest.lon.toFloat())
            val y2 = mercatorY(dest.lat.toFloat())
            // Glow effect
            drawLine(
                color = SkyAccentBlue.copy(alpha = 0.3f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 6.dp.toPx()
            )
            // Animated dashed line
            drawLine(
                color = SkyAccentBlue,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), dashPhase)
            )
        }

        // Draw airport dots
        for (airport in airports) {
            val x = mercatorX(airport.lon.toFloat())
            val y = mercatorY(airport.lat.toFloat())
            drawCircle(
                color = SkyTextPrimary.copy(alpha = 0.6f),
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = SkyTextPrimary,
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
