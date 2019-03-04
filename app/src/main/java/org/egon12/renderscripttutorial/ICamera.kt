package org.egon12.renderscripttutorial

import android.hardware.camera2.CameraDevice
import android.support.annotation.RequiresPermission
import android.util.Size
import android.view.Surface

interface CameraOpener {

    @RequiresPermission(android.Manifest.permission.CAMERA)
    suspend fun openCamera(cameraId: String): CameraDevice
}

interface CameraChooser {
    fun getCameraId(): String
    fun getSize(cameraId: String): Size
}

interface CameraStarter {
    suspend fun start(surface: Surface)
}