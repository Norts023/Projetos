package com.skytycoon.app.ui.screens.fleet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.R
import com.skytycoon.app.ui.navigation.Screen
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.AircraftCategory
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen(
    navController: NavHostController,
    viewModel: FleetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredAircraft by viewModel.filteredAircraft.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMsg) {
        uiState.successMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }
    LaunchedEffect(uiState.errorMsg) {
        uiState.errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    val selectedTabIndex = when (uiState.filterCategory) {
        AircraftCategory.CHARTER -> 1
        AircraftCategory.HELICOPTER -> 2
        else -> 0
    }
    val currentCategory = when (selectedTabIndex) {
        1 -> AircraftCategory.CHARTER
        2 -> AircraftCategory.HELICOPTER
        else -> AircraftCategory.AIRLINER
    }

    Scaffold(
        containerColor = SkyBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fleet",
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
                onClick = { navController.navigate(Screen.PurchaseAircraft.route) },
                containerColor = SkyAccentBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add aircraft", tint = SkyTextPrimary)
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
                contentColor = SkyAccentBlue,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = SkyAccentBlue
                        )
                    }
                }
            ) {
                listOf(
                    AircraftCategory.AIRLINER,
                    AircraftCategory.CHARTER,
                    AircraftCategory.HELICOPTER
                ).forEachIndexed { index, category ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            viewModel.onFilterChange(
                                when (index) {
                                    1 -> AircraftCategory.CHARTER
                                    2 -> AircraftCategory.HELICOPTER
                                    else -> AircraftCategory.AIRLINER
                                }
                            )
                        },
                        text = { Text(category.name) }
                    )
                }
            }

            if (filteredAircraft.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.fleet_empty),
                        color = SkyTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAircraft, key = { it.id }) { aircraft ->
                        AircraftCard(aircraft = aircraft)
                    }
                }
            }
        }
    }

    if (uiState.showPurchaseDialog) {
        PurchaseDialog(
            uiState = uiState,
            currentCategory = currentCategory,
            onDismiss = { viewModel.onDismissDialog() },
            onPurchase = { acquisitionType, regCode ->
                viewModel.onPurchase(acquisitionType, regCode)
            }
        )
    }
}

@Composable
private fun AircraftCard(aircraft: OwnedAircraft) {
    SkyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = aircraft.registrationCode,
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = aircraft.model.displayName,
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = aircraft.conditionLabel,
                        color = if (aircraft.condition > 60) SkyAccentGreen
                                else if (aircraft.condition > 30) SkyAccentOrange
                                else SkyAccentRed,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Surface(
                        color = SkyDivider,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (aircraft.acquisitionType == AcquisitionType.PURCHASED) "Owned" else "Leased",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { aircraft.condition / 100f },
                    color = if (aircraft.condition > 60) SkyAccentGreen
                            else if (aircraft.condition > 30) SkyAccentOrange
                            else SkyAccentRed,
                    trackColor = SkyDivider,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = if (aircraft.isOperational) SkyAccentGreen.copy(alpha = 0.15f)
                        else SkyAccentRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (aircraft.isOperational) "Operational" else "Maintenance",
                    color = if (aircraft.isOperational) SkyAccentGreen else SkyAccentRed,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseDialog(
    uiState: FleetUiState,
    currentCategory: AircraftCategory,
    onDismiss: () -> Unit,
    onPurchase: (AcquisitionType, String) -> Unit
) {
    val categoryModels = uiState.availableModels.filter { it.category == currentCategory }

    var selectedModel by remember(uiState.showPurchaseDialog) {
        mutableStateOf(uiState.selectedModelForPurchase)
    }
    var acquisitionType by remember { mutableStateOf(AcquisitionType.PURCHASED) }
    var registrationCode by remember { mutableStateOf("") }
    var expandedModelMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Acquire Aircraft",
                color = SkyTextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedModelMenu,
                    onExpandedChange = { expandedModelMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedModel?.displayName ?: "Select model",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Model") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelMenu)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedModelMenu,
                        onDismissRequest = { expandedModelMenu = false }
                    ) {
                        if (categoryModels.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No models available", color = SkyTextSecondary) },
                                onClick = { expandedModelMenu = false }
                            )
                        } else {
                            categoryModels.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.displayName, color = SkyTextPrimary) },
                                    onClick = {
                                        selectedModel = model
                                        expandedModelMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                selectedModel?.let { model ->
                    Text(
                        text = "Range: ${model.rangeKm}km | Speed: ${model.cruiseSpeedKmh}km/h | Pax: ${model.passengerCapacity}",
                        color = SkyTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (acquisitionType == AcquisitionType.PURCHASED) {
                        Button(
                            onClick = { acquisitionType = AcquisitionType.PURCHASED },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyAccentBlue)
                        ) { Text("Purchase") }
                        OutlinedButton(
                            onClick = { acquisitionType = AcquisitionType.LEASED },
                            modifier = Modifier.weight(1f)
                        ) { Text("Lease", color = SkyTextSecondary) }
                    } else {
                        OutlinedButton(
                            onClick = { acquisitionType = AcquisitionType.PURCHASED },
                            modifier = Modifier.weight(1f)
                        ) { Text("Purchase", color = SkyTextSecondary) }
                        Button(
                            onClick = { acquisitionType = AcquisitionType.LEASED },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyAccentBlue)
                        ) { Text("Lease") }
                    }
                }

                OutlinedTextField(
                    value = registrationCode,
                    onValueChange = { registrationCode = it.uppercase() },
                    label = { Text("Registration (e.g. PS-ABC)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                selectedModel?.let { model ->
                    Text(
                        text = if (acquisitionType == AcquisitionType.PURCHASED)
                            "Price: ${model.purchasePriceCoins}¢"
                        else
                            "Lease: ${model.leasingCostPerHourCoins}¢/h",
                        color = SkyAccentGreen,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPurchase(acquisitionType, registrationCode) },
                enabled = selectedModel != null && registrationCode.isNotBlank() && !uiState.isLoading
            ) {
                Text("Confirm", color = SkyAccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SkyTextSecondary)
            }
        }
    )
}
