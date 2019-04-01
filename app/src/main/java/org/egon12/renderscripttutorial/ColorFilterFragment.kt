package org.egon12.renderscripttutorial

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_color_filter.*
import org.egon12.renderscripttutorial.util.SeekBarListener
import org.egon12.renderscripttutorial.util.TextureAvailableListener

class ColorFilterFragment : Fragment() {

    lateinit var viewModel: ColorFilterViewModel

    lateinit var clipboardManager: ClipboardManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_color_filter, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[ColorFilterViewModel::class.java]
        viewModel.inject(activity!!)

        clipboardManager = activity!!.getSystemService(ClipboardManager::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        texture_view.surfaceTextureListener = TextureAvailableListener {
            viewModel.setup(Surface(it))
            viewModel.start()
        }

        texture_view.setOnClickListener {
            val (low, high) = viewModel.getFilterValue()
            val str = "low=$low;high=$high"
            Toast.makeText(activity, str, Toast.LENGTH_LONG).show()
            clipboardManager.primaryClip = ClipData.newPlainText("Filter value", str)
        }

        low_seek_bar.setOnSeekBarChangeListener(SeekBarListener {
            viewModel.setLow(it)
        })

        high_seek_bar.progress = 100
        high_seek_bar.setOnSeekBarChangeListener(SeekBarListener {
            viewModel.setHigh(it)
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }


    companion object {
        @JvmStatic
        fun newInstance() = ColorFilterFragment()
    }
}
