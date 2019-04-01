#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

rs_allocation bottomFrame;

rs_allocation topFrame;

float alpha = 1;

static void getYUV(rs_allocation frame, uint32_t x, uint32_t y, int* Y, int* u, int* v);

static int compositeAlpha(int color1, float alpha1, int color2, float alpha2);

uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {

    int Yb, ub, vb;
    getYUV(bottomFrame, x, y, &Yb, &ub, &vb);

    if (x < 320 && y < 240) {
        int Yt, ut, vt;
        getYUV(topFrame, x, y, &Yt, &ut, &vt);

        int Yo, uo, vo;

        Yo = Yb;
        uo = ub;
        vo = vb;

        if (Yt > 20) {
            //Yo = Yt;
            //uo = ut;
            //vo = vt;

            Yo = compositeAlpha(Yt, alpha, Yb, 1.0);
            uo = compositeAlpha(ut, alpha, ut, 1.0);
            vo = compositeAlpha(vt, alpha, vt, 1.0);
        }

        return rsYuvToRGBA_uchar4(Yo, uo, vo);
    } else {
        return rsYuvToRGBA_uchar4(Yb, ub, vb);
    }
}

static void getYUV(rs_allocation frame, uint32_t x, uint32_t y, int* Y, int* u, int* v) {
    *Y = rsGetElementAtYuv_uchar_Y(frame, x, y);
    *u = rsGetElementAtYuv_uchar_U(frame, x, y);
    *v = rsGetElementAtYuv_uchar_V(frame, x, y);
}

static int compositeAlpha(int color1, float alpha1, int color2, float alpha2) {

    int above = color1 * alpha1 + color2 * alpha2 * (1.0 - alpha1);
    int below = alpha1 + alpha2 * (1.0 - alpha1);
    return above / below;

}


