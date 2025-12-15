package com.curvsurf.arpointcloudfindsurface.helpers

import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

interface ARCoreAppGLRenderer {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onDrawFrame()
}

class RendererAdapter(val renderer: ARCoreAppGLRenderer): GLSurfaceView.Renderer {
    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        renderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        renderer.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        renderer.onDrawFrame()
    }
}

@Composable
fun OpenGLView(
    renderer: ARCoreAppGLRenderer,
    onViewReady: (GLSurfaceView) -> Unit) {

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            GLSurfaceView(context).apply {
                preserveEGLContextOnPause = true
                setEGLContextClientVersion(3)
                setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                setRenderer(RendererAdapter(renderer))
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                onViewReady(this)
            }
        }
    )
}