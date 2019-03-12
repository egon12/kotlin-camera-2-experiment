#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

rs_allocation inputFrame;


bool hForce = 0;
int hForceValue = 0;

bool sForce = 0;
int sMultiplier = 100;

bool vForce = 0;
int vForceValue = 0;


static void rgb2hsv(uchar4 in, int* h, uchar* s, uchar* v);

static uchar4 hsv2rgb(int h, uchar s, uchar v);

static uchar4 getRgb(uint32_t x, uint32_t y);

uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {
    uchar4 in = getRgb(x, y);


    if (x > 320) {
        return in;
    } else {
        int h;
        uchar s, v;
        rgb2hsv(in, &h, &s, &v);

        if (hForce) {
            h = hForceValue;
        }

        if (sForce) {
            s = s * sMultiplier / 100;
        }

        if (vForce) {
            v = vForceValue;
        }


        return hsv2rgb(h, s, v);
    }
}

static uchar4 getRgb(uint32_t x, uint32_t y) {
    int Y = rsGetElementAtYuv_uchar_Y(inputFrame, x, y);
    int u = rsGetElementAtYuv_uchar_U(inputFrame, x, y);
    int v = rsGetElementAtYuv_uchar_V(inputFrame, x, y);
    return rsYuvToRGBA_uchar4(Y, u, v);
}


static void rgb2hsv(uchar4 in, int* h, uchar* s, uchar* v) {
    uchar minRGB = min( in.r, min( in.g, in.b ) );
    uchar maxRGB = max( in.r, max( in.g, in.b ) );
    uchar deltaRGB = maxRGB - minRGB;
    *v = maxRGB;

    if ( deltaRGB <= 0) {
        *h = 0; // undefined ???
        *s = 0;
    } else { // deltaRGB > 0 -> maxRGB > 0
        *s = (255 * deltaRGB) / maxRGB;
        if (in.r >= maxRGB) {
            if( in.g > in.b ) {
                *h = (30 * (in.g - in.b)) / deltaRGB;        // between yellow & magenta
            } else {
                *h = 180 + (30 * (in.g - in.b)) / deltaRGB;
            }
        } else if (in.g >= maxRGB) {
            *h = 60 + (30 * (in.b - in.r)) / deltaRGB;  // between cyan & yellow
        } else {
            *h = 120 + (30 * (in.r - in.g)) / deltaRGB;  // between magenta & cyan
        }
    }
}

static uchar4 hsv2rgb(int h, uchar s, uchar v) {
    uchar4 out;

    out.a = 255;
    if (s == 0) {
        out.r = v;
        out.g = v;
        out.b = v;
        return out;
    }

    float hueSix = h / 360.0;
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

