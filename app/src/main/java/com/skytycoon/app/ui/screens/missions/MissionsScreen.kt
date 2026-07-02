package com.skytycoon.app.ui.screens.missions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionStatus
import com.skytycoon.app.ui.components.SkyCard
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentGreen
import com.skytycoon.app.ui.theme.SkyAccentOrange
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyAccentRed
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyDivider
import com.skytycoon.app.ui.theme.SkyGold
import com.skytycoon.app.ui.theme.SkyTextPrimary
import com.skytycoon.app.ui.theme.SkyTextSecondary

@Composable
fun MissionsScreen(
    navController: NavHostController,
    viewModel: MissionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Primary", "Secondary", "Daily")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlack)
    ) {
        // Screen header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SkyDarkBlue)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Missions",
                color = SkyTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tab row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SkyDarkBlue,
            contentColor = SkyAccentBlue,
            divider = { HorizontalDivider(color = SkyDivider) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) SkyAccentBlue else SkyTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab content with slide animation
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn(tween(200)) togetherWith
                                slideOutHorizontally { -it } + fadeOut(tween(200))
                    } else {
                        slideInHorizontally { -it } + fadeIn(tween(200)) togetherWith
                                slideOutHorizontally { it } + fadeOut(tween(200))
                    }
                },
                label = "tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { tabIndex ->
                val missions = when (tabIndex) {
                    0 -> uiState.primaryMissions
                    1 -> uiState.secondaryMissions
                    else -> uiState.dailyMissions
                }
                val isDaily = tabIndex == 2

                if (missions.isEmpty()) {
                    EmptyMissionsState(modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(missions, key = { it.id }) { mission ->
                            MissionCard(
                                mission = mission,
                                currentDay = uiState.dayNumber,
                                showExpiry = isDaily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: Mission,
    currentDay: Int,
    showExpiry: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = mission.progressPercent,
        animationSpec = tween(durationMillis = 600),
        label = "mission_progress"
    )
    val progressColor = if (mission.status == MissionStatus.COMPLETED) SkyAccentGreen else SkyAccentBlue

    SkyCard(modifier = modifier.fillMaxWidth()) {
        // Title row with status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = mission.title,
                color = SkyTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            StatusBadge(status = mission.status)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Description
        Text(
            text = mission.description,
            color = SkyTextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress fraction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                color = SkyTextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "${mission.currentValue} / ${mission.targetValue}",
                color = SkyTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = progressColor,
            trackColor = SkyDivider
        )

        // Rewards row
        val hasReward = mission.rewardMoneyCoins > 0 ||
                mission.rewardReputation > 0 ||
                mission.rewardResearchPoints > 0
        if (hasReward) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mission.rewardMoneyCoins > 0) {
                    RewardChip(
                        icon = Icons.Filled.MonetizationOn,
                        label = mission.rewardMoneyCoins.toString(),
                        accentColor = SkyGold
                    )
                }
                if (mission.rewardReputation > 0) {
                    RewardChip(
                        icon = Icons.Filled.Star,
                        label = "+${mission.rewardReputation}",
                        accentColor = SkyAccentPurple
                    )
                }
                if (mission.rewardResearchPoints > 0) {
                    RewardChip(
                        icon = Icons.Filled.Science,
                        label = "+${mission.rewardResearchPoints}",
                        accentColor = SkyAccentBlue
                    )
                }
            }
        }

        // Daily expiry countdown
        if (showExpiry && mission.expiresAtGameDay != null) {
            Spacer(modifier = Modifier.height(10.dp))
            val daysLeft = mission.expiresAtGameDay - currentDay
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = SkyAccentOrange,
                    modifier = Modifier.size(14.dp)
                )
                val countdownText = when {
                    daysLeft > 1 -> "Expires Day ${mission.expiresAtGameDay} ($daysLeft days left)"
                    daysLeft == 1 -> "Expires Day ${mission.expiresAtGameDay} (1 day left)"
                    daysLeft == 0 -> "Expires Day ${mission.expiresAtGameDay} (expires today)"
                    else -> "Expires Day ${mission.expiresAtGameDay} (expired)"
                }
                Text(
                    text = countdownText,
                    color = SkyAccentOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RewardChip(
    icon: ImageVector,
    label: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = label,
            color = SkyTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusBadge(status: MissionStatus) {
    val (color, label) = when (status) {
        MissionStatus.ACTIVE -> SkyAccentBlue to "ACTIVE"
        MissionStatus.COMPLETED -> SkyAccentGreen to "COMPLETED"
        MissionStatus.EXPIRED -> SkyAccentRed to "EXPIRED"
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun EmptyMissionsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = SkyTextSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No missions available",
            color = SkyTextSecondary,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Check back later for new objectives",
            color = SkyTextSecondary.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
    }
}
