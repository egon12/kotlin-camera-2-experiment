package org.egon12.renderscripttutorial

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog

class CameraActivity : AppCompatActivity(), CameraContract.View {

    private lateinit var presenter: CameraContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
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
}
