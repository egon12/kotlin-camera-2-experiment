package org.egon12.renderscripttutorial.camera

import android.hardware.camera2.CameraDevice
import androidx.annotation.RequiresPermission
import android.util.Size
import android.view.Surface

interface CameraOpener {

    @RequiresPermission(android.Manifest.permission.CAMERA)
    suspend fun openCamera(cameraId: String): CameraDevice
}

interface CameraChooser {
    fun getCameraId(): String
    fun getSize(cameraId: String): Size
    fun getSize() = getSize(getCameraId())
}

interface CameraStarter {
    suspend fun start(surface: Surface)
}