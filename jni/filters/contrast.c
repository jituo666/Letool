#include <math.h>
#include "filters.h"

unsigned char clamp(int c)
{
    int N = 255;
    c &= ~(c >> 31);
    c -= N;
    c &= (c >> 31);
    c += N;
    return  (unsigned char) c;
}

int clampMax(int c,int max)
{
    c &= ~(c >> 31);
    c -= max;
    c &= (c >> 31);
    c += max;
    return  c;
}

void JNIFUNCF(ImageFilterContrast, nativeApplyFilter, jobject bitmap, jint width, jint height, jfloat bright)
{
    char* destination = 0;
    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
    unsigned char * rgb = (unsigned char * )destination;
    int i;
    int len = width * height * 4;
    float m =  (float)pow(2, bright/100.);
    float c =  127-m*127;

    for (i = 0; i < len; i+=4) {
        rgb[RED]   = clamp((int)(m*rgb[RED]+c));
        rgb[GREEN] = clamp((int)(m*rgb[GREEN]+c));
        rgb[BLUE]  = clamp((int)(m*rgb[BLUE]+c));
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}

