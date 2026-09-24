// =============================================================================
// GameBooster Native — 2026 Process & Thread Cloaking Engine (proc_cloak_engine)
// Masks process execution identity, threads, and procfs metadata from anti-cheat
// =============================================================================

#include <jni.h>
#include <sys/prctl.h>
#include <pthread.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

#define TAG "ProcCloakEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

extern "C" {

/**
 * Spoofs the calling thread and process comm name in /proc/<pid>/comm
 * and /proc/<pid>/task/<tid>/comm via prctl and pthread_setname_np.
 */
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeCloakProcessIdentity(
        JNIEnv *env, jclass clazz, jstring jTargetName) {
    if (!jTargetName) return JNI_FALSE;
    const char *targetName = env->GetStringUTFChars(jTargetName, nullptr);
    if (!targetName) return JNI_FALSE;

    // 1. Set thread comm via prctl (max 16 chars including null terminator)
    char commBuf[16];
    memset(commBuf, 0, sizeof(commBuf));
    strncpy(commBuf, targetName, sizeof(commBuf) - 1);

    int prctlRes = prctl(PR_SET_NAME, (unsigned long)commBuf, 0, 0, 0);

    // 2. Set pthread name for current thread
    pthread_setname_np(pthread_self(), commBuf);

    // 3. Mark process as non-dumpable to prevent casual /proc/<pid>/mem reads by non-root AC
    prctl(PR_SET_DUMPABLE, 0, 0, 0, 0);

    LOGI("Cloaked process comm to: %s (result: %d)", commBuf, prctlRes);

    env->ReleaseStringUTFChars(jTargetName, targetName);
    return (prctlRes == 0) ? JNI_TRUE : JNI_FALSE;
}

/**
 * Query current thread name from kernel /proc/self/comm to verify stealth
 */
JNIEXPORT jstring JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeGetCloakedComm(
        JNIEnv *env, jclass clazz) {
    char commBuf[64] = {0};
    int fd = open("/proc/self/comm", O_RDONLY | O_CLOEXEC);
    if (fd >= 0) {
        ssize_t bytes = read(fd, commBuf, sizeof(commBuf) - 1);
        if (bytes > 0) {
            if (commBuf[bytes - 1] == '\n') commBuf[bytes - 1] = '\0';
        }
        close(fd);
    }
    return env->NewStringUTF(commBuf);
}

} // extern "C"
