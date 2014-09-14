#include <math.h>
#include "filters.h"

void JNIFUNCF(ImageFilterBwFilter, nativeApplyFilter, jobject bitmap, jint width, jint height, jint rw, jint gw, jint bw)
{
    char* destination = 0;
    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
    unsigned char * rgb = (unsigned char * )destination;
    float sr = rw;
    float sg = gw;
    float sb = bw;

    float min = MIN(sg,sb);
    min = MIN(sr,min);
    float max =  MAX(sg,sb);
    max = MAX(sr,max);
    float avg = (min+max)/2;
    sb /= avg;
    sg /= avg;
    sr /= avg;
    int i;
    int len = width * height * 4;

    for (i = 0; i < len; i+=4)
    {
        float r = sr *rgb[RED];
        float g = sg *rgb[GREEN];
        float b = sb *rgb[BLUE];
        min = MIN(g,b);
        min = MIN(r,min);
        max = MAX(g,b);
        max = MAX(r,max);
        avg =(min+max)/2;
        rgb[RED]   = CLAMP(avg);
        rgb[GREEN] = rgb[RED];
        rgb[BLUE]  = rgb[RED];
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}
