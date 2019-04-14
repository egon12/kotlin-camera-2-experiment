#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

float low=0;

float high=360;

rs_allocation input;

static void getYuv(rs_allocation input, uint32_t x, uint32_t y, int* Y, int* u, int* v);

static bool in_range(float r);

uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {
    int Y, u, v;
    getYuv(input, x, y, &Y, &u, &v);

    float r = atan2((float) (u - 127), (float) (v - 127));
    if (r < 0) {
        r += M_PI * 2;
    } else if (r > M_PI * 2) {
        r -= M_PI * 2;
    }

    if (in_range(r)) {
        return rsYuvToRGBA_uchar4(Y, u, v);
    } else {
        return rsYuvToRGBA_uchar4(Y, 127, 127);
    }
}

static void getYuv(rs_allocation input, uint32_t x, uint32_t y, int* Y, int* u, int* v) {
    *Y = rsGetElementAtYuv_uchar_Y(input, x, y);
    *u = rsGetElementAtYuv_uchar_U(input, x, y);
    *v = rsGetElementAtYuv_uchar_V(input, x, y);
}

static bool in_range(float r) {
    if (low < high) {
        return (low < r && r < high);
    } else {
        return (low < r || r < high);
    }
}

