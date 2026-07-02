package com.skytycoon.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.skytycoon.app.R
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyAccentPurple
import com.skytycoon.app.ui.theme.SkyBlack
import com.skytycoon.app.ui.theme.SkyTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val navDestination by viewModel.navDestination.collectAsStateWithLifecycle()

    LaunchedEffect(navDestination) {
        navDestination?.let { route ->
            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        delay(200L)
        launch {
            iconAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        delay(400L)
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flight,
                contentDescription = null,
                tint = SkyAccentBlue,
                modifier = Modifier
                    .size(64.dp)
                    .alpha(iconAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SkyTycoon",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    brush = Brush.linearGradient(
                        colors = listOf(SkyAccentBlue, SkyAccentPurple),
                        start = Offset(0f, 0f),
                        end = Offset(Float.MAX_VALUE, 0f)
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = SkyTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
