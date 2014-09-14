#include <math.h>
#include "filters.h"

void JNIFUNCF(ImageFilterShadows, nativeApplyFilter, jobject bitmap, jint width, jint height, float scale){
    double shadowFilterMap[] = {
            -0.00591,  0.0001,
             1.16488,  0.01668,
            -0.18027, -0.06791,
            -0.12625,  0.09001,
             0.15065, -0.03897
    };

    char* destination = 0;
    AndroidBitmap_lockPixels(env, bitmap, (void**) &destination);
    unsigned char * rgb = (unsigned char * )destination;
    int i;
    double s = (scale>=0)?scale:scale/5;
    int len = width * height * 4;

    double *poly = (double *) malloc(5*sizeof(double));
    for (i = 0; i < 5; i++) {
        poly[i] = fastevalPoly(shadowFilterMap+i*2,2 , s);
    }

    unsigned short * hsv = (unsigned short *)malloc(3*sizeof(short));

    for (i = 0; i < len; i+=4)
    {
        rgb2hsv(rgb,i,hsv,0);

        double v = (fastevalPoly(poly,5,hsv[0]/4080.)*4080);
        if (v>4080) v = 4080;
        hsv[0] = (unsigned short) ((v>0)?v:0);

        hsv2rgb(hsv,0, rgb,i);
    }

    free(poly);
    free(hsv);
    AndroidBitmap_unlockPixels(env, bitmap);
}
