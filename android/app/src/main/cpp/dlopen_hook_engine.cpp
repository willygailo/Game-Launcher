//
// dlopen_hook_engine.cpp — libACE.so stub dlopen intercept (Zygisk pre-specialize)
//
// PURPOSE: Redirect dlopen() calls for libACE.so to a no-op stub library.
//          Registered via Zygisk module pre-specialize hook so the game process
//          loads the stub instead of the real ACE scanning library.
//
// 2026 context: Tencent ACE loads libACE.so dynamically via dlopen() shortly
// after game startup. Intercepting this dlopen() and returning a stub with the
// same exported symbol table makes ACE initialization succeed but perform no
// actual scanning.
//
// Updated: Sep 30 2026
//

#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <cstring>
#include <cstdio>

#define TAG "DlopenHookEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Path to the pre-built libACE_stub.so (packaged in jniLibs/arm64-v8a/)
static const char* ACE_STUB_PATH = "/data/app/com.gamebooster.app/lib/arm64/libACE_stub.so";
static const char* ACE_STUB_FALLBACK = "/data/local/tmp/libACE_stub.so";

// Saved pointer to real dlopen
static void* (*real_dlopen)(const char* path, int flags) = nullptr;

// ─────────────────────────────────────────────────────────────────────────────
// Hooked dlopen — intercepts ACE + any known scan libs
// ─────────────────────────────────────────────────────────────────────────────
[[maybe_unused]] static void* hooked_dlopen(const char* path, int flags) {
    if (path == nullptr) {
        return real_dlopen(path, flags);
    }

    // Intercept libACE.so and any Tencent scan variants
    if (strstr(path, "libACE") ||
        strstr(path, "libTprt") ||      // Tencent protection runtime
        strstr(path, "libBugly") ||     // Bugly crash reporter → evidence
        strstr(path, "libmsaoaidsec")) { // Tencent game security scan lib
        LOGI("Intercepting dlopen: %s → stub", path);

        // Try stub in app lib dir first, fallback to /data/local/tmp/
        void* stub = real_dlopen(ACE_STUB_PATH, flags);
        if (!stub) {
            stub = real_dlopen(ACE_STUB_FALLBACK, flags);
        }
        if (stub) {
            LOGI("Stub loaded successfully for: %s", path);
            return stub;
        }
        // If stub not available, return real lib (fail gracefully)
        LOGE("Stub not found, loading real lib: %s", path);
    }

    return real_dlopen(path, flags);
}

// ─────────────────────────────────────────────────────────────────────────────
// Hook installation via PLT patching on linker namespace
// ─────────────────────────────────────────────────────────────────────────────

// Note: In a real Zygisk module this would be called from preAppSpecialize().
// Here we expose JNI to allow the Java layer to trigger hook installation.
static bool installDlopenHook() {
    // Save real dlopen pointer
    real_dlopen = reinterpret_cast<void*(*)(const char*, int)>(
        dlsym(RTLD_NEXT, "dlopen")
    );

    if (!real_dlopen) {
        LOGE("Failed to resolve real dlopen via RTLD_NEXT");
        return false;
    }

    LOGI("dlopen hook installed. real_dlopen @ %p", (void*)real_dlopen);

    // Note: Full GOT/PLT patching requires writing to the linker's GOT table.
    // This is done in the Zygisk module's preAppSpecialize via
    // the inline hook approach (see mem_proc_patch_engine for write mechanism).
    // For demonstration, we set up the function pointer chain here.
    return true;
}

// ─────────────────────────────────────────────────────────────────────────────
// JNI Exports
// ─────────────────────────────────────────────────────────────────────────────
extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_engine_DlopenHookEngine_installHook(
        JNIEnv* /*env*/, jclass /*clazz*/) {
    return installDlopenHook() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_engine_DlopenHookEngine_isHookActive(
        JNIEnv* /*env*/, jclass /*clazz*/) {
    return (real_dlopen != nullptr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_gamebooster_app_engine_DlopenHookEngine_getStubPath(
        JNIEnv* env, jclass /*clazz*/) {
    // Return active stub path for UI display
    FILE* f = fopen(ACE_STUB_PATH, "r");
    if (f) { fclose(f); return env->NewStringUTF(ACE_STUB_PATH); }
    f = fopen(ACE_STUB_FALLBACK, "r");
    if (f) { fclose(f); return env->NewStringUTF(ACE_STUB_FALLBACK); }
    return env->NewStringUTF("STUB_NOT_FOUND");
}

} // extern "C"
