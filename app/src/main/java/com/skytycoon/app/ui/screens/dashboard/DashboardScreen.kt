package com.skytycoon.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.ui.components.RouteMapCanvas
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.components.StatTile
import com.skytycoon.app.ui.components.TimeAdvanceBar
import com.skytycoon.app.ui.navigation.Screen
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

private val bottomNavItems = listOf(
    BottomNavItem("Fleet", Icons.Default.Flight, Screen.Fleet),
    BottomNavItem("Schedule", Icons.Default.Schedule, Screen.Schedule),
    BottomNavItem("Employees", Icons.Default.People, Screen.Employees),
    BottomNavItem("Missions", Icons.Default.EmojiEvents, Screen.Missions),
    BottomNavItem("Map", Icons.Default.Map, Screen.Map)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.gameState?.companyName ?: "SkyTycoon",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkyTextPrimary
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = uiState.gameState?.timeDisplay ?: "--:--",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SkyTextPrimary
                            )
                            Text(
                                text = "Day ${uiState.gameState?.dayNumber ?: 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkyTextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SkyDarkBlue
                )
            )
        },
        bottomBar = {
            Column {
                TimeAdvanceBar(
                    isAdvancing = uiState.isAdvancingTime,
                    onAdvanceNormal = { viewModel.onAdvanceTime(fastMode = false) },
                    onAdvanceFast = { viewModel.onAdvanceTime(fastMode = true) }
                )
                NavigationBar(
                    containerColor = SkyDarkBlue
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate(item.screen.route) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SkyAccentBlue,
                                unselectedIconColor = SkyTextSecondary,
                                selectedTextColor = SkyAccentBlue,
                                unselectedTextColor = SkyTextSecondary,
                                indicatorColor = SkyDarkBlue
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Balance",
                        value = uiState.gameState?.balanceDisplay ?: "0",
                        accentColor = SkyAccentGreen
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Rep.",
                        value = uiState.gameState?.reputationLabel ?: "-",
                        accentColor = SkyAccentPurple
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Fleet",
                        value = uiState.fleetSize.toString(),
                        accentColor = SkyAccentBlue
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "Flights",
                        value = uiState.activeFlightsCount.toString(),
                        accentColor = SkyAccentOrange
                    )
                }
            }

            item {
                SkyCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        RouteMapCanvas(
                            modifier = Modifier.fillMaxSize(),
                            airports = uiState.airports,
                            routes = emptyList()
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = uiState.alerts.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.alerts.forEach { alert ->
                            SkyCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = SkyAccentOrange
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚠",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SkyAccentOrange,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = alert,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SkyTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SkyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Contracts",
                                style = MaterialTheme.typography.titleSmall,
                                color = SkyTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.availableContractsCount} contracts waiting",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkyTextSecondary
                            )
                        }
                        Button(
                            onClick = { navController.navigate(Screen.Schedule.route) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkyAccentBlue
                            )
                        ) {
                            Text(
                                text = "View Contracts",
                                color = SkyTextPrimary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
