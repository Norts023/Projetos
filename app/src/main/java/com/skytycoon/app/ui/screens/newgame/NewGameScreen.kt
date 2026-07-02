package com.skytycoon.app.ui.screens.newgame

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
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
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@Composable
fun NewGameScreen(
    navController: NavHostController,
    viewModel: NewGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bgRotation")
    val bgRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Rotating gradient background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val sweepBrush = Brush.sweepGradient(
                colors = listOf(
                    SkyDarkBlue,
                    SkyAccentBlue.copy(alpha = 0.35f),
                    SkyDarkBlue,
                    SkyAccentPurple.copy(alpha = 0.25f),
                    SkyDarkBlue
                ),
                center = Offset(cx, cy)
            )
            withTransform({
                rotate(degrees = bgRotation, pivot = Offset(cx, cy))
                scale(scaleX = 2f, scaleY = 2f, pivot = Offset(cx, cy))
            }) {
                drawRect(brush = sweepBrush)
            }
        }

        // Dark overlay to keep content readable
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = SkyBlack.copy(alpha = 0.55f))
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Logo
            Text(
                text = "SkyTycoon",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    brush = Brush.linearGradient(
                        colors = listOf(SkyAccentBlue, SkyAccentPurple),
                        start = Offset(0f, 0f),
                        end = Offset(Float.MAX_VALUE, 0f)
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.new_game_title),
                style = MaterialTheme.typography.titleMedium,
                color = SkyTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Company name input
            OutlinedTextField(
                value = uiState.companyName,
                onValueChange = viewModel::onCompanyNameChange,
                label = {
                    Text(stringResource(R.string.new_game_company_name_label))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyAccentBlue,
                    unfocusedBorderColor = SkyDivider,
                    focusedLabelColor = SkyAccentBlue,
                    unfocusedLabelColor = SkyTextSecondary,
                    cursorColor = SkyAccentBlue,
                    focusedTextColor = SkyTextPrimary,
                    unfocusedTextColor = SkyTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.new_game_select_mode),
                style = MaterialTheme.typography.titleSmall,
                color = SkyTextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mode selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeSelectionCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.new_game_mode_realistic),
                    description = stringResource(R.string.new_game_mode_realistic_desc),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = SkyAccentBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    isSelected = uiState.selectedMode == GameMode.REALISTIC,
                    accentColor = SkyAccentBlue,
                    onClick = { viewModel.onModeChange(GameMode.REALISTIC) }
                )

                ModeSelectionCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.new_game_mode_fictional),
                    description = stringResource(R.string.new_game_mode_fictional_desc),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = SkyAccentPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    isSelected = uiState.selectedMode == GameMode.FICTIONAL,
                    accentColor = SkyAccentPurple,
                    onClick = { viewModel.onModeChange(GameMode.FICTIONAL) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Error message
            if (uiState.errorMsg != null) {
                Text(
                    text = uiState.errorMsg!!,
                    color = SkyAccentRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
            }

            // Start Game button
            Button(
                onClick = viewModel::onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.companyName.isNotBlank() && !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyAccentBlue,
                    contentColor = SkyTextPrimary,
                    disabledContainerColor = SkyAccentBlue.copy(alpha = 0.35f),
                    disabledContentColor = SkyTextSecondary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SkyTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.new_game_start_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSelectionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    SkyCard(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = accentColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) accentColor else SkyTextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SkyTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
