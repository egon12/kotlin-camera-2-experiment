#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

rs_allocation input;

int input_width;

int input_height;

int *minX;

int *maxX;

int *minY;

int *maxY;

float low=6.2831855;
float high=3.2044244;

//float low=5.969026;
//float high=6.1575217;

//float low = 0;
//float high = M_PI * 2;

static void getYuv(rs_allocation input, uint32_t x, uint32_t y, int *Y, int *u, int * v);

static bool in_range(int u, int v);

static bool set_min_x_in(uint32_t y);
static void set_max_x_in(uint32_t y);

static bool set_min_y_in(uint32_t x);
static void set_max_y_in(uint32_t x);

static int get_smallest_x();
static int get_biggest_x();

/*
float ma = 0.44929;
float mb = -0.11852;
float mc = 85.28391;
float md = 0.08463;
float me = 0.29896;
float mf = 115.75328;
float mg = 0.00030;
float mh = -0.00070;
*/

float ma = 2.31787383150352;
float mb = 0.35880216964855;
float mc = -239.209871245237;
float md = -0.304408105194477;
float me = 2.58454517064966;
float mf = -273.208467364182;
float mg = -0.000908447823087188;
float mh = 0.0017015409685602;
float mi = 0.880517034218644;

uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {
    int z = 1;
    float newz = mg*x + mh*y + mi * z;
    float newx = (ma*x + mb*y + mc*z)/newz;
    float newy = (md*x + me*y + mf*z)/newz;

    int nx = newx;
    int ny = newy;

    if (nx > 0 && nx < input_width && ny > 0 && ny < input_height) {
        int Y,u,v;
        getYuv(input, nx, ny, &Y, &u, &v);
        return rsYuvToRGBA_uchar4(Y, u, v);

    } else {
        uchar4 r;
        r.g = 255;
        r.a = 255;
        return r;
    }
}


//uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {
//
//    int Y,u,v;
//    if (
//        minX[y] == x
//        || maxX[y] == x
//        || minY[x] == y
//        || maxY[x] == y
//    ) {
//        uchar4 r;
//        r.g = 255;
//        r.a = 255;
//        return r;
//    } else {
//        int Y,u,v;
//        getYuv(input, x, y, &Y, &u, &v);
//        return rsYuvToRGBA_uchar4(Y, u, v);
//    }
//}


void getBorder() {
    // reset
    for (int x=0; x<input_width; x++) {
        minY[x] = -1;
        maxY[x] = -1;
    }

    for (int y=0; y<input_height; y++) {
        minX[y] = -1;
        maxX[y] = -1;
    }

    for (int y=0; y<input_height; y++) {
        if (set_min_x_in(y)) {
            set_max_x_in(y);
        }
    }

    for (int x=get_smallest_x(); x<=get_biggest_x(); x++) {
        if (set_min_y_in(x)) {
            set_max_y_in(x);
        }
    }
}

static bool set_min_x_in(uint32_t y) {
    for(int x=0; x<input_width; x++) {
        int Y,u,v;
        getYuv(input, x, y, &Y, &u, &v);
        if (in_range(u, v)) {
            minX[y] = x;
            return 1;
        }
    }
    return 0;
}

static void set_max_x_in(uint32_t y) {
    uint32_t x;
    for(int x=input_width-1; x>0; x--) {
        int Y,u,v;
        getYuv(input, x, y, &Y, &u, &v);
        if (in_range(u, v)) {
            maxX[y] = x;
            break;
        }
    }
}


static bool set_min_y_in(uint32_t x) {
    for(int y=0; y<input_height; y++) {
        int Y,u,v;
        getYuv(input, x, y, &Y, &u, &v);
        if (in_range(u, v)) {
            minY[x] = y;
            return 1;
        }
    }
    return 0;
}

static void set_max_y_in(uint32_t x) {
    uint32_t y;
    for(int y=input_height-1; y>0; y--) {
        int Y,u,v;
        getYuv(input, x, y, &Y, &u, &v);
        if (in_range(u, v)) {
            maxY[x] = y;
            break;
        }
    }
}

static int get_smallest_x() {
    int min_x = input_width;
    for (int i; i<input_height; i++) {
        if (minX[i] > -1) {
            if (minX[i] < min_x) {
                min_x = minX[i];
            }
        }
    }
    return min_x;
}

static int get_biggest_x() {
    int max_x = 0;
    for (int i; i<input_height; i++) {
        if (maxX[i] > -1) {
            if (maxX[i] < max_x) {
                max_x = maxX[i];
            }
        }
    }
    return max_x;
}


static void getYuv(rs_allocation input, uint32_t x, uint32_t y, int *Y, int *u, int * v) {
    *Y = rsGetElementAtYuv_uchar_Y(input, x, y);
    *u = rsGetElementAtYuv_uchar_U(input, x, y);
    *v = rsGetElementAtYuv_uchar_V(input, x, y);
}

static float get_radian(int u, int v) {
    float r = atan2((float) (u - 127), (float) (v - 127));
    if (r < 0) {
        r += M_PI * 2;
    } else if (r > M_PI * 2) {
        r -= M_PI * 2;
    }
    return r;
}


static bool in_range(int u, int v) {
    int ru = u - 127;
    int rv = v - 127;
    if (ru*ru + rv*rv < 401) {
        return 0;
    }

    float r = get_radian(u, v);
    if (low < high) {
        return (low < r && r < high);
    } else {
        return (low < r || r < high);
    }
}


