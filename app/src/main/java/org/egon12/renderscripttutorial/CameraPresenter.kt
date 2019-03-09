package org.egon12.renderscripttutorial

import android.graphics.ImageFormat
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.SystemClock
import android.renderscript.Allocation
import android.renderscript.Allocation.*
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import androidx.annotation.RequiresPermission
import android.util.Size
import android.view.Surface
import org.egon12.renderscripttutorial.camera.CameraFacade
import org.egon12.renderscripttutorial.video.VideoPlayer

/**
 * Camera Presenter
 *
 * This is the complex way to use camera
 */

class CameraPresenter(
        private val view: CameraContract.View,
        cameraManager: CameraManager,
        private val videoPlayer: VideoPlayer,
        private val mRenderScript: RenderScript
) : CameraContract.Presenter {

    var mViewSurface: Surface? = null

    var mHandler: Handler? = null

    private var scriptC_foo = ScriptC_foo(mRenderScript)

    private var scriptC_video = ScriptC_video(mRenderScript)

    private val cameraFacade = CameraFacade(cameraManager)

    override fun onViewComplete() {
        this.view.setPresenter(this)
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    override fun initCamera(viewSurface: Surface?, handler: Handler) {

        mViewSurface = viewSurface
        mHandler = handler

        try {

            val cameraId = cameraFacade.getCameraId()

            val size = cameraFacade.getSize(cameraId)

            val inputAlloc = createInputAllocation2(Size(320, 240))

            val outputAlloc = createOutputAllocation(Size(320, 240))

            inputAlloc.setOnBufferAvailableListener { allocation ->
                allocation.ioReceive()
//                scriptC_foo.forEach_yonly(outputAlloc)
                scriptC_video.forEach_process(outputAlloc)
//                scriptC_video.forEach_process(outputAlloc)
                outputAlloc.ioSend()
            }


            videoPlayer.something(inputAlloc.surface)
//            cameraFacade.showCameraInSurface(inputAlloc.surface)

        } catch (e: Exception) {
            view.onError("Error on initCamera: " + e.message)
            e.printStackTrace()
            return
        }
    }


    override fun endCamera() {
//        cameraFacade.stop()
    }

    override fun setMinHue(hue: Float) {
        scriptC_foo._minHue = hue
    }

    override fun setMaxHue(hue: Float) {
        scriptC_foo._maxHue = hue
    }

    override fun setFilter(fil: Boolean) {
        scriptC_foo._filter = fil
    }

    private fun createInputAllocation(size: Size): Allocation {
        val type = Type
                .Builder(mRenderScript, Element.YUV(mRenderScript))
                .setX(size.width)
                .setY(size.height)
                .setYuvFormat(ImageFormat.YUV_420_888)
                .create()

        val inputAlloc = createTyped(
                mRenderScript,
                type,
                USAGE_IO_INPUT or USAGE_SCRIPT
        )

        scriptC_foo._gCurrentFrame = inputAlloc

        return inputAlloc
    }

    private fun createInputAllocation2(size: Size): Allocation {
        val type = Type
                .Builder(mRenderScript, Element.YUV(mRenderScript))
                .setX(size.width)
                .setY(size.height)
                .setYuvFormat(ImageFormat.YUV_420_888)
                .create()

        val inputAlloc = createTyped(
                mRenderScript,
                type,
                USAGE_IO_INPUT or USAGE_SCRIPT
        )

        scriptC_video._inputFrame = inputAlloc

//        scriptC_foo._gCurrentFrame = inputAlloc

        return inputAlloc
    }

    private fun createOutputAllocation(size: Size): Allocation {
        val outputType = Type.createXY(
                mRenderScript,
                Element.RGBA_8888(mRenderScript),
                size.width,
                size.height
        )
        val outputAlloc = createTyped(
                mRenderScript,
                outputType,
                USAGE_IO_OUTPUT or USAGE_SCRIPT
        )
        outputAlloc.surface = mViewSurface
        return outputAlloc
    }
}