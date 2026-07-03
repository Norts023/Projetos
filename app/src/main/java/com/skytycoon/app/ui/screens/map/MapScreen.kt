package com.skytycoon.app.ui.screens.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.MapSettings
import com.skytycoon.app.domain.model.MapType as DomainMapType
import com.skytycoon.app.domain.model.ShowAirports
import com.skytycoon.app.ui.components.RouteMapCanvas
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyCardBg
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val airportSheetState = rememberModalBottomSheetState()

    // Detect Google Maps availability at composition time — falls back to Canvas if absent
    val mapsAvailable = remember {
        try {
            Class.forName("com.google.maps.android.compose.GoogleMap")
            true
        } catch (e: Exception) {
            false
        }
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
            if (mapsAvailable) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
                }

                val googleMapType = if (uiState.mapSettings.mapType == DomainMapType.SATELLITE) {
                    MapType.SATELLITE
                } else {
                    MapType.NORMAL
                }

                // Pre-compute flight routes (avoid recomputing inside marker loops)
                val airportMap = uiState.airports.associateBy { it.iata }
                val activeIataCodes = uiState.activeFlights.flatMap {
                    listOf(it.originIata, it.destinationIata)
                }.toSet()

                val visibleAirports = when (uiState.mapSettings.showAirports) {
                    ShowAirports.HIDDEN -> emptyList()
                    ShowAirports.HUB_ONLY -> uiState.airports.filter { it.iata in activeIataCodes }
                    ShowAirports.ALL -> uiState.airports
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(mapType = googleMapType),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    // Airport markers
                    visibleAirports.forEach { airport ->
                        Marker(
                            state = MarkerState(position = LatLng(airport.lat, airport.lon)),
                            title = "${airport.iata} — ${airport.name}",
                            snippet = "${airport.city}, ${airport.country}",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_AZURE
                            ),
                            onClick = {
                                viewModel.onAirportSelected(airport)
                                false
                            }
                        )
                    }

                    // Flight polylines
                    uiState.activeFlights.forEach { flight ->
                        val origin = airportMap[flight.originIata]
                        val dest = airportMap[flight.destinationIata]
                        if (origin != null && dest != null) {
                            Polyline(
                                points = listOf(
                                    LatLng(origin.lat, origin.lon),
                                    LatLng(dest.lat, dest.lon)
                                ),
                                color = SkyAccentBlue,
                                width = 4f
                            )
                        }
                    }
                }
            } else {
                // Fallback: Canvas-based map when Google Maps is not available
                RouteMapCanvas(
                    airports = uiState.airports,
                    routes = buildRoutes(uiState.airports, uiState.activeFlights),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating layers button — top-end corner over the map
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
                    text = "ICAO: ${airport.icao}  |  Region: ${airport.region}",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                if (airport.hasHelipad) {
                    Surface(
                        color = SkyAccentPurple.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Helipad available",
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

        // Map type
        Text(
            text = "Tipo de Mapa",
            color = SkyTextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = settings.mapType == DomainMapType.STANDARD,
                onClick = { onSettingsChanged(settings.copy(mapType = DomainMapType.STANDARD)) },
                colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
            )
            Text(
                text = "Padrão",
                color = SkyTextPrimary,
                modifier = Modifier.padding(end = 16.dp)
            )
            RadioButton(
                selected = settings.mapType == DomainMapType.SATELLITE,
                onClick = { onSettingsChanged(settings.copy(mapType = DomainMapType.SATELLITE)) },
                colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
            )
            Text(text = "Satélite", color = SkyTextPrimary)
        }

        HorizontalDivider(color = SkyDivider)

        // Show airports
        Text(
            text = "Exibir Aeroportos",
            color = SkyTextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.showAirports == ShowAirports.ALL,
                    onClick = { onSettingsChanged(settings.copy(showAirports = ShowAirports.ALL)) },
                    colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
                )
                Text(text = "Todos", color = SkyTextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.showAirports == ShowAirports.HUB_ONLY,
                    onClick = { onSettingsChanged(settings.copy(showAirports = ShowAirports.HUB_ONLY)) },
                    colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
                )
                Text(text = "Somente Hub", color = SkyTextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.showAirports == ShowAirports.HIDDEN,
                    onClick = { onSettingsChanged(settings.copy(showAirports = ShowAirports.HIDDEN)) },
                    colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
                )
                Text(text = "Ocultos", color = SkyTextPrimary)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun buildRoutes(
    airports: List<Airport>,
    flights: List<Flight>
): List<Pair<Airport, Airport>> {
    val airportMap = airports.associateBy { it.iata }
    return flights.mapNotNull { flight ->
        val origin = airportMap[flight.originIata] ?: return@mapNotNull null
        val dest = airportMap[flight.destinationIata] ?: return@mapNotNull null
        origin to dest
    }
}
