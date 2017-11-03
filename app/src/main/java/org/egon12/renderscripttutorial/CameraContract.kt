package org.egon12.renderscripttutorial

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler

/**
 * Created by egon on 11/3/17.
 */
interface CameraContract {
    interface View {

        fun onError(message: String)

        fun info(message: String)

        fun setPresenter(presenter: Presenter)

    }

    interface Presenter  {

        /** Dependecy Injection of needed Service **/
        fun onViewComplete()

        fun initCamera()

        fun startCamera(handler: Handler)








    }
}