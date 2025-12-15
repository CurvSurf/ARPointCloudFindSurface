package com.curvsurf.arpointcloudfindsurface.renderer

import android.content.Context
import android.opengl.GLES32
import androidx.xr.runtime.math.Vector3
import androidx.xr.runtime.math.Vector4
import com.curvsurf.arpointcloudfindsurface.helpers.OpenGL
import com.curvsurf.arpointcloudfindsurface.helpers.loadShaderSourceFromAssets
import com.curvsurf.arpointcloudfindsurface.helpers.math.toFloatArray
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.text.get

class PointCloudRenderPass(context: Context) {

    companion object {
        private const val VERTEX_STRIDE = Float.SIZE_BYTES * 3
    }

    private val program: Int
    private val positionLocation: Int
    private val transformBinding: Int
    private val pointColorLocation: Int
    private val pickedIndexLocation: Int

    private val vertexArray: Int
    private val vertexBuffer: Int
    private var vertexCount: Int = 0

    init {
        val vertexShaderSource = context.loadShaderSourceFromAssets("shaders/point_cloud.vert.glsl")
        val fragmentShaderSource = context.loadShaderSourceFromAssets("shaders/point_cloud.frag.glsl")
        val vertexShader = OpenGL.createShader(GLES32.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = OpenGL.createShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderSource)
        program = OpenGL.createProgram(vertexShader, fragmentShader)
        val attributes = OpenGL.queryAttributeLocations(program)
        val uniformBlocks = OpenGL.queryUniformBlockBindings(program)
        val uniforms = OpenGL.queryUniformLocations(program)

        positionLocation = attributes["in_position"]!!

        vertexBuffer = OpenGL.createBuffer()
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vertexBuffer)
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, Float.SIZE_BYTES * 3 * 100000, null, GLES32.GL_STREAM_DRAW)
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)

        vertexArray = OpenGL.createVertexArray()
        GLES32.glBindVertexArray(vertexArray)
        GLES32.glEnableVertexAttribArray(positionLocation)
        GLES32.glBindVertexBuffer(0, vertexBuffer, 0, VERTEX_STRIDE)
        GLES32.glVertexAttribFormat(positionLocation, 3, GLES32.GL_FLOAT, false, 0)
        GLES32.glVertexAttribBinding(positionLocation, 0)
        GLES32.glBindVertexArray(0)

        transformBinding = uniformBlocks["Transform"]!!
        pointColorLocation = uniforms["point_color"]!!
        pickedIndexLocation = uniforms["picked_index"]!!

        GLES32.glProgramUniform4f(program, pointColorLocation, 1f, 0.38f, 0f, 0.8f)
        GLES32.glProgramUniform1i(program, pickedIndexLocation, -1)
    }

    var transformBuffer: Int? = null

    var pointColor: Vector4 = Vector4(1f, 0.38f, 0f, 0.8f)
        set(value) {
            if (field == value) return
            field = value
            GLES32.glProgramUniform4fv(program, pointColorLocation, 1, value.toFloatArray(), 0)
        }

    var pickedIndex: Int = -1
        set(value) {
            if (field == value) return
            field = value
            GLES32.glProgramUniform1i(program, pickedIndexLocation, value)
        }

    private var floatBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(Float.SIZE_BYTES * 3 * 100000)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    fun clear() {
        vertexCount = 0
    }

    fun updatePointCloud(points: Array<Vector3>) {
        if (points.isEmpty()) {
            vertexCount = 0
            return
        }

        val array = FloatArray(points.size * 3)
        for (i in 0 until points.size) {
            val point = points[i]
            array[3 * i + 0] = point.x
            array[3 * i + 1] = point.y
            array[3 * i + 2] = point.z
        }
        floatBuffer.apply { rewind() }.put(array)
        vertexCount = points.size

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vertexBuffer)
        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, 0, Float.SIZE_BYTES * 3 * vertexCount, floatBuffer.rewind())
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)
    }
    fun draw() {
        val transformBuffer = transformBuffer ?: return
        if (vertexCount == 0) return

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
