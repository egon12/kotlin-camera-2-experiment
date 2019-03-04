package org.egon12.renderscripttutorial

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SimpleCameraStarter(
        private val cameraDevice: CameraDevice
) : CameraStarter {
    override suspend fun start(surface: Surface) {
        val session = createSession(surface)
        val request = createRequest(session, surface)
        val handler = createHandler()
        session.setRepeatingRequest(request, null, handler)
    }

    private fun createHandler(): Handler {
        val thread = HandlerThread("CaptureThread").apply { start() }
        return Handler(thread.looper)
    }

    private suspend fun createSession(surface: Surface): CameraCaptureSession = suspendCoroutine {
        cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                it.resumeWithException(Exception("Failed create session"))
            }

            override fun onConfigured(session: CameraCaptureSession) {
                it.resume(session)
            }
        }, null)
    }

    private fun createRequest(session: CameraCaptureSession, surface: Surface): CaptureRequest {
        val requestBuilder = session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        requestBuilder.addTarget(surface)
        return requestBuilder.build()
    }
}

