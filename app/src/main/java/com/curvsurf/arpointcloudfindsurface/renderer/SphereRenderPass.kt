package com.curvsurf.arpointcloudfindsurface.renderer

import android.content.Context
import android.opengl.GLES32
import androidx.xr.runtime.math.Vector3
import com.curvsurf.arpointcloudfindsurface.Color
import com.curvsurf.arpointcloudfindsurface.helpers.OpenGL
import com.curvsurf.arpointcloudfindsurface.helpers.allocateDirectByteBuffer
import com.curvsurf.arpointcloudfindsurface.helpers.curvsurf.GeometryObject
import com.curvsurf.arpointcloudfindsurface.helpers.loadShaderSourceFromAssets
import com.curvsurf.arpointcloudfindsurface.helpers.putVector3
import java.nio.ByteBuffer
import kotlin.collections.isEmpty

private const val TO_BE_INITIALIZED_LATER: Int = 0
class SphereRenderPass(context: Context) {

    class Buffer() {
        val gpuBuffer: Int = OpenGL.createBuffer(GLES32.GL_UNIFORM_BUFFER, bufferLength, null, GLES32.GL_STATIC_DRAW)
        private val dataBuffer: ByteBuffer = allocateDirectByteBuffer(bufferLength)
        private var updateRequired: Boolean = false

        init {
            set(color = Color.sphere,
                opacity = 0.3f,
                lineColor = Color.lineColor)
            update()
        }

        constructor(
            sphere: GeometryObject.Sphere,
            color: Vector3 = Color.sphere,
            opacity: Float = 0.4f,
            lineColor: Vector3 = Color.lineColor): this() {
            set(sphere, color, opacity, lineColor)
            update()
        }

        companion object {
            var bufferLength: Int = TO_BE_INITIALIZED_LATER
            var centerOffset: Int = TO_BE_INITIALIZED_LATER
            var radiusOffset: Int = TO_BE_INITIALIZED_LATER
            var colorOffset: Int = TO_BE_INITIALIZED_LATER
            var opacityOffset: Int = TO_BE_INITIALIZED_LATER
            var lineColorOffset: Int = TO_BE_INITIALIZED_LATER
            const val gridX: Int = 36
            const val gridY: Int = 36
        }

        fun set(
            sphere: GeometryObject.Sphere? = null,
            color: Vector3? = null,
            opacity: Float? = null,
            lineColor: Vector3? = null
        ) {
            var changed = false
            if (sphere != null) {
                dataBuffer.putVector3(centerOffset, sphere.center)
                dataBuffer.putFloat(radiusOffset, sphere.radius)
                changed = true
            }
            if (color != null) {
                dataBuffer.putVector3(colorOffset, color)
                changed = true
            }
            if (opacity != null) {
                dataBuffer.putFloat(opacityOffset, opacity)
                changed = true
            }
            if (lineColor != null) {
                dataBuffer.putVector3(lineColorOffset, lineColor)
                changed = true
            }
            if (changed) { updateRequired = true }
        }

        fun update() {
            if (!updateRequired) return
            GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, gpuBuffer)
            GLES32.glBufferSubData(GLES32.GL_UNIFORM_BUFFER, 0,
                bufferLength, dataBuffer.rewind())
            GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, 0)
            updateRequired = false
        }

        fun release() {
            GLES32.glDeleteBuffers(1, intArrayOf(gpuBuffer), 0)
        }
    }

    companion object;

    private val program: Int
    private val transformQuery: OpenGL.UniformBlockQuery
    private val sphereUniformQuery: OpenGL.UniformBlockQuery
    private val gridLocation: Int

    init {
        val vertexShaderSource = context.loadShaderSourceFromAssets("shaders/sphere.vert.glsl")
        val fragmentShaderSource = context.loadShaderSourceFromAssets("shaders/sphere.frag.glsl")
        val vertexShader = OpenGL.createShader(GLES32.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = OpenGL.createShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = OpenGL.createProgram(vertexShader, fragmentShader)
        val uniformBlocks = OpenGL.queryUniformBlocks(program)
        val uniforms = OpenGL.queryUniformLocations(program)

        transformQuery = uniformBlocks["Transform"]!!
        sphereUniformQuery = uniformBlocks["SphereUniform"]!!

        Buffer.bufferLength = sphereUniformQuery.size
        Buffer.centerOffset = sphereUniformQuery.uniformOffsets["center"]!!
        Buffer.radiusOffset = sphereUniformQuery.uniformOffsets["radius"]!!
        Buffer.colorOffset = sphereUniformQuery.uniformOffsets["color"]!!
        Buffer.opacityOffset = sphereUniformQuery.uniformOffsets["opacity"]!!
        Buffer.lineColorOffset = sphereUniformQuery.uniformOffsets["line_color"]!!

        gridLocation = uniforms["grid"]!!
    }

    var transformBuffer: Int? = null

    fun draw(sphereBuffers: Array<Buffer>) {
        val transformBuffer = transformBuffer ?: return
        if (sphereBuffers.isEmpty()) return

        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, transformBuffer)
        GLES32.glUniform2i(gridLocation, Buffer.gridX, Buffer.gridY)

        val vertexCount = Buffer.gridX * Buffer.gridY * 6
        for (sphereBuffer in sphereBuffers) {
            GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, sphereUniformQuery.binding, sphereBuffer.gpuBuffer)

            GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

            GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, sphereUniformQuery.binding, 0)
        }
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, 0)
        GLES32.glUseProgram(0)
        GLES32.glDisable(GLES32.GL_BLEND)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }

    fun draw(sphereBuffer: Buffer) {
        val transformBuffer = transformBuffer ?: return

        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, transformBuffer)
        GLES32.glUniform2i(gridLocation, Buffer.gridX, Buffer.gridY)

        val vertexCount = Buffer.gridX * Buffer.gridY * 6
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, sphereUniformQuery.binding, sphereBuffer.gpuBuffer)

        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, sphereUniformQuery.binding, 0)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, 0)
        GLES32.glUseProgram(0)
        GLES32.glDisable(GLES32.GL_BLEND)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }
}