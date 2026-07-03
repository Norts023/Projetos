package com.skytycoon.app.ui.screens.purchaseaircraft

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyCardBg
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseAircraftScreen(
    navController: NavHostController,
    viewModel: PurchaseAircraftViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val manufacturers by viewModel.manufacturers.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val modelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Show snackbar on purchase result message
    LaunchedEffect(uiState.message) {
        val msg = uiState.message
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = SkyBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Comprar Aeronave",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SkyTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyDarkBlue)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (manufacturers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SkyAccentBlue)
                }
            } else {
                val selectedIndex = manufacturers.indexOf(uiState.selectedManufacturer)
                    .coerceAtLeast(0)

                // Manufacturer tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = SkyDarkBlue,
                    contentColor = SkyAccentBlue,
                    edgePadding = 0.dp
                ) {
                    manufacturers.forEachIndexed { index, manufacturer ->
                        Tab(
                            selected = index == selectedIndex,
                            onClick = { viewModel.onSelectManufacturer(manufacturer) },
                            text = {
                                Text(
                                    text = manufacturer,
                                    color = if (index == selectedIndex) SkyAccentBlue else SkyTextSecondary,
                                    fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Models list for the selected manufacturer
                val modelsForManufacturer = uiState.models
                    .filter { it.manufacturer == uiState.selectedManufacturer }

                val byFamily = modelsForManufacturer
                    .groupBy { it.family.ifBlank { it.model } }
                    .entries
                    .sortedBy { it.key }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    byFamily.forEach { (family, models) ->
                        // Family section header
                        item(key = "header_$family") {
                            Text(
                                text = family,
                                color = SkyAccentBlue,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Model cards within the family
                        items(models, key = { it.id }) { model ->
                            AircraftModelCard(
                                model = model,
                                isSelected = uiState.selectedModel?.id == model.id,
                                canAfford = uiState.gameBalance >= model.purchasePriceCoins,
                                onClick = { viewModel.onSelectModel(model) }
                            )
                        }

                        item(key = "spacer_$family") {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Selected model detail bottom sheet
    if (uiState.selectedModel != null) {
        val model = uiState.selectedModel!!
        val canAfford = uiState.gameBalance >= model.purchasePriceCoins

        ModalBottomSheet(
            onDismissRequest = { viewModel.onDismissModelSheet() },
            sheetState = modelSheetState,
            containerColor = SkyCardBg
        ) {
            ModelDetailSheetContent(
                model = model,
                gameBalance = uiState.gameBalance,
                canAfford = canAfford,
                isLoading = uiState.isLoading,
                onPurchase = { viewModel.onPurchase() }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Aircraft model card
// ---------------------------------------------------------------------------

@Composable
private fun AircraftModelCard(
    model: AircraftModel,
    isSelected: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    SkyCard(
        modifier = Modifier.fillMaxWidth(),
        highlighted = isSelected,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.model,
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (model.variantCode.isNotBlank()) {
                    Text(
                        text = model.variantCode,
                        color = SkyTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(label = "${model.passengerCapacity} pax")
                    StatChip(label = "${model.rangeKm} km")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCoins(model.purchasePriceCoins),
                    color = if (canAfford) SkyAccentGreen else SkyAccentRed,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "coins",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = SkyAccentBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Selecionado — toque para detalhes",
                    color = SkyAccentBlue,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Model detail bottom-sheet content
// ---------------------------------------------------------------------------

@Composable
private fun ModelDetailSheetContent(
    model: AircraftModel,
    gameBalance: Long,
    canAfford: Boolean,
    isLoading: Boolean,
    onPurchase: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = model.displayName,
            color = SkyTextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (model.variantCode.isNotBlank()) {
            Text(
                text = "Variante: ${model.variantCode}",
                color = SkyTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(label = "Capacidade", value = "${model.passengerCapacity} pax", modifier = Modifier.weight(1f))
            StatTile(label = "Alcance", value = "${model.rangeKm} km", modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(label = "Velocidade", value = "${model.cruiseSpeedKmh} km/h", modifier = Modifier.weight(1f))
            StatTile(label = "Combustível", value = "${model.fuelBurnLph} L/h", modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = "Manutenção/h",
                value = "${model.maintenanceCostPerHourCoins} coins",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Categoria",
                value = model.category.name,
                modifier = Modifier.weight(1f)
            )
        }

        // Price vs balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Preço",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = formatCoins(model.purchasePriceCoins),
                    color = if (canAfford) SkyAccentGreen else SkyAccentRed,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Saldo",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = formatCoins(gameBalance),
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Purchase button
        Button(
            onClick = onPurchase,
            enabled = canAfford && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyAccentGreen,
                disabledContainerColor = SkyAccentGreen.copy(alpha = 0.35f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SkyBlack,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (canAfford) "Comprar" else "Saldo insuficiente",
                    color = SkyBlack,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable mini-components
// ---------------------------------------------------------------------------

@Composable
private fun StatChip(label: String) {
    Text(
        text = label,
        color = SkyTextSecondary,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    SkyCard(modifier = modifier) {
        Text(
            text = label,
            color = SkyTextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = SkyAccentOrange,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------------------------
// Formatting helpers
// ---------------------------------------------------------------------------

private fun formatCoins(amount: Long): String = when {
    amount >= 1_000_000 -> "${"%.1f".format(amount / 1_000_000.0)}M"
    amount >= 1_000 -> "${"%.0f".format(amount / 1_000.0)}K"
    else -> amount.toString()
}
