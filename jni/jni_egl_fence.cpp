#include "jni_egl_fence.h"

#include <android/log.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <string.h>

#define  ALOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"egl_fence",__VA_ARGS__)

typedef EGLSyncKHR EGLAPIENTRY (*TypeEglCreateSyncKHR)(EGLDisplay dpy,
    EGLenum type, const EGLint *attrib_list);
typedef EGLBoolean EGLAPIENTRY (*TypeEglDestroySyncKHR)(EGLDisplay dpy,
    EGLSyncKHR sync);
typedef EGLint EGLAPIENTRY (*TypeEglClientWaitSyncKHR)(EGLDisplay dpy,
    EGLSyncKHR sync, EGLint flags, EGLTimeKHR timeout);
static TypeEglCreateSyncKHR FuncEglCreateSyncKHR = NULL;
static TypeEglClientWaitSyncKHR FuncEglClientWaitSyncKHR = NULL;
static TypeEglDestroySyncKHR FuncEglDestroySyncKHR = NULL;
static bool initialized = false;
static bool egl_khr_fence_sync_supported = false;

bool IsEglKHRFenceSyncSupported() {
  if (!initialized) {
    EGLDisplay display = eglGetCurrentDisplay();
    const char* eglExtensions = eglQueryString(eglGetCurrentDisplay(), EGL_EXTENSIONS);
    if (eglExtensions && strstr(eglExtensions, "EGL_KHR_fence_sync")) {
      FuncEglCreateSyncKHR = (TypeEglCreateSyncKHR) eglGetProcAddress("eglCreateSyncKHR");
      FuncEglClientWaitSyncKHR = (TypeEglClientWaitSyncKHR) eglGetProcAddress("eglClientWaitSyncKHR");
      FuncEglDestroySyncKHR = (TypeEglDestroySyncKHR) eglGetProcAddress("eglDestroySyncKHR");
      if (FuncEglCreateSyncKHR != NULL && FuncEglClientWaitSyncKHR != NULL
          && FuncEglDestroySyncKHR != NULL) {
        egl_khr_fence_sync_supported = true;
      }
    }
    initialized = true;
  }
  return egl_khr_fence_sync_supported;
}

void
Java_com_xjt_newpic_photoeditor_FilterStack_nativeEglSetFenceAndWait(JNIEnv* env,
                                                                          jobject thiz) {
  if (!IsEglKHRFenceSyncSupported()) return;
  EGLDisplay display = eglGetCurrentDisplay();

  // Create a egl fence and wait for egl to return it.
  // Additional reference on egl fence sync can be found in:
  // http://www.khronos.org/registry/vg/extensions/KHR/EGL_KHR_fence_sync.txt
  EGLSyncKHR fence = FuncEglCreateSyncKHR(display, EGL_SYNC_FENCE_KHR, NULL);
  if (fence == EGL_NO_SYNC_KHR) {
    return;
  }

  EGLint result = FuncEglClientWaitSyncKHR(display,
                                       fence,
                                       EGL_SYNC_FLUSH_COMMANDS_BIT_KHR,
                                       EGL_FOREVER_KHR);
  if (result == EGL_FALSE) {
    ALOGE("EGL FENCE: error waiting for fence: %#x", eglGetError());
  }
  FuncEglDestroySyncKHR(display, fence);
}
