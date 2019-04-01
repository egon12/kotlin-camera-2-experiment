package org.egon12.renderscripttutorial.video

import android.media.MediaCodec
import android.media.MediaCodec.*
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaFormat.KEY_MIME
import android.util.Size
import android.view.Surface
import java.io.IOException


/**
 * Recreate MediaCodecWrapper without using deprecated function
 */
class MediaCodecWrapper2 (private var mDecoder: MediaCodec) {


    init {
        mDecoder.start()
    }

    /**
     * Releases resources and ends the encoding/decoding session.
     */
    fun stopAndRelease() {
        mDecoder.stop()
        mDecoder.release()
    }

    fun writeSample(extractor: MediaExtractor,
                    presentationTimeUs: Long,
                    flags: Int): Boolean {

        var flagsEdited = flags

        val index = mDecoder.dequeueInputBuffer(40_000)
        if (index < 0) {
            return false
        }

        val buffer = mDecoder.getInputBuffer(index) ?: return false
        val size = extractor.readSampleData(buffer, 0)
        if (size <= 0) {
            flagsEdited = flagsEdited or BUFFER_FLAG_END_OF_STREAM
        }
        println("size: $size, time: ${extractor.sampleTime}")

        mDecoder.queueInputBuffer(index, 0, size, presentationTimeUs, flagsEdited)

        return true
    }

    fun writeSample(byteArray: ByteArray): Boolean {
        val index = mDecoder.dequeueInputBuffer(40_000)
        if (index < 0) return false
        val buffer = mDecoder.getInputBuffer(index) ?: return false
        buffer.put(byteArray)
        mDecoder.queueInputBuffer(index, 0, byteArray.size, 0, 0)
        return true
    }

    fun popSample(render: Boolean) {
        val info = BufferInfo()
        var index = mDecoder.dequeueOutputBuffer(info, 0)
        while (index != -1) {
            if (index >= 0) mDecoder.releaseOutputBuffer(index, render)
            index = mDecoder.dequeueOutputBuffer(info, 0)
        }
    }


    companion object {

        /**
         * Constructs the [MediaCodecWrapper] wrapper object around the video codec.
         * The codec is created using the encapsulated information in the
         * [MediaFormat] object.
         *
         * @param trackFormat The format of the media object to be decoded.
         * @param surface Surface to render the decoded frames.
         * @return
         */
        @Throws(IOException::class)
        fun fromVideoFormat(trackFormat: MediaFormat, surface: Surface): MediaCodecWrapper2 {
            val videoCodec = createDecoderByType("video/avc")
            videoCodec.configure(trackFormat, surface, null, 0)
            return MediaCodecWrapper2(videoCodec)
        }

        fun fromMediaExtractor(mediaExtractor: MediaExtractor, surface: Surface): MediaCodecWrapper2 {
            val mediaFormat = mediaExtractor.getTrackFormat(0)
            val mimeType = mediaFormat.getString(KEY_MIME)

            if (mimeType != "video/avc") {
                throw Exception("Only support video/avc")
            }

            return fromVideoFormat(mediaFormat, surface)
        }

        fun createH264Decoder(inputSize: Size, surface: Surface): MediaCodecWrapper2 {
            val mediaFormat = MediaFormat
                    .createVideoFormat(
                            MediaFormat.MIMETYPE_VIDEO_AVC,
                            inputSize.width,
                            inputSize.height
                    )

            return fromVideoFormat(mediaFormat, surface)
        }

    }
}
