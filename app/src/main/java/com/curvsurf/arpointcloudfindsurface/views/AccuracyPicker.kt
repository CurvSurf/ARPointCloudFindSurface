package com.curvsurf.arpointcloudfindsurface.views

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class AccuracyConstant {
    A3cm, A4cm, A5cm, A6cm, A7cm;

    val rawValue: Float
        get() = when (this) {
            A3cm -> 0.03f
            A4cm -> 0.04f
            A5cm -> 0.05f
            A6cm -> 0.06f
            A7cm -> 0.07f
        }

    val label: String
        get() = when (this) {
            A3cm -> "3cm"
            A4cm -> "4cm"
            A5cm -> "5cm"
            A6cm -> "6cm"
            A7cm -> "7cm"
        }
}

enum class AccuracyCoefficient {
    B0_3cm, B0_4cm, B0_5cm, B0_6cm, B0_7cm;

    val rawValue: Float
        get() = when (this) {
            B0_3cm -> 0.003f
            B0_4cm -> 0.004f
            B0_5cm -> 0.005f
            B0_6cm -> 0.006f
            B0_7cm -> 0.007f
        }

    val label: String
        get() = when (this) {
            B0_3cm -> "0.3cm"
            B0_4cm -> "0.4cm"
            B0_5cm -> "0.5cm"
            B0_6cm -> "0.6cm"
            B0_7cm -> "0.7cm"
        }
}

data class LinearFunction(
    val constant: AccuracyConstant = AccuracyConstant.A5cm,
    val coefficient: AccuracyCoefficient = AccuracyCoefficient.B0_5cm
) {
    operator fun invoke(depth: Float): Float {
        return constant.rawValue + depth * coefficient.rawValue
    }
}

@Composable
fun AccuracyPicker(
    modifier: Modifier = Modifier,
    accuracyFunction: LinearFunction,
    onSetAccuracyFunction: (LinearFunction) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        AccuracyConstantPicker(
            modifier = Modifier.width(IntrinsicSize.Min),
            accuracyConstant = accuracyFunction.constant,
        ) {
            onSetAccuracyFunction(
                accuracyFunction.copy(
                    constant = it
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        AccuracyCoefficientPicker(
            modifier = Modifier.width(IntrinsicSize.Min),
            accuracyCoefficient = accuracyFunction.coefficient
        ) {
            onSetAccuracyFunction(
                accuracyFunction.copy(
                    coefficient = it
                )
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AccuracyConstantPickerPreview() {
    AccuracyPicker(
        accuracyFunction = LinearFunction()
    ) {}
}

@Composable
private fun AccuracyConstantPicker(
    modifier: Modifier = Modifier,
    accuracyConstant: AccuracyConstant,
    onSetAccuracyConstant: (AccuracyConstant) -> Unit
) {
    val items = listOf(
        AccuracyConstant.A3cm,
        AccuracyConstant.A4cm,
        AccuracyConstant.A5cm,
        AccuracyConstant.A6cm,
        AccuracyConstant.A7cm
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        items.forEachIndexed { index, constant ->
            SegmentedButton(
                modifier = Modifier
                    .background(Color.DarkGray.copy(alpha = 0.3f)),
                selected = accuracyConstant == constant,
                onClick = { onSetAccuracyConstant(constant) },
                shape = SegmentedButtonDefaults.itemShape(index, items.size),
            ) {
                Text(constant.label)
            }
        }
    }
}

@Composable
private fun AccuracyCoefficientPicker(
    modifier: Modifier = Modifier,
    accuracyCoefficient: AccuracyCoefficient,
    onSetAccuracyCoefficient: (AccuracyCoefficient) -> Unit
) {
    val items = listOf(
        AccuracyCoefficient.B0_3cm,
        AccuracyCoefficient.B0_4cm,
        AccuracyCoefficient.B0_5cm,
        AccuracyCoefficient.B0_6cm,
        AccuracyCoefficient.B0_7cm
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        items.forEachIndexed { index, coefficient ->
            SegmentedButton(
                modifier = Modifier
                    .background(Color.DarkGray.copy(alpha = 0.3f)),
                selected = accuracyCoefficient == coefficient,
                onClick = { onSetAccuracyCoefficient(coefficient) },
                shape = SegmentedButtonDefaults.itemShape(index, items.size),
            ) {
                Text(coefficient.label)
            }
        }
    }
}
