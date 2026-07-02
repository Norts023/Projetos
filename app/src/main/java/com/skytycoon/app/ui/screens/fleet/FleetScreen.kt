package com.skytycoon.app.ui.screens.fleet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.AircraftCategory
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyCardBg
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

private val categoryTabs = listOf(
    AircraftCategory.AIRLINER,
    AircraftCategory.CHARTER,
    AircraftCategory.HELICOPTER
)

private fun AircraftCategory.displayName(): String = when (this) {
    AircraftCategory.AIRLINER -> "Airliner"
    AircraftCategory.CHARTER -> "Charter"
    AircraftCategory.HELICOPTER -> "Helicopter"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen(
    navController: NavHostController,
    viewModel: FleetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val currentCategory = categoryTabs[selectedTabIndex]

    val displayedAircraft = uiState.ownedAircraft.filter {
        it.model.category == currentCategory
    }

    Scaffold(
        containerColor = SkyBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fleet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SkyTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SkyDarkBlue
                )
            )
        },
        bottomBar = { SkyBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onShowPurchaseDialog(true) },
                containerColor = SkyAccentBlue
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add aircraft",
                    tint = SkyTextPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SkyDarkBlue,
                contentColor = SkyAccentBlue,
                edgePadding = 0.dp
            ) {
                categoryTabs.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            viewModel.onFilterChange(category)
                        },
                        text = {
                            Text(
                                text = category.displayName(),
                                color = if (selectedTabIndex == index) SkyAccentBlue else SkyTextSecondary
                            )
                        }
                    )
                }
            }

            if (displayedAircraft.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${currentCategory.displayName()} aircraft in fleet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkyTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(displayedAircraft, key = { it.id }) { aircraft ->
                        AircraftCard(aircraft = aircraft)
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (uiState.showPurchaseDialog) {
        PurchaseDialog(
            aircraftModels = uiState.aircraftModels.filter { it.category == currentCategory },
            isLoading = uiState.isLoading,
            errorMessage = uiState.purchaseError,
            onDismiss = { viewModel.onShowPurchaseDialog(false) },
            onConfirm = { modelId, acquisitionType, regCode ->
                viewModel.onPurchase(modelId, acquisitionType, regCode)
            }
        )
    }
}

@Composable
private fun AircraftCard(aircraft: OwnedAircraft) {
    val conditionColor = when {
        aircraft.condition >= 80 -> SkyAccentGreen
        aircraft.condition >= 40 -> SkyAccentOrange
        else -> SkyAccentRed
    }

    SkyCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = aircraft.registrationCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkyAccentBlue
                )
                AcquisitionBadge(type = aircraft.acquisitionType)
            }

            Text(
                text = aircraft.model.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = SkyTextPrimary
            )
            Text(
                text = aircraft.model.manufacturer,
                style = MaterialTheme.typography.bodySmall,
                color = SkyTextSecondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Condition",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyTextSecondary
                    )
                    Text(
                        text = aircraft.conditionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = conditionColor
                    )
                }
                LinearProgressIndicator(
                    progress = { aircraft.condition / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = conditionColor,
                    trackColor = SkyDivider
                )
            }

            StatusChip(
                isOperational = aircraft.isOperational,
                needsMaintenance = aircraft.needsMaintenance
            )
        }
    }
}

@Composable
private fun AcquisitionBadge(type: AcquisitionType) {
    val (label, color) = when (type) {
        AcquisitionType.PURCHASED -> "PURCHASED" to SkyAccentGreen
        AcquisitionType.LEASED -> "LEASED" to SkyAccentOrange
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = color.copy(alpha = 0.5f),
            enabled = true
        )
    )
}

@Composable
private fun StatusChip(isOperational: Boolean, needsMaintenance: Boolean) {
    val (label, color) = when {
        needsMaintenance -> "Maintenance" to SkyAccentRed
        isOperational -> "Operational" to SkyAccentGreen
        else -> "Unavailable" to SkyTextSecondary
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = color.copy(alpha = 0.5f),
            enabled = true
        )
    )
}

@Composable
private fun PurchaseDialog(
    aircraftModels: List<AircraftModel>,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (modelId: Int, acquisitionType: AcquisitionType, regCode: String) -> Unit
) {
    var selectedModelId by remember { mutableStateOf(aircraftModels.firstOrNull()?.id) }
    var acquisitionType by remember { mutableStateOf(AcquisitionType.PURCHASED) }
    var registrationCode by remember { mutableStateOf("") }

    val selectedModel = aircraftModels.find { it.id == selectedModelId }
    val priceDisplay = when {
        selectedModel == null -> "-"
        acquisitionType == AcquisitionType.PURCHASED ->
            "${selectedModel.purchasePriceCoins} coins"
        else ->
            "${selectedModel.leasingCostPerHourCoins} coins/hr"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SkyCardBg,
        title = {
            Text(
                text = "Acquire Aircraft",
                style = MaterialTheme.typography.titleLarge,
                color = SkyTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (aircraftModels.isEmpty()) {
                    Text(
                        text = "No aircraft available in this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkyTextSecondary
                    )
                } else {
                    Text(
                        text = "Select Model",
                        style = MaterialTheme.typography.labelLarge,
                        color = SkyTextSecondary
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(aircraftModels, key = { it.id }) { model ->
                            ModelSelectionCard(
                                model = model,
                                isSelected = selectedModelId == model.id,
                                onSelect = { selectedModelId = model.id }
                            )
                        }
                    }

                    Text(
                        text = "Acquisition Type",
                        style = MaterialTheme.typography.labelLarge,
                        color = SkyTextSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AcquisitionType.entries.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = acquisitionType == type,
                                        onClick = { acquisitionType = type },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = acquisitionType == type,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = SkyAccentBlue,
                                        unselectedColor = SkyTextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = type.name.replaceFirstChar { it.titlecase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SkyTextPrimary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = registrationCode,
                        onValueChange = { registrationCode = it.uppercase() },
                        label = {
                            Text(
                                text = "Registration Code",
                                color = SkyTextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SkyTextPrimary,
                            unfocusedTextColor = SkyTextPrimary,
                            focusedBorderColor = SkyAccentBlue,
                            unfocusedBorderColor = SkyDivider,
                            cursorColor = SkyAccentBlue
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Price:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkyTextSecondary
                        )
                        Text(
                            text = priceDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyAccentGreen
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyAccentRed
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val modelId = selectedModelId ?: return@TextButton
                    if (registrationCode.isNotBlank()) {
                        onConfirm(modelId, acquisitionType, registrationCode)
                    }
                },
                enabled = !isLoading && selectedModelId != null && registrationCode.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(contentColor = SkyAccentBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp),
                        color = SkyAccentBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = SkyTextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ModelSelectionCard(
    model: AircraftModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    SkyCard(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            ),
        highlighted = isSelected
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = model.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) SkyAccentBlue else SkyTextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${model.passengerCapacity} pax",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkyTextSecondary
                )
                Text(
                    text = "${model.rangeKm} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkyTextSecondary
                )
            }
        }
    }
}
