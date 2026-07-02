package com.skytycoon.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.ui.components.RouteMapCanvas
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.components.StatTile
import com.skytycoon.app.ui.components.TimeAdvanceBar
import com.skytycoon.app.ui.navigation.Screen
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = SkyBlack,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.gameState?.companyName ?: "SkyTycoon",
                                color = SkyTextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Day ${uiState.gameState?.dayNumber ?: 1}",
                                color = SkyTextSecondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SkyDarkBlue
                    )
                )
            },
            bottomBar = { SkyBottomNavBar(navController) },
            snackbarHost = {
                TimeAdvanceBar(
                    timeDisplay = uiState.gameState?.timeDisplay ?: "00:00",
                    dayNumber = uiState.gameState?.dayNumber ?: 1,
                    onAdvance = { fast -> viewModel.onAdvanceTime(fast) }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Time row
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = uiState.gameState?.timeDisplay ?: "00:00",
                            color = SkyAccentBlue,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        SkyCard(modifier = Modifier.align(Alignment.CenterEnd)) {
                            Text(
                                text = when (uiState.gameState?.gameMode) {
                                    GameMode.REALISTIC -> "Realistic"
                                    GameMode.FICTIONAL -> "Arcade"
                                    null -> "—"
                                },
                                color = SkyAccentPurple,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Stat tiles
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            StatTile(
                                icon = Icons.Default.AccountBalanceWallet,
                                label = "Balance",
                                value = uiState.gameState?.balanceDisplay ?: "0",
                                valueColor = SkyAccentGreen,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                        item {
                            StatTile(
                                icon = Icons.Default.Star,
                                label = "Reputation",
                                value = "${uiState.gameState?.reputation ?: 0} - ${uiState.gameState?.reputationLabel ?: ""}",
                                valueColor = SkyAccentBlue,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                        item {
                            StatTile(
                                icon = Icons.Default.Flight,
                                label = "Fleet",
                                value = "${uiState.fleetSize} aircraft",
                                valueColor = SkyAccentPurple,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                        item {
                            StatTile(
                                icon = Icons.Default.FlightTakeoff,
                                label = "Active",
                                value = "${uiState.activeFlightsCount} flights",
                                valueColor = SkyAccentOrange,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }

                // Route map
                item {
                    RouteMapCanvas(
                        airports = emptyList(),
                        routes = emptyList(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                // Alerts
                item {
                    AnimatedVisibility(visible = uiState.alerts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Alerts",
                                color = SkyAccentOrange,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            uiState.alerts.forEach { alert ->
                                SkyCard {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = SkyAccentOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = alert,
                                            color = SkyTextPrimary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Available contracts
                if (uiState.availableContractsCount > 0) {
                    item {
                        SkyCard(
                            onClick = { navController.navigate(Screen.Schedule.route) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.availableContractsCount} contracts available",
                                    color = SkyAccentGreen,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = SkyAccentGreen
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        if (uiState.isAdvancing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SkyBlack.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SkyAccentBlue)
            }
        }
    }
}
