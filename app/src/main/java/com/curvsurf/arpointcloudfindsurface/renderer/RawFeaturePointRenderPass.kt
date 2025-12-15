package com.curvsurf.arpointcloudfindsurface.renderer

import android.content.Context
import android.opengl.GLES32
import com.curvsurf.arpointcloudfindsurface.helpers.OpenGL
import com.curvsurf.arpointcloudfindsurface.helpers.loadShaderSourceFromAssets
import java.nio.FloatBuffer
import kotlin.text.get

class RawFeaturePointRenderPass(context: Context) {

    companion object {
        private const val VERTEX_STRIDE = Float.SIZE_BYTES * 4
        private const val MAX_POINT_COUNT = 8192
    }

    private val program: Int
    private val transformBinding: Int

    private val vertexArray: Int
    private val vertexBuffer: Int
    private var vertexCount: Int = 0

    init {
        val vertexShaderSource = context.loadShaderSourceFromAssets("shaders/raw_feature_point.vert.glsl")
        val fragmentShaderSource = context.loadShaderSourceFromAssets("shaders/raw_feature_point.frag.glsl")
        val vertexShader = OpenGL.createShader(GLES32.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = OpenGL.createShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = OpenGL.createProgram(vertexShader, fragmentShader)
        val attributes = OpenGL.queryAttributeLocations(program)
        val uniformBlocks = OpenGL.queryUniformBlockBindings(program)

        val positionLocation = attributes["in_position"]!!
        var confidenceLocation = attributes["in_confidence"]!!

        vertexBuffer = OpenGL.createBuffer()
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vertexBuffer)
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, MAX_POINT_COUNT * VERTEX_STRIDE, null, GLES32.GL_STREAM_DRAW)
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)

        vertexArray = OpenGL.createVertexArray()
        GLES32.glBindVertexArray(vertexArray)
        GLES32.glBindVertexBuffer(0, vertexBuffer, 0, VERTEX_STRIDE)
        GLES32.glEnableVertexAttribArray(positionLocation)
        GLES32.glVertexAttribFormat(positionLocation, 3, GLES32.GL_FLOAT, false, 0)
        GLES32.glVertexAttribBinding(positionLocation, 0)
        GLES32.glEnableVertexAttribArray(confidenceLocation)
        GLES32.glVertexAttribFormat(confidenceLocation, 1, GLES32.GL_FLOAT, false, Float.SIZE_BYTES * 3)
        GLES32.glVertexAttribBinding(confidenceLocation, 0)
        GLES32.glBindVertexArray(0)

        transformBinding = uniformBlocks["Transform"]!!
    }

    fun setPoints(floats: FloatBuffer) {
        vertexCount = floats.capacity() / 4
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vertexBuffer)
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, 0, floats.capacity(), floats.apply { rewind() })
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)
    }

    var transformBuffer: Int? = null
        set(value) {
            if (field == value) return
            field = value

        }

    fun draw() {
        if (vertexCount < 1) return
        val transformBuffer = this.transformBuffer ?: return

//        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindVertexArray(vertexArray)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformBinding, transformBuffer)

        GLES32.glDrawArrays(GLES32.GL_POINTS, 0, vertexCount)

        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformBinding, 0)
        GLES32.glBindVertexArray(0)
        GLES32.glUseProgram(0)

        GLES32.glDisable(GLES32.GL_BLEND)
//        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }

}