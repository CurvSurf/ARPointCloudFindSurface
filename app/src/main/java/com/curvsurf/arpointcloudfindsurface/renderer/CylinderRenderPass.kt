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
import kotlin.text.get

private const val TO_BE_INITIALIZED_LATER: Int = 0
class CylinderRenderPass(context: Context) {

    class Buffer() {
        val gpuBuffer: Int = OpenGL.createBuffer(GLES32.GL_UNIFORM_BUFFER, bufferLength, null, GLES32.GL_STATIC_DRAW)
        private val dataBuffer: ByteBuffer = allocateDirectByteBuffer(bufferLength)
        private var updateRequired: Boolean = false

        init {
            set(color = Color.cylinder,
                opacity = 0.4f,
                lineColor = Color.lineColor)
            update()
        }

        constructor(
            cylinder: GeometryObject.Cylinder,
            color: Vector3 = Color.cylinder,
            opacity: Float = 0.6f,
            lineColor: Vector3 = Color.lineColor): this() {
            set(cylinder, color, opacity, lineColor)
            update()
        }

        companion object {
            var bufferLength: Int = TO_BE_INITIALIZED_LATER
            var bottomOffset: Int = TO_BE_INITIALIZED_LATER
            var topOffset: Int = TO_BE_INITIALIZED_LATER
            var radiusOffset: Int = TO_BE_INITIALIZED_LATER
            var colorOffset: Int = TO_BE_INITIALIZED_LATER
            var opacityOffset: Int = TO_BE_INITIALIZED_LATER
            var lineColorOffset: Int = TO_BE_INITIALIZED_LATER
            const val gridX: Int = 36
            const val gridY: Int = 3
        }

        fun set(
            cylinder: GeometryObject.Cylinder? = null,
            color: Vector3? = null,
            opacity: Float? = null,
            lineColor: Vector3? = null
        ) {
            var changed = false
            if (cylinder != null) {
                dataBuffer.putVector3(bottomOffset, cylinder.bottom)
                dataBuffer.putVector3(topOffset, cylinder.top)
                dataBuffer.putFloat(radiusOffset, cylinder.radius)
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

        fun update(){
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
    private val cylinderUniformQuery: OpenGL.UniformBlockQuery
    private val gridLocation: Int

    init {
        val vertexShaderSource = context.loadShaderSourceFromAssets("shaders/cylinder.vert.glsl")
        val fragmentShaderSource = context.loadShaderSourceFromAssets("shaders/cylinder.frag.glsl")
        val vertexShader = OpenGL.createShader(GLES32.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = OpenGL.createShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = OpenGL.createProgram(vertexShader, fragmentShader)
        val uniformBlocks = OpenGL.queryUniformBlocks(program)
        val uniforms = OpenGL.queryUniformLocations(program)

        transformQuery = uniformBlocks["Transform"]!!
        cylinderUniformQuery = uniformBlocks["CylinderUniform"]!!

        Buffer.bufferLength = cylinderUniformQuery.size
        Buffer.bottomOffset = cylinderUniformQuery.uniformOffsets["bottom"]!!
        Buffer.topOffset = cylinderUniformQuery.uniformOffsets["top"]!!
        Buffer.radiusOffset = cylinderUniformQuery.uniformOffsets["radius"]!!
        Buffer.colorOffset = cylinderUniformQuery.uniformOffsets["color"]!!
        Buffer.opacityOffset = cylinderUniformQuery.uniformOffsets["opacity"]!!
        Buffer.lineColorOffset = cylinderUniformQuery.uniformOffsets["line_color"]!!

        gridLocation = uniforms["grid"]!!
    }

    var transformBuffer: Int? = null

    fun draw(cylinderBuffers: Array<Buffer>) {
        val transformBuffer = transformBuffer ?: return
        if (cylinderBuffers.isEmpty()) return

        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, transformBuffer)
        val vertexCount = Buffer.gridX * Buffer.gridY * 6
        for (cylinderBuffer in cylinderBuffers) {
            GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, cylinderUniformQuery.binding, cylinderBuffer.gpuBuffer)
            GLES32.glUniform2i(gridLocation, Buffer.gridX, Buffer.gridY)

            GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

            GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, cylinderUniformQuery.binding, 0)
        }
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, 0)
        GLES32.glUseProgram(0)
        GLES32.glDisable(GLES32.GL_BLEND)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }

    fun draw(cylinderBuffer: Buffer) {
        val transformBuffer = transformBuffer ?: return

        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, transformBuffer)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, cylinderUniformQuery.binding, cylinderBuffer.gpuBuffer)
        GLES32.glUniform2i(gridLocation, Buffer.gridX, Buffer.gridY)

        val vertexCount = Buffer.gridX * Buffer.gridY * 6
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, cylinderUniformQuery.binding, 0)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, 0)
        GLES32.glUseProgram(0)
        GLES32.glDisable(GLES32.GL_BLEND)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }
}