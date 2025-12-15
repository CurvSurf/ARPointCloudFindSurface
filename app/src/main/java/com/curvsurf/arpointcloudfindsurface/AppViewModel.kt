package com.curvsurf.arpointcloudfindsurface

import android.app.Application
import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.unit.IntSize
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.curvsurf.arpointcloudfindsurface.helpers.MotionTrackingStabilizer
import com.curvsurf.arpointcloudfindsurface.views.AccuracyCoefficient
import com.curvsurf.arpointcloudfindsurface.views.AccuracyConstant
import com.curvsurf.arpointcloudfindsurface.views.LinearFunction
import com.curvsurf.findsurface.FeatureType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class UIEffect {
    data class Snackbar(
        val message: String,
        val actionLabel: String? = null,
        val withDismissAction: Boolean = false,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ): UIEffect()
}

private val Context.dataStore by preferencesDataStore(name = "raw_feature_app_store")
private val KEY_SEED_RATIO = doublePreferencesKey("seed-radius-ratio")
private val KEY_ACCURACY_CONSTANT = stringPreferencesKey("accuracy-constant")
private val KEY_ACCURACY_COEFFICIENT = stringPreferencesKey("accuracy-coefficient")

class AppViewModel(
    application: Application
): AndroidViewModel(application), ContentViewModel {

    private val appContext: Context = application.applicationContext
    private val _stabilizationData = MutableStateFlow(
        StabilizationData()
    )
    override val stabilizationData = _stabilizationData.asStateFlow()

    override fun updateStabilizationData(
        status: MotionTrackingStabilizer.Status?,
        notEnoughFeatures: Boolean?,
        progress: Float?
    ) {
        _stabilizationData.update { s ->
            s.copy(
                status = status ?: s.status,
                notEnoughFeatures = notEnoughFeatures ?: s.notEnoughFeatures,
                progress = progress ?: s.progress
            )
        }
    }

    fun updateStabilizationData(stabilizer: MotionTrackingStabilizer) {
        updateStabilizationData(
            status = stabilizer.status,
            notEnoughFeatures = stabilizer.notEnoughFeatures,
            progress = stabilizer.progress
        )
    }

    private val _recordingData = MutableStateFlow(
        RecordingData()
    )
    override val recordingData = _recordingData.asStateFlow()

    override fun updateRecordingData(
        recording: Boolean?,
        pointCount: Int?
    ) {
        _recordingData.update { s ->
            s.copy(
                recording = recording ?: s.recording,
                pointCount = pointCount ?: s.pointCount
            )
        }
    }

    private val _radiusData = MutableStateFlow(
        RadiusData()
    )
    override val radiusData = _radiusData.asStateFlow()

    override fun updateRadiusData(
        seedRadiusRatio: Float?,
        probeRadiusRatio: Float?,
        lastCanvasSize: IntSize?
    ) {
        _radiusData.update { s ->
            s.copy(
                seedRadiusRatio = (seedRadiusRatio ?: s.seedRadiusRatio).coerceIn(0.01f, 1.0f),
                probeRadiusRatio = probeRadiusRatio ?: s.probeRadiusRatio,
                lastCanvasSize = lastCanvasSize ?: s.lastCanvasSize
            )
        }
    }

    private val _findSurfaceData = MutableStateFlow(
        FindSurfaceData()
    )
    override val findSurfaceData = _findSurfaceData.asStateFlow()

    override fun updateFindSurfaceData(
        featureType: FeatureType?,
        previewEnabled: Boolean?,
        hasToSaveOne: Boolean?,
        transactionIsEmpty: Boolean?
    ) {
        _findSurfaceData.update { s ->
            s.copy(
                featureType = featureType ?: s.featureType,
                previewEnabled = previewEnabled ?: s.previewEnabled,
                hasToSaveOne = hasToSaveOne ?: s.hasToSaveOne,
                transactionIsEmpty = transactionIsEmpty ?: s.transactionIsEmpty
            )
        }
    }

    private val _accuracyFunction = MutableStateFlow(
        LinearFunction()
    )
    override val accuracyFunction = _accuracyFunction.asStateFlow()

    override fun updateAccuracyFunction(
        constant: AccuracyConstant?,
        coefficient: AccuracyCoefficient?
    ) {
        _accuracyFunction.update { s ->
            s.copy(
                constant = constant ?: s.constant,
                coefficient = coefficient ?: s.coefficient
            )
        }
    }

    private val _effects = MutableSharedFlow<UIEffect>(extraBufferCapacity = 16)
    override val effects: SharedFlow<UIEffect> = _effects.asSharedFlow()

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        _effects.tryEmit(
            UIEffect.Snackbar(message, actionLabel, withDismissAction, duration)
        )
    }

    private val _events = MutableSharedFlow<UIEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<UIEvent> = _events.asSharedFlow()

    override fun undo() {
        _events.tryEmit(UIEvent.Undo)
    }

    override fun clearGeometries() {
        _events.tryEmit(UIEvent.ClearGeometries)
    }

    override fun clearPointCloud() {
        _events.tryEmit(UIEvent.ClearPointCloud)
    }

    override fun captureGeometry() {
        updateFindSurfaceData(hasToSaveOne = true)
    }

    init {
        viewModelScope.launch {
            val prefs = appContext.dataStore.data.first()

            val savedSeedRadiusRatio = (prefs[KEY_SEED_RATIO] ?: 0.5).toFloat()
            val savedAccuracyConstant = prefs[KEY_ACCURACY_CONSTANT]?.let { AccuracyConstant.valueOf(it) } ?: AccuracyConstant.A5cm
            val savedAccuracyCoefficient = prefs[KEY_ACCURACY_COEFFICIENT]?.let { AccuracyCoefficient.valueOf(it) } ?: AccuracyCoefficient.B0_5cm
            _radiusData.value = _radiusData.value.copy(
                seedRadiusRatio = savedSeedRadiusRatio.coerceIn(0.1f, 1.0f),
            )
            _accuracyFunction.value = _accuracyFunction.value.copy(
                constant = savedAccuracyConstant,
                coefficient = savedAccuracyCoefficient
            )
        }
    }

    override fun savePreferenceValues() {
        val seed = _radiusData.value.seedRadiusRatio.toDouble()
        val accuracyFunction = _accuracyFunction.value
        val accuracyConstant = accuracyFunction.constant.name
        val accuracyCoefficient = accuracyFunction.coefficient.name
        viewModelScope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[KEY_SEED_RATIO] = seed
                prefs[KEY_ACCURACY_CONSTANT] = accuracyConstant
                prefs[KEY_ACCURACY_COEFFICIENT] = accuracyCoefficient
            }
        }
    }

    private val _toasts = MutableSharedFlow<String>(extraBufferCapacity = 4)
    override val toasts = _toasts.asSharedFlow()
}
