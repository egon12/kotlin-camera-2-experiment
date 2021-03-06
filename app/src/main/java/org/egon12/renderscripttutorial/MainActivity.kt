package org.egon12.renderscripttutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment = when(intent.dataString) {
            "hsv" -> HsvFragment.newInstance()
            "rs_input" -> RsInputFragment.newInstance()
            "composite" -> CompositeFragment.newInstance()
            "convolution" -> ConvolutionFragment.newInstance()
            "colorfilter" -> ColorFilterFragment.newInstance()
            "pt" -> PerspectiveTransformFragment()
            "simpleBlur" -> SimpleBlurFragment()
            "rotateY" -> RotateYFragment()
            "glsl" -> GlslFragment()
            else -> HsvFragment.newInstance()
        }

        supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
    }
}
