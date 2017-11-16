#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

rs_allocation gCurrentFrame;
float minHue = 0.0;
float maxHue = 0.0;
bool filter = 0;

uchar4 RS_KERNEL filtercolor(uint32_t x, uint32_t y) {
    uchar4 curPixel;

    curPixel.r = rsGetElementAtYuv_uchar_Y(gCurrentFrame, x, y);
    curPixel.g = rsGetElementAtYuv_uchar_U(gCurrentFrame, x, y);
    curPixel.b = rsGetElementAtYuv_uchar_V(gCurrentFrame, x, y);

    float hue = atan2pi((float) curPixel.g - 128, (float) curPixel.b - 128) * 180.f + 15.f;
    if (minHue < hue && hue < maxHue) {
        return rsYuvToRGBA(curPixel.r, curPixel.g, curPixel.b);
    }

    if (filter) {
        curPixel.r = 0;
        curPixel.g = 0;
        curPixel.b = 0;
        curPixel.a = 255;
        return curPixel;
    }

    curPixel.g = curPixel.r;
    curPixel.b = curPixel.r;
    curPixel.a = 255;
    return curPixel;
}
