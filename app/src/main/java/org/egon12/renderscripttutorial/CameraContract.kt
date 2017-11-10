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

        fun initCamera(viewSurface: Surface?, handler: Handler)

        fun endCamera()

        fun setMinHue(hue: Float)

        fun setMaxHue(hue: Float)

        fun setFilter(fil: Boolean)


    }
}