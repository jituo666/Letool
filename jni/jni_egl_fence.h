
#ifndef COM_ANDROID_GALLERY3D_PHOTOEDITOR_JNI_EGL_FENSE_H
#define COM_ANDROID_GALLERY3D_PHOTOEDITOR_JNI_EGL_FENSE_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_xjt_newpic_photoeditor_FilterStack_nativeEglSetFenceAndWait(JNIEnv* env,
                                                                            jobject thiz);
#ifdef __cplusplus
}
#endif

#endif  /* COM_ANDROID_GALLERY3D_PHOTOEDITOR_JNI_EGL_FENSE_H */
