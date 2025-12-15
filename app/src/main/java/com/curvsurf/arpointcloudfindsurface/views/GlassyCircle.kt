package com.curvsurf.arpointcloudfindsurface.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassyCircle(modifier: Modifier = Modifier,
                 diameter: Dp = 56.dp,
                 alpha: Float = 0.6f,
                 content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .size(diameter)
            .clip(CircleShape)
            .background(Color.DarkGray.copy(alpha))
            .border(1.dp, Color.White.copy(0.08f), CircleShape),
        contentAlignment = Alignment.Center,
        content = content
    )
}
