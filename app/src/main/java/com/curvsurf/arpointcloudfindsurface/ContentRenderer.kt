package com.curvsurf.arpointcloudfindsurface

import android.content.Context
import android.opengl.GLES32
import android.util.Log
import androidx.xr.runtime.math.Vector3
import com.curvsurf.arpointcloudfindsurface.helpers.GLError
import com.curvsurf.arpointcloudfindsurface.helpers.OpenGL
import com.curvsurf.arpointcloudfindsurface.helpers.TAG
import com.curvsurf.arpointcloudfindsurface.helpers.curvsurf.GeometryObject
import com.curvsurf.arpointcloudfindsurface.renderer.CameraTextureRenderPass
import com.curvsurf.arpointcloudfindsurface.renderer.CylinderRenderPass
import com.curvsurf.arpointcloudfindsurface.renderer.InlierPointRenderPass
import com.curvsurf.arpointcloudfindsurface.renderer.PlaneRenderPass
import com.curvsurf.arpointcloudfindsurface.renderer.PointCloudRenderPass
import com.curvsurf.arpointcloudfindsurface.renderer.RawFeaturePointRenderPass
import com.curvsurf.arpointcloudfindsurface.renderer.SphereRenderPass
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ContentRenderer(private val context: Context) {

    enum class PreviewShape {
        None, Plane, Sphere, Cylinder, Cone, Torus, PartialTorus
    }

    private lateinit var cameraTextureRenderPass: CameraTextureRenderPass
    private lateinit var rawFeaturePointRenderPass: RawFeaturePointRenderPass
    private lateinit var pointCloudRenderPass: PointCloudRenderPass

    private var transformBuffer: Int = -1

    private lateinit var planeRenderPass: PlaneRenderPass
    private var planes = mutableListOf<PlaneRenderPass.Buffer>()
    private lateinit var planePreview: PlaneRenderPass.Buffer

    private lateinit var sphereRenderPass: SphereRenderPass
    private var spheres = mutableListOf<SphereRenderPass.Buffer>()
    private lateinit var spherePreview: SphereRenderPass.Buffer

    private lateinit var cylinderRenderPass: CylinderRenderPass
    private var cylinders = mutableListOf<CylinderRenderPass.Buffer>()
    private lateinit var cylinderPreview: CylinderRenderPass.Buffer

    private lateinit var inlierPointRenderPass: InlierPointRenderPass
    private var inlierPoints = mutableListOf<InlierPointRenderPass.Buffer>()

    private var previewShape = PreviewShape.None

    fun onInit() {
        GLES32.glClearColor(0.223f, 0.243f, 0.275f, 1f)

        cameraTextureRenderPass = CameraTextureRenderPass(context)
        rawFeaturePointRenderPass = RawFeaturePointRenderPass(context)
        pointCloudRenderPass = PointCloudRenderPass(context)

        transformBuffer = OpenGL.createBuffer(GLES32.GL_UNIFORM_BUFFER, Float.SIZE_BYTES * 16 * 3, null, GLES32.GL_STREAM_DRAW)

        planeRenderPass = PlaneRenderPass(context)
        planePreview = PlaneRenderPass.Buffer()

        sphereRenderPass = SphereRenderPass(context)
        spherePreview = SphereRenderPass.Buffer()

        cylinderRenderPass = CylinderRenderPass(context)
        cylinderPreview = CylinderRenderPass.Buffer()

        inlierPointRenderPass = InlierPointRenderPass(context)

        rawFeaturePointRenderPass.transformBuffer = transformBuffer
        pointCloudRenderPass.transformBuffer = transformBuffer
        planeRenderPass.transformBuffer = transformBuffer
        sphereRenderPass.transformBuffer = transformBuffer
        cylinderRenderPass.transformBuffer = transformBuffer
        inlierPointRenderPass.transformBuffer = transformBuffer

        GLError.maybeThrowGLException("Something's wrong...", "onInit")
    }

    fun onResize(width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)
        Log.e(TAG, "onResize: $width, $height")
        GLError.maybeThrowGLException("Something's wrong...", "onResize")
    }

    val cameraTextureID: Int
        get() = cameraTextureRenderPass.cameraTexture

    fun setRawFeaturePoints(features: FloatBuffer) {
        rawFeaturePointRenderPass.setPoints(features)
    }

    private val matrixFloatBuffer = ByteBuffer
        .allocateDirect(Float.SIZE_BYTES * 16 * 3)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    fun setTransform(viewMatrixArray: FloatArray, viewMatrixOffset: Int = 0,
                     projectionMatrixArray: FloatArray, projectionMatrixOffset: Int = 0,
                     viewProjectionMatrixArray: FloatArray, viewProjectionMatrixOffset: Int = 0) {
        matrixFloatBuffer
            .put(viewMatrixArray, viewMatrixOffset, 16)
            .put(projectionMatrixArray, projectionMatrixOffset, 16)
            .put(viewProjectionMatrixArray, viewProjectionMatrixOffset, 16)
            .rewind()
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, transformBuffer)
        GLES32.glBufferSubData(GLES32.GL_UNIFORM_BUFFER, 0, 16 * 3 * Float.SIZE_BYTES, matrixFloatBuffer)
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, 0)
    }

    var showRawFeaturePoints = false

    fun onDrawFrame(frame: Frame) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        cameraTextureRenderPass.updateDisplayGeometry(frame)
        if (frame.timestamp != 0L) {
            cameraTextureRenderPass.draw()
            if (showRawFeaturePoints) rawFeaturePointRenderPass.draw()
            pointCloudRenderPass.draw()

            inlierPointRenderPass.draw(inlierPoints.toTypedArray())

            planeRenderPass.draw(planes.toTypedArray())
            sphereRenderPass.draw(spheres.toTypedArray())
            cylinderRenderPass.draw(cylinders.toTypedArray())

            when (previewShape) {
                PreviewShape.Plane -> planeRenderPass.draw(planePreview)
                PreviewShape.Sphere -> sphereRenderPass.draw(spherePreview)
                PreviewShape.Cylinder -> cylinderRenderPass.draw(cylinderPreview)
                else -> {}
            }
        }
        GLError.maybeThrowGLException("Something's wrong...", "onDrawFrame")
    }

    fun updatePointCloud(points: Array<Vector3>) {
        pointCloudRenderPass.updatePointCloud(points)
    }

    fun clearPointCloud() {
        pointCloudRenderPass.clear()
    }

    fun updatePickedIndex(index: Int) {
        pointCloudRenderPass.pickedIndex = index
    }

    fun appendPlane(plane: GeometryObject.Plane, inliers: Array<Vector3>) {
        val buffer = PlaneRenderPass.Buffer(plane)
        buffer.update()
        planes.add(buffer)
        inlierPoints.add(InlierPointRenderPass.Buffer(inliers, Color.planeInlier))
    }

    fun appendSphere(sphere: GeometryObject.Sphere, inliers: Array<Vector3>) {
        val buffer = SphereRenderPass.Buffer(sphere)
        buffer.update()
        spheres.add(buffer)
        inlierPoints.add(InlierPointRenderPass.Buffer(inliers, Color.sphereInlier))
    }

    fun appendCylinder(cylinder: GeometryObject.Cylinder, inliers: Array<Vector3>) {
        val buffer = CylinderRenderPass.Buffer(cylinder)
        buffer.update()
        cylinders.add(buffer)
        inlierPoints.add(InlierPointRenderPass.Buffer(inliers, Color.cylinderInlier))
    }

    fun removeLastPlane() {
        planes.removeAt(planes.lastIndex).release()
    }

    fun removeLastSphere() {
        spheres.removeAt(spheres.lastIndex).release()
    }

    fun removeLastCylinder() {
        cylinders.removeAt(cylinders.lastIndex).release()
    }

    fun removeLastInlierPoints() {
        inlierPoints.removeAt(inlierPoints.lastIndex).release()
    }

    fun clearGeometries() {
        planes.forEach { it.release() }
        planes.clear()
        spheres.forEach { it.release() }
        spheres.clear()
        cylinders.forEach { it.release() }
        cylinders.clear()
        inlierPoints.forEach { it.release() }
        inlierPoints.clear()
    }

    fun setPreviewNone() {
        previewShape = PreviewShape.None
    }

    fun updatePreview(plane: GeometryObject.Plane) {
        previewShape = PreviewShape.Plane
        planePreview.set(plane)
        planePreview.update()
    }

    fun updatePreview(sphere: GeometryObject.Sphere) {
        previewShape = PreviewShape.Sphere
        spherePreview.set(sphere)
        spherePreview.update()
    }

    fun updatePreview(cylinder: GeometryObject.Cylinder) {
        previewShape = PreviewShape.Cylinder
        cylinderPreview.set(cylinder)
        cylinderPreview.update()
    }
}