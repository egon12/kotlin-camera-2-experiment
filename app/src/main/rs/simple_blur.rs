#pragma version(1)

#pragma rs java_package_name(org.egon12.renderscripttutorial)

static void get_color(uint32_t x, uint32_t y, int *Y, int *u, int *v);

rs_allocation input;
int width;
int height;

uchar4 RS_KERNEL root(uint32_t x, uint32_t y) {
    int Y, u, v;

    if (x > 320) {
        x -= 320;
    } else {
        get_color(x, y, &Y, &u, &v);
        uchar Yc = (uchar) clamp(Y, 0, 255);
        return (uchar4) { Yc, Yc, Yc, 255};
    }

    int totalY = 0;

    uint32_t x1 = min((int32_t)x+1, width-1);
    uint32_t x2 = max((int32_t)x-1, 0);
    uint32_t y1 = min((int32_t)y+1, height-1);
    uint32_t y2 = max((int32_t)y-1, 0);

    // x-1
    get_color(x1, y1, &Y, &u, &v);
    totalY += Y;

    get_color(x1, y, &Y, &u, &v);
    totalY += Y;

    get_color(x1, y2, &Y, &u, &v);
    totalY += Y;


    // x = x
    get_color(x, y1, &Y, &u, &v);
    totalY += Y;

    get_color(x, y, &Y, &u, &v);
    totalY += Y;

    get_color(x, y2, &Y, &u, &v);
    totalY += Y;

    // x + 1
    get_color(x2, y1, &Y, &u, &v);
    totalY += Y;

    get_color(x2, y, &Y, &u, &v);
    totalY += Y;

    get_color(x2, y2, &Y, &u, &v);
    totalY += Y;

    uchar avg = totalY / 9;
    uchar4 res;
    res.r = avg;
    res.g = avg;
    res.b = avg;
    res.a = 255;
    return res;

}

static void get_color(uint32_t x, uint32_t y, int *Y, int *u, int *v) {

    *Y = rsGetElementAtYuv_uchar_Y(input, x, y);
    *u = rsGetElementAtYuv_uchar_U(input, x, y);
    *v = rsGetElementAtYuv_uchar_V(input, x, y);
}

