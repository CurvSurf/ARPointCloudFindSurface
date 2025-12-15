package com.curvsurf.arpointcloudfindsurface.helpers

import androidx.xr.runtime.math.Vector3
import com.curvsurf.arpointcloudfindsurface.helpers.math.distance
import com.curvsurf.arpointcloudfindsurface.helpers.math.invoke
import com.google.ar.core.Pose
import kotlin.math.max

class MotionTrackingStabilizer(
    val movingDistance: Float = 2.0f,
    val sensorDistance: Float = 0.10f
) {
    private var position: Vector3 = Vector3()
    private var distanceRemaining: Float = movingDistance

    val progress: Float
        get() = ((movingDistance - distanceRemaining) / movingDistance).coerceIn(0f, 1f)

    enum class Status {
        NotStarted, Working, Finished;

        val isNotFinished: Boolean
            get() = this != Finished

        val isFinished: Boolean
            get() = this == Finished

        val isWorking: Boolean
            get() = this == Working

        val hasNotStarted: Boolean
            get() = this == NotStarted
    }
    var status: Status = Status.NotStarted
        private set
    private var frameCountNotEnoughFeaturesDetected: Int = 0
    val notEnoughFeatures: Boolean
        get() = frameCountNotEnoughFeaturesDetected > 60

    fun update(cameraPose: Pose, featureCount: Int): Status {
        if (status == Status.Finished) return status
        if (status == Status.NotStarted) status = Status.Working

        if (featureCount < 50) {
            ++frameCountNotEnoughFeaturesDetected
        } else {
            frameCountNotEnoughFeaturesDetected = 0
        }

        if (hasEnoughScans(cameraPose, featureCount)) status = Status.Finished
        return status
    }

    private fun hasEnoughScans(cameraPose: Pose, featureCount: Int): Boolean {
        if (distanceRemaining <= 0f) return true

        // In ARCore, camera looks toward -Z in camera space; a point "in front" is (0,0,-sensorDistance)
        val p = Vector3(cameraPose.transformPoint(floatArrayOf(0f, 0f, -sensorDistance)))
        val old = position
        position = p

        if (featureCount > 50) {
            val d = distance(p, old)
            distanceRemaining = max(distanceRemaining - d, 0f)
        }
        return distanceRemaining == 0f
    }
}