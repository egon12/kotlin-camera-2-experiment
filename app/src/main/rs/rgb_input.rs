#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

uint32_t frame;

static void rgb2hsv(uchar4 in, float* h, float* s, uchar* v);

static uchar4 hsv2rgb(float h, float s, uchar v);

static uchar4 processRGBStandard(uint32_t x, uint32_t y);

static uchar4 processRGBWidth(uint32_t x, uint32_t y);

static uchar4 processHSV(uint32_t x, uint32_t y);

static uchar4 black();

uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {
    return processRGBStandard(x, y);
    //return processRGBWidth(x, y);
    //return processHSV(x, y);
}


static uchar4 processRGBStandard(uint32_t x, uint32_t y) {
    uchar4 c;
    c.r = 255;
    c.g = 0;
    c.b = 0;
    c.a = 255;
    return c;
}

static uchar4 processRGBWidth(uint32_t x, uint32_t y) {
    int r = x * 255 / 512;
    uchar4 c;
    c.r = r;
    c.g = 0;
    c.b = 0;
    c.a = 255;
    return c;
}

static uchar4 processHSV(uint32_t x, uint32_t y) {
    float h, s, v;
    h = x * 360 / 512;
    s = 255;
    //s = y * 255 / 512;
    return hsv2rgb(h, s, 255);
}


static void rgb2hsv(uchar4 in, float* h, float* s, uchar* v) {
    uchar minRGB = min( in.r, min( in.g, in.b ) );
    uchar maxRGB = max( in.r, max( in.g, in.b ) );
    uchar deltaRGB = maxRGB - minRGB;
    *v = maxRGB;

    if (deltaRGB == 0) {
        *h = 0.0;
        *s = 0.0;
        return;
    }

    *s = 255 * deltaRGB / maxRGB;

    float hueSixth;
    if (in.r >= maxRGB) {
        hueSixth = (in.g - in.b) / deltaRGB;
        if (hueSixth < 0.f) hueSixth += 6.f;
    } else if (in.g >= maxRGB) {
        hueSixth = 2.f + (in.b - in.r) / deltaRGB;
    } else {
        hueSixth = 4.f + (in.r - in.g) / deltaRGB;
    }
    *h = 360.0 * hueSixth / 6.f;
}

static uchar4 hsv2rgb(float h, float s, uchar v) {
    uchar4 out;

    out.a = 255;
    if (s == 0) {
        out.r = v;
        out.g = v;
        out.b = v;
        return out;
    }

    float hueSix = h / 60.0;
    int hueSixCategory = (int) hueSix;
    float hueSixReminder = hueSix - hueSixCategory;
    float saturationNorm = s / 255.0;
    uchar maxRGB = v;

    // minRGB is also called p
    uchar minRGB = (1.0 - saturationNorm) * v;
    uchar q = (1.0 - saturationNorm * hueSixReminder) * v;
    uchar t = (1.0 - saturationNorm * (1.0 - hueSixReminder)) * v;

    switch(hueSixCategory) {
        case 6:
        case 0:
            out.r = maxRGB;
            out.g = t;
            out.b = minRGB;
            break;
        case 1:
            out.r = q;
            out.g = maxRGB;
            out.b = minRGB;
            break;
        case 2:
            out.r = minRGB;
            out.g = maxRGB;
            out.b = t;
            break;

        case 3:
            out.r = minRGB;
            out.g = q;
            out.b = maxRGB;
            break;
        case 4:
            out.r = t;
            out.g = minRGB;
            out.b = maxRGB;
            break;
        case 5:
            out.r = maxRGB;
            out.g = minRGB;
            out.b = q;
            break;
        default:
            out.r = 0;
            out.g = 0;
            out.b = 0;

    }
    return out;
}


static uchar4 black() {
    uchar4 result;
    result.r = 0;
    result.g = 0;
    result.b = 0;
    result.a = 255;
    return result;
}


