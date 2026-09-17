// =============================================================================
// GameBooster Native — 2026 Advanced Security, Anti-Tamper & Anti-Ban Bypass Suite
// High-performance direct POSIX security layer
// =============================================================================

#include "native_config_injector.h"
#include "config_common.h"

#include <sys/stat.h>
#include <sys/types.h>
#include <sys/xattr.h>
#include <utime.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <string>
#include <vector>

// ─────────────────────────────────────────────────────────────────────────────
// ─── 1. Extended Attribute (xattr) & Inode Audit Flag Stripper ───────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Android 14–16 ext4/f2fs filesystems tag files with extended attributes
// when edited outside the host app's UID (e.g. user.audit, security.audit).
// This POSIX routine enumerates and strips all non-SELinux xattrs so integrity
// verifiers find clean, unflagged inodes.
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassStripXattrs
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;

    ssize_t size = listxattr(path, nullptr, 0);
    if (size > 0) {
        std::vector<char> buffer(size);
        ssize_t res = listxattr(path, buffer.data(), size);
        if (res > 0) {
            const char *attr = buffer.data();
            while (attr < buffer.data() + res) {
                // Preserve security.selinux; strip all audit/tracking xattrs
                if (strcmp(attr, "security.selinux") != 0) {
                    removexattr(path, attr);
                }
                attr += strlen(attr) + 1;
            }
        }
    }

    env->ReleaseStringUTFChars(jPath, path);
    return JNI_TRUE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── 2. High-Precision Timestamp Cloaking ────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Clones atime and mtime from a source reference file (such as base.apk or the
// parent game directory) to the destination config file via POSIX utime.
// Neutralizes anti-cheat startup heuristics that flag recently-touched files.
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassCloakTimestamps
  (JNIEnv *env, jclass, jstring jTargetPath, jstring jSourcePath) {
    if (!jTargetPath || !jSourcePath) return JNI_FALSE;
    const char *target = env->GetStringUTFChars(jTargetPath, nullptr);
    const char *source = env->GetStringUTFChars(jSourcePath, nullptr);
    if (!target || !source) {
        if (target) env->ReleaseStringUTFChars(jTargetPath, target);
        if (source) env->ReleaseStringUTFChars(jSourcePath, source);
        return JNI_FALSE;
    }

    struct stat stSource;
    bool ok = false;
    if (stat(source, &stSource) == 0) {
        struct utimbuf times;
        times.actime  = stSource.st_atime;
        times.modtime = stSource.st_mtime;
        ok = (utime(target, &times) == 0);
    }

    env->ReleaseStringUTFChars(jTargetPath, target);
    env->ReleaseStringUTFChars(jSourcePath, source);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── 3. Inotify Evasion via Atomic Inode Swap ─────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// When inotify monitors /files/ or /shared_prefs/, standard file truncate/write
// triggers multiple IN_MODIFY events that alert anti-cheat watchdogs.
// Atomic rename(staged, target) delivers a single IN_MOVED_TO event with zero
// partial-read window, completely evading real-time file-hashing monitors.
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassAtomicSwap
  (JNIEnv *env, jclass, jstring jStagedPath, jstring jTargetPath) {
    if (!jStagedPath || !jTargetPath) return JNI_FALSE;
    const char *staged = env->GetStringUTFChars(jStagedPath, nullptr);
    const char *target = env->GetStringUTFChars(jTargetPath, nullptr);
    if (!staged || !target) {
        if (staged) env->ReleaseStringUTFChars(jStagedPath, staged);
        if (target) env->ReleaseStringUTFChars(jTargetPath, target);
        return JNI_FALSE;
    }

    int ret = rename(staged, target);
    bool ok = (ret == 0);

    // Cross-filesystem fallback (e.g. /data/local/tmp to /sdcard)
    if (!ok && errno == EXDEV) {
        std::string content = read_file_posix(staged);
        if (!content.empty()) {
            ok = write_file_atomic(target, content);
            unlink(staged);
        }
    }

    env->ReleaseStringUTFChars(jStagedPath, staged);
    env->ReleaseStringUTFChars(jTargetPath, target);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── 4. Direct POSIX Permission & Ownership Alignment ────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Fast in-process chmod/chown execution without spawning subshells or fork/exec,
// keeping the process execution table completely invisible to anti-cheat scanners.
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassEnforcePermissions
  (JNIEnv *env, jclass, jstring jPath, jint uid, jint gid, jint mode) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;

    bool ok = true;
    if (mode > 0) {
        if (chmod(path, (mode_t)mode) != 0) {
            ok = false;
        }
    }
    if (uid >= 10000 && gid >= 10000) {
        // Attempt chown; may fail if unprivileged (non-fatal)
        chown(path, (uid_t)uid, (gid_t)gid);
    }

    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}
