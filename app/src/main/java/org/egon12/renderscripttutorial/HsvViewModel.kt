package org.egon12.renderscripttutorial

import android.annotation.SuppressLint
import android.renderscript.RenderScript
import android.view.Surface
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.camera.CameraFacade

class HsvViewModel : ViewModel() {

    private lateinit var cameraFacade: CameraFacade

    private lateinit var renderScript: RenderScript

    private lateinit var allocationFactory: AllocationFactory

    private lateinit var scriptC_hsv: ScriptC_hsv

    fun inject(
            cameraFacade: CameraFacade,
            renderScript: RenderScript
    ) {
        this.cameraFacade = cameraFacade
        this.renderScript = renderScript
        this.allocationFactory = AllocationFactory(renderScript)
        this.scriptC_hsv = ScriptC_hsv(renderScript)
    }

    fun forceHueOverride() {
        scriptC_hsv._hForce = true
    }

    fun ignoreHueOverride() {
        scriptC_hsv._hForce = false
    }

    fun setHue(value: Int) {
        scriptC_hsv._hForceValue = value * 360 / 100
    }

    fun forceSaturationOverride() {
        scriptC_hsv._sForce = true
    }

    fun ignoreSaturationOverride() {
        scriptC_hsv._sForce = false
    }

    fun setSaturation(value: Int) {
        scriptC_hsv._sMultiplier = value
    }

    fun forceValueOverride() {
        scriptC_hsv._vForce = true
    }

    fun ignoreValueOverride() {
        scriptC_hsv._vForce = false
    }

    fun setValue(value: Int) {
        scriptC_hsv._vForceValue = value * 255 / 100
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    fun showCameraInSurface(s: Surface) {
        val cameraId = cameraFacade.getCameraId()
        val cameraSize = cameraFacade.getSize(cameraId)
        println("size $cameraSize")
        val inputAllocation = allocationFactory.createYuvInputAllocation(cameraSize)

        val outputAlloctaion = allocationFactory.createOutputAllocation(cameraSize, s)

        scriptC_hsv._inputFrame = inputAllocation
        inputAllocation.setOnBufferAvailableListener {
            inputAllocation.ioReceive()
            scriptC_hsv.forEach_process(outputAlloctaion)
            outputAlloctaion.ioSend()
        }


        cameraFacade.showCameraInSurface(inputAllocation.surface)
    }

    fun stopCamera() {
        cameraFacade.stop()
    }
}
