#include "filters.h"

void JNIFUNCF(ImageFilterNegative, nativeApplyFilter, jobject bitmap, jint width, jint height)
{
    char* destination = 0;
    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);

    int tot_len = height * width * 4;
    int i;
    char * dst = destination;
    for (i = 0; i < tot_len; i+=4) {
        dst[RED] = 255 - dst[RED];
        dst[GREEN] = 255 - dst[GREEN];
        dst[BLUE] = 255 - dst[BLUE];
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}
