package com.skytycoon.app.ui.screens.employees

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.R
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyCardBg
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyGold
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    navController: NavHostController,
    viewModel: EmployeesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredEmployees by viewModel.filteredEmployees.collectAsStateWithLifecycle()
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

    Scaffold(
        containerColor = SkyBlack,
        bottomBar = { SkyBottomNavBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Staff",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyDarkBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onShowHireDialog() },
                containerColor = SkyAccentBlue
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Hire employee")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filterType == null,
                        onClick = { viewModel.onFilterChange(null) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterType == EmployeeType.PILOT ||
                                uiState.filterType == EmployeeType.HELICOPTER_PILOT,
                        onClick = { viewModel.onFilterChange(EmployeeType.PILOT) },
                        label = { Text("Pilots") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterType == EmployeeType.FLIGHT_ATTENDANT,
                        onClick = { viewModel.onFilterChange(EmployeeType.FLIGHT_ATTENDANT) },
                        label = { Text("Crew") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterType == EmployeeType.MECHANIC,
                        onClick = { viewModel.onFilterChange(EmployeeType.MECHANIC) },
                        label = { Text("Mechanics") }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEmployees, key = { it.id }) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onFire = { viewModel.onFire(employee.id) }
                    )
                }
                if (filteredEmployees.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.employees_empty),
                                color = SkyTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showHireDialog) {
        HireDialog(
            isLoading = uiState.isLoading,
            onHire = { name, type, level -> viewModel.onHire(name, type, level) },
            onDismiss = { viewModel.onDismissDialog() }
        )
    }
}

@Composable
private fun EmployeeCard(
    employee: Employee,
    onFire: () -> Unit
) {
    val typeColor: Color = when (employee.type) {
        EmployeeType.PILOT, EmployeeType.HELICOPTER_PILOT, EmployeeType.COPILOT -> SkyAccentBlue
        EmployeeType.FLIGHT_ATTENDANT -> SkyAccentPurple
        EmployeeType.MECHANIC -> SkyAccentOrange
        EmployeeType.ADMIN -> SkyAccentGreen
    }
    val statusColor: Color = when {
        employee.isAvailable -> SkyAccentGreen
        employee.fatigue < 80 -> SkyAccentOrange
        else -> SkyAccentRed
    }

    SkyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.first().uppercaseChar().toString(),
                    color = typeColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = employee.name,
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "★".repeat(employee.level),
                        color = SkyGold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = typeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = employee.type.name.replace("_", " "),
                            color = typeColor,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = employee.fatigueLabel,
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { employee.fatigue / 100f },
                    color = if (employee.fatigue < 60) SkyAccentGreen
                            else if (employee.fatigue < 80) SkyAccentOrange
                            else SkyAccentRed,
                    trackColor = SkyDivider,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Text(
                    text = "Fatigue: ${employee.fatigue}%  |  Salary: ${employee.dailySalaryCoins}¢/day",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = onFire) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fire employee",
                    tint = SkyAccentRed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HireDialog(
    isLoading: Boolean,
    onHire: (name: String, type: EmployeeType, level: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hireName by remember { mutableStateOf("") }
    var hireType by remember { mutableStateOf(EmployeeType.PILOT) }
    var hireLevel by remember { mutableIntStateOf(1) }
    var expandedTypeMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SkyCardBg,
        title = {
            Text(
                text = "Hire Staff",
                color = SkyTextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hireName,
                    onValueChange = { hireName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = expandedTypeMenu,
                    onExpandedChange = { expandedTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = hireType.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeMenu)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeMenu,
                        onDismissRequest = { expandedTypeMenu = false }
                    ) {
                        EmployeeType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace("_", " ")) },
                                onClick = {
                                    hireType = type
                                    expandedTypeMenu = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                Text(
                    text = "Level",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = hireLevel.toFloat(),
                    onValueChange = { hireLevel = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Level $hireLevel",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "★".repeat(hireLevel),
                        color = SkyGold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                val dailySalary = hireLevel * 50L
                val hiringFee = hireLevel * 200L
                Text(
                    text = "Daily salary: ${dailySalary}¢  |  Hiring fee: ${hiringFee}¢",
                    color = SkyAccentGreen,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onHire(hireName, hireType, hireLevel) },
                enabled = hireName.isNotBlank() && !isLoading
            ) {
                Text("Hire")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
