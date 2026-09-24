// =============================================================================
// GameBooster Native — 2026 In-Memory Zero-Disk memfd Injection Engine
// Provides anonymous memory file descriptors completely invisible to inotify disk scans
// =============================================================================

#include <jni.h>
#include <sys/syscall.h>
#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>

#define TAG "MemfdInjectEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#ifndef MFD_CLOEXEC
#define MFD_CLOEXEC 0x0001U
#endif

#ifndef MFD_ALLOW_SEALING
#define MFD_ALLOW_SEALING 0x0002U
#endif

#ifndef __NR_memfd_create
#if defined(__aarch64__)
#define __NR_memfd_create 279
#elif defined(__arm__)
#define __NR_memfd_create 385
#elif defined(__x86_64__)
#define __NR_memfd_create 319
#else
#define __NR_memfd_create 356
#endif
#endif

static int create_anonymous_memfd(const char *name, unsigned int flags) {
    return (int)syscall(__NR_memfd_create, name, flags);
}

extern "C" {

/**
 * Creates an anonymous in-memory file descriptor and writes the payload into it.
 * Returns the open file descriptor (>= 0) or -1 on failure.
 */
JNIEXPORT jint JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeCreateAnonymousMemFd(
        JNIEnv *env, jclass clazz, jstring jName, jbyteArray jData) {
    if (!jName || !jData) return -1;

    const char *name = env->GetStringUTFChars(jName, nullptr);
    if (!name) return -1;

    jsize dataLen = env->GetArrayLength(jData);
    jbyte *dataBytes = env->GetByteArrayElements(jData, nullptr);

    int fd = create_anonymous_memfd(name, MFD_CLOEXEC | MFD_ALLOW_SEALING);
    if (fd < 0) {
        LOGE("Failed to create memfd for %s", name);
        env->ReleaseByteArrayElements(jData, dataBytes, JNI_ABORT);
        env->ReleaseStringUTFChars(jName, name);
        return -1;
    }

    if (dataLen > 0 && dataBytes != nullptr) {
        ssize_t written = write(fd, dataBytes, (size_t)dataLen);
        if (written != (ssize_t)dataLen) {
            LOGE("Partial write to memfd: %zd / %d", written, dataLen);
        }
        lseek(fd, 0, SEEK_SET);
    }

    env->ReleaseByteArrayElements(jData, dataBytes, JNI_ABORT);
    env->ReleaseStringUTFChars(jName, name);

    LOGI("Created anonymous memfd %d (size: %d bytes)", fd, dataLen);
    return fd;
}

/**
 * Returns the /proc/self/fd/<fd> path for an open memfd
 */
JNIEXPORT jstring JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeGetMemFdPath(
        JNIEnv *env, jclass clazz, jint fd) {
    if (fd < 0) return nullptr;
    char pathBuf[64];
    snprintf(pathBuf, sizeof(pathBuf), "/proc/self/fd/%d", fd);
    return env->NewStringUTF(pathBuf);
}

/**
 * Closes an open anonymous memfd
 */
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeCloseMemFd(
        JNIEnv *env, jclass clazz, jint fd) {
    if (fd < 0) return JNI_FALSE;
    int res = close(fd);
    return (res == 0) ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
