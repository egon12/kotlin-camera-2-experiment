package org.egon12.renderscripttutorial


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_perspective_transform.*
import org.egon12.renderscripttutorial.util.TextureAvailableListener

class PerspectiveTransformFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_perspective_transform, container, false)
    }

    private lateinit var viewModel: PerspectiveTransformViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[PerspectiveTransformViewModel::class.java]

        viewModel.inject(activity!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        texture_view.surfaceTextureListener = TextureAvailableListener {

            viewModel.setup(Surface(it))
            viewModel.start()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }


}
