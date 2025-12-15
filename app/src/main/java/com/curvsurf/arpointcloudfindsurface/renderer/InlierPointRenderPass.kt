package com.curvsurf.arpointcloudfindsurface.renderer

import android.content.Context
import android.opengl.GLES32
import androidx.xr.runtime.math.Vector3
import com.curvsurf.arpointcloudfindsurface.helpers.OpenGL
import com.curvsurf.arpointcloudfindsurface.helpers.allocateDirectByteBuffer
import com.curvsurf.arpointcloudfindsurface.helpers.loadShaderSourceFromAssets
import com.curvsurf.arpointcloudfindsurface.helpers.putVector3
import kotlin.text.get

class InlierPointRenderPass(context: Context) {

    class Buffer(points: Array<Vector3>,
                 val color: Vector3) {
        val gpuBuffer: Int
        val vertexCount: Int

        init {
            val bufferLength = Float.SIZE_BYTES * 3 * points.size
            vertexCount = points.size
            val buffer = allocateDirectByteBuffer(bufferLength)
            for (point in points) {
                buffer.putVector3(point)
            }
            buffer.rewind()
            gpuBuffer = OpenGL.createBuffer(GLES32.GL_ARRAY_BUFFER, bufferLength, buffer, GLES32.GL_STATIC_DRAW)
        }

        fun release() {
            GLES32.glDeleteBuffers(1, intArrayOf(gpuBuffer), 0)
        }
    }

    companion object {
        const val VERTEX_STRIDE = Float.SIZE_BYTES * 3
    }

    private val program: Int
    private val positionLocation: Int
    private val transformBinding: Int
    private val pointColorLocation: Int

    private val vertexArray: Int

    init {
        val vertexShaderSource = context.loadShaderSourceFromAssets("shaders/inlier.vert.glsl")
        val fragmentShaderSource = context.loadShaderSourceFromAssets("shaders/inlier.frag.glsl")
        val vertexShader = OpenGL.createShader(GLES32.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = OpenGL.createShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = OpenGL.createProgram(vertexShader, fragmentShader)
        val attributes = OpenGL.queryAttributeLocations(program)
        val uniformBlocks = OpenGL.queryUniformBlockBindings(program)
        val uniforms = OpenGL.queryUniformLocations(program)

        positionLocation = attributes["in_position"]!!

        vertexArray = OpenGL.createVertexArray()
        GLES32.glBindVertexArray(vertexArray)
        GLES32.glEnableVertexAttribArray(positionLocation)
        GLES32.glVertexAttribFormat(positionLocation, 3, GLES32.GL_FLOAT, false, 0)
        GLES32.glVertexAttribBinding(positionLocation, 0)
        GLES32.glBindVertexArray(0)

        transformBinding = uniformBlocks["Transform"]!!
        pointColorLocation = uniforms["point_color"]!!
    }

    var transformBuffer: Int? = null

    fun draw(buffers: Array<Buffer>) {
        val transformBuffer = transformBuffer ?: return
        if (buffers.isEmpty()) return

//        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        GLES32.glUseProgram(program)
        GLES32.glBindVertexArray(vertexArray)
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformBinding, transformBuffer)

        for (buffer in buffers) {
            GLES32.glUniform4f(pointColorLocation, buffer.color.x, buffer.color.y, buffer.color.z, 0.8f)
            GLES32.glBindVertexBuffer(0, buffer.gpuBuffer, 0, VERTEX_STRIDE)

            GLES32.glDrawArrays(GLES32.GL_POINTS, 0, buffer.vertexCount)

//            GLES32.glBindVertexBuffer(0, 0, 0, VERTEX_STRIDE)
        }

        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, transformBinding, 0)
        GLES32.glBindVertexArray(0)
        GLES32.glUseProgram(0)
        GLES32.glDisable(GLES32.GL_BLEND)
//        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
    }
}
