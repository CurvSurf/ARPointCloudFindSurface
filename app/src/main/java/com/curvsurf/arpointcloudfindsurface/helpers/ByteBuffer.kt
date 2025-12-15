package com.curvsurf.arpointcloudfindsurface.helpers

import androidx.xr.runtime.math.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun allocateDirectByteBuffer(capacity: Int): ByteBuffer {
    return ByteBuffer.allocateDirect(capacity)
        .order(ByteOrder.nativeOrder())
}

fun ByteBuffer.putVector3(vector: Vector3): ByteBuffer {
    putFloat(vector.x)
    putFloat(vector.y)
    putFloat(vector.z)
    return this
}

fun ByteBuffer.putVector3(byteOffset: Int, vector: Vector3): ByteBuffer {
    putFloat(byteOffset, vector.x)
    putFloat(byteOffset + Float.SIZE_BYTES, vector.y)
    putFloat(byteOffset + Float.SIZE_BYTES * 2, vector.z)
    return this
}

fun FloatBuffer.putVector3(vector: Vector3): FloatBuffer {
    put(vector.x)
    put(vector.y)
    put(vector.z)
    return this
}

fun FloatBuffer.putVector3(index: Int, vector: Vector3): FloatBuffer {
    put(index, vector.x)
    put(index + 1, vector.y)
    put(index + 2, vector.z)
    return this
}