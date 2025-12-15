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
class PlaneRenderPass(context: Context) {

    class Buffer() {
        val gpuBuffer: Int = OpenGL.createBuffer(GLES32.GL_UNIFORM_BUFFER, bufferLength, null, GLES32.GL_STATIC_DRAW)
        private val dataBuffer: ByteBuffer = allocateDirectByteBuffer(bufferLength)
        private var updateRequired: Boolean = false
        var gridX: Int = 0
        var gridY: Int = 0

        init {
            set(color = Color.plane,
                opacity = 0.2f,
                lineColor = Color.lineColor)
            update()
        }

        constructor(
            plane: GeometryObject.Plane,
            color: Vector3 = Color.plane,
            opacity: Float = 0.6f,
            lineColor: Vector3 = Color.lineColor): this() {
            set(plane, color, opacity, lineColor)
            update()
        }

        companion object {
            var bufferLength: Int = TO_BE_INITIALIZED_LATER
            var lowerLeftOffset: Int = TO_BE_INITIALIZED_LATER
            var upperLeftOffset: Int = TO_BE_INITIALIZED_LATER
            var upperRightOffset: Int = TO_BE_INITIALIZED_LATER
            var lowerRightOffset: Int = TO_BE_INITIALIZED_LATER
            var colorOffset: Int = TO_BE_INITIALIZED_LATER
            var opacityOffset: Int = TO_BE_INITIALIZED_LATER
            var lineColorOffset: Int = TO_BE_INITIALIZED_LATER
            private fun calcGridSize(plane: GeometryObject.Plane): Pair<Int, Int> {
                val width = plane.width
                val height = plane.height
                val longerSteps = (3 * width / height).toInt()
                return if (width > height) { longerSteps to 3 } else { 3 to longerSteps }
            }
        }

        fun set(
            plane: GeometryObject.Plane? = null,
            color: Vector3? = null,
            opacity: Float? = null,
            lineColor: Vector3? = null
        ) {
            var changed = false
            if (plane != null) {
                val (gridX, gridY) = calcGridSize(plane)
                this.gridX = gridX
                this.gridY = gridY

                dataBuffer.putVector3(lowerLeftOffset, plane.bottomLeft)
                dataBuffer.putVector3(upperLeftOffset, plane.topLeft)
                dataBuffer.putVector3(upperRightOffset, plane.topRight)
                dataBuffer.putVector3(lowerRightOffset, plane.bottomRight)
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
            GLES32.glBufferSubData(GLES32.GL_UNIFORM_BUFFER, 0, bufferLength, dataBuffer.rewind())
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
    private val planeUniformQuery: OpenGL.UniformBlockQuery
    private val gridLocation: Int

    init {
        val vertexShaderSource = context.loadShaderSourceFromAssets("shaders/plane.vert.glsl")
        val fragmentShaderSource = context.loadShaderSourceFromAssets("shaders/plane.frag.glsl")
        val vertexShader = OpenGL.createShader(GLES32.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = OpenGL.createShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = OpenGL.createProgram(vertexShader, fragmentShader)
        val uniformBlocks = OpenGL.queryUniformBlocks(program)
        val uniforms = OpenGL.queryUniformLocations(program)

        transformQuery = uniformBlocks["Transform"]!!
        planeUniformQuery = uniformBlocks["PlaneUniform"]!!

        Buffer.bufferLength = planeUniformQuery.size
        Buffer.lowerLeftOffset = planeUniformQuery.uniformOffsets["lower_left"]!!
        Buffer.upperLeftOffset = planeUniformQuery.uniformOffsets["upper_left"]!!
        Buffer.upperRightOffset = planeUniformQuery.uniformOffsets["upper_right"]!!
        Buffer.lowerRightOffset = planeUniformQuery.uniformOffsets["lower_right"]!!
        Buffer.colorOffset = planeUniformQuery.uniformOffsets["color"]!!
        Buffer.opacityOffset = planeUniformQuery.uniformOffsets["opacity"]!!
        Buffer.lineColorOffset = planeUniformQuery.uniformOffsets["line_color"]!!

        gridLocation = uniforms["grid"]!!
    }

    var transformBuffer: Int? = null

    fun draw(planeBuffers: Array<Buffer>) {
        val transformBuffer = transformBuffer ?: return
        if (planeBuffers.isEmpty()) return

        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, transformBuffer)
        for (planeBuffer in planeBuffers) {
            GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, planeUniformQuery.binding, planeBuffer.gpuBuffer)
            GLES32.glUniform2i(gridLocation, planeBuffer.gridX, planeBuffer.gridY)

            val vertexCount = planeBuffer.gridX * planeBuffer.gridY * 6
            GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

            GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, planeUniformQuery.binding, 0)
        }
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, 0)
        GLES32.glUseProgram(0)
        GLES32.glDisable(GLES32.GL_BLEND)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }

    fun draw(planeBuffer: Buffer) {
        val transformBuffer = transformBuffer ?: return

        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, transformBuffer)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, planeUniformQuery.binding, planeBuffer.gpuBuffer)
        GLES32.glUniform2i(gridLocation, planeBuffer.gridX, planeBuffer.gridY)

        val vertexCount = planeBuffer.gridX * planeBuffer.gridY * 6
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, planeUniformQuery.binding, 0)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformQuery.binding, 0)
        GLES32.glUseProgram(0)
        GLES32.glDisable(GLES32.GL_BLEND)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }
}