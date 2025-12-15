package com.curvsurf.arpointcloudfindsurface.views


import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.curvsurf.arpointcloudfindsurface.R

@Composable
fun SeedRadiusTutorialView(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    val neverShowAgainPref = prefs.getBoolean("never_show_again", false)

    var showTutorial by remember { mutableStateOf(!neverShowAgainPref) }
    var neverShowAgain by remember { mutableStateOf(neverShowAgainPref) }

    val infinite = rememberInfiniteTransition(label = "seed-circle")
    val scale by infinite.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "seed-scale"
    )

    if (!showTutorial) return

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.pinch_24px),
                contentDescription = "Pinch",
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                painter = painterResource(id = R.drawable.arrow_right_alt_24px),
                contentDescription = "Arrow",
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .wrapContentSize()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sports_basketball_24px),
                    contentDescription = "Object",
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                val circleSize = 44.dp * scale
                Box(
                    modifier = Modifier
                        .offset(x = 10.dp, y = 10.dp)
                        .size(circleSize)
                ) {
                    DashedCircle(
                        color = Color.Green,
                        strokeWidth = 2.dp,
                        dash = floatArrayOf(2.5f, 5f)
                    )
                }
            }
        }

        Text(
            text = "Pinch the screen to adjust the green circle to be slightly smaller than the object's size.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(true,
                        onClick = {
                            neverShowAgain = !neverShowAgain
                        }
                    )
            ) {
                val resourceID = if (neverShowAgain) R.drawable.select_check_box_24px
                                 else R.drawable.check_box_outline_blank_24px
                Icon(
                    painter = painterResource(id = resourceID),
                    contentDescription = "Never Show Again checkbox",
                    tint = Color.White
                )
                Text(
                    text = "Don't show again",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    showTutorial = false // NOTE: Don't remove this even if your IDE tells you it will be never read.
                                         //       The dialog will disappear only when you checked "never show again"
                                         //       if you remove the statement.
                    prefs.edit {
                        putBoolean("never_show_again", neverShowAgain)
                    }
                },
                colors = ButtonDefaults.textButtonColors()
            ) {
                Text("Dismiss", maxLines = 1, softWrap = false)
            }
        }
    }
}

@Composable
private fun DashedCircle(
    color: Color,
    strokeWidth: Dp = 2.dp,
    dash: FloatArray = floatArrayOf(2f, 4f)
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(dash)
            )
        )
    }
}