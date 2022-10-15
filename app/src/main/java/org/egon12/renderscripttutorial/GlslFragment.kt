package org.egon12.renderscripttutorial


import android.animation.TimeAnimator
import android.os.Bundle
import android.renderscript.RenderScript
import android.util.Size
import android.view.*
import androidx.fragment.app.Fragment
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.util.TextureAvailableListener

/**
 * A simple [Fragment] subclass.
 */
class GlslFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val textureView = TextureView(activity!!)


        textureView.surfaceTextureListener = TextureAvailableListener {

            val renderScript = RenderScript.create(activity)
            val allocationFactory = AllocationFactory(renderScript)
            val allocation = allocationFactory.createOutputAllocation(
                    Size(512, 512),
                    Surface(it)
            )

            val scriptC = ScriptC_pretzel(renderScript)

            val timeAnimator = TimeAnimator()
            timeAnimator.setTimeListener { _, _, _ ->
                scriptC.forEach_root(allocation)
                allocation.ioSend()
            }

            timeAnimator.start()
        }

        return textureView
    }
}
