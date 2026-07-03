package com.skytycoon.app.ui.screens.map

import android.graphics.Color
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.MapSettings
import com.skytycoon.app.domain.model.OperationType
import com.skytycoon.app.domain.model.ShowAirports
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyCardBg
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val airportSheetState = rememberModalBottomSheetState()

    // MapView created once for the lifetime of this composable
    val mapView = remember { MapView(context) }

    // Zoom level tracked via OSMDroid MapListener → only recompose at bucket boundary
    val mapZoom = remember { mutableStateOf(3.0) }

    // Mutable overlay lists — cleared and rebuilt when data changes
    val addedMarkers = remember { mutableListOf<Marker>() }
    val addedPolylines = remember { mutableListOf<Polyline>() }

    // Initialise MapView once and handle Android lifecycle
    DisposableEffect(mapView) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.isTilesScaledToDpi = true
        mapView.controller.setZoom(3.0)
        mapView.controller.setCenter(GeoPoint(20.0, 0.0))

        val zoomListener = object : MapListener {
            override fun onScroll(event: ScrollEvent?) = true
            override fun onZoom(event: ZoomEvent?): Boolean {
                mapZoom.value = mapView.zoomLevelDouble
                return true
            }
        }
        mapView.addMapListener(zoomListener)
        mapView.onResume()
        onDispose {
            mapView.removeMapListener(zoomListener)
            mapView.onPause()
        }
    }

    // Derived state — only recompute active IATA set when flights change
    val activeIataCodes = remember(uiState.activeFlights) {
        uiState.activeFlights.flatMap { listOf(it.originIata, it.destinationIata) }.toSet()
    }

    // Zoom bucket prevents continuous overlay rebuilds while the user is zooming
    val zoomBucket = if (mapZoom.value < 5.0) "low" else "high"

    // Rebuild airport markers when airports, filter setting, or zoom bucket changes
    LaunchedEffect(uiState.airports, uiState.mapSettings.showAirports, zoomBucket) {
        mapView.overlays.removeAll(addedMarkers.toSet())
        addedMarkers.clear()

        val visibleAirports: List<Airport> = when {
            uiState.mapSettings.showAirports == ShowAirports.HIDDEN -> emptyList()
            mapZoom.value < 5.0 ->
                // Below zoom 5: show only airports with active flights, capped at 200
                uiState.airports.filter { it.iata in activeIataCodes }.take(200)
            uiState.mapSettings.showAirports == ShowAirports.HUB_ONLY ->
                uiState.airports.filter { it.iata in activeIataCodes }
            else -> uiState.airports
        }

        visibleAirports.forEach { airport ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(airport.lat, airport.lon)
                title = airport.iata
                snippet = "${airport.name} · ${airport.city}"
                infoWindow = null // suppress default tooltip; Compose BottomSheet used instead
                setOnMarkerClickListener { _, _ ->
                    viewModel.onAirportSelected(airport)
                    true
                }
            }
            mapView.overlays.add(marker)
            addedMarkers.add(marker)
        }
        mapView.invalidate()
    }

    // Rebuild flight polylines when active flights or airport list changes
    LaunchedEffect(uiState.activeFlights, uiState.airports) {
        mapView.overlays.removeAll(addedPolylines.toSet())
        addedPolylines.clear()

        val airportMap = uiState.airports.associateBy { it.iata }
        uiState.activeFlights.forEach { flight ->
            val origin = airportMap[flight.originIata] ?: return@forEach
            val dest   = airportMap[flight.destinationIata] ?: return@forEach
            val lineColor = when (flight.operationType) {
                OperationType.AIRLINE    -> Color.parseColor("#2196F3") // blue
                OperationType.CHARTER    -> Color.parseColor("#4CAF50") // green
                OperationType.HELICOPTER -> Color.parseColor("#FF5722") // deep orange
            }
            val poly = Polyline(mapView).apply {
                setPoints(listOf(
                    GeoPoint(origin.lat, origin.lon),
                    GeoPoint(dest.lat, dest.lon)
                ))
                outlinePaint.color = lineColor
                outlinePaint.strokeWidth = 4f
            }
            // Insert polylines below markers so they don't occlude taps
            mapView.overlays.add(0, poly)
            addedPolylines.add(poly)
        }
        mapView.invalidate()
    }

    Scaffold(
        containerColor = SkyBlack,
        bottomBar = { SkyBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mapa",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyDarkBlue)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            // Floating layers button — top-end corner
            IconButton(
                onClick = { viewModel.onToggleSettingsSheet() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SkyDarkBlue.copy(alpha = 0.90f),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.Layers,
                        contentDescription = "Configurações do mapa",
                        tint = SkyTextPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    // Map settings bottom sheet
    if (uiState.showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onToggleSettingsSheet() },
            sheetState = settingsSheetState,
            containerColor = SkyCardBg
        ) {
            MapSettingsSheetContent(
                settings = uiState.mapSettings,
                onSettingsChanged = { viewModel.onUpdateSettings(it) }
            )
        }
    }

    // Airport detail bottom sheet
    if (uiState.selectedAirport != null) {
        val airport = uiState.selectedAirport!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.onAirportSelected(null) },
            sheetState = airportSheetState,
            containerColor = SkyCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${airport.iata} — ${airport.name}",
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${airport.city}, ${airport.country}",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "ICAO: ${airport.icao}  |  Região: ${airport.region}",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                if (airport.hasHelipad) {
                    Surface(
                        color = SkyAccentPurple.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Helipad disponível",
                            color = SkyAccentPurple,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = "Voos ativos: ${
                        uiState.activeFlights.count {
                            it.originIata == airport.iata || it.destinationIata == airport.iata
                        }
                    }",
                    color = SkyAccentBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Settings sheet content
// ---------------------------------------------------------------------------

@Composable
private fun MapSettingsSheetContent(
    settings: MapSettings,
    onSettingsChanged: (MapSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Configurações do Mapa",
            color = SkyTextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Exibir Aeroportos",
            color = SkyTextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
        Column {
            listOf(
                ShowAirports.ALL to "Todos",
                ShowAirports.HUB_ONLY to "Somente Hub",
                ShowAirports.HIDDEN to "Ocultos"
            ).forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.showAirports == value,
                        onClick = { onSettingsChanged(settings.copy(showAirports = value)) },
                        colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
                    )
                    Text(text = label, color = SkyTextPrimary)
                }
            }
        }

        HorizontalDivider(color = SkyDivider)

        Text(
            text = "Legenda de Rotas",
            color = SkyTextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RouteColorLegendRow(color = ComposeColor(0xFF2196F3), label = "Airline")
            RouteColorLegendRow(color = ComposeColor(0xFF4CAF50), label = "Charter")
            RouteColorLegendRow(color = ComposeColor(0xFFFF5722), label = "Helicopter")
        }
    }
}

@Composable
private fun RouteColorLegendRow(
    color: ComposeColor,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 12.dp)
    ) {
        Surface(
            modifier = Modifier.width(32.dp).height(4.dp),
            color = color,
            shape = RoundedCornerShape(2.dp)
        ) {}
        Text(text = label, color = SkyTextPrimary, style = MaterialTheme.typography.bodySmall)
    }
}
