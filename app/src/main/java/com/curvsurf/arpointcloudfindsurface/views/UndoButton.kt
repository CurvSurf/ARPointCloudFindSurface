package com.curvsurf.arpointcloudfindsurface.views

import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.curvsurf.arpointcloudfindsurface.R

@Composable
fun UndoButton(
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    GlassyCircle(
        modifier = Modifier.combinedClickable(
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.undo_24px),
            contentDescription = "Undo Button",
            tint = Color.White.copy(alpha = if (enabled) 1f else 0.4f)
        )
    }
}