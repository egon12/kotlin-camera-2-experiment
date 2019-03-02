package org.egon12.renderscripttutorial

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.SystemClock
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import android.util.Size
import android.view.Surface
import java.util.Arrays.asList

/**
 * Camera Presenter
 *
 * This is the complex way to use camera
 */

class CameraPresenter(private val view: CameraContract.View, private val mCameraManager: CameraManager, private val mRenderScript: RenderScript) : CameraContract.Presenter {

    var mCameraDevice: CameraDevice? = null

    var mSurface: Surface? = null

    var mViewSurface: Surface? = null

    var mHandler: Handler? = null

    var scriptC_foo = ScriptC_foo(mRenderScript)

    val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {}

    override fun onViewComplete() {
        this.view.setPresenter(this)
    }

    override fun initCamera(viewSurface: Surface?, handler: Handler) {

        mViewSurface = viewSurface
        mHandler = handler

        try {
            val cameraId = getLensFacingBackCameraId(mCameraManager)

            // check camera capability and recommended size
            val cc = mCameraManager.getCameraCharacteristics(cameraId)
            checkCameraCapability(cc) // should throw error if it doesnt has capability needed by this app
            val size = getCaptureSizes(cc, below = Size(1024, 768)) ?: return

            // get the size
            createAllocation(size)

            // ok open camera and start the callback
            openCamera(mCameraManager, cameraId, {
                view.info("Init Camera Done")
                startCamera(it, handler, true)
            })

        } catch (e: Exception) {
            view.onError("Error on initCamera: " + e.message)
            e.printStackTrace()
            return
        }
    }

    /**
     * how if the device have two camera in back?
     * or have four camera two in front and two in back.
     *
     * Sorry but it has some checking in here.
     */
    private fun getLensFacingBackCameraId(cameraManager: CameraManager): String {

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

    private fun createAllocation(size: Size): Allocation {
        // Create input allocation
        val typeBuilder = Type.Builder(mRenderScript, Element.YUV(mRenderScript))
        typeBuilder.setX(size.width)
        typeBuilder.setY(size.height)
        typeBuilder.setYuvFormat(ImageFormat.YUV_420_888)
        val type = typeBuilder.create()
        val inputAlloc = Allocation.createTyped(mRenderScript, type, Allocation.USAGE_IO_INPUT or Allocation.USAGE_SCRIPT)
        scriptC_foo._gCurrentFrame = inputAlloc
        mSurface = inputAlloc.surface

        // create output allocation
        val outputType = Type.createXY(mRenderScript, Element.RGBA_8888(mRenderScript), size.width, size.height)
        val outputAlloc = Allocation.createTyped(mRenderScript, outputType, Allocation.USAGE_IO_OUTPUT or Allocation.USAGE_SCRIPT)
        outputAlloc.surface = mViewSurface

        // transfer input to output allocation
        inputAlloc.setOnBufferAvailableListener { allocation ->
            allocation.ioReceive()
            scriptC_foo.forEach_yonly(outputAlloc)
            outputAlloc.ioSend()
            SystemClock.sleep(100)

        }

        return inputAlloc
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

    private fun startCamera(cameraDevice: CameraDevice?, handler: Handler, debug: Boolean) {
        if (cameraDevice == null) {
            view.onError("Camera Device is opened but null")
            return
        }

        mCameraDevice = cameraDevice
        val surface = mSurface

        if (surface == null) {
            view.onError("Surface is not set yet! Allocation maybe fail")
            return
        }

        createSession(this, mCameraDevice, asList<Surface>(surface)) {
            val requestBuilder = it?.device?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            requestBuilder?.addTarget(surface)
            val request = requestBuilder?.build() ?: return@createSession

            var captureCallback: CameraCaptureSession.CaptureCallback? = null
            if (debug) captureCallback = mCaptureCallback

            it.setRepeatingRequest(request, captureCallback, handler)
        }

    }

    override fun endCamera() {
        mCameraDevice?.close()

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

    companion object {

        private fun checkCameraCapability(cameraCharacteristics: CameraCharacteristics) {
            //val a = ::class.java
            val afModes = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                    ?: throw Exception("Cannot get camera characteristic")

            if (!afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO)) {
                // comment for now because emulator doesn't have auto focus
//                throw Exception("Camera doesn't have auto focus")
            }
        }

        private fun getCaptureSizes(cameraCharacteristics: CameraCharacteristics, below: Size): Size? {
            val capabilityConfiguration = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: throw RuntimeException("Cannot configuration")

            if (!capabilityConfiguration.outputFormats.contains(ImageFormat.YUV_420_888)) {
                throw Exception("Cannot configure camera to use YUV format")
            }

            val outputSizes = capabilityConfiguration.getOutputSizes(ImageFormat.YUV_420_888)
                    .filter { it.width * it.height < below.width * below.height }
            if (outputSizes.isEmpty()) {
                throw Exception("Camera doesn't have size below " + below.width + " x " + below.height)
            }
            return outputSizes.maxBy { it.height }
        }

        private fun createSession(cameraPresenter: CameraPresenter, cameraDevice: CameraDevice?, surfaceList: List<Surface>, callback: (captureSession: CameraCaptureSession?) -> Unit) {
            cameraDevice?.createCaptureSession(surfaceList, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(p0: CameraCaptureSession?) {
                    callback(p0)
                }

                override fun onConfigureFailed(p0: CameraCaptureSession?) {
                    cameraPresenter.view.onError("Camera configuration failed?")
                }
            }, null)
        }
    }
}