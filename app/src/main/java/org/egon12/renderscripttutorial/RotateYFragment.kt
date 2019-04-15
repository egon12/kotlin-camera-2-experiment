package org.egon12.renderscripttutorial

import android.annotation.SuppressLint
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Matrix3f
import android.renderscript.RenderScript
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_rotate_y.*
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.camera.CameraFacade
import org.egon12.renderscripttutorial.util.SeekBarListener
import org.egon12.renderscripttutorial.util.TextureAvailableListener
import kotlin.math.cos
import kotlin.math.sin

class RotateYFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rotate_y, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val cameraManager = activity!!.getSystemService(CameraManager::class.java)

        cameraFacade = CameraFacade(cameraManager)
        renderScript = RenderScript.create(activity)
        allocationFactory = AllocationFactory(renderScript)
        inputAllocation = allocationFactory.createYuvInputAllocation(cameraFacade.getSize())
        scriptC = ScriptC_rotate_y(renderScript)
        scriptC._input = inputAllocation
        width = cameraFacade.getSize().width
        height = cameraFacade.getSize().height
        scriptC._input_width = width.toLong()
        scriptC._input_height = height.toLong()

        matrix3f = Matrix3f()
        matrix3f.loadIdentity()

        scriptC._trans = matrix3f

    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        texture_view.surfaceTextureListener = TextureAvailableListener {

            outputAllocation = allocationFactory.createOutputAllocation(
                    cameraFacade.getSize(),
                    Surface(it)
            )



            inputAllocation.setOnBufferAvailableListener {
                inputAllocation.ioReceive()
                scriptC.forEach_process(outputAllocation)
                outputAllocation.ioSend()
            }

            cameraFacade.showCameraInSurface(inputAllocation.surface)


        }


        seek_bar.setOnSeekBarChangeListener(SeekBarListener {

            val r = (it / 100.0) * 0.001 * Math.PI
            val c = cos(r).toFloat()
            val s = sin(r).toFloat()

            matrix3f.set(0, 0,  c)
            matrix3f.set(1, 0, 0F)
            matrix3f.set(2, 0, -s)


            matrix3f.set(0, 1, 0F)
            matrix3f.set(1, 1, 1F)
            matrix3f.set(2, 1, 0F)

            matrix3f.set(0, 2, s)
            matrix3f.set(1, 2, 0F)
            matrix3f.set(2, 2, c)


            scriptC._trans = matrix3f
        })
    }

    override fun onPause() {
        super.onPause()
        cameraFacade.stop()
    }

    lateinit var cameraFacade: CameraFacade
    lateinit var renderScript: RenderScript
    lateinit var allocationFactory: AllocationFactory
    lateinit var inputAllocation: Allocation
    lateinit var outputAllocation: Allocation
    lateinit var scriptC: ScriptC_rotate_y
    lateinit var matrix3f: Matrix3f
    var width: Int = 0
    var height: Int = 0


}
