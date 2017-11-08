package org.egon12.renderscripttutorial

import android.os.Handler
import android.view.Surface

/**
 * Contract between view and Presenter
 */
interface CameraContract {
    interface View {

        fun onError(message: String)

        fun info(message: String)

        fun setPresenter(presenter: Presenter)

    }

    interface Presenter {

        /** Dependecy Injection of needed Service **/
        fun onViewComplete()

        fun initCamera()

        fun setSurface(surface: Surface?)

        fun startCamera(handler: Handler)

        fun setMinHue(hue: Float)

        fun setMaxHue(hue: Float)


    }
}