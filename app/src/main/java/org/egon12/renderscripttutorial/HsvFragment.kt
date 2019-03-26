package org.egon12.renderscripttutorial

import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.renderscript.RenderScript
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.saturation_fragment.*
import org.egon12.renderscripttutorial.camera.CameraFacade
import org.egon12.renderscripttutorial.util.SeekBarListener
import org.egon12.renderscripttutorial.util.TextureAvailableListener


class HsvFragment : Fragment() {

    companion object {
        fun newInstance() = HsvFragment()
    }

    private lateinit var viewModel: HsvViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.saturation_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HsvViewModel::class.java)

        val cameraManager = activity!!.getSystemService(CameraManager::class.java)
        val renderScript = RenderScript.create(activity!!)

        viewModel.inject(CameraFacade(cameraManager), renderScript)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        texture_view.surfaceTextureListener = TextureAvailableListener {
            val surface = Surface(it)
            viewModel.showCameraInSurface(surface)
        }

        hue_force_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.forceHueOverride()
            else viewModel.ignoreHueOverride()
        }

        hue_value_seek_bar.setOnSeekBarChangeListener(SeekBarListener {
            viewModel.setHue(it)
        })

        saturation_force_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.forceSaturationOverride()
            else viewModel.ignoreSaturationOverride()
        }

        saturation_value_seek_bar.setOnSeekBarChangeListener(SeekBarListener {
            viewModel.setSaturation(it)
        })

        value_force_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.forceValueOverride()
            else viewModel.ignoreValueOverride()
        }

        value_value_seek_bar.setOnSeekBarChangeListener(SeekBarListener {
            viewModel.setValue(it)
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopCamera()

    }
}


