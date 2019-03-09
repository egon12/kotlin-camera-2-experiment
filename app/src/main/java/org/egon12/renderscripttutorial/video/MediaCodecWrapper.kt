package org.egon12.renderscripttutorial.video

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Simplifies the MediaCodec interface by wrapping around the buffer processing operations.
 */
class MediaCodecWrapper private constructor(private var mDecoder: MediaCodec) {

    // Handler to use for {@code OutputSampleListener} and {code OutputFormatChangedListener}
    // callbacks
    private var mHandler: Handler? = null

    /**
     * Getter for the registered [OutputFormatChangedListener]
     */
    var outputFormatChangedListener: OutputFormatChangedListener? = null
        private set

    // References to the internal buffers managed by the codec. The codec
    // refers to these buffers by index, never by reference so it's up to us
    // to keep track of which buffer is which.
    private val mInputBuffers: Array<ByteBuffer>
    private var mOutputBuffers: Array<ByteBuffer>? = null

    // Indices of the input buffers that are currently available for writing. We'll
    // consume these in the order they were dequeued from the codec.
    private val mAvailableInputBuffers: Queue<Int>

    // Indices of the output buffers that currently hold valid data, in the order
    // they were produced by the codec.
    private val mAvailableOutputBuffers: Queue<Int>

    // Information about each output buffer, by index. Each entry in this array
    // is valid if and only if its index is currently contained in mAvailableOutputBuffers.
    private var mOutputBufferInfo: Array<MediaCodec.BufferInfo>


    // Callback when media output format changes.
    interface OutputFormatChangedListener {
        fun outputFormatChanged(sender: MediaCodecWrapper, newFormat: MediaFormat)
    }

    /**
     * Callback for decodes frames. Observers can register a listener for optional stream
     * of decoded data
     */
    interface OutputSampleListener {
        fun outputSample(sender: MediaCodecWrapper, info: MediaCodec.BufferInfo, buffer: ByteBuffer)
    }

    init {
        mDecoder.start()
        mInputBuffers = mDecoder.getInputBuffers()
        mOutputBuffers = mDecoder.getOutputBuffers()

        mOutputBufferInfo = Array(mOutputBuffers!!.size) { MediaCodec.BufferInfo() }
        mAvailableInputBuffers = ArrayDeque(mOutputBuffers!!.size)
        mAvailableOutputBuffers = ArrayDeque(mInputBuffers.size)
    }

    /**
     * Releases resources and ends the encoding/decoding session.
     */
    fun stopAndRelease() {
        mDecoder.stop()
        mDecoder.release()
//        mDecoder = null
        mHandler = null
    }

    /**
     * Write a media sample to the decoder.
     *
     * A "sample" here refers to a single atomic access unit in the media stream. The definition
     * of "access unit" is dependent on the type of encoding used, but it typically refers to
     * a single frame of video or a few seconds of audio. [android.media.MediaExtractor]
     * extracts data from a stream one sample at a time.
     *
     * @param extractor  Instance of [android.media.MediaExtractor] wrapping the media.
     *
     * @param presentationTimeUs The time, relative to the beginning of the media stream,
     * at which this buffer should be rendered.
     *
     * @param flags  Flags to pass to the decoder. See [MediaCodec.queueInputBuffer]
     *
     * @throws MediaCodec.CryptoException
     */
    fun writeSample(extractor: MediaExtractor,
                    presentationTimeUs: Long,
                    flags: Int): Boolean {
        var flags = flags
        var result = false

        if (!mAvailableInputBuffers.isEmpty()) {
            val index = mAvailableInputBuffers.remove()
            val buffer = mInputBuffers[index]

            // reads the sample from the file using extractor into the buffer
            val size = extractor.readSampleData(buffer, 0)
            if (size <= 0) {
                flags = flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
            }

            // Submit the buffer to the codec for decoding. The presentationTimeUs
            // indicates the position (play time) for the current sample.
            mDecoder.queueInputBuffer(index, 0, size, presentationTimeUs, flags)

            result = true
        }
        return result
    }

    /**
     * Performs a peek() operation in the queue to extract media info for the buffer ready to be
     * released i.e. the head element of the queue.
     *
     * @param out_bufferInfo An output var to hold the buffer info.
     *
     * @return True, if the peek was successful.
     */
    fun peekSample(out_bufferInfo: MediaCodec.BufferInfo): Boolean {
        // dequeue available buffers and synchronize our data structures with the codec.
        update()
        var result = false
        if (!mAvailableOutputBuffers.isEmpty()) {
            val index = mAvailableOutputBuffers.peek()
            val info = mOutputBufferInfo[index]
            // metadata of the sample
            out_bufferInfo.set(
                    info.offset,
                    info.size,
                    info.presentationTimeUs,
                    info.flags)
            result = true
        }
        return result
    }

    /**
     * Processes, releases and optionally renders the output buffer available at the head of the
     * queue. All observers are notified with a callback. See [ ][OutputSampleListener.outputSample]
     *
     * @param render True, if the buffer is to be rendered on the [Surface] configured
     */
    fun popSample(render: Boolean) {
        // dequeue available buffers and synchronize our data structures with the codec.
        update()
        if (!mAvailableOutputBuffers.isEmpty()) {
            val index = mAvailableOutputBuffers.remove()

            // releases the buffer back to the codec
            mDecoder.releaseOutputBuffer(index, render)
        }
    }

    /**
     * Synchronize this object's state with the internal state of the wrapped
     * MediaCodec.
     */
    private fun update() {
        // BEGIN_INCLUDE(update_codec_state)
        var index: Int

        // Get valid input buffers from the codec to fill later in the same order they were
        // made available by the codec.
        index = mDecoder.dequeueInputBuffer(0)
        while (index != MediaCodec.INFO_TRY_AGAIN_LATER) {
            mAvailableInputBuffers.add(index)
            index = mDecoder.dequeueInputBuffer(0)
        }


        // Likewise with output buffers. If the output buffers have changed, start using the
        // new set of output buffers. If the output format has changed, notify listeners.
        val info = MediaCodec.BufferInfo()
        index = mDecoder.dequeueOutputBuffer(info, 0)

        while (index != MediaCodec.INFO_TRY_AGAIN_LATER) {
            when (index) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                    mOutputBuffers = mDecoder.outputBuffers
                    mOutputBufferInfo = Array(mOutputBuffers!!.size) { MediaCodec.BufferInfo() }
                    mAvailableOutputBuffers.clear()
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> if (outputFormatChangedListener != null) {
                    mHandler!!.post {
                        outputFormatChangedListener!!
                                .outputFormatChanged(this@MediaCodecWrapper,
                                        mDecoder.outputFormat)
                    }
                }
                else ->
                    if (index >= 0) {
                        mOutputBufferInfo[index] = info
                        mAvailableOutputBuffers.add(index)
                    } else {
                        throw IllegalStateException("Unknown status from dequeueOutputBuffer")
                    }
            }
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
        fun fromVideoFormat(trackFormat: MediaFormat,
                            surface: Surface): MediaCodecWrapper? {
            var result: MediaCodecWrapper? = null
            var videoCodec: MediaCodec? = null

            val mimeType = trackFormat.getString(MediaFormat.KEY_MIME)

            if (mimeType.contains("video/")) {
                videoCodec = MediaCodec.createDecoderByType(mimeType)
                videoCodec!!.configure(trackFormat, surface, null, 0)

            }

            if (videoCodec != null) {
                result = MediaCodecWrapper(videoCodec)
            }

            return result
        }

    }
}
