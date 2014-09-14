#include <math.h>
#include "filters.h"

void JNIFUNCF(ImageFilterHighlights, nativeApplyFilter, jobject bitmap,
              jint width, jint height, jfloatArray luminanceMap){
    char* destination = 0;
    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
    unsigned char * rgb = (unsigned char * )destination;
    int i;
    int len = width * height * 4;
    jfloat* lum = (*env)->GetFloatArrayElements(env, luminanceMap,0);
    unsigned short * hsv = (unsigned short *)malloc(3*sizeof(short));

    for (i = 0; i < len; i+=4)
    {
        rgb2hsv(rgb,i,hsv,0);
        int v = clampMax(hsv[0],4080);
        hsv[0] = (unsigned short) clampMax(lum[((255*v)/4080)]*4080,4080);
        hsv2rgb(hsv,0, rgb,i);
    }

    free(hsv);
    AndroidBitmap_unlockPixels(env, bitmap);
}
