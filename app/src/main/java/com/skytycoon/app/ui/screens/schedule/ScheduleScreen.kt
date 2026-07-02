package com.skytycoon.app.ui.screens.schedule

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.Contract
import com.skytycoon.app.domain.model.ContractStatus
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.domain.model.OperationType
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.domain.usecase.ScheduleFlightRequest
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Flights", "Contracts")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onDismissError()
        }
    }

    Scaffold(
        containerColor = SkyBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SkyTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyDarkBlue)
            )
        },
        bottomBar = { SkyBottomNavBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onShowScheduleSheet(true) },
                containerColor = SkyAccentBlue
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Schedule Flight")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> FlightsTab(
                    flights = uiState.flights,
                    isLoading = uiState.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                1 -> ContractsTab(
                    contracts = uiState.contracts,
                    isLoading = uiState.isLoading,
                    onViewContract = { _ ->
                        viewModel.onShowScheduleSheet(true)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }

    if (uiState.showScheduleSheet) {
        ScheduleBottomSheet(
            ownedAircraft = uiState.ownedAircraft,
            onDismiss = { viewModel.onShowScheduleSheet(false) },
            onSchedule = { request -> viewModel.onScheduleFlight(request) }
        )
    }
}

// ── Flights tab ──────────────────────────────────────────────────────────────

@Composable
private fun FlightsTab(
    flights: List<Flight>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        flights.isEmpty() -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "No flights scheduled yet", color = SkyTextSecondary)
        }
        else -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(flights, key = { it.id }) { flight ->
                FlightCard(flight = flight)
            }
        }
    }
}

@Composable
private fun FlightCard(flight: Flight) {
    SkyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${flight.originIata} → ${flight.destinationIata}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SkyTextPrimary,
                modifier = Modifier.weight(1f)
            )
            FlightStatusChip(status = flight.status)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${formatGameMinutes(flight.departureGameMinutes)} → ${formatGameMinutes(flight.arrivalGameMinutes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = SkyTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "AC: ${flight.aircraftId}",
                style = MaterialTheme.typography.bodySmall,
                color = SkyTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${flight.passengerCount} pax  •  ${flight.revenueCoins} coins",
            style = MaterialTheme.typography.bodySmall,
            color = SkyTextSecondary
        )
    }
}

@Composable
private fun FlightStatusChip(status: FlightStatus) {
    val color = when (status) {
        FlightStatus.SCHEDULED -> SkyAccentBlue
        FlightStatus.IN_FLIGHT -> SkyAccentGreen
        FlightStatus.BOARDING -> SkyAccentOrange
        FlightStatus.COMPLETED -> SkyTextSecondary
        FlightStatus.CANCELLED -> SkyAccentRed
        FlightStatus.DELAYED -> SkyAccentOrange
    }
    val label = when (status) {
        FlightStatus.SCHEDULED -> "Scheduled"
        FlightStatus.IN_FLIGHT -> "In Flight"
        FlightStatus.BOARDING -> "Boarding"
        FlightStatus.COMPLETED -> "Completed"
        FlightStatus.CANCELLED -> "Cancelled"
        FlightStatus.DELAYED -> "Delayed"
    }
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ── Contracts tab ─────────────────────────────────────────────────────────────

@Composable
private fun ContractsTab(
    contracts: List<Contract>,
    isLoading: Boolean,
    onViewContract: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        contracts.isEmpty() -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "No contracts available", color = SkyTextSecondary)
        }
        else -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(contracts, key = { it.id }) { contract ->
                ContractCard(contract = contract, onView = { onViewContract(contract.id) })
            }
        }
    }
}

@Composable
private fun ContractCard(
    contract: Contract,
    onView: () -> Unit
) {
    SkyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${contract.originIata} → ${contract.destinationIata}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SkyTextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (contract.status == ContractStatus.AVAILABLE) {
                TextButton(onClick = onView) {
                    Text("View", color = SkyAccentBlue)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${contract.passengerCount} passengers  •  ${contract.operationType.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = SkyTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${contract.totalValueCoins} coins + ${contract.bonusOnTimeCoins} bonus",
                style = MaterialTheme.typography.bodyMedium,
                color = SkyTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Day ${contract.deadlineGameMinutes / 1440 + 1}",
                style = MaterialTheme.typography.bodySmall,
                color = SkyTextSecondary
            )
        }
        if (contract.maxEarnings > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Max: ${contract.maxEarnings} coins",
                style = MaterialTheme.typography.labelSmall,
                color = SkyAccentGreen
            )
        }
    }
}

// ── Schedule bottom sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleBottomSheet(
    ownedAircraft: List<OwnedAircraft>,
    onDismiss: () -> Unit,
    onSchedule: (ScheduleFlightRequest) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedAircraft by remember { mutableStateOf(ownedAircraft.firstOrNull()) }
    var selectedOperationType by remember { mutableStateOf(OperationType.AIRLINE) }
    var originIata by remember { mutableStateOf("") }
    var destinationIata by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var passengerCount by remember { mutableStateOf("") }
    var revenueCoins by remember { mutableStateOf("") }

    var aircraftExpanded by remember { mutableStateOf(false) }
    var operationTypeExpanded by remember { mutableStateOf(false) }

    val isFormValid = selectedAircraft != null &&
            originIata.length >= 3 &&
            destinationIata.length >= 3 &&
            departureTime.matches(Regex("\\d{1,2}:\\d{2}")) &&
            passengerCount.toIntOrNull() != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Schedule Flight",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SkyTextPrimary
            )

            // Aircraft picker
            ExposedDropdownMenuBox(
                expanded = aircraftExpanded,
                onExpandedChange = { aircraftExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedAircraft?.registrationCode ?: "Select aircraft",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aircraft") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = aircraftExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = aircraftExpanded,
                    onDismissRequest = { aircraftExpanded = false }
                ) {
                    if (ownedAircraft.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No aircraft available", color = SkyTextSecondary) },
                            onClick = { aircraftExpanded = false }
                        )
                    } else {
                        ownedAircraft.forEach { aircraft ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(aircraft.registrationCode)
                                        Text(
                                            text = aircraft.model.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SkyTextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedAircraft = aircraft
                                    aircraftExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Operation type picker
            ExposedDropdownMenuBox(
                expanded = operationTypeExpanded,
                onExpandedChange = { operationTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedOperationType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Operation Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = operationTypeExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = operationTypeExpanded,
                    onDismissRequest = { operationTypeExpanded = false }
                ) {
                    OperationType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                selectedOperationType = type
                                operationTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Route row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = originIata,
                    onValueChange = { originIata = it.uppercase().take(4) },
                    label = { Text("Origin IATA") },
                    placeholder = { Text("GRU") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = destinationIata,
                    onValueChange = { destinationIata = it.uppercase().take(4) },
                    label = { Text("Destination IATA") },
                    placeholder = { Text("CGH") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Departure time
            OutlinedTextField(
                value = departureTime,
                onValueChange = { departureTime = it },
                label = { Text("Departure Time") },
                placeholder = { Text("HH:MM") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Passengers + revenue row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = passengerCount,
                    onValueChange = { passengerCount = it },
                    label = { Text("Passengers") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = revenueCoins,
                    onValueChange = { revenueCoins = it },
                    label = { Text("Revenue (coins)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    val aircraft = selectedAircraft ?: return@Button
                    val timeParts = departureTime.split(":")
                    val hours = timeParts.getOrNull(0)?.toLongOrNull() ?: return@Button
                    val minutes = timeParts.getOrNull(1)?.toLongOrNull() ?: return@Button
                    val depMinutes = hours * 60 + minutes
                    val passengers = passengerCount.toIntOrNull() ?: return@Button
                    val ticketPrice = revenueCoins.toLongOrNull()

                    onSchedule(
                        ScheduleFlightRequest(
                            aircraftId = aircraft.id,
                            operationType = selectedOperationType,
                            originIata = originIata,
                            destinationIata = destinationIata,
                            departureGameMinutes = depMinutes,
                            passengerCount = passengers,
                            ticketPricePerPax = ticketPrice
                        )
                    )
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Schedule Flight")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatGameMinutes(gameMinutes: Long): String {
    val totalInDay = gameMinutes % 1440
    val hours = totalInDay / 60
    val minutes = totalInDay % 60
    return "%02d:%02d".format(hours, minutes)
}
