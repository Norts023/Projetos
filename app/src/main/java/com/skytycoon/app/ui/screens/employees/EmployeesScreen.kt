package com.skytycoon.app.ui.screens.employees

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyGold
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary
import kotlin.math.roundToInt

private val PILOT_TYPES = setOf(
    EmployeeType.PILOT,
    EmployeeType.HELICOPTER_PILOT,
    EmployeeType.COPILOT
)
private val CREW_TYPES = setOf(EmployeeType.FLIGHT_ATTENDANT)
private val MECHANIC_TYPES = setOf(EmployeeType.MECHANIC, EmployeeType.ADMIN)

@Composable
fun EmployeesScreen(
    navController: NavHostController,
    viewModel: EmployeesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Pilots", "Crew", "Mechanics")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.hireError) {
        uiState.hireError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onDismissError()
        }
    }

    val filteredEmployees = remember(selectedTab, uiState.employees) {
        when (selectedTab) {
            1 -> uiState.employees.filter { it.type in PILOT_TYPES }
            2 -> uiState.employees.filter { it.type in CREW_TYPES }
            3 -> uiState.employees.filter { it.type in MECHANIC_TYPES }
            else -> uiState.employees
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onShowHireDialog(true) }
            ) {
                Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = "Hire Employee")
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
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                filteredEmployees.isEmpty() -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No employees in this category",
                        color = SkyTextSecondary
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEmployees, key = { it.id }) { employee ->
                        EmployeeCard(employee = employee)
                    }
                }
            }
        }
    }

    if (uiState.showHireDialog) {
        HireDialog(
            isLoading = uiState.isLoading,
            onDismiss = { viewModel.onShowHireDialog(false) },
            onHire = { name, type, level -> viewModel.onHire(name, type, level) }
        )
    }
}

// ── Employee card ─────────────────────────────────────────────────────────────

@Composable
private fun EmployeeCard(employee: Employee) {
    SkyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = employeeTypeIcon(employee.type),
                contentDescription = employee.type.name,
                tint = SkyAccentBlue,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = employee.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkyTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    EmployeeStatusChip(employee = employee)
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Level stars
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val filledStars = employee.level.coerceIn(0, 5)
                    repeat(filledStars) {
                        Text(text = "★", color = SkyGold, style = MaterialTheme.typography.bodyMedium)
                    }
                    repeat(5 - filledStars) {
                        Text(text = "★", color = SkyDivider, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "  Lv.${ employee.level }  •  ${employeeTypeLabel(employee.type)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Fatigue bar
                val fatigue = employee.fatigue.coerceIn(0, 100)
                val fatigueProgress = fatigue / 100f
                val fatigueColor = when {
                    fatigue < 30 -> SkyAccentGreen
                    fatigue < 60 -> SkyAccentOrange
                    else -> SkyAccentRed
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Fatigue",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyTextSecondary
                    )
                    Text(
                        text = employee.fatigueLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = fatigueColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { fatigueProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = fatigueColor,
                    trackColor = SkyDivider
                )
            }
        }
    }
}

@Composable
private fun EmployeeStatusChip(employee: Employee) {
    val (label, color) = when {
        employee.currentFlightId != null -> "Flying" to SkyAccentBlue
        !employee.isAvailable -> "Fatigued" to SkyAccentRed
        else -> "Available" to SkyAccentGreen
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

// ── Hire dialog ───────────────────────────────────────────────────────────────

private val HIREABLE_TYPES = listOf(
    EmployeeType.PILOT,
    EmployeeType.COPILOT,
    EmployeeType.FLIGHT_ATTENDANT,
    EmployeeType.MECHANIC
)

@Composable
private fun HireDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onHire: (name: String, type: EmployeeType, level: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EmployeeType.PILOT) }
    var levelSlider by remember { mutableFloatStateOf(1f) }
    val level = levelSlider.roundToInt()

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text("Hire Employee", fontWeight = FontWeight.Bold, color = SkyTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Employee type chips
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = SkyTextSecondary
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HIREABLE_TYPES.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(employeeTypeLabel(type)) }
                        )
                    }
                }

                // Level slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(level) {
                                Text("★", color = SkyGold, style = MaterialTheme.typography.bodySmall)
                            }
                            repeat(5 - level) {
                                Text("★", color = SkyDivider, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Slider(
                        value = levelSlider,
                        onValueChange = { levelSlider = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Salary preview
                Surface(
                    color = SkyAccentBlue.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Estimated salary: ${level * 100} coins / day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkyAccentBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onHire(name.trim(), selectedType, level) },
                enabled = name.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Hire")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun employeeTypeIcon(type: EmployeeType): ImageVector = when (type) {
    EmployeeType.PILOT, EmployeeType.HELICOPTER_PILOT -> Icons.Filled.FlightTakeoff
    EmployeeType.MECHANIC -> Icons.Filled.Build
    EmployeeType.ADMIN -> Icons.Filled.ManageAccounts
    EmployeeType.COPILOT, EmployeeType.FLIGHT_ATTENDANT -> Icons.Filled.Person
}

private fun employeeTypeLabel(type: EmployeeType): String = when (type) {
    EmployeeType.PILOT -> "Pilot"
    EmployeeType.HELICOPTER_PILOT -> "Heli Pilot"
    EmployeeType.COPILOT -> "Co-Pilot"
    EmployeeType.FLIGHT_ATTENDANT -> "Attendant"
    EmployeeType.MECHANIC -> "Mechanic"
    EmployeeType.ADMIN -> "Admin"
}
