package org.egon12.renderscripttutorial

import android.animation.TimeAnimator
import android.annotation.SuppressLint
import android.media.MediaFormat
import android.renderscript.Allocation
import android.util.Size
import android.view.Surface
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.camera.CameraFacade
import org.egon12.renderscripttutorial.video.MediaCodecWrapper2
import java.io.InputStream

class CompositeViewModel : ViewModel() {

    private lateinit var cameraFacade: CameraFacade

    private lateinit var scriptC_composite: ScriptC_composite

    private lateinit var allocationFactory: AllocationFactory

    private lateinit var inputStream: InputStream

    private lateinit var frames: List<Frame>

    private lateinit var codec: MediaCodecWrapper2

    private var timeAnimator = TimeAnimator()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val noScope = CoroutineScope(Dispatchers.Unconfined)


    fun inject(
            cameraFacade: CameraFacade,
            allocationFactory: AllocationFactory,
            scriptC_composite: ScriptC_composite,
            inputStream: InputStream
    ) {
        this.cameraFacade = cameraFacade
        this.allocationFactory = allocationFactory
        this.scriptC_composite = scriptC_composite
        this.inputStream = inputStream
    }

    // allocation for camera
    private lateinit var bottomAllocation: Allocation

    // allocation for video
    private lateinit var topAllocation: Allocation

    // allocation for output
    private lateinit var outputAllocation: Allocation

    fun setup() {
        setupCameraAllocation()

        val videoSize = Size(320, 240)

        setupVidioAllocation(videoSize)

        setupCodec(videoSize)

        prepareFile()
    }

    private fun prepareFile() {
        val bytes = inputStream.readBytes()
        val offsets = getOffset(bytes)
        frames = getFrames(bytes, offsets)
    }

    private fun getFrames(bytes: ByteArray, offsets: List<Int>): List<Frame> {

        val result = mutableListOf<ByteArray>()

        for (i in 0 until offsets.size - 1) {
            val b = bytes.sliceArray(offsets[i] until offsets[i + 1])
            result.add(b)
        }

        val lastFrame = bytes.sliceArray(offsets.last() until bytes.size)
        result.add(lastFrame)

        return result.map { Frame(it) }
    }

    class Frame(val bytes: ByteArray)

    private fun getOffset(bytes: ByteArray): List<Int> {
        val offset = mutableListOf<Int>()
        for (i in 0 until bytes.size) {
            val one = 0x01
            val zero = 0x00

            if (
                    bytes[i] == one.toByte()
                    && bytes[i - 1] == zero.toByte()
                    && bytes[i - 2] == zero.toByte()
                    && bytes[i - 3] == zero.toByte()
            ) {
                offset.add(i - 3)
            }
        }
        return offset
    }

    private fun setupCodec(videoSize: Size) {
        val mediaFormat = MediaFormat
                .createVideoFormat(
                        MediaFormat.MIMETYPE_VIDEO_AVC,
                        videoSize.width,
                        videoSize.height
                )
        codec = MediaCodecWrapper2
                .fromVideoFormat(mediaFormat, topAllocation.surface)
    }

    private fun setupVidioAllocation(videoSize: Size) {
        topAllocation = allocationFactory.createYuvInputAllocation(videoSize)
        scriptC_composite._topFrame = topAllocation
    }

    private fun setupCameraAllocation() {
        val id = cameraFacade.getCameraId()
        val size = cameraFacade.getSize(id)
        bottomAllocation = allocationFactory.createYuvInputAllocation(size)
        scriptC_composite._bottomFrame = bottomAllocation
    }


    fun setupOutputAllocation(surface: Surface) {
        val id = cameraFacade.getCameraId()
        val size = cameraFacade.getSize(id)
        outputAllocation = allocationFactory.createOutputAllocation(size, surface)
    }

    @SuppressLint("MissingPermission")
    private fun startCamera() {
        cameraFacade.showCameraInSurface(bottomAllocation.surface)
    }


    val channelFromBottom = Channel<Unit>(1)
    val channelFromTop = Channel<Unit>(1)
    private var frameNumber = 0
    fun start() {

        startCamera()

        bottomAllocation.setOnBufferAvailableListener {

            ioScope.launch {
                println("bottom sendBlocking")
                channelFromBottom.send(Unit)
                println("done bottom sendBlocking")
            }
        }

        timeAnimator.setTimeListener { _, _, _ ->


                if (frameNumber >= frames.size) {
                    frameNumber = 0
                }

                codec.writeSample(frames[frameNumber].bytes)
                frameNumber++
                codec.popSample(true)

        }

        topAllocation.setOnBufferAvailableListener {

            noScope.launch {
                println("top send Blocking")
                channelFromTop.send(Unit)
                println("done top sendBlocking")
            }

        }

        timeAnimator.start()


        MainScope().launch {

            while(true) {
                channelFromBottom.receive()

                println("bottom get")
                channelFromTop.receive()
                println("top get")

                bottomAllocation.ioReceive()
                topAllocation.ioReceive()

                scriptC_composite.forEach_process(outputAllocation)

                outputAllocation.ioSend()
            }
        }

    }

    fun setAlpha(value: Int) {
        scriptC_composite._alpha = value.toFloat() / 100.0F
    }


}