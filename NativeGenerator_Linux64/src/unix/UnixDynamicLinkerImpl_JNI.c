/* !---- DO NOT EDIT: This file autogenerated by com\sun\gluegen\JavaEmitter.java on Mon Jul 31 16:26:59 PDT 2006 ----! */

#include <jni.h>

#include <assert.h>

#include "jogamp_common_os_UnixDynamicLinkerImpl.h"

 #include <dlfcn.h>
 #include <inttypes.h>

#ifndef RTLD_DEFAULT
    #define RTLD_DEFAULT   ((void *) 0)
#endif

// #define DEBUG_DLOPEN 1

#ifdef DEBUG_DLOPEN
    typedef void *(*DLOPEN_FPTR_TYPE)(const char *filename, int flag);
    #define VERBOSE_ON 1
#endif

// #define VERBOSE_ON 1

#ifdef VERBOSE_ON
    #ifdef ANDROID
        #include <android/log.h>
        #define  DBG_PRINT(...)  __android_log_print(ANDROID_LOG_DEBUG, "JogAmp", __VA_ARGS__)
    #else
        #define  DBG_PRINT(...) fprintf(stderr, __VA_ARGS__); fflush(stderr)
    #endif
#else
        #define  DBG_PRINT(...)
#endif

/*
 * Class:     jogamp_common_os_UnixDynamicLinkerImpl
 * Method:    dlclose
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_de_dhbw_rahmlab_nativelibloader_impl_jogamp_common_os_UnixDynamicLinkerImpl_dlclose(JNIEnv *env, jclass _unused, jlong arg0) {
  int _res;
  _res = dlclose((void *) (intptr_t) arg0);
  return _res;
}


/*
 * Class:     jogamp_common_os_UnixDynamicLinkerImpl
 * Method:    dlerror
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_de_dhbw_rahmlab_nativelibloader_impl_jogamp_common_os_UnixDynamicLinkerImpl_dlerror(JNIEnv *env, jclass _unused) {
  char *  _res;
  _res = dlerror();
  if (_res == NULL) return NULL;  return (*env)->NewStringUTF(env, _res);
}

/*
 * Class:     jogamp_common_os_UnixDynamicLinkerImpl
 * Method:    dlopen
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL
Java_de_dhbw_rahmlab_nativelibloader_impl_jogamp_common_os_UnixDynamicLinkerImpl_dlopen(JNIEnv *env, jclass _unused, jstring arg0, jint arg1) {
  const char* _UTF8arg0 = NULL;
  void *  _res;
#ifdef DEBUG_DLOPEN
  DLOPEN_FPTR_TYPE dlopenFunc = NULL;
  DBG_PRINT("XXX dlopen.0\n");
#endif

  if (arg0 != NULL) {
    if (arg0 != NULL) {
      _UTF8arg0 = (*env)->GetStringUTFChars(env, arg0, (jboolean*)NULL);
    if (_UTF8arg0 == NULL) {
      (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"),
                       "Failed to get UTF-8 chars for argument \"arg0\" in native dispatcher for \"dlopen\"");
      return 0;
    }
    }
  }
#ifdef DEBUG_DLOPEN
  dlopenFunc = (DLOPEN_FPTR_TYPE) dlsym(RTLD_DEFAULT, "dlopen");
  DBG_PRINT("XXX dlopen.1: lib %s, dlopen-fptr %p %p (%d)\n", _UTF8arg0, dlopen, dlopenFunc, dlopen==dlopenFunc);
  _res = dlopen((char *) _UTF8arg0, (int) arg1);
  DBG_PRINT("XXX dlopen.2: %p\n", _res);
#else
  _res = dlopen((char *) _UTF8arg0, (int) arg1);
#endif
  if (arg0 != NULL) {
    (*env)->ReleaseStringUTFChars(env, arg0, _UTF8arg0);
  }
#ifdef DEBUG_DLOPEN
  DBG_PRINT("XXX dlopen.X\n");
#endif
  return (jlong) (intptr_t) _res;
}

/*
 * Class:     jogamp_common_os_UnixDynamicLinkerImpl
 * Method:    dlsym
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_de_dhbw_rahmlab_nativelibloader_impl_jogamp_common_os_UnixDynamicLinkerImpl_dlsym(JNIEnv *env, jclass _unused, jlong arg0, jstring arg1) {
  const char* _UTF8arg1 = NULL;
  void *  _res;
  if (arg1 != NULL) {
    if (arg1 != NULL) {
      _UTF8arg1 = (*env)->GetStringUTFChars(env, arg1, (jboolean*)NULL);
    if (_UTF8arg1 == NULL) {
      (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"),
                       "Failed to get UTF-8 chars for argument \"arg1\" in native dispatcher for \"dlsym\"");
      return 0;
    }
    }
  }
  _res = dlsym((void *) (intptr_t) arg0, (char *) _UTF8arg1);
  DBG_PRINT("XXX dlsym: handle %p, symbol %s -> %p\n", (void *) (intptr_t) arg0, _UTF8arg1, _res);

  if (arg1 != NULL) {
    (*env)->ReleaseStringUTFChars(env, arg1, _UTF8arg1);
  }
  return (jlong) (intptr_t) _res;
}
