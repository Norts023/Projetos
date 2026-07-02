package com.skytycoon.app.ui.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.ui.components.RouteMapCanvas
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyCardBg
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

@HiltViewModel
class MapViewModel @Inject constructor(
    private val airportRepository: AirportRepository,
    private val flightRepository: FlightRepository,
    @Suppress("UnusedPrivateMember")
    private val gameStateRepository: GameStateRepository
) : ViewModel() {

    data class MapUiState(
        val airports: List<Airport> = emptyList(),
        val activeRoutes: List<Pair<Airport, Airport>> = emptyList(),
        val selectedAirport: Airport? = null
    )

    private val _selectedAirport = MutableStateFlow<Airport?>(null)

    val uiState: StateFlow<MapUiState> = combine(
        airportRepository.getAll(),
        flightRepository.getAll(),
        _selectedAirport
    ) { airports, flights, selectedAirport ->
        val activeFlights = flights.filter {
            it.status == FlightStatus.IN_FLIGHT || it.status == FlightStatus.SCHEDULED
        }
        val airportMap = airports.associateBy { it.iata }
        val routes = activeFlights.mapNotNull { flight ->
            val origin = airportMap[flight.originIata]
            val dest = airportMap[flight.destinationIata]
            if (origin != null && dest != null) Pair(origin, dest) else null
        }
        MapUiState(
            airports = airports,
            activeRoutes = routes,
            selectedAirport = selectedAirport
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState()
    )

    fun onSelectAirport(airport: Airport?) {
        _selectedAirport.value = airport
    }
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "World Map",
                        color = SkyTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SkyTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SkyDarkBlue,
                    titleContentColor = SkyTextPrimary
                )
            )
        },
        containerColor = SkyBlack
    ) { paddingValues ->
        MapContent(
            uiState = uiState,
            onSelectAirport = viewModel::onSelectAirport,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

// ---------------------------------------------------------------------------
// Map content with tap detection and overlays
// ---------------------------------------------------------------------------

@Composable
private fun MapContent(
    uiState: MapViewModel.MapUiState,
    onSelectAirport: (Airport?) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val tolerancePx = remember(density) { with(density) { 20.dp.toPx() } }

    // rememberUpdatedState keeps current values accessible inside the
    // long-lived pointerInput coroutine without restarting it.
    val latestCanvasSize = androidx.compose.runtime.rememberUpdatedState(canvasSize)
    val latestAirports = androidx.compose.runtime.rememberUpdatedState(uiState.airports)
    val latestTolerance = androidx.compose.runtime.rememberUpdatedState(tolerancePx)

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                canvasSize = coordinates.size
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val size = latestCanvasSize.value
                    if (size.width > 0 && size.height > 0) {
                        val tapped = findNearestAirport(
                            offset = offset,
                            airports = latestAirports.value,
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat(),
                            tolerancePx = latestTolerance.value
                        )
                        onSelectAirport(tapped)
                    }
                }
            }
    ) {
        // Base map canvas
        RouteMapCanvas(
            airports = uiState.airports,
            routes = uiState.activeRoutes,
            modifier = Modifier.fillMaxSize()
        )

        // Legend — bottom-start corner
        MapLegend(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        )

        // Airport info card — slides in above the legend
        if (uiState.selectedAirport != null) {
            AirportInfoCard(
                airport = uiState.selectedAirport,
                onDismiss = { onSelectAirport(null) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 76.dp)
            )
        }
    }
}

@Composable
private fun MapLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(SkyAccentBlue, CircleShape)
        )
        Text(
            text = "Active Route",
            color = SkyTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AirportInfoCard(
    airport: Airport,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SkyCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SkyDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = airport.iata,
                    color = SkyAccentBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = SkyTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = airport.name,
                color = SkyTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${airport.city}, ${airport.country}",
                color = SkyTextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tap-to-airport helper
// ---------------------------------------------------------------------------

/**
 * Returns the airport nearest to [offset] within [tolerancePx] pixels,
 * using the same Mercator projection as RouteMapCanvas.
 * Returns null if no airport is within tolerance or if the list is empty.
 */
private fun findNearestAirport(
    offset: Offset,
    airports: List<Airport>,
    canvasWidth: Float,
    canvasHeight: Float,
    tolerancePx: Float
): Airport? {
    if (airports.isEmpty()) return null

    val nearest = airports.minByOrNull { airport ->
        val x = (airport.lon.toFloat() + 180f) / 360f * canvasWidth
        val y = (1f - (airport.lat.toFloat() + 90f) / 180f) * canvasHeight
        val dx = x - offset.x
        val dy = y - offset.y
        dx * dx + dy * dy          // compare squared distances — no sqrt needed
    } ?: return null

    val nx = (nearest.lon.toFloat() + 180f) / 360f * canvasWidth
    val ny = (1f - (nearest.lat.toFloat() + 90f) / 180f) * canvasHeight
    val dist = sqrt((nx - offset.x) * (nx - offset.x) + (ny - offset.y) * (ny - offset.y))
    return if (dist <= tolerancePx) nearest else null
}
