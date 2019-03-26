package org.egon12.renderscripttutorial

import android.animation.TimeAnimator
import android.os.Bundle
import android.renderscript.RenderScript
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.rs_input_fragment.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.util.TextureAvailableListener


class RsInputFragment : Fragment() {

    private lateinit var renderScript: RenderScript

    private lateinit var scriptC: ScriptC_uv_hue

    private val timeAnimator = TimeAnimator()

    private val channel = Channel<Unit>(2)

    companion object {
        fun newInstance() = RsInputFragment()
    }

    private lateinit var viewModel: RsInputViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.rs_input_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        renderScript = RenderScript.create(activity!!)
        scriptC = ScriptC_uv_hue(renderScript)
        viewModel = ViewModelProviders.of(this).get(RsInputViewModel::class.java)
        println("one")
        channel.sendBlocking(Unit)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        texture_view.surfaceTextureListener = TextureAvailableListener {

            val allocation = AllocationFactory(renderScript)
                    .createOutputAllocation(
                            Size(512, 512),
                            Surface(it)
                    )


            timeAnimator.setTimeListener { _, _, _ ->
                scriptC._frame += 1
                scriptC.forEach_process(allocation)
                allocation.ioSend()
            }

            println("two")
            channel.sendBlocking(Unit)
        }
    }

    override fun onResume() {
        super.onResume()
        MainScope().launch {
            println("three")
            channel.receive()
            println("four")
            channel.receive()
            println("fivec")
            timeAnimator.start()
        }
    }
}
