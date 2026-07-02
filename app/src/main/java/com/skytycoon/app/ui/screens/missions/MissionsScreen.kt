package com.skytycoon.app.ui.screens.missions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionStatus
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.navigation.SkyBottomNavBar
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    navController: NavHostController,
    viewModel: MissionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = SkyBlack,
        bottomBar = { SkyBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Missions",
                        color = SkyTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyDarkBlue)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SkyDarkBlue,
                contentColor = SkyAccentBlue
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                ) {
                    Text("Primary", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                ) {
                    Text("Secondary", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 }
                ) {
                    Text("Daily", modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            val currentMissions = when (selectedTabIndex) {
                0 -> uiState.primaryMissions
                1 -> uiState.secondaryMissions
                else -> uiState.dailyMissions
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTabIndex == 2) {
                    item {
                        Text(
                            text = stringResource(R.string.missions_daily_reset_notice),
                            color = SkyTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                items(currentMissions, key = { it.id }) { mission ->
                    MissionCard(mission = mission)
                }
                if (currentMissions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.missions_empty),
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
}

@Composable
private fun MissionCard(mission: Mission) {
    val animatedProgress by animateFloatAsState(
        targetValue = mission.progressPercent,
        animationSpec = tween(800),
        label = "mission_progress"
    )
    val statusColor: Color = when (mission.status) {
        MissionStatus.ACTIVE -> SkyAccentBlue
        MissionStatus.COMPLETED -> SkyAccentGreen
        MissionStatus.EXPIRED -> SkyTextSecondary
    }

    SkyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mission.title,
                    color = SkyTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = mission.description,
                    color = SkyTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (mission.status == MissionStatus.COMPLETED) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SkyAccentGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = mission.status.name,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            color = statusColor,
            trackColor = SkyDivider,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Text(
            text = "${(mission.progressPercent * 100).toInt()}% (${mission.currentValue}/${mission.targetValue})",
            color = SkyTextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mission.rewardMoneyCoins > 0) {
                RewardChip(label = "💰 ${mission.rewardMoneyCoins}¢", color = SkyAccentGreen)
            }
            if (mission.rewardReputation > 0) {
                RewardChip(label = "⭐ +${mission.rewardReputation} rep", color = SkyAccentBlue)
            }
            if (mission.rewardResearchPoints > 0) {
                RewardChip(label = "🔬 +${mission.rewardResearchPoints} R&D", color = SkyAccentPurple)
            }
        }
    }
}

@Composable
private fun RewardChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
