//
// ace_cloak_engine.cpp — Native Tencent ACE / Anti-Cheat Expert In-Process Cloak
// Targets: libanort.so, libanogs.so, libCrashSight.so
//

#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <cstring>
#include <cstdio>
#include <cstdint>
#include <string>

#include <cinttypes>

#define TAG "AceCloakEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace {

// Stealth memory writer via /proc/self/mem to avoid mprotect VM_WRITE flags
bool patch_code_arm64(uintptr_t addr, const uint32_t* insns, size_t count) {
    int fd = open("/proc/self/mem", O_RDWR);
    if (fd < 0) return false;

    off_t target = static_cast<off_t>(addr);
    ssize_t bytes = static_cast<ssize_t>(count * sizeof(uint32_t));
    ssize_t written = pwrite(fd, insns, bytes, target);
    close(fd);

    if (written == bytes) {
        __builtin___clear_cache(reinterpret_cast<char*>(addr),
                                reinterpret_cast<char*>(addr + bytes));
        return true;
    }
    return false;
}

// ARM64 RET instruction opcode (0xD65F03C0)
// MOV X0, #0 ; RET -> 0xD2800000, 0xD65F03C0
static const uint32_t RET_ZERO_STUB[] = {
    0xD2800000, // mov x0, #0
    0xD65F03C0  // ret
};

} // namespace

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_engine_AceCloakEngine_cloakAceModules(
        JNIEnv* /*env*/, jclass /*clazz*/) {
    LOGI("⚡ Initializing Native ACE Cloaking Shield...");

    // Check if libanort.so is loaded in memory
    FILE* fp = fopen("/proc/self/maps", "r");
    if (!fp) return JNI_FALSE;

    char line[512];
    uintptr_t anort_base = 0;
    uintptr_t anogs_base = 0;

    while (fgets(line, sizeof(line), fp)) {
        if (!anort_base && strstr(line, "libanort.so")) {
            uintptr_t start = 0;
            if (sscanf(line, "%" SCNxPTR "-", &start) == 1) {
                anort_base = start;
            }
        }
        if (!anogs_base && strstr(line, "libanogs.so")) {
            uintptr_t start = 0;
            if (sscanf(line, "%" SCNxPTR "-", &start) == 1) {
                anogs_base = start;
            }
        }
    }
    fclose(fp);

    int patchedCount = 0;
    if (anort_base) {
        LOGI("libanort.so base located @ 0x%" PRIxPTR, anort_base);
        // Neutralize ACE report thread entry point
        // Apply ret 0 stub to known entry offsets
        if (patch_code_arm64(anort_base + 0x2A140, RET_ZERO_STUB, 2)) {
            patchedCount++;
        }
    }

    if (anogs_base) {
        LOGI("libanogs.so base located @ 0x%" PRIxPTR, anogs_base);
        if (patch_code_arm64(anogs_base + 0x1E080, RET_ZERO_STUB, 2)) {
            patchedCount++;
        }
    }

    LOGI("ACE Cloaking applied: %d targets patched.", patchedCount);
    return (patchedCount > 0) ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
