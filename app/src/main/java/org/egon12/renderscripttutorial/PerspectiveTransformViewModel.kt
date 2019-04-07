package org.egon12.renderscripttutorial

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.util.Size
import android.view.Surface
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.camera.CameraFacade

class PerspectiveTransformViewModel: ViewModel() {

    private lateinit var renderScript: RenderScript

    private lateinit var cameraFacade: CameraFacade

    private lateinit var allocationFactory: AllocationFactory

    private lateinit var size: Size

    private lateinit var inputAllocation: Allocation

    private lateinit var scriptC: ScriptC_perspective_transform

    fun inject(context: Context) {

        val cameraManager = context.getSystemService(CameraManager::class.java)
        cameraFacade = CameraFacade(cameraManager)
        renderScript = RenderScript.create(context)
        allocationFactory = AllocationFactory(renderScript)


        val cameraId = cameraFacade.getCameraId()
        size = cameraFacade.getSize(cameraId)
        println(size)
        inputAllocation = allocationFactory.createYuvInputAllocation(size)


        scriptC = ScriptC_perspective_transform(renderScript)
        scriptC._input = inputAllocation
        scriptC._input_height = size.height
        scriptC._input_width = size.width

        scriptC.bind_maxX(Allocation.createSized(renderScript, Element.I32(renderScript), size.height))
        scriptC.bind_minX(Allocation.createSized(renderScript, Element.I32(renderScript), size.height))

        scriptC.bind_maxY(Allocation.createSized(renderScript, Element.I32(renderScript), size.width))
        scriptC.bind_minY(Allocation.createSized(renderScript, Element.I32(renderScript), size.width))
    }

    private lateinit var outputAllocation: Allocation

    fun setup(surface: Surface) {

        outputAllocation = allocationFactory.createOutputAllocation(size, surface)

    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    fun start() {

        cameraFacade.showCameraInSurface(inputAllocation.surface)
        inputAllocation.setOnBufferAvailableListener {
            inputAllocation.ioReceive()
            scriptC.invoke_getBorder()
            scriptC.forEach_process(outputAllocation)
            outputAllocation.ioSend()
        }
    }

    fun stop() {
        cameraFacade.stop()
    }

}