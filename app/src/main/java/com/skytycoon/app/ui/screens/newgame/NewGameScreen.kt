package com.skytycoon.app.ui.screens.newgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.R
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.navigation.Screen
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@Composable
fun NewGameScreen(
    navController: NavHostController,
    viewModel: NewGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToDashboard) {
        if (uiState.navigateToDashboard) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.NewGame.route) { inclusive = true }
            }
            viewModel.onNavigated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP SECTION
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "✈ SkyTycoon",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    brush = Brush.linearGradient(listOf(SkyAccentBlue, SkyAccentGreen))
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                color = SkyTextSecondary,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
        }

        // MIDDLE SECTION
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Company Name",
                color = SkyTextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            OutlinedTextField(
                value = uiState.companyName,
                onValueChange = viewModel::onCompanyNameChange,
                label = { Text("Company Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyAccentBlue,
                    unfocusedBorderColor = SkyDivider,
                    focusedTextColor = SkyTextPrimary,
                    unfocusedTextColor = SkyTextPrimary,
                    cursorColor = SkyAccentBlue
                )
            )
            Text(
                text = "Game Mode",
                color = SkyTextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkyCard(
                    modifier = Modifier.weight(1f),
                    highlighted = uiState.selectedMode == GameMode.REALISTIC,
                    onClick = { viewModel.onModeChange(GameMode.REALISTIC) }
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = SkyAccentBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Realistic",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.new_game_mode_realistic_desc),
                        color = SkyTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                SkyCard(
                    modifier = Modifier.weight(1f),
                    highlighted = uiState.selectedMode == GameMode.FICTIONAL,
                    onClick = { viewModel.onModeChange(GameMode.FICTIONAL) }
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = SkyAccentPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Arcade",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.new_game_mode_fictional_desc),
                        color = SkyTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (uiState.errorMsg != null) {
                Text(
                    text = uiState.errorMsg!!,
                    color = SkyAccentRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // BOTTOM SECTION
        Column {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = SkyAccentBlue,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Button(
                onClick = viewModel::onStartGame,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.companyName.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyAccentBlue,
                    disabledContainerColor = SkyAccentBlue.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Start Engine ✈",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
