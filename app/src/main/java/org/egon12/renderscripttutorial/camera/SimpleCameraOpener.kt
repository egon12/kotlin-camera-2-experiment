package org.egon12.renderscripttutorial.camera

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.support.annotation.RequiresPermission
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SimpleCameraOpener(private val cameraManager: CameraManager) : CameraOpener {

    @RequiresPermission(android.Manifest.permission.CAMERA)
    override suspend fun openCamera(cameraId: String): CameraDevice = suspendCoroutine {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) = it.resume(camera)

            override fun onDisconnected(camera: CameraDevice) {}

            override fun onError(camera: CameraDevice, error: Int) {
                val errorMessage = when (error) {
                    ERROR_CAMERA_IN_USE -> "Camera is used by another app"
                    ERROR_MAX_CAMERAS_IN_USE -> "Too many camera is used"
                    ERROR_CAMERA_DISABLED -> "Camera is disabled"
                    ERROR_CAMERA_DEVICE -> "Camera device is error"
                    ERROR_CAMERA_SERVICE -> "Error in Camera system"
                    else -> "UnknownError"

                }
                it.resumeWithException(Exception(errorMessage))
            }
        }, null)
    }
}