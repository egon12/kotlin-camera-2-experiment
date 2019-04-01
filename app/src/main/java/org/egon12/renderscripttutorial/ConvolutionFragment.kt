package org.egon12.renderscripttutorial


import android.os.Bundle
import android.renderscript.RenderScript
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_convolution.*
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.util.TextureAvailableListener

class ConvolutionFragment : Fragment() {

    lateinit var viewModel: ConvolutionViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_convolution, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[ConvolutionViewModel::class.java]
        injectViewModel()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        texture_view.surfaceTextureListener = TextureAvailableListener {
            viewModel.setup(Surface(it))
            viewModel.start()
        }
    }

    private fun injectViewModel() {
        val renderScript = RenderScript.create(activity)
        val scriptC_convolution = ScriptC_convolution(renderScript)
        val allocationFactory = AllocationFactory(renderScript)
        viewModel.inject(
                allocationFactory,
                scriptC_convolution,
                resources.openRawResource(R.raw.hok2)
        )
    }


    companion object {
        @JvmStatic
        fun newInstance() = ConvolutionFragment()
    }
}
