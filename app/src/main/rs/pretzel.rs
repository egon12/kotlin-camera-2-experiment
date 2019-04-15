#pragma version(1)
#pragma rs java_package_name(org.egon12.renderscripttutorial)


static float time = 0.0;
static uchar4 pretzel(uint32_t x, uint32_t y) {

    if (x == 0 && y == 0) {
        time += 0.025;
    }

    float2 r = {512.0, 512.0};
    float2 p = {x, 512-y};
    float2 o = p - r / 2.0;

    float lo = length(o) / r.y - .35;
    float ao = atan2(o.y, o.x);

    float4 s = 0.1 * cos(1.5 * (float4){0.0, 1.0, 2.0, 3.0} + time + ao + sin(ao) * sin(time)*2.);
    float4 e = s.yzwx;
    float4 f = min(lo-s, e-lo);

    float4 myresult =  dot(clamp(f*r.y, (float) 0.0, (float) 1.0), 80. * (s-e)) * (s-0.1) - f;

    float4 res = clamp(myresult * 100.0, (float) 0.0, (float) 255.0);
    res.a = 255.0;
    return convert_uchar4(res);
}

uchar4 RS_KERNEL root(uint32_t x, uint32_t y) {
    return pretzel(x, y);
}