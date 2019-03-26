package org.egon12.renderscripttutorial.allocation

import android.graphics.ImageFormat
import android.renderscript.Allocation
import android.renderscript.Allocation.*
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import android.util.Size
import android.view.Surface

class AllocationFactory(
        private val renderScript: RenderScript
) {

    fun createYuvInputAllocation(size: Size): Allocation {

        val type = Type
                .Builder(renderScript, Element.YUV(renderScript))
                .setX(size.width)
                .setY(size.height)
                .setYuvFormat(ImageFormat.YUV_420_888)
                .create()

        return createTyped(
                renderScript,
                type,
                USAGE_IO_INPUT or USAGE_SCRIPT
        )
    }

    fun createRgbInputAllocation(size: Size): Allocation {
        val type = Type.createXY(
                renderScript,
                Element.RGBA_8888(renderScript),
                size.width,
                size.height
        )

        return createTyped(
                renderScript,
                type,
                USAGE_IO_INPUT or USAGE_SCRIPT
        )
    }

    fun createOutputAllocation(size: Size, surface: Surface): Allocation {
        val type = Type.createXY(
                renderScript,
                Element.RGBA_8888(renderScript),
                size.width,
                size.height
        )
        val outputAlloc = createTyped(
                renderScript,
                type,
                USAGE_IO_OUTPUT or USAGE_SCRIPT
        )

        outputAlloc.surface = surface

        return outputAlloc
    }

    fun createYuvOutputAllocation(size: Size, surface: Surface): Allocation {

        val type = Type
                .Builder(renderScript, Element.YUV(renderScript))
                .setX(size.width)
                .setY(size.height)
                .setYuvFormat(ImageFormat.YUV_420_888)
                .create()

        val outputAlloc = createTyped(
                renderScript,
                type,
                USAGE_IO_OUTPUT or USAGE_SCRIPT
        )

        outputAlloc.surface = surface
        return outputAlloc
    }

}
