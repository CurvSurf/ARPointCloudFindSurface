package com.curvsurf.arpointcloudfindsurface.helpers.math

import androidx.xr.runtime.math.Vector2
import androidx.xr.runtime.math.Vector3
import androidx.xr.runtime.math.Vector4
import kotlin.math.sqrt

operator fun Float.times(vector: Vector2) = vector * this
operator fun Float.times(vector: Vector3) = vector * this
operator fun Float.times(vector: Vector4) = vector * this

operator fun Vector2.times(vector: Vector2) = Vector2(x * vector.x, y * vector.y)
operator fun Vector3.times(vector: Vector3) = Vector3(x * vector.x, y * vector.y, z * vector.z)
operator fun Vector4.times(vector: Vector4) = Vector4(x * vector.x, y * vector.y, z * vector.z, w * vector.w)

fun length(vector: Vector2): Float = vector.length
fun length(vector: Vector3): Float = vector.length
fun length(vector: Vector4): Float = vector.length

fun length2(vector: Vector2): Float = vector.lengthSquared
fun length2(vector: Vector3): Float = vector.lengthSquared
fun length2(vector: Vector4): Float = vector.lengthSquared

fun cross(lhs: Vector3, rhs: Vector3): Vector3 = lhs.cross(rhs)

fun dot(lhs: Vector2, rhs: Vector2): Float = lhs.dot(rhs)
fun dot(lhs: Vector3, rhs: Vector3): Float = lhs.dot(rhs)
fun dot(lhs: Vector4, rhs: Vector4): Float = lhs.dot(rhs)

fun normalize(vector: Vector2): Vector2 = vector.toNormalized()
fun normalize(vector: Vector3): Vector3 = vector.toNormalized()
fun normalize(vector: Vector4): Vector4 = vector.toNormalized()

fun distance2(from: Vector2, to: Vector2): Float {
    val dx = from.x - to.x
    val dy = from.y - to.y
    return dx * dx + dy * dy
}
fun distance2(from: Vector3, to: Vector3): Float {
    val dx = from.x - to.x
    val dy = from.y - to.y
    val dz = from.z - to.z
    return dx * dx + dy * dy + dz * dz
}
fun distance2(from: Vector4, to: Vector4): Float {
    val dx = from.x - to.x
    val dy = from.y - to.y
    val dz = from.z - to.z
    val dw = from.w - to.w
    return dx * dx + dy * dy + dz * dz + dw * dw
}

fun distance(from: Vector2, to: Vector2): Float = sqrt(distance2(from, to))
fun distance(from: Vector3, to: Vector3): Float = sqrt(distance2(from, to))
fun distance(from: Vector4, to: Vector4): Float = sqrt(distance2(from, to))

val Vector4.xyz: Vector3
    get() = Vector3(x, y, z)

operator fun Vector4.Companion.invoke(xyz: Vector3, w: Float): Vector4 {
    return Vector4(x = xyz.x, y = xyz.y, z = xyz.z, w)
}

operator fun Vector2.Companion.invoke(array: FloatArray, offset: Int = 0): Vector2 {
    require(array.size >= 2)
    return Vector2(x = array[offset + 0], y = array[offset + 1])
}

operator fun Vector3.Companion.invoke(array: FloatArray, offset: Int = 0): Vector3 {
    require(array.size >= 3)
    return Vector3(
        x = array[offset + 0],
        y = array[offset + 1],
        z = array[offset + 2])
}

operator fun Vector4.Companion.invoke(array: FloatArray, offset: Int = 0): Vector4 {
    require(array.size >= 4)
    return Vector4(
        x = array[offset + 0],
        y = array[offset + 1],
        z = array[offset + 2],
        w = array[offset + 3])
}

fun Vector2.toFloatArray(): FloatArray = floatArrayOf(x, y)
fun Vector3.toFloatArray(): FloatArray = floatArrayOf(x, y, z)
fun Vector4.toFloatArray(): FloatArray = floatArrayOf(x, y, z, w)
