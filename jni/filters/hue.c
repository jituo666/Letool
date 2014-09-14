#include "filters.h"

void JNIFUNCF(ImageFilterHue, nativeApplyFilter, jobject bitmap, jint width, jint height, jfloatArray matrix)
{
    char* destination = 0;
    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
    unsigned char * rgb = (unsigned char * )destination;
    int i;
    int len = width * height * 4;
    jfloat* mat = (*env)->GetFloatArrayElements(env, matrix,0);

    for (i = 0; i < len; i+=4)
    {
      int r = rgb[RED];
      int g = rgb[GREEN];
      int b = rgb[BLUE];

      float rf = r*mat[0] + g*mat[4] +  b*mat[8] + mat[12];
      float gf = r*mat[1] + g*mat[5] +  b*mat[9] + mat[13];
      float bf = r*mat[2] + g*mat[6] +  b*mat[10] + mat[14];

      rgb[RED]   = clamp((int)rf);
      rgb[GREEN] = clamp((int)gf);
      rgb[BLUE]  = clamp((int)bf);
    }

    (*env)->ReleaseFloatArrayElements(env, matrix, mat, 0);
    AndroidBitmap_unlockPixels(env, bitmap);
}

