#include "filters.h"

void JNIFUNCF(ImageFilterExposure, nativeApplyFilter, jobject bitmap, jint width, jint height, jfloat bright)
{
    char* destination = 0;
    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
    unsigned char * rgb = (unsigned char * )destination;
    int i;
    int len = width * height * 4;

    int m =   (255-bright);

    for (i = 0; i < len; i+=4)
    {
        rgb[RED]   = clamp((255*(rgb[RED]))/m);
        rgb[GREEN] = clamp((255*(rgb[GREEN]))/m);
        rgb[BLUE]  = clamp((255*(rgb[BLUE]))/m);
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}

