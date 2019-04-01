package org.egon12.renderscripttutorial.video

object H264Extractor {

    fun getBytesPerFrame(byteArray: ByteArray): List<ByteArray> {
        val offset = getOffset(byteArray)
        return getFrames(byteArray, offset)
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