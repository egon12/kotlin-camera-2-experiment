package org.egon12.renderscripttutorial


import android.annotation.SuppressLint
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.camera.CameraFacade
import org.egon12.renderscripttutorial.util.TextureAvailableListener

/**
 * A simple [Fragment] subclass.
 */
class SimpleBlurFragment : Fragment() {

    lateinit var textureView: TextureView
    lateinit var cameraManager: CameraManager
    lateinit var cameraFacade: CameraFacade
    lateinit var renderScript: RenderScript
    lateinit var allocationFactory: AllocationFactory
    lateinit var inputAllocation: Allocation
    lateinit var outputAllocation: Allocation
    lateinit var scriptC: ScriptC_simple_blur



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cameraManager = activity!!.getSystemService(CameraManager::class.java)
        cameraFacade = CameraFacade(cameraManager)

        renderScript = RenderScript.create(activity)
        allocationFactory = AllocationFactory(renderScript)

        inputAllocation = allocationFactory.createYuvInputAllocation(cameraFacade.getSize())

        scriptC = ScriptC_simple_blur(renderScript)

        scriptC._input = inputAllocation
        scriptC._width = cameraFacade.getSize().width
        scriptC._height = cameraFacade.getSize().height
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        textureView = TextureView(activity)

        textureView.surfaceTextureListener = TextureAvailableListener {

            outputAllocation = allocationFactory.createOutputAllocation(
                    cameraFacade.getSize(),
                    Surface(it)
            )

            cameraFacade.showCameraInSurface(inputAllocation.surface);

            inputAllocation.setOnBufferAvailableListener {
                inputAllocation.ioReceive()
                scriptC.forEach_root(outputAllocation)
                outputAllocation.ioSend()
            }

        }

        return textureView
    }

    override fun onPause() {
        super.onPause()
        cameraFacade.stop()
    }


}
