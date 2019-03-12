package org.egon12.renderscripttutorial.camera

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraCharacteristics.*
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_AUTO
import android.hardware.camera2.CameraMetadata.LENS_FACING_BACK
import android.util.Size


class SimpleYuvCameraChooser(private val cameraManager: CameraManager) : CameraChooser {

    private val sizeBelow = Size(1024, 768)

    override fun getSize(cameraId: String): Size {
        val characteristics = cameraManager
                .getCameraCharacteristics(cameraId)

        return getCaptureSizes(characteristics)
                ?: throw Exception("Cannot find size for camera:" + cameraId)
    }


    override fun getCameraId(): String {
        val id = getFirstLensFacingBackCameraId()
        val characteristics = cameraManager.getCameraCharacteristics(id)

        verifyCapability(characteristics)

        return id
    }

    private fun getFirstLensFacingBackCameraId(): String {

        for (cameraId in cameraManager.cameraIdList) {

            val lensFacing = cameraManager
                    .getCameraCharacteristics(cameraId)
                    .get(LENS_FACING)

            val isLensFacingBack = lensFacing == LENS_FACING_BACK

            if (isLensFacingBack) return cameraId
        }

        throw Exception("Device doesn't have back camera.")
    }

    private fun verifyCapability(characteristics: CameraCharacteristics) {
        //verifyHasAutoFocus(characteristics)
        verifyHasYuvOutput(characteristics)
        verifyHasSizeNeeded(characteristics)
    }


    private fun verifyHasAutoFocus(characteristics: CameraCharacteristics) {
        val afModes = characteristics.get(CONTROL_AF_AVAILABLE_MODES)
                ?: throw Exception("Can't get info about auto focus")

        if (!afModes.contains(CONTROL_AF_MODE_AUTO)) {
            throw Exception("Camera doesn't have auto focus")
        }
    }

    private fun verifyHasYuvOutput(characteristics: CameraCharacteristics) {
        val configuration = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP)
                ?: throw RuntimeException("Cannot configuration")

        if (!configuration.outputFormats.contains(ImageFormat.YUV_420_888)) {
            throw Exception("Cannot configure camera to use YUV format")
        }
    }

    private fun verifyHasSizeNeeded(characteristics: CameraCharacteristics) {
        val configuration = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP)
                ?: throw RuntimeException("Cannot configuration")

        val sizeBelowTotal = sizeBelow.width * sizeBelow.height

        val outputSizes = configuration
                .getOutputSizes(ImageFormat.YUV_420_888)
                .filter { it.width * it.height < sizeBelowTotal }

        if (outputSizes.isEmpty()) {
            throw Exception("Camera doesn't have size below " + sizeBelow.width + " x " + sizeBelow.height)
        }
    }


    private fun getCaptureSizes(characteristics: CameraCharacteristics): Size? {
        val configuration = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP)
                ?: throw RuntimeException("Cannot configuration")

        val sizeBelowTotal = sizeBelow.width * sizeBelow.height

        val outputSizes = configuration
                .getOutputSizes(ImageFormat.YUV_420_888)
                .filter { it.width * it.height < sizeBelowTotal }

        if (outputSizes.isEmpty()) {
            throw Exception("Camera doesn't have size below " + sizeBelow.width + " x " + sizeBelow.height)
        }

        return outputSizes.maxBy { it.height }
    }
}