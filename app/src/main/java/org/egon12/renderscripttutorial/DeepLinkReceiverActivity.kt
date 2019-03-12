package org.egon12.renderscripttutorial

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DeepLinkReceiverActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        // TODO what to do whit this uri
    }

}
