package com.skytycoon.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytycoon.app.ui.theme.*

@Composable
fun TimeAdvanceBar(
    timeDisplay: String,
    dayNumber: Int,
    onAdvance: (fast: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(SkyDarkBlue, SkyCardBg)),
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Day $dayNumber",
                color = SkyTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = timeDisplay,
                color = SkyAccentBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onAdvance(false) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyAccentBlue)
            ) {
                Text("▶ +1h", fontSize = 13.sp)
            }
            Button(
                onClick = { onAdvance(true) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkyAccentBlue)
            ) {
                Text("⏩ +8h", fontSize = 13.sp)
            }
        }
    }
}
