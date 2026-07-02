package com.skytycoon.app.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.ui.components.RouteMapCanvas
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyCardBg
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = SkyBlack,
        bottomBar = { SkyBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "World Map",
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
            RouteMapCanvas(
                airports = uiState.airports,
                routes = buildRoutes(uiState.airports, uiState.activeFlights),
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                color = SkyDarkBlue.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = SkyAccentBlue, label = "Airline")
                    LegendItem(color = SkyAccentGreen, label = "Charter")
                    LegendItem(color = SkyAccentPurple, label = "Helicopter")
                }
            }
        }
    }

    if (uiState.selectedAirport != null) {
        val airport = uiState.selectedAirport!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.onAirportSelected(null) },
            sheetState = sheetState,
            containerColor = SkyCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    text = "Active flights: ${
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

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = SkyTextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

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
