#include "filters.h"

void JNIFUNCF(ImageFilter, nativeApplyGradientFilter, jobject bitmap, jint width, jint height,
        jintArray redGradient, jintArray greenGradient, jintArray blueGradient)
{
    char* destination = 0;
    jint* redGradientArray = 0;
    jint* greenGradientArray = 0;
    jint* blueGradientArray = 0;
    if (redGradient)
        redGradientArray = (*env)->GetIntArrayElements(env, redGradient, NULL);
    if (greenGradient)
        greenGradientArray = (*env)->GetIntArrayElements(env, greenGradient, NULL);
    if (blueGradient)
        blueGradientArray = (*env)->GetIntArrayElements(env, blueGradient, NULL);

    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
    int i;
    int len = width * height * 4;
    for (i = 0; i < len; i+=4)
    {
        if (redGradient)
        {
            int r = destination[RED];
            r = redGradientArray[r];
            destination[RED] = r;
        }
        if (greenGradient)
        {
            int g = destination[GREEN];
            g = greenGradientArray[g];
            destination[GREEN] = g;
        }
        if (blueGradient)
        {
            int b = destination[BLUE];
            b = blueGradientArray[b];
            destination[BLUE] = b;
        }
    }
    if (redGradient)
        (*env)->ReleaseIntArrayElements(env, redGradient, redGradientArray, 0);
    if (greenGradient)
        (*env)->ReleaseIntArrayElements(env, greenGradient, greenGradientArray, 0);
    if (blueGradient)
        (*env)->ReleaseIntArrayElements(env, blueGradient, blueGradientArray, 0);
    AndroidBitmap_unlockPixels(env, bitmap);
}

