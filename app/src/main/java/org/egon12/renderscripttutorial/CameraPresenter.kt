package org.egon12.renderscripttutorial

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.view.Surface
import java.util.Arrays.asList

/**
 * Camera Presenter
 *
 * This is the complex way to use camera
 */

class CameraPresenter(private val view: CameraContract.View, private val mCameraManager: CameraManager) : CameraContract.Presenter {

    val mMaxSize = 1024 * 768

    var mImageReader: ImageReader? = null

    var mCameraDevice: CameraDevice? = null

    var mSurface: Surface? = null

    val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            super.onCaptureCompleted(session, request, result)
            Log.v("CameraCaptureComplete", SystemClock.currentThreadTimeMillis().toString())
        }
    }



    override fun onViewComplete() {
        this.view.setPresenter(this)
    }

    override fun initCamera() {

        try {
            val cameraId = getLensFacingBackCamera(mCameraManager)

            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId)

            checkCameraCapability(cameraCharacteristics)

            val size = getCaptureSizes(cameraCharacteristics) ?: return

            mImageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.YUV_420_888, 3)

            mSurface = mImageReader?.surface

            openCamera(mCameraManager, cameraId, callback = {
                mCameraDevice = it
                view.info("Init Camera Done")
            })

        } catch (e: Exception) {
            view.onError(e.message ?: "Error on initCamera")
            return
        }
    }

    override fun setSurface(surface: Surface?) {
        if (surface == null) {
            return
        }
        mSurface = surface
    }

    override fun startCamera(handler: Handler) {
        startCamera(handler, true)
    }

    fun startCamera(handler: Handler, debug: Boolean) {
        createSession(mCameraDevice, asList<Surface>(mSurface), callback = {
            val requestBuilder = it?.device?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            requestBuilder?.addTarget(mSurface)
            val request = requestBuilder?.build()

            var captureCallback: CameraCaptureSession.CaptureCallback? = null
            if (debug) captureCallback = mCaptureCallback

            it?.setRepeatingRequest(request, captureCallback, handler)
        })

    }

    /**
     * how if the device have two camea in back?
     * or have four camera two in front and two in back.
     *
     * Sorry but it has some checking in here.
     *
     *
     */
    fun getLensFacingBackCamera(cameraManager: CameraManager): String {

        var cameraLensFacingNumber = 0
        var cameraLensFacingId = ""

        for (cameraId in cameraManager.cameraIdList) {
            if (cameraManager
                        .getCameraCharacteristics(cameraId)
                        .get(CameraCharacteristics.LENS_FACING) ==
                            CameraCharacteristics.LENS_FACING_BACK) {
                cameraLensFacingId = cameraId
                cameraLensFacingNumber += 1
            }
        }

        if (cameraLensFacingNumber == 0) {
            throw Exception("Device doesn't have back camera.")
        }

        if (cameraLensFacingNumber > 1) {
            throw Exception("Device have more than one back camera. We confuse which to use.")
        }

        return cameraLensFacingId
    }


    private fun checkCameraCapability(cameraCharacteristics: CameraCharacteristics) {

        /*
        val afModes = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
        if (!afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO)) {
            throw Exception("Camera doesn't have auto focus")
        }
        */
    }

    private fun getCaptureSizes(cameraCharacteristics: CameraCharacteristics): Size? {
        val capabilityConfiguration = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        if (!capabilityConfiguration.outputFormats.contains(ImageFormat.YUV_420_888)) {
            throw Exception("Cannot configure camera to use YUV format")
        }

        val outputSizes = capabilityConfiguration.getOutputSizes(ImageFormat.YUV_420_888)
                .filter { it.width * it.height < mMaxSize }
        if (outputSizes.isEmpty()) {
            throw Exception("Camera doesn't have size below 1024 x 768")
        }
        return outputSizes.maxBy { it.height }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(cameraManager: CameraManager, cameraLensFacingId: String, callback: (cameraDevice: CameraDevice?) -> Unit) {
        cameraManager.openCamera(cameraLensFacingId, object : CameraDevice.StateCallback() {
            override fun onOpened(p0: CameraDevice?) {
                if (p0 == null) {
                    view.onError("Camera is null when open? why? I don't know")
                    return
                }
                callback(p0)
            }

            override fun onDisconnected(p0: CameraDevice?) {
                p0?.close()
                view.onError("Camera is disconnected.")
            }

            override fun onError(p0: CameraDevice?, p1: Int) {
                p0?.close()
                when (p1) {
                    ERROR_CAMERA_IN_USE -> view.onError("Error: Camera is in used.")
                    ERROR_MAX_CAMERAS_IN_USE -> view.onError("Error: More than one cameras in use.")
                    ERROR_CAMERA_DISABLED -> view.onError("Error: Camera is disabled.")
                    ERROR_CAMERA_DEVICE -> view.onError("Error: Camera device is error, hardware is broke?")
                    ERROR_CAMERA_SERVICE -> view.onError("Error: Camera service is error? reset your phone?")
                    else -> view.onError("Unknown Error: Cannot open camera")
                }
            }
        }, null)
    }

    private fun createSession(cameraDevice: CameraDevice?, surfaceList: List<Surface>, callback: (captureSession: CameraCaptureSession?) -> Unit) {
        cameraDevice?.createCaptureSession(surfaceList, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(p0: CameraCaptureSession?) {
                callback(p0)
            }

            override fun onConfigureFailed(p0: CameraCaptureSession?) {
                view.onError("Camera configuration failed?")
            }
        }, null)
    }


}