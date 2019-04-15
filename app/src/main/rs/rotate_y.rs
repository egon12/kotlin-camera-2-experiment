#pragma version(1)

#pragma rs java_package_name(org.egon12.renderscripttutorial)

rs_allocation input;
uint32_t input_width;
uint32_t input_height;

rs_matrix3x3 trans;

static void getYuv(uint32_t x, uint32_t y, int *Y, int *u, int *v);

void debugTrans() {
    rsDebug("m0", trans.m[0]);
    rsDebug("m1", trans.m[1]);
    rsDebug("m2", trans.m[2]);

    rsDebug("m3", trans.m[3]);
    rsDebug("m4", trans.m[4]);
    rsDebug("m5", trans.m[5]);

    rsDebug("m6", trans.m[6]);
    rsDebug("m7", trans.m[7]);
    rsDebug("m8", trans.m[8]);
}




uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {

    float oldX = (float)x - (float)input_width/2;
    float oldY = (float)y - (float)input_height/2;

    float newZ = trans.m[2] * oldX + trans.m[5] * oldY + trans.m[8];
    float newY = (trans.m[1] * oldX + trans.m[4] * oldY + trans.m[7]) / newZ;
    float newX = (trans.m[0] * oldX + trans.m[3] * oldY + trans.m[6]) / newZ;

    int Y,u,v;
    uint32_t nx = newX + input_width/2;
    uint32_t ny = newY + input_height/2;
    getYuv(nx, ny, &Y, &u, &v);

    return rsYuvToRGBA_uchar4(Y, u, v);
}

static void getYuv(uint32_t x, uint32_t y, int *Y, int *u, int *v) {
    if (x > input_width || y > input_height) return;
    *Y = rsGetElementAtYuv_uchar_Y(input, x, y);
    *u = rsGetElementAtYuv_uchar_U(input, x, y);
    *v = rsGetElementAtYuv_uchar_V(input, x, y);
}

