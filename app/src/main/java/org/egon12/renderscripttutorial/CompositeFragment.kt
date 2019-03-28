package org.egon12.renderscripttutorial

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.renderscript.RenderScript
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_composite.*
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.camera.CameraFacade
import org.egon12.renderscripttutorial.util.TextureAvailableListener

class CompositeFragment : Fragment() {

    lateinit var renderScript: RenderScript

    lateinit var rsComposite: ScriptC_composite

    lateinit var viewModel: CompositeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_composite, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        renderScript = RenderScript.create(activity)

        rsComposite = ScriptC_composite(renderScript)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CompositeViewModel::class.java]
        ViewModelInjector(activity as Context).inject(viewModel)
        viewModel.setup()
        texture_view.surfaceTextureListener = TextureAvailableListener {
            viewModel.setupOutputAllocation(Surface(it))
            viewModel.start()
        }
    }


    private class ViewModelInjector(private val context: Context) {

        fun inject(viewModel: CompositeViewModel) {
            val cameraManager = context.getSystemService(CameraManager::class.java)
            val cameraFacade = CameraFacade(cameraManager)
            val renderScript = RenderScript.create(context)
            val allocationFactory = AllocationFactory(renderScript)
            val scriptC = ScriptC_composite(renderScript)
            val inputStream = context.resources.openRawResource(R.raw.roto4)
            viewModel.inject(
                    cameraFacade,
                    allocationFactory,
                    scriptC,
                    inputStream
            )
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = CompositeFragment()
    }

}
