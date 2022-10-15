package org.egon12.renderscripttutorial.util

import android.graphics.SurfaceTexture
import android.view.TextureView

/**
 *
 */
open class TextureAvailableListener(val func: (SurfaceTexture) -> Unit) : TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        surface?.let { func(it) }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
}