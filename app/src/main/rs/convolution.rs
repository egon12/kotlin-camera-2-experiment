#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

rs_allocation input;

int input_width;

int input_height;

int start_matrix = 0;

int end_matrix = 0;

float numbers[100];

static void get_color(int m, int n, int* Y, int* u, int *v);

static int convolution(int x, int y);

static uchar4 convolutionRGB(int x, int y);

uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {

    //int Y, u, v;
    //get_color(x, y, &Y, &u, &v);
    //return rsYuvToRGBA_uchar4(Y, u, v);

    //int Y = convolution(x, y);
    //return rsYuvToRGBA_uchar4(Y, 127, 127);

    if (x < 144) {
        int Y = convolution(x, y);
        return rsYuvToRGBA_uchar4(Y, 127, 127);

        //return convolutionRGB(x, y);
    } else {
        int Y, u, v;
        get_color(x, y, &Y, &u, &v);
        return rsYuvToRGBA_uchar4(Y, u, v);
    }
}

int last_fill;
int internal_matrix_size;
bool matrix_edit;

void reset_matrix(int matrix_size) {
    start_matrix = (matrix_size / 2) * -1;
    end_matrix = matrix_size / 2;

    last_fill = 0;
    matrix_edit = 1;
    internal_matrix_size = matrix_size;

}

void add_matrix_number(float number) {
    if (!matrix_edit) {
        return;
    }

    if (last_fill < internal_matrix_size * internal_matrix_size) {
        numbers[last_fill] = number;
        last_fill += 1;
        return;
    } else {
        return;
    }
}

int divider = 1;

void end_edit_matrix() {
    if (last_fill < internal_matrix_size) {
        return;
    } else {
        matrix_edit = 0;
        return;
    }
}

void set_size(int width, int height) {
    input_width = width;
    input_height = height;
}

static void get_color(int m, int n, int* Y, int* u, int *v) {
    if (m < 0 || n < 0 || m >= input_width || n >= input_height) {
        *Y = 0;
        *u = 127;
        *v = 127;
    } else {
        *Y = rsGetElementAtYuv_uchar_Y(input, m, n);
        *u = rsGetElementAtYuv_uchar_U(input, m, n);
        *v = rsGetElementAtYuv_uchar_V(input, m, n);
    }
}

static int convolution(int x, int y) {
    int totalY = 0;

    for (int m = start_matrix; m <= end_matrix; m++) {
        for (int n = start_matrix; n <= end_matrix; n++) {

            int Y, u, v;
            get_color(x + m, y + n, &Y, &u, &v);

            int multiplier_index = (n - start_matrix) * internal_matrix_size + (m - start_matrix);
            int multiplier = numbers[multiplier_index];
            totalY += Y * multiplier;
        }
    }

    return abs(totalY / divider);

}

static uchar4 convolutionRGB(int x, int y) {

    int totalR = 0;
    int totalG = 0;
    int totalB = 0;

    for (int m = start_matrix; m <= end_matrix; m++) {
        for (int n = start_matrix; n <= end_matrix; n++) {

            int Y, u, v;
            get_color(x + m, y + n, &Y, &u, &v);
            uchar4 c = rsYuvToRGBA_uchar4(Y, u, v);

            int multiplier_index = (n - start_matrix) * internal_matrix_size + (m - start_matrix);
            int multiplier = numbers[multiplier_index];
            totalR += c.r * multiplier;
            totalG += c.g * multiplier;
            totalB += c.b * multiplier;
        }
    }


    uchar4 result;
    result.r = abs(totalR / divider);
    result.g = abs(totalG / divider);
    result.b = abs(totalB / divider);
    result.a = 255;

    return result;
}

