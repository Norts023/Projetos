package com.skytycoon.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skytycoon.app.ui.theme.*

@Composable
fun SkyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (highlighted) SkyAccentBlue else SkyDivider
    val borderWidth = if (highlighted) 2.dp else 1.dp
    val gradient = Brush.linearGradient(listOf(SkyCardBg, Color(0xFF1E2640)))

    Column(
        modifier = modifier
            .clip(shape)
            .background(gradient)
            .border(borderWidth, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        content = content
    )
}
