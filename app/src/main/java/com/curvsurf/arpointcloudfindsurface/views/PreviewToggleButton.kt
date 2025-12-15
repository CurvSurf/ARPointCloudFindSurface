package com.curvsurf.arpointcloudfindsurface.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.curvsurf.arpointcloudfindsurface.R

@Composable
fun PreviewToggleButton(
    previewEnabled: Boolean,
    onClick: (Boolean) -> Unit
) {
    GlassyCircle(
        modifier = Modifier.clickable(
            onClick = { onClick(!previewEnabled) }
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.curvsurf_logo),
            contentDescription = if (previewEnabled) "Preview On Button" else "Preview Off Button",
            modifier = Modifier.size(24.dp),
            tint = if (previewEnabled) Color.Unspecified else Color.Unspecified.copy(alpha = 0.4f)
        )
    }
}
