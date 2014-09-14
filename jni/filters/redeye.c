#include <math.h>
#include "filters.h"

 void JNIFUNCF(ImageFilterRedEye, nativeApplyFilter, jobject bitmap, jint width, jint height, jshortArray vrect)
 {
     char* destination = 0;
     AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
     unsigned char * rgb = (unsigned char * )destination;
     short* rect = (*env)->GetShortArrayElements(env, vrect,0);

     filterRedEye(rgb,rgb,width,height,rect);

     (*env)->ReleaseShortArrayElements(env, vrect, rect, 0);
     AndroidBitmap_unlockPixels(env, bitmap);
 }
