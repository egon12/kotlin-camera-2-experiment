#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

uint32_t frame;

static void rgb2hsv(uchar4 in, float* h, float* s, uchar* v);

static uchar4 black();

static uchar4 yuvColorSpace(uint32_t x, uint32_t y, uint32_t z) {

    if (x > 512 || y > 512)  {
        uchar4 result;
        return result;
    }

    int Y = z % 255;
    int u = y/2;
    int v = x/2;

    uchar4 in = rsYuvToRGBA_uchar4(Y, u, v);

    float h, s;
    uchar b;

    rgb2hsv(in, &h, &s, &b);

    if (h > 350 || h < 20) return in;
    return black();
}


uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {
    return yuvColorSpace(x, y, frame);
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

static uchar4 black() {
    uchar4 result;
    result.r = 0;
    result.g = 0;
    result.b = 0;
    result.a = 255;
    return result;
}


