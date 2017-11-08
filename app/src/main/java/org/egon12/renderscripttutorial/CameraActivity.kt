package org.egon12.renderscripttutorial

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Surface
import android.view.TextureView
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.activity_camera.*


class CameraActivity : AppCompatActivity(), CameraContract.View, TextureView.SurfaceTextureListener {

    private lateinit var presenter: CameraContract.Presenter

    private val _cameraRequestCode = 19


    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

        //background.execute()
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

        minHue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                var hue = 0F
                try {
                    hue = minHue.text.toString().toFloat()
                } catch (e: NumberFormatException) {
                }

                presenter.setMinHue(hue)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        maxHue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                var hue = 0F
                try {
                    hue = maxHue.text.toString().toFloat()
                } catch (e: NumberFormatException) {
                }

                presenter.setMaxHue(hue)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        switch1.setOnCheckedChangeListener({ compoundButton: CompoundButton?, b: Boolean ->
            presenter.setFilter(b)
        })


    }

    val background = @SuppressLint("StaticFieldLeak")
    object : AsyncTask<Unit, Unit, Bitmap>() {
        override fun doInBackground(vararg p0: Unit?): Bitmap {
            val bitmap = Bitmap.createBitmap(255, 255, Bitmap.Config.ARGB_8888)
            val alloc = Allocation.createFromBitmap(mRs, bitmap)
            val foo = ScriptC_foo(mRs)

            foo.forEach_bar(alloc)

            //alloc.copyTo(bitmap)

            val alloc2 = Allocation.createFromBitmap(mRs, bitmap)

            foo.forEach_invert(alloc, alloc2)
            alloc2.copyTo(bitmap)
            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            //super.onPostExecute(result)
            if (result == null) {
                return
            }
            val surface = Surface(cameraPreview.surfaceTexture)
            val canvas = surface.lockCanvas(null)
            canvas.drawBitmap(result, 0F, 0F, null)
            surface.unlockCanvasAndPost(canvas)


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

            //presenter.setSurface
            presenter.setSurface(surface)


            val handlerThread = HandlerThread("CameraBackground")
            handlerThread.start()
            val handler = Handler(handlerThread.looper)
            presenter.startCamera(handler)
        }
    }
}
