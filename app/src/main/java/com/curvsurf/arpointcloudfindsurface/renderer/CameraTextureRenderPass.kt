package com.curvsurf.arpointcloudfindsurface.renderer

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES32
import android.util.Log
import com.curvsurf.arpointcloudfindsurface.helpers.OpenGL
import com.curvsurf.arpointcloudfindsurface.helpers.TAG
import com.curvsurf.arpointcloudfindsurface.helpers.loadShaderSourceFromAssets
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.text.get

class CameraTextureRenderPass(context: Context) {

    companion object {
        private const val BUFFER_SIZE = Float.SIZE_BYTES * 2 * 4
        private val NDC_QUAD_COORDS_BUFFER: FloatBuffer = ByteBuffer
            .allocateDirect(BUFFER_SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(floatArrayOf(-1f, -1f, +1f, -1f, -1f, +1f, +1f, +1f))
            .apply { rewind() }
    }

    private val program: Int

    val cameraTexture: Int

    private val vertexArray: Int
    private val vertexBuffer: Int

    private val texcoordBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(BUFFER_SIZE)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    init {
        val vertexShaderSource = context.loadShaderSourceFromAssets("shaders/camera_texture.vert.glsl")
        var fragmentShaderSource = context.loadShaderSourceFromAssets("shaders/camera_texture.frag.glsl")
        val vertexShader = OpenGL.createShader(GLES32.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = OpenGL.createShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = OpenGL.createProgram(vertexShader, fragmentShader)
        val attributes = OpenGL.queryAttributeLocations(program)
        val uniforms = OpenGL.queryUniformLocations(program)

        val positionLocation = attributes["in_position"]!!
        val texcoordLocation = attributes["in_texcoord"]!!

        vertexBuffer = OpenGL.createBuffer()
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vertexBuffer)
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, BUFFER_SIZE * 2, null, GLES32.GL_STATIC_DRAW)
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, 0, BUFFER_SIZE, NDC_QUAD_COORDS_BUFFER.rewind())
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, BUFFER_SIZE, BUFFER_SIZE, texcoordBuffer.rewind())
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)

        vertexArray = OpenGL.createVertexArray()
        GLES32.glBindVertexArray(vertexArray)
        GLES32.glEnableVertexAttribArray(positionLocation)
        GLES32.glBindVertexBuffer(0, vertexBuffer, 0, Float.SIZE_BYTES * 2)
        GLES32.glVertexAttribFormat(positionLocation, 2, GLES32.GL_FLOAT, false, 0)
        GLES32.glVertexAttribBinding(positionLocation, 0)
        GLES32.glEnableVertexAttribArray(texcoordLocation)
        GLES32.glBindVertexBuffer(1, vertexBuffer, BUFFER_SIZE.toLong(), Float.SIZE_BYTES * 2)
        GLES32.glVertexAttribFormat(texcoordLocation, 2, GLES32.GL_FLOAT, false, 0)
        GLES32.glVertexAttribBinding(texcoordLocation, 1)
        GLES32.glBindVertexArray(0)

        cameraTexture = OpenGL.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_LINEAR, GLES32.GL_LINEAR, GLES32.GL_CLAMP_TO_EDGE, GLES32.GL_CLAMP_TO_EDGE)

        uniforms["camera_texture"]?.let { cameraTextureLocation ->
            GLES32.glProgramUniform1i(program, cameraTextureLocation, 0)
        } ?: run {
            Log.e(TAG, "Could not find camera_texture uniform location")
        }
    }

    fun updateDisplayGeometry(frame: Frame) {

        if (!frame.hasDisplayGeometryChanged()) return
        Log.e(TAG, "CameraTexture Bar")
        frame.transformCoordinates2d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
            NDC_QUAD_COORDS_BUFFER.apply { rewind() },
            Coordinates2d.TEXTURE_NORMALIZED,
            texcoordBuffer.apply { rewind() })

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vertexBuffer)
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, BUFFER_SIZE, BUFFER_SIZE, texcoordBuffer.rewind())
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)
    }

    fun draw() {

        val depthTest = GLES32.glIsEnabled(GLES32.GL_DEPTH_TEST)
        val depthMask = IntArray(1).apply { GLES32.glGetIntegerv(GLES32.GL_DEPTH_WRITEMASK, this, 0) }[0]
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
        GLES32.glDepthMask(false)
        GLES32.glUseProgram(program)
        GLES32.glBindVertexArray(vertexArray)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexture)

        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4)

        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        GLES32.glBindVertexArray(0)
        GLES32.glUseProgram(0)
        if (depthTest) { GLES32.glEnable(GLES32.GL_DEPTH_TEST) }
        else { GLES32.glDisable(GLES32.GL_DEPTH_TEST) }
        GLES32.glDepthMask(depthMask != 0)
    }
}