package com.curvsurf.arpointcloudfindsurface.views

import androidx.annotation.DrawableRes
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.curvsurf.arpointcloudfindsurface.R

@Composable
fun RecordingToggleButton(
    recording: Boolean,
    onClick: (Boolean) -> Unit,
    onLongClick: () -> Unit
) {
    GlassyCircle(
        modifier = Modifier.combinedClickable(
            onClick = { onClick(!recording) },
            onLongClick = onLongClick
        )
    ) {
        @DrawableRes val id = if (recording) R.drawable.pause_circle_24px
        else R.drawable.radio_button_checked_24px
        Icon(
            painter = painterResource(id = id),
            contentDescription = if (recording) "Stop Recording Button" else "Start Recording Button",
            tint = if (recording) Color.White else Color(0xFFFF3B30)
        )
    }
}