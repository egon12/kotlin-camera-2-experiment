package org.egon12.renderscripttutorial

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import android.view.TextureView
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity(), CameraContract.View {

    private lateinit var presenter: CameraContract.Presenter

    private val _cameraRequestCode = 19

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        presenter = CameraPresenter(this, getSystemService(Context.CAMERA_SERVICE) as CameraManager)

        cameraPreview.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                return true
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                openCamera()
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
            presenter.initCamera()
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

        if (message == "Init Camera Done") {

            val surfaceTexture = cameraPreview.surfaceTexture
            val surface = Surface(surfaceTexture)

            presenter.setSurface(surface)


            val handlerThread = HandlerThread("CameraBackground")
            handlerThread.start()
            val handler = Handler(handlerThread.looper)
            presenter.startCamera(handler)
        }
    }
}
