package com.curvsurf.arpointcloudfindsurface.helpers

import android.opengl.GLES32
import android.opengl.GLException
import android.opengl.GLU
import android.util.Log
import java.util.Locale
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.let
import kotlin.text.format

object GLError {

    private fun getGlErrors(): List<Int>? {
        var errorCode = GLES32.glGetError()
        if (errorCode == GLES32.GL_NO_ERROR) {
            return null
        }

        val errorCodes: MutableList<Int> = kotlin.collections.ArrayList()
        errorCodes.add(errorCode)
        while (true) {
            errorCode = GLES32.glGetError()
            if (errorCode == GLES32.GL_NO_ERROR) {
                break
            }
            errorCodes.add(errorCode)
        }
        return errorCodes
    }

    private fun formatErrorMessage(reason: String, api: String, errorCodes: List<Int>): String {
        val header = String.format("%s: %s: ", reason, api)
        val errorString = errorCodes.map { errorCode ->
            val errorString = GLU.gluErrorString(errorCode)
            return String.format(Locale.US, "%s (%d)", errorString, errorCode)
        }.joinToString(", ")
        return header + errorString
    }

    fun maybeThrowGLException(reason: String, api: String) {
        getGlErrors()?.let {
            val error = it[0]
            val message = formatErrorMessage(reason, api, it)
            throw GLException(error, message)
        }
    }

    fun maybeLogGLError(priority: Int, tag: String, reason: String, api: String) {
        getGlErrors()?.let {
            val message = formatErrorMessage(reason, api, it)
            Log.println(priority, tag, message)
        }
    }
}