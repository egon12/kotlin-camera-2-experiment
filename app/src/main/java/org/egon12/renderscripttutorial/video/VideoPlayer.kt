package org.egon12.renderscripttutorial.video

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import org.egon12.renderscripttutorial.R

class VideoPlayer(private val context: Context) {


    fun something(surface: Surface) {
        val mediaPlayer = MediaPlayer()
        val uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.example2)

        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.setSurface(surface)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

}
