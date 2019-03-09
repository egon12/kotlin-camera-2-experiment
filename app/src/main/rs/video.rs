#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)

#include "rs_graphics.rsh"

rs_allocation inputFrame;

uchar4 RS_KERNEL process(uint32_t x, uint32_t y) {
  uchar4 out;
  out.r = 255;
  out.g = 255;
  out.b = 255;
  out.a = 255;
  return out;
}
