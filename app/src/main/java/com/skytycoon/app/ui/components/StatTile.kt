package com.skytycoon.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytycoon.app.ui.theme.*

@Composable
fun StatTile(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = SkyAccentBlue,
    modifier: Modifier = Modifier
) {
    SkyCard(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = valueColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = SkyTextSecondary,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.animateContentSize(animationSpec = tween(300))
        )
    }
}
