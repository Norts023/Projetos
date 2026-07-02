package com.skytycoon.app.ui.screens.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.domain.model.Contract
import com.skytycoon.app.domain.model.EmployeeType
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.domain.model.OperationType
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.domain.usecase.ScheduleFlightRequest
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary
import com.skytycoon.app.ui.utils.toGameTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showScheduleSheet by remember { mutableStateOf(false) }
    var prefilledContractId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(uiState.successMsg) {
        uiState.successMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMsg) {
        uiState.errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = SkyBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Schedule",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyDarkBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { SkyBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    prefilledContractId = null
                    showScheduleSheet = true
                },
                containerColor = SkyAccentBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule flight", tint = SkyTextPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SkyDarkBlue,
                contentColor = SkyAccentBlue
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Flights (${uiState.flights.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Contracts (${uiState.contracts.size})") }
                )
            }

            when (selectedTabIndex) {
                0 -> FlightsTab(
                    flights = uiState.flights,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                1 -> ContractsTab(
                    contracts = uiState.contracts,
                    onAccept = { contractId ->
                        prefilledContractId = contractId
                        showScheduleSheet = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }

    if (showScheduleSheet) {
        ScheduleFlightSheet(
            uiState = uiState,
            prefilledContractId = prefilledContractId,
            onDismiss = { showScheduleSheet = false },
            onSchedule = { request ->
                viewModel.onScheduleFlight(request)
                showScheduleSheet = false
            }
        )
    }
}

// ── Flights tab ────────────────────────────────────────────────────────────────

@Composable
private fun FlightsTab(
    flights: List<Flight>,
    modifier: Modifier = Modifier
) {
    if (flights.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "No flights scheduled yet", color = SkyTextSecondary)
        }
    } else {
        LazyColumn(
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
    val statusColor = when (flight.status) {
        FlightStatus.SCHEDULED -> SkyAccentBlue
        FlightStatus.IN_FLIGHT, FlightStatus.BOARDING -> SkyAccentOrange
        FlightStatus.COMPLETED -> SkyAccentGreen
        else -> SkyAccentRed
    }
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
                    text = "${flight.originIata} → ${flight.destinationIata}",
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dep: ${flight.departureGameMinutes.toGameTimeString()}  Arr: ${flight.arrivalGameMinutes.toGameTimeString()}",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Pax: ${flight.passengerCount}  Rev: ${flight.revenueCoins}¢",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = flight.status.name,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Contracts tab ──────────────────────────────────────────────────────────────

@Composable
private fun ContractsTab(
    contracts: List<Contract>,
    onAccept: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (contracts.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "No contracts available", color = SkyTextSecondary)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(contracts, key = { it.id }) { contract ->
                ContractCard(contract = contract, onAccept = { onAccept(contract.id) })
            }
        }
    }
}

@Composable
private fun ContractCard(
    contract: Contract,
    onAccept: () -> Unit
) {
    SkyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${contract.originIata} → ${contract.destinationIata}",
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${contract.passengerCount} pax | ${contract.totalValueCoins}¢ + ${contract.bonusOnTimeCoins}¢ bonus",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Deadline: ${contract.deadlineGameMinutes.toGameTimeString()}",
                    color = SkyAccentOrange,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = SkyAccentGreen)
            ) {
                Text("Accept", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ── Schedule bottom sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleFlightSheet(
    uiState: ScheduleUiState,
    prefilledContractId: Long?,
    onDismiss: () -> Unit,
    onSchedule: (ScheduleFlightRequest) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedAircraftId by remember { mutableStateOf<Long?>(null) }
    var originQuery by remember { mutableStateOf("") }
    var destQuery by remember { mutableStateOf("") }
    var selectedOrigin by remember { mutableStateOf<Airport?>(null) }
    var selectedDest by remember { mutableStateOf<Airport?>(null) }
    var departureHour by remember {
        mutableIntStateOf(
            uiState.gameState?.currentGameMinutes?.let { ((it % 1440L) / 60L).toInt() } ?: 8
        )
    }
    var departureMinute by remember { mutableIntStateOf(0) }
    var ticketPrice by remember { mutableStateOf("100") }
    var selectedPilotId by remember { mutableStateOf<Long?>(null) }
    var expandedAircraft by remember { mutableStateOf(false) }
    var expandedPilot by remember { mutableStateOf(false) }
    var showOriginList by remember { mutableStateOf(false) }
    var showDestList by remember { mutableStateOf(false) }

    val departureGameMinutes = (uiState.gameState?.currentGameMinutes ?: 0L) +
            (departureHour * 60L) + departureMinute.toLong()

    val pilots = uiState.availableEmployees.filter { it.type == EmployeeType.PILOT }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Schedule Flight",
                color = SkyTextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Aircraft picker
            ExposedDropdownMenuBox(
                expanded = expandedAircraft,
                onExpandedChange = { expandedAircraft = it }
            ) {
                OutlinedTextField(
                    value = uiState.ownedAircraft.find { it.id == selectedAircraftId }
                        ?.registrationCode ?: "Select aircraft",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aircraft") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAircraft)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAircraft,
                    onDismissRequest = { expandedAircraft = false }
                ) {
                    if (uiState.ownedAircraft.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No aircraft available", color = SkyTextSecondary) },
                            onClick = { expandedAircraft = false }
                        )
                    } else {
                        uiState.ownedAircraft.forEach { aircraft ->
                            DropdownMenuItem(
                                text = { Text(aircraft.registrationCode, color = SkyTextPrimary) },
                                onClick = {
                                    selectedAircraftId = aircraft.id
                                    expandedAircraft = false
                                }
                            )
                        }
                    }
                }
            }

            // Origin and destination row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = originQuery,
                        onValueChange = {
                            originQuery = it
                            showOriginList = true
                            selectedOrigin = null
                        },
                        label = { Text("Origin IATA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showOriginList && originQuery.length >= 2) {
                        val filtered = uiState.airports.filter {
                            it.iata.contains(originQuery, ignoreCase = true) ||
                                    it.city.contains(originQuery, ignoreCase = true)
                        }
                        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                            items(filtered, key = { it.id }) { airport ->
                                AirportSuggestionItem(airport = airport) {
                                    selectedOrigin = airport
                                    originQuery = airport.iata
                                    showOriginList = false
                                }
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = destQuery,
                        onValueChange = {
                            destQuery = it
                            showDestList = true
                            selectedDest = null
                        },
                        label = { Text("Destination IATA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showDestList && destQuery.length >= 2) {
                        val filtered = uiState.airports.filter {
                            it.iata.contains(destQuery, ignoreCase = true) ||
                                    it.city.contains(destQuery, ignoreCase = true)
                        }
                        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                            items(filtered, key = { it.id }) { airport ->
                                AirportSuggestionItem(airport = airport) {
                                    selectedDest = airport
                                    destQuery = airport.iata
                                    showDestList = false
                                }
                            }
                        }
                    }
                }
            }

            // Departure hour slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Dep. Hour", color = SkyTextSecondary, style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = departureHour.toFloat(),
                    onValueChange = { departureHour = it.toInt() },
                    valueRange = 0f..23f,
                    steps = 22,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%02d:00".format(departureHour),
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Departure minute slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Dep. Minute", color = SkyTextSecondary, style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = departureMinute.toFloat(),
                    onValueChange = { departureMinute = ((it.toInt() / 5) * 5).coerceIn(0, 55) },
                    valueRange = 0f..55f,
                    steps = 10,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%02d".format(departureMinute),
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Ticket price (only for non-contract flights)
            if (prefilledContractId == null) {
                OutlinedTextField(
                    value = ticketPrice,
                    onValueChange = { ticketPrice = it },
                    label = { Text("Ticket Price ¢/pax") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Pilot picker
            ExposedDropdownMenuBox(
                expanded = expandedPilot,
                onExpandedChange = { expandedPilot = it }
            ) {
                OutlinedTextField(
                    value = pilots.find { it.id == selectedPilotId }?.name ?: "Select Pilot (Optional)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pilot") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPilot)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedPilot,
                    onDismissRequest = { expandedPilot = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None", color = SkyTextSecondary) },
                        onClick = {
                            selectedPilotId = null
                            expandedPilot = false
                        }
                    )
                    pilots.forEach { pilot ->
                        DropdownMenuItem(
                            text = { Text(pilot.name, color = SkyTextPrimary) },
                            onClick = {
                                selectedPilotId = pilot.id
                                expandedPilot = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val req = ScheduleFlightRequest(
                        aircraftId = selectedAircraftId!!,
                        operationType = if (prefilledContractId != null) OperationType.CHARTER else OperationType.AIRLINE,
                        originIata = selectedOrigin!!.iata,
                        destinationIata = selectedDest!!.iata,
                        departureGameMinutes = departureGameMinutes,
                        ticketPricePerPax = ticketPrice.toLongOrNull(),
                        contractId = prefilledContractId,
                        assignedPilotId = selectedPilotId
                    )
                    onSchedule(req)
                },
                enabled = selectedAircraftId != null && selectedOrigin != null && selectedDest != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SkyAccentBlue)
            ) {
                Text("Schedule")
            }

            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun AirportSuggestionItem(
    airport: Airport,
    onClick: () -> Unit
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${airport.iata} - ${airport.city}",
            color = SkyTextPrimary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
