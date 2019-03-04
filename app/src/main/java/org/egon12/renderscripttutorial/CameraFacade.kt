package org.egon12.renderscripttutorial

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.support.annotation.RequiresPermission
import android.view.Surface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CameraFacade(
        private val cameraManager: CameraManager
) : CameraChooser by SimpleCameraChooser(cameraManager) {

    private val cameraOpener = SimpleCameraOpener(cameraManager)

    private lateinit var cameraDevice: CameraDevice

    @ExperimentalCoroutinesApi
    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun showCameraInSurface(surface: Surface) = MainScope().launch {
        val cameraId = this@CameraFacade.getCameraId()
        cameraDevice = cameraOpener.openCamera(cameraId)
        val starter = SimpleCameraStarter(cameraDevice)
        starter.start(surface)
    }

    fun stop() {
        cameraDevice.close()
    }

}
