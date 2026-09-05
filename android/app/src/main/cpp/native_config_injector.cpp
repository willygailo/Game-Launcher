// =============================================================================
// Native Config Injector Core System, Engine & Kernel Layer
// High-performance isolated translation unit for GameBooster Native
// =============================================================================

#include "native_config_injector.h"
#include "config_common.h"

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetProcessCpuAffinity
  (JNIEnv *, jclass, jint pid, jint cpuMask) {
    if (pid <= 0) return JNI_FALSE;

    int maskVal = (cpuMask > 0) ? cpuMask : detect_cpu_cluster_mask(true);
    cpu_set_t cpuset;
    CPU_ZERO(&cpuset);

    for (int i = 0; i < 32; i++) {
        if (maskVal & (1 << i)) {
            CPU_SET(i, &cpuset);
        }
    }

    // Set process affinity
    int ret = sched_setaffinity(pid, sizeof(cpu_set_t), &cpuset);

    // Also iterate and pin all individual task threads in /proc/<pid>/task/
    std::string taskDir = "/proc/" + std::to_string(pid) + "/task";
    DIR *dir = opendir(taskDir.c_str());
    if (dir) {
        struct dirent *entry;
        while ((entry = readdir(dir)) != nullptr) {
            if (entry->d_name[0] != '.') {
                int tid = atoi(entry->d_name);
                if (tid > 0) {
                    sched_setaffinity(tid, sizeof(cpu_set_t), &cpuset);
                }
            }
        }
        closedir(dir);
    }

    return (ret == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetThreadSchedulingPolicy
  (JNIEnv *, jclass, jint pid, jint policy, jint priority) {
    if (pid <= 0) return JNI_FALSE;

    // First, set nice priority -20 (maximum standard real-time priority)
    setpriority(PRIO_PROCESS, pid, -20);

    // If real-time FIFO/RR scheduling requested (policy 1=FIFO, 2=RR)
    struct sched_param param;
    param.sched_priority = (priority > 0 && priority <= 99) ? priority : 50;

    int schedPolicy = SCHED_OTHER;
    if (policy == 1) schedPolicy = SCHED_FIFO;
    else if (policy == 2) schedPolicy = SCHED_RR;

    int ret = sched_setscheduler(pid, schedPolicy, &param);
    return (ret == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetIoPriority
  (JNIEnv *, jclass, jint pid, jint ioClass, jint ioPriority) {
    if (pid <= 0) return JNI_FALSE;
    int cls = (ioClass >= 1 && ioClass <= 3) ? ioClass : IOPRIO_CLASS_RT;
    int prio = (ioPriority >= 0 && ioPriority <= 7) ? ioPriority : 0;
    int ioprio = IOPRIO_PRIO_VALUE(cls, prio);

    long ret = syscall(SYS_ioprio_set, IOPRIO_WHO_PROCESS, pid, ioprio);
    return (ret == 0) ? JNI_TRUE : JNI_FALSE;
}

// ─── Direct Process I/O & Nice Priority Syscall Accelerator ──────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetProcessIOPriority
  (JNIEnv *, jclass, jint pid, jint schedPriority, jint ioprioClass, jint ioprioLevel) {
    if (pid <= 0) return JNI_FALSE;

    // 1. Set CFS CPU nice priority (-20 to 19, -10 is high performance)
    int prioRes = setpriority(PRIO_PROCESS, pid, schedPriority);

    // 2. Set Linux I/O scheduling class and priority (IOPRIO_CLASS_RT / IOPRIO_CLASS_BE)
    int cls = (ioprioClass >= 1 && ioprioClass <= 3) ? ioprioClass : IOPRIO_CLASS_BE;
    int data = (ioprioLevel >= 0 && ioprioLevel <= 7) ? ioprioLevel : 0;
    long ioRes = syscall(SYS_ioprio_set, IOPRIO_WHO_PROCESS, pid, IOPRIO_PRIO_VALUE(cls, data));

    LOGI("nativeSetProcessIOPriority: PID=%d nice=%d (res=%d) ioprio=(cls=%d, lvl=%d, res=%ld)",
         pid, schedPriority, prioRes, cls, data, ioRes);
    return (prioRes == 0 || ioRes == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeOptimizeMemoryMapping
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    int fd = open(path, O_RDONLY);
    if (fd < 0) {
        env->ReleaseStringUTFChars(jPath, path);
        return JNI_FALSE;
    }

    struct stat st;
    if (fstat(fd, &st) < 0 || st.st_size == 0) {
        close(fd);
        env->ReleaseStringUTFChars(jPath, path);
        return JNI_FALSE;
    }

    void *addr = mmap(nullptr, st.st_size, PROT_READ, MAP_SHARED, fd, 0);
    if (addr != MAP_FAILED) {
        madvise(addr, st.st_size, MADV_WILLNEED | MADV_SEQUENTIAL);
#ifdef MADV_HUGEPAGE
        madvise(addr, st.st_size, MADV_HUGEPAGE);
#endif
        munmap(addr, st.st_size);
    }

    close(fd);
    env->ReleaseStringUTFChars(jPath, path);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeFastMemorySync
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    int fd = open(path, O_RDONLY);
    if (fd >= 0) {
        fdatasync(fd);
        close(fd);
    }

    env->ReleaseStringUTFChars(jPath, path);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeForceVulkanPipelineCache
  (JNIEnv *env, jclass, jstring jPath, jstring jPkg) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);

    make_parent_dirs(pathStr);

    struct stat st;
    if (stat(path, &st) != 0 || st.st_size < static_cast<off_t>(sizeof(VkPipelineCacheHeaderV1))) {
        VkPipelineCacheHeaderV1 header;
        memset(&header, 0, sizeof(header));
        header.headerSize = sizeof(VkPipelineCacheHeaderV1);
        header.headerVersion = 1; // VK_PIPELINE_CACHE_HEADER_VERSION_ONE
        header.vendorID = 0x5143; // Qualcomm Adreno / ARM Mali generic ID
        header.deviceID = 0x0001;

        int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0666);
        if (fd >= 0) {
            write(fd, &header, sizeof(header));
            fchmod(fd, 0777);
            fdatasync(fd);
            close(fd);
        }
    } else {
        chmod(path, 0777);
    }

    if (jPkg) {
        const char *pkg = env->GetStringUTFChars(jPkg, nullptr);
        std::string codeCache = "/data/data/" + std::string(pkg) + "/code_cache";
        chmod(codeCache.c_str(), 0777);
        env->ReleaseStringUTFChars(jPkg, pkg);
    }

    env->ReleaseStringUTFChars(jPath, path);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectVulkanOptimization
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::vector<std::pair<std::string, std::string>> vulkanKeys = {
        {"r.Vulkan.Enable", "1"},
        {"r.Vulkan.UsePipelines", "1"},
        {"r.Vulkan.RobustBufferAccess", "0"},
        {"r.Mobile.EnableVulkanPreTransform", "1"},
        {"r.AsyncCompute", "1"},
        {"r.EnableAsyncPipelineCompilation", "1"},
        {"r.VRS.Enable", "1"},
        {"VulkanEnabled", "1"},
        {"VulkanPipelineCache", "1"},
        {"AsyncCompute", "1"},
        {"VRS", "1"},
        {"PreloadShaders", "1"},
        {"bPreloadShaders", "True"},
        {"ShaderPrecompile", "1"},
        {"EnableAsyncPipelineCompilation", "1"},
        {"VulkanThreadCount", "4"},
        {"ShaderWarmupAtLaunch", "1"},
        {"GPUPipelineWarmup", "1"}
    };

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : vulkanKeys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, false);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePreserveFileTimestamps
  (JNIEnv *env, jclass, jstring jPath, jlong atimeSec, jlong mtimeSec) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    struct utimbuf times;
    times.actime = (atimeSec > 0) ? atimeSec : time(nullptr);
    times.modtime = (mtimeSec > 0) ? mtimeSec : time(nullptr);
    int ret = utime(path, &times);

    env->ReleaseStringUTFChars(jPath, path);
    return (ret == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeStealthWrite
  (JNIEnv *env, jclass, jstring jPath, jstring jContent) {
    if (!jPath || !jContent) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *content = env->GetStringUTFChars(jContent, nullptr);

    struct stat st;
    bool hasStat = (stat(path, &st) == 0);

    bool ok = write_file_atomic(path, content, 0666);

    if (ok && hasStat) {
        struct utimbuf times;
        times.actime = st.st_atime;
        times.modtime = st.st_mtime;
        utime(path, &times);
    }

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jContent, content);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeCalculateConfigCrc32
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return 0;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    std::string content = read_file_posix(path);
    env->ReleaseStringUTFChars(jPath, path);

    if (content.empty()) return 0;
    return static_cast<jlong>(calculate_crc32(reinterpret_cast<const uint8_t*>(content.data()), content.size()));
}

// ─── JNI Implementation Functions ─────────────────────────────────────────────

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectConfig
  (JNIEnv *env, jclass, jstring jPath, jstring jContent) {
    if (!jPath || !jContent) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *content = env->GetStringUTFChars(jContent, nullptr);

    bool ok = write_file_atomic(path, content);

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jContent, content);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchKey
  (JNIEnv *env, jclass, jstring jPath, jstring jKey, jstring jValue) {
    if (!jPath || !jKey || !jValue) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *key = env->GetStringUTFChars(jKey, nullptr);
    const char *val = env->GetStringUTFChars(jValue, nullptr);

    std::string content = read_file_posix(path);
    std::string pathStr(path);
    bool ok = false;

    if (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos) {
        ok = patch_xml_node(content, "string", key, val);
    } else if (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{')) {
        ok = patch_json_node(content, key, val, false);
    } else if (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos) {
        ok = patch_cvar(content, key, val);
    } else {
        ok = patch_key_value(content, key, val);
    }

    if (ok) {
        ok = write_file_atomic(path, content);
    }

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jKey, key);
    env->ReleaseStringUTFChars(jValue, val);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeBatchPatchKeys
  (JNIEnv *env, jclass, jstring jPath, jobjectArray jKeys, jobjectArray jValues) {
    if (!jPath || !jKeys || !jValues) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);

    jsize count = env->GetArrayLength(jKeys);
    std::vector<std::pair<std::string, std::string>> keys;
    keys.reserve(count);

    for (jsize i = 0; i < count; i++) {
        auto jKeyStr = (jstring) env->GetObjectArrayElement(jKeys, i);
        auto jValStr = (jstring) env->GetObjectArrayElement(jValues, i);
        if (jKeyStr && jValStr) {
            const char *k = env->GetStringUTFChars(jKeyStr, nullptr);
            const char *v = env->GetStringUTFChars(jValStr, nullptr);
            if (k && v) {
                keys.emplace_back(std::string(k), std::string(v));
            }
            if (k) env->ReleaseStringUTFChars(jKeyStr, k);
            if (v) env->ReleaseStringUTFChars(jValStr, v);
        }
        if (jKeyStr) env->DeleteLocalRef(jKeyStr);
        if (jValStr) env->DeleteLocalRef(jValStr);
    }

    bool ok = apply_keys_to_file(pathStr, path, keys, "BatchPatchKeys");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchContentInMemory
  (JNIEnv *env, jclass, jstring jContent, jobjectArray jKeys, jobjectArray jValues, jint formatType) {
    if (!jContent) return nullptr;
    const char *rawContent = env->GetStringUTFChars(jContent, nullptr);
    std::string content = rawContent ? rawContent : "";
    env->ReleaseStringUTFChars(jContent, rawContent);

    if (!jKeys || !jValues) {
        return env->NewStringUTF(content.c_str());
    }

    jsize count = env->GetArrayLength(jKeys);
    if (formatType == 1 && !content.empty() && content.find("<map>") == std::string::npos) {
        // Not a SharedPreferences XML - refuse to corrupt non-<map> XML files
        return env->NewStringUTF(content.c_str());
    }
    bool isXml = (formatType == 1 || content.find("<map>") != std::string::npos);
    bool isJson = (formatType == 2 || (!content.empty() && content.front() == '{'));
    bool isCvar = (formatType == 0 && (content.find("+CVars=") != std::string::npos || content.find("[UserCustom") != std::string::npos));

    for (jsize i = 0; i < count; i++) {
        auto jKeyStr = (jstring) env->GetObjectArrayElement(jKeys, i);
        auto jValStr = (jstring) env->GetObjectArrayElement(jValues, i);
        if (jKeyStr && jValStr) {
            const char *rawK = env->GetStringUTFChars(jKeyStr, nullptr);
            const char *rawV = env->GetStringUTFChars(jValStr, nullptr);
            std::string k = rawK ? rawK : "";
            std::string v = rawV ? rawV : "";
            if (k.rfind("+CVars=", 0) == 0) {
                k = k.substr(7);
            }
            if (isXml) {
                std::string valLower = v;
                std::transform(valLower.begin(), valLower.end(), valLower.begin(), ::tolower);
                std::string tag = "int";
                std::string valFinal = v;
                if (valLower == "true" || valLower == "false") {
                    tag = "boolean";
                    valFinal = valLower;
                } else if (v.find('.') != std::string::npos) {
                    tag = "float";
                }
                patch_xml_node(content, tag, k, valFinal);
            } else if (isJson) {
                bool isNum = (!v.empty() && (isdigit((unsigned char)v[0]) || v[0] == '-'));
                patch_json_node(content, k, v, isNum);
            } else if (isCvar) {
                patch_cvar(content, k, v);
            } else {
                patch_key_value(content, k, v);
            }
            env->ReleaseStringUTFChars(jKeyStr, rawK);
            env->ReleaseStringUTFChars(jValStr, rawV);
        }
        if (jKeyStr) env->DeleteLocalRef(jKeyStr);
        if (jValStr) env->DeleteLocalRef(jValStr);
    }

    return env->NewStringUTF(content.c_str());
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchXmlKey
  (JNIEnv *env, jclass, jstring jPath, jstring jTag, jstring jKey, jstring jValue) {
    if (!jPath || !jTag || !jKey || !jValue) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *tag = env->GetStringUTFChars(jTag, nullptr);
    const char *key = env->GetStringUTFChars(jKey, nullptr);
    const char *val = env->GetStringUTFChars(jValue, nullptr);

    std::string content = read_file_posix(path);
    if (!content.empty() && content.find("<map>") == std::string::npos) {
        env->ReleaseStringUTFChars(jPath, path);
        env->ReleaseStringUTFChars(jTag, tag);
        env->ReleaseStringUTFChars(jKey, key);
        env->ReleaseStringUTFChars(jValue, val);
        return JNI_FALSE;
    }
    bool ok = patch_xml_node(content, tag, key, val);
    if (ok) {
        ok = write_file_atomic(path, content);
    }

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jTag, tag);
    env->ReleaseStringUTFChars(jKey, key);
    env->ReleaseStringUTFChars(jValue, val);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchJsonKey
  (JNIEnv *env, jclass, jstring jPath, jstring jKey, jstring jValue, jboolean isNumeric) {
    if (!jPath || !jKey || !jValue) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *key = env->GetStringUTFChars(jKey, nullptr);
    const char *val = env->GetStringUTFChars(jValue, nullptr);

    std::string content = read_file_posix(path);
    bool ok = patch_json_node(content, key, val, isNumeric == JNI_TRUE);
    if (ok) {
        ok = write_file_atomic(path, content);
    }

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jKey, key);
    env->ReleaseStringUTFChars(jValue, val);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUnrealEngineIni
  (JNIEnv *env, jclass, jstring jPath, jint targetFps) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    int fps = (targetFps > 0) ? targetFps : 120;
    std::string fpsStr = std::to_string(fps);

    std::vector<std::pair<std::string, std::string>> ueKeys = {
        {"t.MaxFPS", fpsStr},
        {"r.VSync", "0"},
        {"r.FinishCurrentFrame", "0"},
        {"r.OneFrameThreadLag", "0"},
        {"r.MobileContentScaleFactor", "1.0"},
        {"r.Streaming.PoolSize", "0"},
        {"r.RenderTargetPoolMin", "1024"},
        {"r.ShadowQuality", "0"},
        {"r.BloomQuality", "1"},
        {"r.DepthOfFieldQuality", "0"},
        {"r.PostProcessAAQuality", "1"},
        {"r.Vulkan.Enable", "1"},
        {"r.Mobile.EnableVulkanPreTransform", "1"},
        {"r.Vulkan.UsePipelines", "1"},
        {"r.AllowOcclusionQueries", "1"}
    };

    for (const auto& kv : ueKeys) {
        patch_cvar(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUnityBootConfig
  (JNIEnv *env, jclass, jstring jPath, jint targetFps) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    int fps = (targetFps > 0) ? targetFps : 120;
    std::string fpsStr = std::to_string(fps);

    std::vector<std::pair<std::string, std::string>> unityKeys = {
        {"gfx-enable-native-gles", "1"},
        {"wait-for-native-debugger", "0"},
        {"player-connection-debug", "0"},
        {"target-frame-rate", fpsStr},
        {"hdr-display-enabled", "0"},
        {"gc-max-time-slice", "3"},
        {"vulkan-enable-validation-layers", "0"},
        {"vr-device-cardboard-enable", "0"},
        {"force-driver-memory-reclaim", "1"},
        {"single-threaded-rendering", "0"}
    };

    for (const auto& kv : unityKeys) {
        patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenEngineOptimizations
  (JNIEnv *env, jclass, jstring jPath, jint targetFps, jint engineType) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);

    bool ok = false;
    if (engineType == 1 || pathStr.rfind("Engine.ini") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos) {
        ok = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUnrealEngineIni(env, nullptr, jPath, targetFps);
    } else if (engineType == 2 || pathStr.rfind("boot.config") != std::string::npos) {
        ok = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUnityBootConfig(env, nullptr, jPath, targetFps);
    } else {
        ok = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, targetFps);
    }

    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling
  (JNIEnv *env, jclass, jstring jPath, jint pollingRateHz) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    int rate = (pollingRateHz > 0) ? pollingRateHz : 1000;
    std::string rateStr = std::to_string(rate);

    std::vector<std::pair<std::string, std::string>> touchKeys = {
        {"TouchPollingRate", rateStr},
        {"TouchSampleRate", rateStr},
        {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"TouchSlopReduction", "1"},
        {"TouchResponseLevel", "3"},
        {"InputBufferRate", rateStr},
        {"TouchInterpolation", "1"}
    };

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    for (const auto& kv : touchKeys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, false);
        else patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    int fps = (targetFps > 0) ? targetFps : 120;
    std::string fpsStr = std::to_string(fps);

    std::vector<std::pair<std::string, std::string>> graphicsKeys = {
        {"TargetFPS", fpsStr},
        {"MaxFrameRate", fpsStr},
        {"FrameRateLimit", fpsStr},
        {"Vsync", "0"},
        {"bFramePacingEnabled", "1"},
        {"AllowOcclusionQueries", "1"},
        {"PreloadShaders", "1"},
        {"ResolutionScale", "120"},
        {"HDR10Plus", "1"},
        {"UltraExtreme", "1"},
        {"UltraExtreme2026", "1"},
        {"VulkanPipelineCache", "1"},
        {"AsyncCompute", "1"},
        {"VRS", "1"}
    };

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : graphicsKeys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, false);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPerGameProfile
  (JNIEnv *env, jclass, jstring jPath, jstring, jint targetFps, jboolean, jboolean, jboolean, jboolean) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, targetFps);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeAimCalibration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::vector<std::pair<std::string, std::string>> scopeKeys = {
        {"TouchPollingRate", "1000"},
        {"TouchSampleRate", "1000"},
        {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"NoScopeTouchRate", "1000"},
        {"HipfireDeadzone", "0"},
        {"HipfireSensitivityBoost", "1.2"},
        {"IronSightSensitivity", "1.0"},
        {"RedDotSensScale", "1.0"},
        {"HoloSensScale", "1.0"},
        {"Scope2xSensitivity", "1.0"},
        {"Scope2xGyroSample", "1000"},
        {"Scope3xSensitivity", "0.9"},
        {"Scope3xGyroStabilization", "1"},
        {"Scope4xSensitivity", "0.85"},
        {"Scope4xGyroStabilization", "1"},
        {"Scope6xSensitivity", "0.75"},
        {"Scope6xMicroDamping", "1"},
        {"Scope8xSensitivity", "0.65"},
        {"Scope8xPrecisionFilter", "1"},
        {"Scope8xGyro1000Hz", "1"},
        {"GyroSampleRate", "1000"},
        {"GyroZeroDelay", "1"},
        {"GyroStabilization", "1"}
    };

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : scopeKeys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, false);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHitRegDpsBoost
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::vector<std::pair<std::string, std::string>> hitRegKeys = {
        {"r.OneFrameThreadLag", "0"},
        {"r.FinishCurrentFrame", "0"},
        {"r.Streaming.PoolSize", "0"},
        {"r.MobileReduceLoadedMips", "0"},
        {"bFramePacingEnabled", "1"},
        {"InputBufferRate", "1000"},
        {"HitRegSyncRate", "1000"},
        {"ZeroInputLag", "1"},
        {"AllowOcclusionQueries", "1"},
        {"PreloadShaders", "1"}
    };

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : hitRegKeys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, false);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageLockMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::vector<std::pair<std::string, std::string>> damageLockKeys = {
        {"DamageLockMax", "1"},
        {"DamageBoost", "1"},
        {"EffectiveDPSMode", "3"},
        {"PenetrationBoost", "1"},
        {"CritRateBoost", "1"},
        {"FrameSyncDamage", "1"},
        {"HitRegSyncRate", "1000"},
        {"HitRegistrationRate", "1000"},
        {"InstantHitReg", "1"},
        {"BulletVelocityBoost", "1"},
        {"MuzzleVelocityFactor", "1.0"},
        {"r.OneFrameThreadLag", "0"},
        {"r.FinishCurrentFrame", "0"},
        {"bFramePacingEnabled", "1"},
        {"InputBufferRate", "1000"},
        {"ZeroInputLag", "1"},
        {"AllowOcclusionQueries", "1"},
        {"PreloadShaders", "1"},
        {"r.VSync", "0"},
        {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}
    };

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : damageLockKeys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, false);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssistLockMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::vector<std::pair<std::string, std::string>> aimAssistLockKeys = {
        {"AimAssistLockMax", "1"},
        {"AimAssistEnabled", "1"},
        {"AimAssistStrength", "100"},
        {"AimMagnetism", "3"},
        {"LockOnRange", "1.0"},
        {"AimSnapSpeed", "10"},
        {"AimSnapThreshold", "0"},
        {"AimStabilizer", "1"},
        {"HeadMagnetism", "1"},
        {"HeadBoneAimPriority", "1"},
        {"AdsZeroDelay", "1"},
        {"AimSmoothFactor", "0"},
        {"TouchPollingRate", "1000"},
        {"TouchSampleRate", "1000"},
        {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"GyroSampleRate", "1000"},
        {"GyroZeroDelay", "1"},
        {"GyroStabilization", "1"},
        {"GyroLatencyMode", "0"},
        {"GyroSensitivityRatio", "2.5"},
        {"HeroLock", "1"},
        {"SkillSmartAim", "1"},
        {"AimMethod", "1"},
        {"TargetPriority", "0"},
        {"WeaponSway", "0"},
        {"WeaponSpread", "0"},
        {"WeaponRecoilScale", "0"},
        {"PredictiveAim", "1"}
    };

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : aimAssistLockKeys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, false);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─── Direct Hardware Mask Profile Injector ───────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHardwareMaskProfile
  (JNIEnv *env, jclass, jstring jPath, jstring jGpuRenderer, jstring jSocModel, jint ramMb, jint targetHz) {
    if (!jPath) return JNI_FALSE;
    const char* pathChars = env->GetStringUTFChars(jPath, nullptr);
    if (!pathChars) return JNI_FALSE;
    std::string path(pathChars);
    env->ReleaseStringUTFChars(jPath, pathChars);

    std::string gpu = "Adreno (TM) 840";
    if (jGpuRenderer) {
        const char* g = env->GetStringUTFChars(jGpuRenderer, nullptr);
        if (g) { gpu = g; env->ReleaseStringUTFChars(jGpuRenderer, g); }
    }
    std::string soc = "Snapdragon 8 Elite";
    if (jSocModel) {
        const char* s = env->GetStringUTFChars(jSocModel, nullptr);
        if (s) { soc = s; env->ReleaseStringUTFChars(jSocModel, s); }
    }
    int hz = targetHz > 0 ? (targetHz < 120 ? 120 : (targetHz > 185 ? 185 : targetHz)) : 185;
    int ram = ramMb > 0 ? ramMb : 24576;

    struct stat stBefore;
    bool hasStat = (stat(path.c_str(), &stBefore) == 0);

    // CRITICAL FIX: Read existing file content from disk to prevent wiping existing user data / configs!
    std::string content = read_file_posix(path);

    if (path.find(".ini") != std::string::npos) {
        if (content.empty()) {
            std::ostringstream ss;
            ss << "[DeviceProfile]\n"
               << "DeviceName=" << soc << "\n"
               << "BaseProfileName=Android_High\n"
               << "GpuRenderer=" << gpu << "\n"
               << "GpuVendor=Qualcomm\n"
               << "TotalMemoryMB=" << ram << "\n"
               << "MaxRefreshRate=" << hz << "\n"
               << "TargetFPS=" << hz << "\n"
               << "QualityBucket=Ultra\n"
               << "+CVars=r.PUBGDeviceFPS=10\n"
               << "+CVars=r.PUBGFrameRateLimit=" << hz << "\n"
               << "+CVars=r.MobileFPSLimit=" << hz << "\n"
               << "+CVars=r.FrameRateLimit=" << hz << "\n"
               << "+CVars=r.MobileTouchBoostRate=" << hz << "\n"
               << "+CVars=r.MobileContentScaleFactor=1.0\n"
               << "+CVars=r.Vulkan.Enable=1\n"
               << "+CVars=r.Vulkan.FastPipeline=1\n"
               << "+CVars=r.Streaming.PoolSize=4096\n"
               << "+CVars=r.Android.DisableProgramBinaryCache=0\n"
               << "+CVars=r.MaxAnisotropy=16\n"
               << "+CVars=r.ShadowQuality=4\n"
               << "+CVars=r.TonemapperFilm=1\n"
               << "TouchPollingRate=1000\n"
               << "TouchZeroDelay=1\n"
               << "Unlock185Hz=1\n"
               << "Unlock165Hz=1\n"
               << "Unlock144Hz=1\n"
               << "Unlock120FPS=1\n";
            content = ss.str();
        } else {
            // Safely patch or append keys to existing INI
            std::vector<std::string> keysToPatch = {
                "DeviceName=" + soc,
                "GpuRenderer=" + gpu,
                "TotalMemoryMB=" + std::to_string(ram),
                "MaxRefreshRate=" + std::to_string(hz),
                "TargetFPS=" + std::to_string(hz)
            };
            for (const auto& kv : keysToPatch) {
                size_t eq = kv.find('=');
                if (eq != std::string::npos) {
                    std::string k = kv.substr(0, eq);
                    size_t pos = content.find(k + "=");
                    if (pos != std::string::npos) {
                        size_t end = content.find('\n', pos);
                        if (end == std::string::npos) end = content.length();
                        content.replace(pos, end - pos, kv);
                    } else {
                        size_t sec = content.find("[DeviceProfile]");
                        if (sec != std::string::npos) {
                            size_t afterSec = content.find('\n', sec);
                            if (afterSec != std::string::npos) {
                                content.insert(afterSec + 1, kv + "\n");
                            } else {
                                content += "\n" + kv + "\n";
                            }
                        } else {
                            content += "\n[DeviceProfile]\n" + kv + "\n";
                        }
                    }
                }
            }
        }
    } else if (path.find(".json") != std::string::npos) {
        if (content.empty()) {
            std::ostringstream ss;
            ss << "{\n"
               << "  \"DeviceModel\": \"" << soc << "\",\n"
               << "  \"DeviceBrand\": \"samsung\",\n"
               << "  \"Manufacturer\": \"samsung\",\n"
               << "  \"GPURenderer\": \"" << gpu << "\",\n"
               << "  \"GPUVendor\": \"Qualcomm\",\n"
               << "  \"SoCModel\": \"" << soc << "\",\n"
               << "  \"SoCManufacturer\": \"Qualcomm\",\n"
               << "  \"CPUCores\": 8,\n"
               << "  \"RAMTotalMB\": " << ram << ",\n"
               << "  \"MaxFrameRate\": " << hz << ",\n"
               << "  \"TargetFPS\": " << hz << ",\n"
               << "  \"FPSLimit\": " << hz << ",\n"
               << "  \"FrameRateLimit\": " << hz << ",\n"
               << "  \"MobileFPSLimit\": " << hz << ",\n"
               << "  \"FrameRateLevel\": 9,\n"
               << "  \"GraphicQuality\": 4,\n"
               << "  \"UnlockUltraHighFPS\": true,\n"
               << "  \"Unlock185Hz\": true,\n"
               << "  \"Unlock165Hz\": true,\n"
               << "  \"Unlock144Hz\": true,\n"
               << "  \"Unlock120Hz\": true,\n"
               << "  \"VulkanSupport\": true,\n"
               << "  \"gpu_renderer\": \"" << gpu << "\",\n"
               << "  \"gpu_vendor\": \"Qualcomm\",\n"
               << "  \"soc_model\": \"" << soc << "\",\n"
               << "  \"ram_mb\": " << ram << ",\n"
               << "  \"max_fps\": " << hz << ",\n"
               << "  \"target_hz\": " << hz << ",\n"
               << "  \"fps_limit\": " << hz << ",\n"
               << "  \"graphics_quality\": \"ultra\",\n"
               << "  \"vulkan_enabled\": true,\n"
               << "  \"unlock_ultra_fps\": true,\n"
               << "  \"touch_rate_hz\": 1000\n"
               << "}\n";
            content = ss.str();
        } else {
            patch_json_node(content, "GPURenderer", gpu, false);
            patch_json_node(content, "GPUVendor", "Qualcomm", false);
            patch_json_node(content, "SoCModel", soc, false);
            patch_json_node(content, "RAMTotalMB", std::to_string(ram), true);
            patch_json_node(content, "MaxFrameRate", std::to_string(hz), true);
            patch_json_node(content, "TargetFPS", std::to_string(hz), true);
            patch_json_node(content, "Unlock120Hz", "true", true);
            patch_json_node(content, "Unlock165Hz", "true", true);
        }
    } else if (path.find(".xml") != std::string::npos) {
        if (content.empty() || content.find("<map>") == std::string::npos) {
            content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n</map>\n";
        }
        patch_xml_node(content, "string", "SystemInfo_graphicsDeviceName", gpu);
        patch_xml_node(content, "string", "SystemInfo_graphicsDeviceVendor", "Qualcomm");
        patch_xml_node(content, "string", "SystemInfo_deviceModel", soc);
        patch_xml_node(content, "int", "SystemInfo_systemMemorySize", std::to_string(ram));
        patch_xml_node(content, "int", "SystemInfo_graphicsMemorySize", "8192");
        patch_xml_node(content, "int", "TargetFrameRate", std::to_string(hz));
        patch_xml_node(content, "int", "MaxRefreshRate", std::to_string(hz));
        patch_xml_node(content, "int", "Unlock185Hz", "1");
        patch_xml_node(content, "int", "Unlock165Hz", "1");
        patch_xml_node(content, "int", "Unlock144Hz", "1");
        patch_xml_node(content, "int", "Unlock120Hz", "1");
    } else {
        if (content.empty()) {
            std::ostringstream ss;
            ss << "gpu=" << gpu << "\n"
               << "soc=" << soc << "\n"
               << "ram=" << ram << "\n"
               << "hz=" << hz << "\n"
               << "TargetFPS=" << hz << "\n"
               << "Unlock185Hz=1\n"
               << "Unlock165Hz=1\n"
               << "Unlock144Hz=1\n"
               << "Unlock120Hz=1\n";
            content = ss.str();
        }
    }

    bool ok = write_file_atomic(path, content, 0666);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path.c_str(), &times);
    }
    LOGI("HardwareMaskProfile native inject: %s [ok=%d, gpu=%s, hz=%d, ram=%d]", path.c_str(), ok, gpu.c_str(), hz, ram);
    return ok ? JNI_TRUE : JNI_FALSE;
}

