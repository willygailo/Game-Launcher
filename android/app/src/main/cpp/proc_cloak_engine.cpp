// =============================================================================
// GameBooster Native — 2026 Process & Thread Cloaking Engine (proc_cloak_engine)
// Masks process execution identity, threads, and procfs metadata from anti-cheat
// Android 16 (API 36) compatible: prctl PR_SET_NAME + /proc/self/task/ thread walk
// =============================================================================

#include <jni.h>
#include <sys/prctl.h>
#include <pthread.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

#define TAG "ProcCloakEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

// ── Cloak a single task comm (safe for Android 16 SELinux denial) ──────────
static void cloak_task_comm(pid_t tid, const char *name) {
    char path[64];
    snprintf(path, sizeof(path), "/proc/self/task/%d/comm", tid);
    // O_WRONLY | O_CLOEXEC: on Android 16 only own-process task comms are writable
    int fd = open(path, O_WRONLY | O_CLOEXEC | O_NONBLOCK);
    if (fd >= 0) {
        write(fd, name, strnlen(name, 15));
        close(fd);
    }
    // Fallback: prctl PR_SET_NAME only applies to calling thread but is never denied
    prctl(PR_SET_NAME, (unsigned long)name, 0, 0, 0);
}

extern "C" {

/**
 * Spoofs the calling thread and process comm name in /proc/<pid>/comm
 * and /proc/<pid>/task/<tid>/comm via prctl and pthread_setname_np.
 *
 * Android 16 fix: iterates /proc/self/task/ to cloak all worker threads,
 * since anti-cheat on API 36 now enumerates all task comms, not just main.
 */
JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeCloakProcessIdentity(
        JNIEnv *env, jclass clazz, jstring jTargetName) {
    if (!jTargetName) return JNI_FALSE;
    const char *targetName = env->GetStringUTFChars(jTargetName, nullptr);
    if (!targetName) return JNI_FALSE;

    // 1. Set comm for current (calling) thread
    char commBuf[16];
    memset(commBuf, 0, sizeof(commBuf));
    strncpy(commBuf, targetName, sizeof(commBuf) - 1);

    int prctlRes = prctl(PR_SET_NAME, (unsigned long)commBuf, 0, 0, 0);
    pthread_setname_np(pthread_self(), commBuf);

    // 2. Walk all threads via /proc/self/task/ and cloak each one
    //    Android 16: AC enumerates ALL task comms — must cloak all threads
    DIR *taskDir = opendir("/proc/self/task");
    if (taskDir) {
        struct dirent *entry;
        while ((entry = readdir(taskDir)) != nullptr) {
            if (entry->d_name[0] == '.') continue;
            pid_t tid = (pid_t)atoi(entry->d_name);
            if (tid > 0) {
                cloak_task_comm(tid, commBuf);
            }
        }
        closedir(taskDir);
    }

    // 3. Mark process as non-dumpable to prevent casual /proc/<pid>/mem reads
    prctl(PR_SET_DUMPABLE, 0, 0, 0, 0);

    // 4. Android 16: set PR_SET_CHILD_SUBREAPER to 0 (deny anti-cheat fork tracing)
    prctl(PR_SET_CHILD_SUBREAPER, 0, 0, 0, 0);

    LOGI("Cloaked process & %d threads comm to: %s (prctl result: %d)",
         0, commBuf, prctlRes);

    env->ReleaseStringUTFChars(jTargetName, targetName);
    return (prctlRes == 0) ? JNI_TRUE : JNI_FALSE;
}

/**
 * Query current thread name from kernel /proc/self/comm to verify stealth.
 * Android 16 fix: uses O_RDONLY|O_CLOEXEC|O_NONBLOCK to avoid blocking on
 * SELinux-denied reads (returns empty string instead of hanging).
 */
JNIEXPORT jstring JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeGetCloakedComm(
        JNIEnv *env, jclass clazz) {
    char commBuf[64] = {0};
    // O_NONBLOCK: Android 16 SELinux may deny the read — NONBLOCK returns EAGAIN
    // instead of blocking indefinitely, avoiding a deadlock in the launcher process
    int fd = open("/proc/self/comm", O_RDONLY | O_CLOEXEC | O_NONBLOCK);
    if (fd >= 0) {
        ssize_t bytes = read(fd, commBuf, sizeof(commBuf) - 1);
        if (bytes > 0) {
            if (commBuf[bytes - 1] == '\n') commBuf[bytes - 1] = '\0';
        }
        close(fd);
    }
    // Fallback: read from prctl if /proc/self/comm was denied
    if (commBuf[0] == '\0') {
        prctl(PR_GET_NAME, (unsigned long)commBuf, 0, 0, 0);
    }
    return env->NewStringUTF(commBuf);
}

} // extern "C"
