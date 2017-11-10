package org.egon12.renderscripttutorial

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.RenderScript
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import android.view.TextureView
import android.widget.CompoundButton
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_camera.*


class CameraActivity : AppCompatActivity(), CameraContract.View, TextureView.SurfaceTextureListener {

    private lateinit var presenter: CameraContract.Presenter

    private val _cameraRequestCode = 19


    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        presenter.endCamera()
        return true
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, width: Int, height: Int) {
        openCamera()

        val rotation = this.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val bufferRect = RectF(0f, 0f, 720F, 544F)
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_0 == rotation || Surface.ROTATION_180 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            /*
            matrix.setRectToRect(bufferRect, viewRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                    width.toFloat() / 720F,
                    height.toFloat() / 544F)
            matrix.postScale(scale, scale, centerX, centerY)
            */
            matrix.postRotate(90 * (rotation + 1F), centerX, centerY)
        }
        cameraPreview.setTransform(matrix)
    }


    var mRs: RenderScript? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        mRs = RenderScript.create(this)
        if (mRs == null) {
            onError("Render Script is not initialized")
        }
        presenter = CameraPresenter(this, getSystemService(Context.CAMERA_SERVICE) as CameraManager, mRs!!)

        cameraPreview.surfaceTextureListener = this

        minHue.setOnSeekBarChangeListener(onSeekbarChanged)
        maxHue.setOnSeekBarChangeListener(onSeekbarChanged)

        switch1.setOnCheckedChangeListener({ compoundButton: CompoundButton?, b: Boolean ->
            presenter.setFilter(b)
        })

    }

    val onSeekbarChanged = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(p0: SeekBar?) {}

        override fun onStopTrackingTouch(p0: SeekBar?) {}

        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            if (p0 == null) {
                return
            }

            when (p0.id) {
                R.id.minHue -> presenter.setMinHue((p1 - 200).toFloat())
                R.id.maxHue -> presenter.setMaxHue((p1 - 200).toFloat())
            }
        }
    }


    fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), _cameraRequestCode)
            }
        } else {
            val surfaceTexture = cameraPreview.surfaceTexture
            val surface = Surface(surfaceTexture)
            val handlerThread = HandlerThread("CameraBackground")
            handlerThread.start()
            val handler = Handler(handlerThread.looper)
            presenter.initCamera(surface, handler)
        }
    }


    override fun setPresenter(presenter: CameraContract.Presenter) {
        this.presenter = presenter
    }

    override fun onError(message: String) {
        AlertDialog.Builder(this).setMessage(message).create().show()
    }

    override fun info(message: String) {
        AlertDialog.Builder(this).setMessage(message).create().show()
    }

    override fun onPause() {
        presenter.endCamera()
        super.onPause()
    }
}
