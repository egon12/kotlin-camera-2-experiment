package org.egon12.renderscripttutorial.video

import android.media.MediaCodec
import android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM
import android.media.MediaExtractor
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.view.Surface
import kotlinx.android.synthetic.main.activity_surface.*
import org.egon12.renderscripttutorial.R
import org.egon12.renderscripttutorial.SurfaceActivity
import org.junit.Rule
import org.junit.Test

class VideoPlayerTest {

    @get:Rule
    val rule = ActivityTestRule(SurfaceActivity::class.java)

    lateinit var mc1: MediaCodec
    lateinit var me: MediaExtractor

    @Test
    fun listAllCodecs() {

        val context = InstrumentationRegistry.getTargetContext()
        val uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.example)

        me = MediaExtractor()
        me.setDataSource(context, uri, null)
        me.selectTrack(0)

        val mediaFormat = me.getTrackFormat(0)

        val surface = Surface(rule.activity.root.surfaceTexture)
        SystemClock.sleep(1_000L)
//        val mc = MediaCodecWrapper.fromVideoFormat(mediaFormat, surface) ?: throw Exception("FUCK")
        val mc2 = MediaCodecWrapper2.fromMediaExtractor(me, surface)

        while (go(me, mc2)) {
            SystemClock.sleep(40)
        }

        SystemClock.sleep(10_000L)
    }


    private fun go(me: MediaExtractor, mc: MediaCodecWrapper2): Boolean {

        val isEos = me.sampleFlags and BUFFER_FLAG_END_OF_STREAM == BUFFER_FLAG_END_OF_STREAM

        if (!isEos) {

            val result: Boolean = try {
                mc.writeSample(
                        me,
                        me.sampleTime,
                        me.sampleFlags
                )
            } catch (e: MediaCodec.CodecException) {
                println(e.diagnosticInfo)
                false
            }

            if (result) {
                me.advance()
            } else {
                return false
            }
        }
        mc.popSample(true)
        return true
    }


    @Test
    fun playWithMediaPlayer() {
        SystemClock.sleep(1_000L)
        val mediaPlayer = MediaPlayer()
        val context = InstrumentationRegistry.getTargetContext()
        val uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.example)
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.setSurface(Surface(rule.activity.root.surfaceTexture))

        mediaPlayer.prepare()
        mediaPlayer.start()

        SystemClock.sleep(10_000L)

    }

    @Test
    fun play264() {
        val context = InstrumentationRegistry.getTargetContext()

        val inputStream = context.resources.openRawResource(R.raw.example3)
        val ba = ByteArray(128411)
        val size = inputStream.read(ba)

        val offset = mutableListOf<Int>()
        for (i in 0 until ba.size) {
            val one = 0x01
            val zero = 0x00

            if (
                    ba[i] == one.toByte()
                    && ba[i - 1] == zero.toByte()
                    && ba[i - 2] == zero.toByte()
                    && ba[i - 3] == zero.toByte()
            ) {
                offset.add(i - 3)
            }
        }

        val uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.example)

        me = MediaExtractor()
        me.setDataSource(context, uri, null)
        me.selectTrack(0)
        val mediaFormat = me.getTrackFormat(0)
        val surface = Surface(rule.activity.root.surfaceTexture)
        SystemClock.sleep(1_000L)
        val mc = MediaCodecWrapper2.fromMediaExtractor(me, surface)


        for (i in 0 until offset.size - 1) {
            val diff = offset[i + 1] - offset[i]

            val batemp = ba.slice(offset[i] until offset[i + 1])
            var a = false
            while (!a) {
                a = mc.writeSample(batemp.toByteArray())
            }
            mc.popSample(true)
            SystemClock.sleep(40)
        }


//        val mediaFormat = me.getTrackFormat(0)
//
//        val surface = Surface(rule.activity.root.surfaceTexture)
//        SystemClock.sleep(1_000L)
////        val mc = MediaCodecWrapper.fromVideoFormat(mediaFormat, surface) ?: throw Exception("FUCK")
//        val mc2 = MediaCodecWrapper2.fromMediaExtractor(me, surface)
//
//        while (go(me,mc2)) {
//            SystemClock.sleep(40)
//        }
//        SystemClock.sleep(10_000L)

    }

}
//    private fun go(me: MediaExtractor, mc: MediaCodecWrapper) {
//
//        val isEos = me.sampleFlags and BUFFER_FLAG_END_OF_STREAM == BUFFER_FLAG_END_OF_STREAM
//
//        if (!isEos) {
//            val result = mc.writeSample(
//                    me,
//                    me.sampleTime,
//                    me.sampleFlags
//            )
//
//            if (result) {
//                me.advance()
//            }
//        }
//        val info = MediaCodec.BufferInfo()
//        mc.peekSample(info)
//
//        if (info.size <= 0 && isEos) {
//            mc.stopAndRelease()
//            me.release()
//        } else if (info.presentationTimeUs / 1000 < 4000) {
//            mc.popSample(true)
//        }
//        // END_INCLUDE(render_sample)
//
//    }
