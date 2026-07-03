package com.skytycoon.app.ui.screens.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings

    Scaffold(
        containerColor = SkyBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurações",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SkyTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyDarkBlue)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                SectionHeader("Idioma")
                LanguageOption(
                    label = "Português",
                    code = "pt",
                    selectedCode = settings.languageCode,
                    onClick = { viewModel.onLanguageChange("pt") }
                )
                LanguageOption(
                    label = "English",
                    code = "en",
                    selectedCode = settings.languageCode,
                    onClick = { viewModel.onLanguageChange("en") }
                )
                LanguageOption(
                    label = "Auto",
                    code = "auto",
                    selectedCode = settings.languageCode,
                    onClick = { viewModel.onLanguageChange("auto") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SkyDivider)
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Tempo & Velocidade")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto-avanço de tempo ao iniciar",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.defaultAutoTime,
                        onCheckedChange = { viewModel.onAutoTimeChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SkyAccentBlue,
                            checkedTrackColor = SkyAccentBlue.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Velocidade padrão",
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                listOf(1 to "1×", 2 to "2×", 5 to "5×", 10 to "10×").forEach { (value, label) ->
                    SpeedOption(
                        label = label,
                        value = value,
                        selectedValue = settings.defaultSpeedMultiplier,
                        onClick = { viewModel.onSpeedMultiplierChange(value) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = SkyTextSecondary,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun LanguageOption(
    label: String,
    code: String,
    selectedCode: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedCode == code,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = SkyTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SpeedOption(
    label: String,
    value: Int,
    selectedValue: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedValue == value,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = SkyAccentBlue)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = SkyTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
