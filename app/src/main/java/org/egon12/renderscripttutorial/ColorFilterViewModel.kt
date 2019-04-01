package org.egon12.renderscripttutorial

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.util.Size
import android.view.Surface
import androidx.lifecycle.ViewModel
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.camera.CameraFacade

class ColorFilterViewModel: ViewModel() {

    lateinit var renderScript: RenderScript
    lateinit var allocationFactory: AllocationFactory
    lateinit var inputAllocation: Allocation
    lateinit var outputAllocation: Allocation
    lateinit var cameraFacade: CameraFacade
    lateinit var scriptC: ScriptC_color_filter
    lateinit var size: Size

    fun inject(
            context: Context
       ) {
        renderScript = RenderScript.create(context)
        allocationFactory = AllocationFactory(renderScript)
        val cameraManager = context.getSystemService(CameraManager::class.java)
        cameraFacade = CameraFacade(cameraManager)


        val cameraId = cameraFacade.getCameraId()
        size = cameraFacade.getSize(cameraId)

        inputAllocation = allocationFactory.createYuvInputAllocation(size)

        scriptC = ScriptC_color_filter(renderScript)
        scriptC._input = inputAllocation

    }

    fun setup(surface: Surface) {
        outputAllocation = allocationFactory.createOutputAllocation(size, surface)

        inputAllocation.setOnBufferAvailableListener {
            inputAllocation.ioReceive()

            scriptC.forEach_process(outputAllocation)
            outputAllocation.ioSend()
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        cameraFacade.showCameraInSurface(inputAllocation.surface)
    }

    fun stop() {
        cameraFacade.stop()
    }

    fun setLow(value: Int) {
        scriptC._low = (value.toFloat() / 100 * 2 * Math.PI).toFloat()
    }

    fun setHigh(value: Int) {
        scriptC._high = (value.toFloat() / 100 * 2 * Math.PI).toFloat()
    }


    fun getFilterValue() = Pair(scriptC._low, scriptC._high)

}
