package org.egon12.renderscripttutorial

import android.animation.TimeAnimator
import android.renderscript.Allocation
import android.renderscript.ScriptIntrinsicConvolve3x3
import android.util.Size
import android.view.Surface
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withTimeoutOrNull
import org.egon12.renderscripttutorial.allocation.AllocationFactory
import org.egon12.renderscripttutorial.video.MediaCodecWrapper2
import java.io.InputStream

class ConvolutionViewModel : ViewModel() {

    lateinit var allocationFactory: AllocationFactory

    lateinit var scriptIntrinsicConvolve3x3: ScriptIntrinsicConvolve3x3

    lateinit var scriptC: ScriptC_convolution

    lateinit var inputAllocation: Allocation

    lateinit var outputAllocation: Allocation

    private val timeAnimator = TimeAnimator()

    lateinit var inputStream: InputStream

    lateinit var codec: MediaCodecWrapper2

    lateinit var frames: List<ByteArray>


    fun inject(
            allocationFactory: AllocationFactory,
            scriptC_convolution: ScriptC_convolution,
            inputStream: InputStream
    ) {
        this.allocationFactory = allocationFactory
        this.scriptC = scriptC_convolution
        this.inputStream = inputStream
        setMatrix()
    }

    private fun prepareFile() {
        val ba = inputStream.readBytes()
        val offset = getOffset(ba)
        frames = getFrames(ba, offset)
    }

    private fun getFrames(bytes: ByteArray, offsets: List<Int>): List<ByteArray> {

        val result = mutableListOf<ByteArray>()

        for (i in 0 until offsets.size - 1) {
            val b = bytes.sliceArray(offsets[i] until offsets[i + 1])
            result.add(b)
        }

        val lastFrame = bytes.sliceArray(offsets.last() until bytes.size)
        result.add(lastFrame)

        return result
    }

    fun setup(surface: Surface) {

        prepareFile()

        val inputSize = Size(288, 480)
        //val inputSize = Size(320, 240)

        inputAllocation = allocationFactory.createYuvInputAllocation(inputSize)

        scriptC._input = inputAllocation
        scriptC.invoke_set_size(inputSize.width, inputSize.height)

        codec = MediaCodecWrapper2
                .createH264Decoder(inputSize, inputAllocation.surface)

        outputAllocation = allocationFactory.createOutputAllocation(inputSize, surface)
    }

    fun setMatrix() {
        val matrixInt = arrayOf(
                1, 1, 1,
                1, 1, 1,
                1, 1, 1
        )

//        val matrixInt = arrayOf(
//                1, 1, 1, 1, 1,
//                1, 1, 1, 1, 1,
//                1, 1, 1, 1, 1,
//                1, 1, 1, 1, 1
//        )
//
//        val matrixInt = (0 until 81).map { 1 }.toIntArray()

//        val matrixInt = arrayOf(
//                 0,  1, 0,
//                -1,  0, 1,
//                 0, -1, 0
//        )

//        val matrixInt = arrayOf(
//                -1, 0, 1,
//                -2, 0, 2,
//                -1, 0, 1
//        )

        scriptC._divider = 1

        val matrix = matrixInt.map { it.toFloat() }


        val matrixSize = Math.sqrt(matrix.size.toDouble()).toInt()

        scriptC.invoke_reset_matrix(matrixSize)
        for (i in 0 until (matrix.size -1)) {
            scriptC.invoke_add_matrix_number(matrix[i])
        }
        scriptC.invoke_end_edit_matrix()
    }


    var frameNumber = 0
    fun start() {

        timeAnimator.setTimeListener { _, _, _ ->


            if (frameNumber >= frames.size) {
                frameNumber = 0
            }

            codec.writeSample(frames[frameNumber])
            frameNumber++
            codec.popSample(true)


        }

        inputAllocation.setOnBufferAvailableListener {
            inputAllocation.ioReceive()
            scriptC.forEach_process(outputAllocation)
            outputAllocation.ioSend()
        }

        timeAnimator.start()

    }


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

}
