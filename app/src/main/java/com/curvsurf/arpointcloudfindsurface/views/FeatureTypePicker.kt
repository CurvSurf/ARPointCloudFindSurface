package com.curvsurf.arpointcloudfindsurface.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.curvsurf.arpointcloudfindsurface.R
import com.curvsurf.findsurface.FeatureType

@Composable
fun FeatureTypePicker(
    modifier: Modifier = Modifier,
    targetFeature: FeatureType,
    onSetTargetFeature: (FeatureType) -> Unit
) {
    val items = listOf(
        FeatureType.Plane,
        FeatureType.Sphere,
        FeatureType.Cylinder
    )
    Card (
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray.copy(alpha = 0.3f)
        ),
        modifier = modifier
    ) {
        Column {
            items.forEach { featureType ->
                SingleChoiceSegmentedButtonRow {

                    SegmentedButton(
                        selected = targetFeature == featureType,
                        onClick = { onSetTargetFeature(featureType) },
                        shape = SegmentedButtonDefaults.itemShape(0, 1),
                        label = {
                            when (featureType) {
                                FeatureType.Plane ->
                                    Icon(
                                        painter = painterResource(id = R.drawable.square_24px),
                                        contentDescription = "Plane",
                                        tint = Color.Red
                                    )

                                FeatureType.Sphere ->
                                    Icon(
                                        painter = painterResource(id = R.drawable.sports_basketball_24px),
                                        contentDescription = "Sphere",
                                        tint = Color.Green
                                    )

                                FeatureType.Cylinder ->
                                    Icon(
                                        painter = painterResource(id = R.drawable.database_24px),
                                        contentDescription = "Cylinder",
                                        tint = Color.Cyan
                                    )

                                else ->
                                    Icon(
                                        painter = painterResource(id = R.drawable.error_24px),
                                        contentDescription = "Error",
                                        tint = Color.Red
                                    )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun FeatureTypePickerPreview() {
    FeatureTypePicker(
        targetFeature = FeatureType.Plane,
        onSetTargetFeature = {}
    )
}