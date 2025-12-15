package com.curvsurf.arpointcloudfindsurface.helpers.math

import androidx.xr.runtime.math.Matrix3
import androidx.xr.runtime.math.Matrix4
import androidx.xr.runtime.math.Vector3
import androidx.xr.runtime.math.Vector4
import kotlin.math.abs
import kotlin.math.sqrt

private const val EPS = 1e-6f
private fun safeNormalize(vector: Vector3, fallback: Vector3 = Vector3.Zero): Vector3 {
    val lengthSquared = length2(vector = vector)
    return if (lengthSquared < EPS) fallback else vector / sqrt(lengthSquared)
}

private fun project(vector: Vector3, onto: Vector3): Vector3 {
    val lengthSquared = length2(onto)
    if (lengthSquared < EPS) return Vector3.Zero
    val t = dot(vector, onto) / lengthSquared
    return onto * t
}

operator fun Matrix4.Companion.invoke(
    m00: Float, m01: Float, m02: Float, m03: Float,
    m10: Float, m11: Float, m12: Float, m13: Float,
    m20: Float, m21: Float, m22: Float, m23: Float,
    m30: Float, m31: Float, m32: Float, m33: Float
): Matrix4 {
    val dataToCopy = floatArrayOf(
        m00, m10, m20, m30,
        m01, m11, m21, m31,
        m02, m12, m22, m32,
        m03, m13, m23, m33
    )
    return Matrix4(dataToCopy)
}

fun Matrix4.Companion.fromRowMajor(array: FloatArray, offset: Int = 0): Matrix4 {
    return Matrix4(
        array[offset + 0], array[offset + 1], array[offset + 2], array[offset + 3],
        array[offset + 4], array[offset + 5], array[offset + 6], array[offset + 7],
        array[offset + 8], array[offset + 9], array[offset + 10], array[offset + 11],
        array[offset + 12], array[offset + 13], array[offset + 14], array[offset + 15]
    )
}

fun Matrix4.Companion.fromColumnMajor(array: FloatArray, offset: Int = 0): Matrix4 {
    return Matrix4(
        array[offset + 0], array[offset + 4], array[offset + 8], array[offset + 12],
        array[offset + 1], array[offset + 5], array[offset + 9], array[offset + 13],
        array[offset + 2], array[offset + 6], array[offset + 10], array[offset + 14],
        array[offset + 3], array[offset + 7], array[offset + 11], array[offset + 15]
    )
}

operator fun Matrix4.Companion.invoke(xAxis: Vector3,
                                      yAxis: Vector3,
                                      zAxis: Vector3,
                                      position: Vector3 = Vector3.Zero): Matrix4 {
    return Matrix4(
        xAxis.x, yAxis.x, zAxis.x, position.x,
        xAxis.y, yAxis.y, zAxis.y, position.y,
        xAxis.z, yAxis.z, zAxis.z, position.z,
        0f, 0f, 0f, 1f
    )
}

fun Matrix4.Companion.fromX(axis: Vector3, position: Vector3 = Vector3.Zero): Matrix4 {
    val right = Vector3.Right
    val up = Vector3.Up
    val backward = Vector3.Backward
    val x = safeNormalize(axis, right)
    val yRef = if (abs(dot(x, up)) < 0.999f) up else backward
    var y = safeNormalize(yRef - project(yRef, x), up)
    val z = safeNormalize(cross(x, y), backward)
    y = safeNormalize(cross(z, x), up)
    return Matrix4(x, y, z, position)
}

fun Matrix4.Companion.fromY(axis: Vector3, position: Vector3 = Vector3.Zero): Matrix4 {
    val right = Vector3.Right
    val up = Vector3.Up
    val backward = Vector3.Backward
    val y = safeNormalize(axis, up)
    val zRef = if (abs(dot(y, backward)) < 0.999f) backward else right
    var z = safeNormalize(zRef - project(zRef, y), backward)
    val x = safeNormalize(cross(y, z), right)
    z = safeNormalize(cross(x, y), backward)
    return Matrix4(x, y, z, position)
}

fun Matrix4.Companion.fromZ(axis: Vector3, position: Vector3 = Vector3.Zero): Matrix4 {
    val right = Vector3.Right
    val up = Vector3.Up
    val backward = Vector3.Backward
    val z = safeNormalize(axis, backward)
    val xRef = if (abs(dot(z, right)) < 0.999f) right else up
    var x = safeNormalize(xRef - project(xRef, z), right)
    val y = safeNormalize(cross(z, x), up)
    x = safeNormalize(cross(y, z), right)
    return Matrix4(x, y, z, position)
}

fun Matrix4.Companion.transform(xAxis: Vector3,
                                yAxis: Vector3,
                                zAxis: Vector3,
                                origin: Vector3 = Vector3.Zero): Matrix4 {

    return Matrix4(
        xAxis.x, xAxis.y, xAxis.z, -origin.dot(xAxis),
        yAxis.x, yAxis.y, yAxis.z, -origin.dot(yAxis),
        zAxis.x, zAxis.y, zAxis.z, -origin.dot(zAxis),
        0f, 0f, 0f, 1f
    )
}

var Matrix3.column0: Vector3
    get() = Vector3(data[0], data[1], data[2])
    set(value) {
        data[0] = value.x
        data[1] = value.y
        data[2] = value.z
    }

var Matrix3.column1: Vector3
    get() = Vector3(data[3], data[4], data[5])
    set(value) {
        data[3] = value.x
        data[4] = value.y
        data[5] = value.z
    }

var Matrix3.column2: Vector3
    get() = Vector3(data[6], data[7], data[8])
    set(value) {
        data[6] = value.x
        data[7] = value.y
        data[8] = value.z
    }

var Matrix3.row0: Vector3
    get() = Vector3(data[0], data[3], data[6])
    set(value) {
        data[0] = value.x
        data[3] = value.y
        data[6] = value.z
    }

var Matrix3.row1: Vector3
    get() = Vector3(data[1], data[4], data[7])
    set(value) {
        data[1] = value.x
        data[4] = value.y
        data[7] = value.z
    }

var Matrix3.row2: Vector3
    get() = Vector3(data[2], data[5], data[8])
    set(value) {
        data[2] = value.x
        data[5] = value.y
        data[8] = value.z
    }

var Matrix4.column0: Vector4
    get() = Vector4(data[0], data[1], data[2], data[3])
    set(value) {
        data[0] = value.x
        data[1] = value.y
        data[2] = value.z
        data[3] = value.w
    }

var Matrix4.column1: Vector4
    get() = Vector4(data[4], data[5], data[6], data[7])
    set(value) {
        data[4] = value.x
        data[5] = value.y
        data[6] = value.z
        data[7] = value.w
    }

var Matrix4.column2: Vector4
    get() = Vector4(data[8], data[9], data[10], data[11])
    set(value) {
        data[8] = value.x
        data[9] = value.y
        data[10] = value.z
        data[11] = value.w
    }

var Matrix4.column3: Vector4
    get() = Vector4(data[12], data[13], data[14], data[15])
    set(value) {
        data[12] = value.x
        data[13] = value.y
        data[14] = value.z
        data[15] = value.w
    }

var Matrix4.row0: Vector4
    get() = Vector4(data[0], data[4], data[8], data[12])
    set(value) {
        data[0] = value.x
        data[4] = value.y
        data[8] = value.z
        data[12] = value.w
    }

var Matrix4.row1: Vector4
    get() = Vector4(data[1], data[5], data[9], data[13])
    set(value) {
        data[1] = value.x
        data[5] = value.y
        data[9] = value.z
        data[13] = value.w
    }

var Matrix4.row2: Vector4
    get() = Vector4(data[2], data[6], data[10], data[14])
    set(value) {
        data[2] = value.x
        data[6] = value.y
        data[10] = value.z
        data[14] = value.w
    }

var Matrix4.row3: Vector4
    get() = Vector4(data[3], data[7], data[11], data[15])
    set(value) {
        data[3] = value.x
        data[7] = value.y
        data[11] = value.z
        data[15] = value.w
    }

var Matrix4.xAxis: Vector3
    get() = Vector3(data[0], data[1], data[2])
    set(value) {
        data[0] = value.x
        data[1] = value.y
        data[2] = value.z
    }

var Matrix4.yAxis: Vector3
    get() = Vector3(data[4], data[5], data[6])
    set(value) {
        data[4] = value.x
        data[5] = value.y
        data[6] = value.z
    }

var Matrix4.zAxis: Vector3
    get() = Vector3(data[8], data[9], data[10])
    set(value) {
        data[8] = value.x
        data[9] = value.y
        data[10] = value.z
    }

var Matrix4.position: Vector3
    get() = Vector3(data[12], data[13], data[14])
    set(value) {
        data[12] = value.x
        data[13] = value.y
        data[14] = value.z
    }

operator fun Matrix3.times(vector: Vector3): Vector3 {
    return Vector3(
        x = dot(row0, vector),
        y = dot(row1, vector),
        z = dot(row2, vector)
    )
}

operator fun Vector3.times(matrix: Matrix3): Vector3 {
    return Vector3(
        x = dot(this, matrix.column0),
        y = dot(this, matrix.column1),
        z = dot(this, matrix.column2)
    )
}

operator fun Matrix4.times(vector: Vector4): Vector4 {
    return Vector4(
        x = dot(row0, vector),
        y = dot(row1, vector),
        z = dot(row2, vector),
        w = dot(row3, vector)
    )
}

operator fun Vector4.times(matrix: Matrix4): Vector4 {
    return Vector4(
        x = dot(this, matrix.column0),
        y = dot(this, matrix.column1),
        z = dot(this, matrix.column2),
        w = dot(this, matrix.column3)
    )
}

fun Matrix4.times(vector: Vector3, w: Float): Vector3 {
    return (this * Vector4(vector, w)).xyz
}