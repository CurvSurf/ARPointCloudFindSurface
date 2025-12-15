package com.curvsurf.arpointcloudfindsurface.views

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.curvsurf.arpointcloudfindsurface.R

@Composable
fun CaptureButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    GlassyCircle {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White.copy(alpha = if (enabled) 1f else 0.4f)
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.camera_24px),
                contentDescription = "Capture Button"
            )
        }
    }
}