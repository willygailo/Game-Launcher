//
// il2cpp_direct_scanner.cpp — Standalone Memory Pattern Scanner for libil2cpp.so
// Provides native RVA resolution directly in C++
//

#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>
#include <cstring>
#include <cstdio>
#include <cstdint>
#include <cinttypes>
#include <vector>
#include <string>

#define TAG "Il2cppDirectScanner"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace {

uintptr_t find_module_base(const char* module_name, size_t* out_size = nullptr) {
    FILE* fp = fopen("/proc/self/maps", "r");
    if (!fp) return 0;

    char line[512];
    uintptr_t base = 0;
    uintptr_t end = 0;

    while (fgets(line, sizeof(line), fp)) {
        if (strstr(line, module_name)) {
            uintptr_t seg_start = 0, seg_end = 0;
            if (sscanf(line, "%" SCNxPTR "-%" SCNxPTR, &seg_start, &seg_end) == 2) {
                if (base == 0) base = seg_start;
                end = seg_end;
            }
        }
    }
    fclose(fp);

    if (out_size && base > 0) {
        *out_size = end - base;
    }
    return base;
}

uintptr_t scan_pattern(uintptr_t start, size_t length, const uint8_t* pattern, const char* mask) {
    size_t pattern_len = strlen(mask);
    if (length < pattern_len) return 0;

    const uint8_t* current = reinterpret_cast<const uint8_t*>(start);
    for (size_t i = 0; i <= length - pattern_len; ++i) {
        bool match = true;
        for (size_t j = 0; j < pattern_len; ++j) {
            if (mask[j] != '?' && pattern[j] != current[i + j]) {
                match = false;
                break;
            }
        }
        if (match) {
            return start + i;
        }
    }
    return 0;
}

} // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_gamebooster_app_engine_Il2cppDirectScanner_getIl2cppBase(
        JNIEnv* /*env*/, jclass /*clazz*/) {
    return static_cast<jlong>(find_module_base("libil2cpp.so"));
}

JNIEXPORT jlong JNICALL
Java_com_gamebooster_app_engine_Il2cppDirectScanner_scanPattern(
        JNIEnv* env, jclass /*clazz*/, jbyteArray jPattern, jstring jMask) {
    if (!jPattern || !jMask) return 0;

    size_t mod_size = 0;
    uintptr_t base = find_module_base("libil2cpp.so", &mod_size);
    if (!base || mod_size == 0) return 0;

    jsize pat_len = env->GetArrayLength(jPattern);
    jbyte* pat_bytes = env->GetByteArrayElements(jPattern, nullptr);
    const char* mask_str = env->GetStringUTFChars(jMask, nullptr);

    uintptr_t found = scan_pattern(base, mod_size,
                                   reinterpret_cast<const uint8_t*>(pat_bytes),
                                   mask_str);

    env->ReleaseByteArrayElements(jPattern, pat_bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(jMask, mask_str);

    return static_cast<jlong>(found);
}

} // extern "C"
