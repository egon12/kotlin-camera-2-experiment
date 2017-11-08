#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

//rs_mesh snowMesh;

typedef struct __attribute__((packed, aligned(4))) Snow {
    float2 velocity;
    float2 position;
    uchar4 color;
} Snow_t;
Snow_t *snow;

float2 wind;
float2 grav;

/*
int root() {
    rsgClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    rsgDrawMesh(snowMesh);
    return 0;
}

void init() {
    grav.x = 0;
    grav.y = 18;
    wind.x = rsRand(50)+20;
    wind.y = rsRand(4) - 2;
}

void initSnow() {
    const float w = rsgGetWidth();
    const float h = rsgGetHeight();

    int snowCount = rsAllocationGetDimX(rsGetAllocation(snow));

    Snow_t *pSnow = snow;
    for (int i=0; i < snowCount; i++) {
        pSnow->position.x = rsRand(w);
        pSnow->position.y = rsRand(h);

        pSnow->velocity.y = rsRand(60);
        pSnow->velocity.x = rsRand(100);
        pSnow->velocity.x -= 50;

        uchar4 c = rsPackColorTo8888(255, 255, 255);
        pSnow->color = c;
        pSnow++;
    }
}
*/


uchar4 __attribute__((kernel)) bar(uint32_t x, uint32_t y) {
    //uchar4 ret = ((uchar) x, (uchar) y, (uchar) x+ y, (uchar) 255);
    uchar4 ret;
    ret.g = x;
    ret.b = y;
    ret.r = 255;
    ret.a = 255;
    return ret;
}

uchar4 RS_KERNEL invert(uchar4 in, uint32_t x, uint32_t y) {
  uchar4 out = in;
  out.r = 255 - in.r;
  out.g = 255 - in.g;
  out.b = 255 - in.b;
  return out;
}

rs_allocation gCurrentFrame;

float minHue = 0.0;
float maxHue = 0.0;
bool filter = 0;

uchar4 RS_KERNEL yonly(uint32_t x, uint32_t y) {
    uchar4 curPixel;

    curPixel.r = rsGetElementAtYuv_uchar_Y(gCurrentFrame, x, y);
    curPixel.g = rsGetElementAtYuv_uchar_U(gCurrentFrame, x, y);
    curPixel.b = rsGetElementAtYuv_uchar_V(gCurrentFrame, x, y);

    float hue = atan2pi((float) curPixel.g, (float) curPixel.b) * 180.f + 15.f;
    if (minHue < hue && hue < maxHue) {
        return rsYuvToRGBA_uchar4(curPixel.r, curPixel.g, curPixel.b);
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
