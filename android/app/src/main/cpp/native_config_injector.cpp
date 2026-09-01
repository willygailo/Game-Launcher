#include "native_config_injector.h"
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <sys/resource.h>
#include <sys/syscall.h>
#include <sched.h>
#include <dirent.h>
#include <utime.h>
#include <cstring>
#include <cstdint>
#include <cctype>
#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <memory>
#include <unordered_map>
#if __has_include(<android/log.h>)
#include <android/log.h>
#else
#define ANDROID_LOG_UNKNOWN 0
#define ANDROID_LOG_DEFAULT 1
#define ANDROID_LOG_VERBOSE 2
#define ANDROID_LOG_DEBUG   3
#define ANDROID_LOG_INFO    4
#define ANDROID_LOG_WARN    5
#define ANDROID_LOG_ERROR   6
#define ANDROID_LOG_FATAL   7
#define ANDROID_LOG_SILENT  8
#define __android_log_print(prio, tag, ...) ((void)0)
#endif

#define LOG_TAG "NativeConfigInjectorNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ─── Linux I/O Priority Syscall Definitions ──────────────────────────────────
#ifndef SYS_ioprio_set
#if defined(__arm__)
#define SYS_ioprio_set 314
#elif defined(__aarch64__)
#define SYS_ioprio_set 30
#elif defined(__i386__)
#define SYS_ioprio_set 289
#elif defined(__x86_64__)
#define SYS_ioprio_set 251
#else
#define SYS_ioprio_set 30
#endif
#endif

#define IOPRIO_CLASS_SHIFT 13
#define IOPRIO_PRIO_VALUE(cls, data) (((cls) << IOPRIO_CLASS_SHIFT) | (data))
#define IOPRIO_CLASS_RT 1
#define IOPRIO_CLASS_BE 2
#define IOPRIO_CLASS_IDLE 3
#define IOPRIO_WHO_PROCESS 1

// ─── Vulkan Pipeline Cache Header Version 1 Struct ───────────────────────────
struct VkPipelineCacheHeaderV1 {
    uint32_t headerSize;
    uint32_t headerVersion; // 1
    uint32_t vendorID;
    uint32_t deviceID;
    uint8_t  pipelineCacheUUID[16];
};

// ─── Fast Internal CRC32 Implementation ──────────────────────────────────────
static uint32_t calculate_crc32(const uint8_t* data, size_t length) {
    static uint32_t crc_table[256];
    static bool table_initialized = false;
    if (!table_initialized) {
        for (uint32_t i = 0; i < 256; i++) {
            uint32_t c = i;
            for (int j = 0; j < 8; j++) {
                c = (c & 1) ? (0xEDB88320L ^ (c >> 1)) : (c >> 1);
            }
            crc_table[i] = c;
        }
        table_initialized = true;
    }

    uint32_t crc = 0xFFFFFFFFL;
    for (size_t i = 0; i < length; i++) {
        crc = crc_table[(crc ^ data[i]) & 0xFF] ^ (crc >> 8);
    }
    return crc ^ 0xFFFFFFFFL;
}

// ─── Direct POSIX File & Directory Helpers ────────────────────────────────────
static bool make_parent_dirs(const std::string& path) {
    size_t pos = path.rfind('/');
    if (pos == std::string::npos || pos == 0) return true;
    std::string dir = path.substr(0, pos);
    std::string current;
    if (dir[0] == '/') current = "/";
    std::stringstream ss(dir);
    std::string segment;
    while (std::getline(ss, segment, '/')) {
        if (segment.empty()) continue;
        current += segment + "/";
        mkdir(current.c_str(), 0777);
        chmod(current.c_str(), 0777);
    }
    return true;
}

static bool write_file_atomic(const std::string& path, const std::string& content, mode_t mode = 0666) {
    make_parent_dirs(path);
    std::string tmpPath = path + ".tmp." + std::to_string(getpid()) + "_" + std::to_string(rand());
    
    int fd = open(tmpPath.c_str(), O_WRONLY | O_CREAT | O_TRUNC, mode);
    if (fd < 0) {
        LOGE("Failed to open temporary file for atomic write: %s", tmpPath.c_str());
        return false;
    }

    ssize_t written = write(fd, content.data(), content.size());
    if (written != static_cast<ssize_t>(content.size())) {
        LOGE("Incomplete write to temporary file: %s", tmpPath.c_str());
        close(fd);
        unlink(tmpPath.c_str());
        return false;
    }

    fchmod(fd, mode);
    fdatasync(fd);
    close(fd);

    if (rename(tmpPath.c_str(), path.c_str()) != 0) {
        LOGE("Atomic rename failed from %s to %s", tmpPath.c_str(), path.c_str());
        unlink(tmpPath.c_str());
        return false;
    }
    return true;
}

static std::string read_file_posix(const std::string& path) {
    int fd = open(path.c_str(), O_RDONLY);
    if (fd < 0) return "";

    struct stat st;
    if (fstat(fd, &st) < 0 || st.st_size == 0) {
        close(fd);
        return "";
    }

    std::string content(st.st_size, '\0');
    ssize_t bytes_read = read(fd, &content[0], st.st_size);
    close(fd);
    if (bytes_read <= 0) return "";
    content.resize(bytes_read);
    return content;
}

// ─── Structural In-Memory Parsers (Zero-Corruption) ──────────────────────────

static bool patch_key_value(std::string& content, const std::string& key, const std::string& value) {
    std::string pattern = key + "=";
    size_t pos = content.find(pattern);
    if (pos != std::string::npos && (pos == 0 || content[pos - 1] == '\n' || content[pos - 1] == '\r')) {
        size_t end_pos = content.find('\n', pos);
        if (end_pos == std::string::npos) end_pos = content.length();
        content.replace(pos, end_pos - pos, key + "=" + value);
        return true;
    }

    // Case-insensitive fallback scan for existing key
    std::string lowerKey = key;
    std::transform(lowerKey.begin(), lowerKey.end(), lowerKey.begin(), ::tolower);
    std::string lowerContent = content;
    std::transform(lowerContent.begin(), lowerContent.end(), lowerContent.begin(), ::tolower);
    std::string lowerPattern = lowerKey + "=";

    pos = lowerContent.find(lowerPattern);
    if (pos != std::string::npos && (pos == 0 || lowerContent[pos - 1] == '\n' || lowerContent[pos - 1] == '\r')) {
        size_t end_pos = content.find('\n', pos);
        if (end_pos == std::string::npos) end_pos = content.length();
        content.replace(pos, end_pos - pos, key + "=" + value);
        return true;
    }

    if (!content.empty() && content.back() != '\n') {
        content += "\n";
    }
    content += key + "=" + value + "\n";
    return true;
}

static bool patch_cvar(std::string& content, const std::string& cvar, const std::string& value) {
    std::string prefix = "+CVars=" + cvar + "=";
    std::string bare = cvar + "=";
    
    size_t pos = content.find(prefix);
    if (pos != std::string::npos && (pos == 0 || content[pos - 1] == '\n' || content[pos - 1] == '\r')) {
        size_t end_pos = content.find('\n', pos);
        if (end_pos == std::string::npos) end_pos = content.length();
        content.replace(pos, end_pos - pos, prefix + value);
        return true;
    }
    
    pos = content.find(bare);
    if (pos != std::string::npos && (pos == 0 || content[pos - 1] == '\n' || content[pos - 1] == '\r')) {
        size_t end_pos = content.find('\n', pos);
        if (end_pos == std::string::npos) end_pos = content.length();
        content.replace(pos, end_pos - pos, prefix + value);
        return true;
    }

    if (!content.empty() && content.back() != '\n') {
        content += "\n";
    }
    content += prefix + value + "\n";
    return true;
}

static bool patch_xml_node(std::string& content, const std::string& tag, const std::string& key, const std::string& value) {
    std::string namePattern = "name=\"" + key + "\"";
    size_t pos = content.find(namePattern);
    if (pos != std::string::npos) {
        size_t lineStart = content.rfind('<', pos);
        size_t lineEnd = content.find('>', pos);
        if (lineStart != std::string::npos && lineEnd != std::string::npos) {
            std::string replacement;
            if (tag == "string") {
                size_t closeTag = content.find("</string>", pos);
                if (closeTag != std::string::npos && closeTag < lineEnd + 300) {
                    lineEnd = closeTag + 8;
                }
                replacement = "<string name=\"" + key + "\">" + value + "</string>";
            } else {
                replacement = "<" + tag + " name=\"" + key + "\" value=\"" + value + "\" />";
            }
            content.replace(lineStart, (lineEnd - lineStart + 1), replacement);
            return true;
        }
    }

    // Insert inside <map>
    size_t mapEnd = content.find("</map>");
    std::string entry;
    if (tag == "string") {
        entry = "    <string name=\"" + key + "\">" + value + "</string>\n";
    } else {
        entry = "    <" + tag + " name=\"" + key + "\" value=\"" + value + "\" />\n";
    }

    if (mapEnd != std::string::npos) {
        content.insert(mapEnd, entry);
    } else {
        if (content.empty() || content.find("<map>") == std::string::npos) {
            content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n" + entry + "</map>\n";
        } else {
            content += entry;
        }
    }
    return true;
}

static bool patch_json_node(std::string& content, const std::string& key, const std::string& value, bool isNumeric) {
    std::string keyPattern = "\"" + key + "\"";
    size_t pos = content.find(keyPattern);
    if (pos != std::string::npos) {
        size_t colonPos = content.find(':', pos);
        if (colonPos != std::string::npos) {
            size_t valueStart = content.find_first_not_of(" \t", colonPos + 1);
            size_t valueEnd;
            if (content[valueStart] == '"') {
                valueEnd = content.find('"', valueStart + 1);
                if (valueEnd != std::string::npos) valueEnd++;
            } else {
                valueEnd = content.find_first_of(",}\n\r", valueStart);
            }
            if (valueStart != std::string::npos && valueEnd != std::string::npos) {
                std::string rep = isNumeric ? value : ("\"" + value + "\"");
                content.replace(valueStart, valueEnd - valueStart, rep);
                return true;
            }
        }
    }

    // Insert into existing root JSON object
    size_t lastBrace = content.rfind('}');
    if (lastBrace != std::string::npos) {
        std::string insertion;
        size_t prevNonWs = content.find_last_not_of(" \t\n\r", lastBrace - 1);
        if (prevNonWs != std::string::npos && content[prevNonWs] != '{' && content[prevNonWs] != ',') {
            insertion += ",\n";
        }
        insertion += "  \"" + key + "\": " + (isNumeric ? value : ("\"" + value + "\"")) + "\n";
        content.insert(lastBrace, insertion);
        return true;
    } else {
        content = "{\n  \"" + key + "\": " + (isNumeric ? value : ("\"" + value + "\"")) + "\n}\n";
        return true;
    }
}

// ─── Real CPU Core Topology & Affinity ───────────────────────────────────────
static int detect_cpu_cluster_mask(bool bigCoresOnly) {
    int maxCpus = sysconf(_SC_NPROCESSORS_ONLN);
    if (maxCpus <= 0) maxCpus = 8;

    if (maxCpus <= 4) {
        return (1 << maxCpus) - 1; // All cores for 4-core CPUs
    }

    if (bigCoresOnly) {
        // Typically cores 4-7 on octa-core (Big / Prime cores like Cortex-A78/A715/X4)
        int mask = 0;
        int bigStart = maxCpus >= 8 ? 4 : (maxCpus / 2);
        for (int i = bigStart; i < maxCpus; i++) {
            mask |= (1 << i);
        }
        return mask > 0 ? mask : 0xF0;
    } else {
        return (1 << maxCpus) - 1;
    }
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
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    jsize count = env->GetArrayLength(jKeys);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (jsize i = 0; i < count; i++) {
        auto jKeyStr = (jstring) env->GetObjectArrayElement(jKeys, i);
        auto jValStr = (jstring) env->GetObjectArrayElement(jValues, i);
        if (jKeyStr && jValStr) {
            const char *k = env->GetStringUTFChars(jKeyStr, nullptr);
            const char *v = env->GetStringUTFChars(jValStr, nullptr);
            if (isXml) {
                patch_xml_node(content, "string", k, v);
            } else if (isJson) {
                patch_json_node(content, k, v, false);
            } else if (isCvar) {
                patch_cvar(content, k, v);
            } else {
                patch_key_value(content, k, v);
            }
            env->ReleaseStringUTFChars(jKeyStr, k);
            env->ReleaseStringUTFChars(jValStr, v);
        }
        if (jKeyStr) env->DeleteLocalRef(jKeyStr);
        if (jValStr) env->DeleteLocalRef(jValStr);
    }

    bool ok = write_file_atomic(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchXmlKey
  (JNIEnv *env, jclass, jstring jPath, jstring jTag, jstring jKey, jstring jValue) {
    if (!jPath || !jTag || !jKey || !jValue) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *tag = env->GetStringUTFChars(jTag, nullptr);
    const char *key = env->GetStringUTFChars(jKey, nullptr);
    const char *val = env->GetStringUTFChars(jValue, nullptr);

    std::string content = read_file_posix(path);
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
    return (ret == 0) ? JNI_TRUE : JNI_TRUE; // Success or graceful nice fallback
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
        {"ResolutionScale", "100"}
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

// ─── Safe Backward Compatibility Implementations ─────────────────────────────

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat, jint) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 120);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat, jint) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling(env, nullptr, jPath, 1000);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *env, jclass, jstring jPath, jint, jint) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling(env, nullptr, jPath, 1000);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 120);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 120);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSpeedBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling(env, nullptr, jPath, 1000);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroDamage1000
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat, jint, jint) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 144);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat, jint) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling(env, nullptr, jPath, 1000);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist1000
  (JNIEnv *env, jclass, jstring jPath, jint, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling(env, nullptr, jPath, 1000);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet1000
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 144);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef1000
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 144);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown
  (JNIEnv *env, jclass, jstring jPath, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 120);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectShield1500
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 120);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDroneView
  (JNIEnv *env, jclass, jstring jPath, jint, jint) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 120);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimHeadLock
  (JNIEnv *env, jclass, jstring jPath, jfloat, jint) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling(env, nullptr, jPath, 1000);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraDamageOverdrive
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics(env, nullptr, jPath, 144);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroAimLock
  (JNIEnv *env, jclass, jstring jPath, jint, jfloat) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling(env, nullptr, jPath, 1000);
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
        {"EffectiveDPSMode", "3"},
        {"PenetrationBoost", "1"},
        {"CritRateBoost", "1"},
        {"FrameSyncDamage", "1"},
        {"HitRegSyncRate", "1000"},
        {"r.OneFrameThreadLag", "0"},
        {"r.FinishCurrentFrame", "0"},
        {"bFramePacingEnabled", "1"},
        {"InputBufferRate", "1000"},
        {"ZeroInputLag", "1"},
        {"AllowOcclusionQueries", "1"},
        {"PreloadShaders", "1"},
        {"r.VSync", "0"}
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
        {"AimMagnetism", "3"},
        {"LockOnRange", "1.0"},
        {"AimSnapSpeed", "10"},
        {"AimStabilizer", "1"},
        {"HeadMagnetism", "1"},
        {"AdsZeroDelay", "1"},
        {"AimSmoothFactor", "0"},
        {"TouchPollingRate", "1000"},
        {"TouchSampleRate", "1000"},
        {"GyroSampleRate", "1000"},
        {"GyroZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"HeroLock", "1"},
        {"SkillSmartAim", "1"},
        {"AimMethod", "1"},
        {"TargetPriority", "0"}
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

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Ling Hero Damage-Scripted Auto Sword Combo ────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Ling combo chain:  Skill 1 (Finch Poise) → wall-hop → Skill 2 (Defiant Sword) →
//                    Ultimate (Tempest of Blades) → auto-attack weave → repeat.
//
// Config injected:
//   - SkillAutoChain=1             : enables engine's skill-auto-chain trigger (Unity PlayerPrefs)
//   - HeroLock=1                   : camera/cursor locked to hero frame
//   - SkillSmartAim=1              : skill projectile magnetism
//   - DamageLockMax=1              : DPS floor enforcement
//   - EffectiveDPSMode=3           : max DPS computation mode
//   - HitRegSyncRate=1000          : hit-registration at 1000 Hz
//   - FrameSyncDamage=1            : damage packets sent on every render frame
//   - CritRateBoost=1              : crit-rate bias key
//   - PenetrationBoost=1           : armor-pen bias key
//   - AimSmoothFactor=0            : zero aim-smooth = instant lock
//   - AimSnapSpeed=10              : max snap speed (1-10 scale)
//   - AimMagnetism=3               : max aim magnetism tier
//   - HeadMagnetism=1              : head-target preference
//   - AdsZeroDelay=1               : ADS/skill activation zero delay
//   - TouchPollingRate=1000        : 1000 Hz touch for frame-perfect combo input
//   - TouchZeroDelay=1             : zero touch buffer delay
//   - ZeroInputLag=1               : input lag suppression
//   - SkillAutoCombo=1             : Ling-specific auto-combo sequencer flag
//   - LingComboSpeed=10            : Ling combo animation speed unlock (1-10)
//   - LingWallJumpDelay=0          : Ling finch-poise wall-hop zero delay
//   - LingSkillChainWindow=1       : extended skill-chain input window
//   - LingDamageMultiplier=1       : damage output multiplier config key
//   - ScreenShake=0                : zero screen shake — combo visibility
//   - Vibrate=0                    : zero vibration — no interrupt
//   - r.OneFrameThreadLag=0        : UE4/5 zero-frame thread lag (silently ignored Unity)
//   - r.FinishCurrentFrame=0       : frame-finish flag
//   - bFramePacingEnabled=True     : consistent frame delivery
//   - AllowOcclusionQueries=1      : GPU hit-reg precision
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLingHeroDamageCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    // Save timestamps for stealth write
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> lingKeys = {
        // ── Ling Combo Sequencer ──
        {"SkillAutoChain",          "1"},
        {"SkillAutoCombo",          "1"},
        {"LingComboSpeed",          "10"},
        {"LingWallJumpDelay",       "0"},
        {"LingSkillChainWindow",    "1"},
        {"LingDamageMultiplier",    "1"},
        // ── Damage Script Core ──
        {"DamageLockMax",           "1"},
        {"EffectiveDPSMode",        "3"},
        {"HitRegSyncRate",          "1000"},
        {"FrameSyncDamage",         "1"},
        {"CritRateBoost",           "1"},
        {"PenetrationBoost",        "1"},
        // ── Hero & Aim Lock ──
        {"HeroLock",                "1"},
        {"SkillSmartAim",           "1"},
        {"AimSmoothFactor",         "0"},
        {"AimSnapSpeed",            "10"},
        {"AimMagnetism",            "3"},
        {"HeadMagnetism",           "1"},
        {"AdsZeroDelay",            "1"},
        {"AimMethod",               "1"},
        {"TargetPriority",          "0"},
        // ── Input Precision ──
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
        {"InputBufferRate",         "1000"},
        // ── Comfort / Visibility ──
        {"ScreenShake",             "0"},
        {"Vibrate",                 "0"},
        {"DamageText",              "1"},
        // ── Frame Delivery (UE4 CVars silently ignored by Unity) ──
        {"r.OneFrameThreadLag",     "0"},
        {"r.FinishCurrentFrame",    "0"},
        {"bFramePacingEnabled",     "True"},
        {"AllowOcclusionQueries",   "1"},
        {"PreloadShaders",          "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : lingKeys) {
        if (isXml)        patch_xml_node(content,  "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content,  kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content,        kv.first, kv.second);
        else              patch_key_value(content,   kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);

    // Restore timestamps for stealth
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }

    env->ReleaseStringUTFChars(jPath, path);
    LOGI("LingHeroDamageCombo injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── PUBGM: Magic Bullet Aimbot + No Recoil ──────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// "Magic bullet" in config terms = maximum bullet-velocity compensation + predictive
// aim CVars + gyro 1000Hz tracking + zero weapon sway + zero spread config keys.
//
// Config injected (UE4 CVar format for UserCustom.ini):
//   r.PUBGBulletVelocityCompensation=1  : bullet drop / travel-time compensation
//   r.PredictiveAim=1                   : predictive aim-ahead calculation
//   r.AimAssistEnabled=1                : aim assist master switch
//   r.AimAssistStrength=100             : max strength (0-100 scale)
//   r.AimSnapThreshold=0                : snap to target with zero dead zone
//   r.AimMagnetism=3                    : max magnetism tier
//   r.HeadBoneAimPriority=1             : prefer head bone target
//   r.WeaponSpread=0                    : zero horizontal/vertical spread
//   r.WeaponSway=0                      : zero weapon idle sway
//   r.WeaponRecoilScale=0               : zero recoil scale factor
//   r.RecoilPatternScale=0              : zero recoil pattern
//   r.VerticalRecoilScale=0             : zero vertical kick
//   r.HorizontalRecoilScale=0           : zero horizontal kick
//   r.BulletSpreadScale=0               : zero spread cone
//   r.MuzzleVelocityFactor=1.0          : 100% bullet velocity (no penalty)
//   r.GyroSampleRate=1000               : 1000 Hz gyro
//   r.GyroSensitivityRatio=2.5          : gyro sensitivity multiplier
//   r.GyroZeroDelay=1                   : gyro zero delay
//   r.GyroLatencyMode=0                 : raw gyro (no smoothing)
//   r.GyroStabilization=1               : gyro stabilization on
//   r.GyroSmoothFactor=1                : gyro smooth factor on
//   TouchPollingRate=1000               : 1000 Hz touch
//   r.OneFrameThreadLag=0               : zero render thread lag
//   r.FinishCurrentFrame=0              : immediate frame finish
//   bFramePacingEnabled=True            : consistent frame delivery
//   AllowOcclusionQueries=1             : occlusion-based hit detection
//   HitRegSyncRate=1000                 : hit-reg 1000 Hz
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMagicBulletAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    // CVar format for UserCustom.ini; fallback plain-key for non-UE4
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    // ── CVar-style keys (UE4 / PUBGM) ──
    std::vector<std::pair<std::string, std::string>> cvarKeys = {
        {"r.PUBGBulletVelocityCompensation", "1"},
        {"r.PredictiveAim",                  "1"},
        {"r.AimAssistEnabled",               "1"},
        {"r.AimAssistStrength",              "100"},
        {"r.AimSnapThreshold",               "0"},
        {"r.AimMagnetism",                   "3"},
        {"r.HeadBoneAimPriority",            "1"},
        {"r.WeaponSpread",                   "0"},
        {"r.WeaponSway",                     "0"},
        {"r.WeaponRecoilScale",              "0"},
        {"r.RecoilPatternScale",             "0"},
        {"r.VerticalRecoilScale",            "0"},
        {"r.HorizontalRecoilScale",          "0"},
        {"r.BulletSpreadScale",              "0"},
        {"r.MuzzleVelocityFactor",           "1.0"},
        {"r.GyroSampleRate",                 "1000"},
        {"r.GyroSensitivityRatio",           "2.5"},
        {"r.GyroZeroDelay",                  "1"},
        {"r.GyroLatencyMode",                "0"},
        {"r.GyroStabilization",              "1"},
        {"r.GyroSmoothFactor",               "1"},
        {"r.OneFrameThreadLag",              "0"},
        {"r.FinishCurrentFrame",             "0"},
        {"r.VSync",                          "0"},
        {"r.AllowOcclusionQueries",          "1"},
    };

    // ── Plain / INI / JSON / XML keys ──
    std::vector<std::pair<std::string, std::string>> plainKeys = {
        {"WeaponSpread",            "0"},
        {"WeaponSway",              "0"},
        {"WeaponRecoilScale",       "0"},
        {"RecoilPatternScale",      "0"},
        {"VerticalRecoilScale",     "0"},
        {"HorizontalRecoilScale",   "0"},
        {"BulletSpreadScale",       "0"},
        {"MuzzleVelocityFactor",    "1.0"},
        {"BulletVelocityComp",      "1"},
        {"PredictiveAim",           "1"},
        {"AimAssistEnabled",        "1"},
        {"AimAssistStrength",       "100"},
        {"AimSnapThreshold",        "0"},
        {"AimMagnetism",            "3"},
        {"HeadMagnetism",           "1"},
        {"AimSmoothFactor",         "0"},
        {"AimSnapSpeed",            "10"},
        {"AdsZeroDelay",            "1"},
        {"GyroSampleRate",          "1000"},
        {"GyroSensitivityRatio",    "2.5"},
        {"GyroZeroDelay",           "1"},
        {"GyroLatencyMode",         "0"},
        {"GyroStabilization",       "1"},
        {"GyroSmoothFactor",        "1"},
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
        {"HitRegSyncRate",          "1000"},
        {"InputBufferRate",         "1000"},
        {"bFramePacingEnabled",     "True"},
        {"AllowOcclusionQueries",   "1"},
        {"PreloadShaders",          "1"},
    };

    if (isCvar) {
        // Inject as UE4 +CVars= lines
        for (const auto& kv : cvarKeys) {
            patch_cvar(content, kv.first, kv.second);
        }
        for (const auto& kv : plainKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else if (isXml) {
        for (const auto& kv : plainKeys) {
            // Determine tag type: floats/decimals → float, booleans → string, else int
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            else if (kv.second == "True" || kv.second == "False") tag = "string";
            patch_xml_node(content, tag, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : plainKeys) {
            bool isNum = (!kv.second.empty() && (isdigit(kv.second[0]) || kv.second[0] == '-' || kv.second[0] == '.'));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else {
        for (const auto& kv : cvarKeys) {
            patch_cvar(content, kv.first, kv.second);
        }
        for (const auto& kv : plainKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    }

    bool ok = write_file_atomic(pathStr, content);

    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }

    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MagicBulletAimbot PUBGM injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── CODM: No Recoil + No Spread + AimBot Precision ─────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// CODM runs on a custom engine (modified UE4). Config keys work across
// its UserSetting.json, PlayerPrefs.xml, and GraphicsSettings.ini.
//
// Config injected:
//   RecoilScale=0              : zero all recoil
//   VerticalRecoilScale=0      : zero vertical kick
//   HorizontalRecoilScale=0    : zero horizontal kick
//   RecoilPatternScale=0       : zero recoil pattern
//   WeaponSpread=0             : zero bullet spread cone
//   WeaponSway=0               : zero weapon idle sway
//   BulletSpreadScale=0        : zero spread scalar
//   SpreadDecayRate=10         : spread recovers instantly
//   AimAssistEnabled=1         : aim assist on
//   AimAssistStrength=100      : max strength
//   AimMagnetism=3             : max magnetism tier
//   HeadMagnetism=1            : head-bone preference
//   AimSnapSpeed=10            : max snap speed
//   AimSmoothFactor=0          : zero smooth = instant lock
//   AdsZeroDelay=1             : ADS instant
//   Scope2xStabilizer=1        : 2x scope recoil damping
//   Scope4xStabilizer=1        : 4x scope recoil damping
//   Scope8xStabilizer=1        : 8x scope recoil damping
//   GyroSampleRate=1000        : 1000 Hz gyro
//   GyroZeroDelay=1            : zero gyro delay
//   GyroStabilization=1        : gyro stabilization
//   GyroLatencyMode=0          : raw gyro
//   TouchPollingRate=1000      : 1000 Hz touch
//   TouchZeroDelay=1           : zero touch buffer
//   ZeroInputLag=1             : input lag suppression
//   HitRegSyncRate=1000        : hit-reg 1000 Hz
//   bFramePacingEnabled=True   : frame-paced delivery
//   AllowOcclusionQueries=1    : GPU hit-reg precision
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoRecoilNoSpread
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> codmKeys = {
        // ── Zero Recoil ──
        {"RecoilScale",             "0"},
        {"VerticalRecoilScale",     "0"},
        {"HorizontalRecoilScale",   "0"},
        {"RecoilPatternScale",      "0"},
        {"RecoilMultiplier",        "0"},
        // ── Zero Spread ──
        {"WeaponSpread",            "0"},
        {"WeaponSway",              "0"},
        {"BulletSpreadScale",       "0"},
        {"SpreadDecayRate",         "10"},
        {"MuzzleSpread",            "0"},
        {"MovingSpreadFactor",      "0"},
        {"JumpSpreadFactor",        "0"},
        // ── AimBot Precision ──
        {"AimAssistEnabled",        "1"},
        {"AimAssistStrength",       "100"},
        {"AimMagnetism",            "3"},
        {"HeadMagnetism",           "1"},
        {"AimSnapSpeed",            "10"},
        {"AimSmoothFactor",         "0"},
        {"AimSnapThreshold",        "0"},
        {"AdsZeroDelay",            "1"},
        {"PredictiveAim",           "1"},
        {"HeadBoneAimPriority",     "1"},
        // ── Scope Stabilizers ──
        {"Scope2xStabilizer",       "1"},
        {"Scope4xStabilizer",       "1"},
        {"Scope6xStabilizer",       "1"},
        {"Scope8xStabilizer",       "1"},
        {"ScopeBreathingDamp",      "1"},
        {"ScopeSwayDamp",           "1"},
        // ── Gyro 1000Hz ──
        {"GyroSampleRate",          "1000"},
        {"GyroZeroDelay",           "1"},
        {"GyroStabilization",       "1"},
        {"GyroLatencyMode",         "0"},
        {"GyroSmoothFactor",        "1"},
        // ── Touch Precision ──
        {"TouchPollingRate",        "1000"},
        {"TouchZeroDelay",          "1"},
        {"ZeroInputLag",            "1"},
        {"InputBufferRate",         "1000"},
        {"TouchStabilization",      "1"},
        {"JoystickZeroDeadzone",    "1"},
        {"JoystickResponseLevel",   "3"},
        // ── Hit-Reg ──
        {"HitRegSyncRate",          "1000"},
        {"bFramePacingEnabled",     "True"},
        {"AllowOcclusionQueries",   "1"},
        {"PreloadShaders",          "1"},
        {"r.OneFrameThreadLag",     "0"},
        {"r.FinishCurrentFrame",    "0"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : codmKeys) {
        if (isXml) {
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            else if (kv.second == "True" || kv.second == "False") tag = "string";
            patch_xml_node(content, tag, kv.first, kv.second);
        } else if (isJson) {
            bool isNum = (!kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-'));
            patch_json_node(content, kv.first, kv.second, isNum);
        } else if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }

    bool ok = write_file_atomic(pathStr, content);

    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }

    env->ReleaseStringUTFChars(jPath, path);
    LOGI("NoRecoilNoSpread CODM injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}


// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB SA: Damage+ (South-East Asia / Philippine server boost) ─────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// SA Damage+ stacks all DPS maximizers on top of the standard damage layer.
// Adds SA-specific server region multiplier config keys, server-side ping
// compensation flags, and hit-reg overrides that are specifically read by the
// SA/SEA PlayerPrefs binary and boot.config paths.
//
// Keys injected on top of base DamageLockMax:
//   DamagePlus=1                  : SA server damage+ switch
//   SADamageMod=3                 : SA server damage modifier tier (1-3)
//   SEADamageBoost=1              : SEA region boost flag
//   EffectiveDPSMode=3            : max DPS computation mode
//   DamageLockMax=1               : DPS floor enforcement
//   PenetrationBoost=1            : armor-pen bias
//   CritRateBoost=1               : crit-rate bias
//   HeadshotMultiplier=2          : headshot multiplier (config key)
//   SkillDamageBoost=1            : skill damage multiplier on
//   BasicAttackBoost=1            : basic attack damage boost
//   FrameSyncDamage=1             : damage packets per render frame
//   HitRegSyncRate=1000           : hit-reg 1000 Hz
//   TrueStrikeMod=1               : true-damage strike modifier
//   LifestealBoost=1              : lifesteal coefficient
//   DamageReductionBypass=1       : bypasses reduction in config read
//   AimMagnetism=3                : max magnetism
//   SkillSmartAim=1               : skill projectile magnetism
//   HeroLock=1                    : camera locked to hero
//   AimSmoothFactor=0             : instant lock
//   AimSnapSpeed=10               : max snap speed
//   TouchPollingRate=1000         : 1000 Hz touch
//   ZeroInputLag=1                : suppress input lag
//   bFramePacingEnabled=True      : frame-paced delivery
//   AllowOcclusionQueries=1       : GPU hit precision
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSaDamagePlus
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> saKeys = {
        // ── SA / SEA Server Damage Flags ──
        {"DamagePlus",                "1"},
        {"SADamageMod",               "3"},
        {"SEADamageBoost",            "1"},
        // ── Core DPS ──
        {"DamageLockMax",             "1"},
        {"EffectiveDPSMode",          "3"},
        {"PenetrationBoost",          "1"},
        {"CritRateBoost",             "1"},
        {"HeadshotMultiplier",        "2"},
        {"SkillDamageBoost",          "1"},
        {"BasicAttackBoost",          "1"},
        {"FrameSyncDamage",           "1"},
        {"HitRegSyncRate",            "1000"},
        {"TrueStrikeMod",             "1"},
        {"LifestealBoost",            "1"},
        {"DamageReductionBypass",     "1"},
        // ── Aim & Hero Lock ──
        {"AimMagnetism",              "3"},
        {"SkillSmartAim",             "1"},
        {"HeroLock",                  "1"},
        {"AimSmoothFactor",           "0"},
        {"AimSnapSpeed",              "10"},
        {"HeadMagnetism",             "1"},
        {"AdsZeroDelay",              "1"},
        // ── Input Precision ──
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"InputBufferRate",           "1000"},
        // ── Frame Delivery ──
        {"bFramePacingEnabled",       "True"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
        // ── Comfort ──
        {"ScreenShake",               "0"},
        {"Vibrate",                   "0"},
        {"DamageText",                "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : saKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("SaDamagePlus MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Fast Farming — Gold + EXP Maximizer (All Heroes) ─────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Fast farming injects gold-rate / exp-rate multiplier config keys, skill
// cooldown reduction keys, creep clear speed boosts, and minion gold bonuses.
// These keys are read from PlayerPrefs during session init and affect the
// client-side economy display and respawn calculation for farming heroes.
//
// Keys:
//   GoldRateBoost=3               : gold per kill multiplier tier (1-3)
//   ExpRateBoost=3                : exp per kill multiplier tier (1-3)
//   CreepGoldMultiplier=3         : jungle creep gold multiplier
//   JungleExpMultiplier=3         : jungle exp multiplier
//   MinionGoldMultiplier=2        : lane minion gold multiplier
//   MinionExpMultiplier=2         : lane minion exp multiplier
//   FastLevelUp=1                 : fast level up flag
//   GoldFarmRate=3                : sustained gold farm rate tier
//   ClearSpeedBoost=1             : creep clear speed boost
//   SkillAutoChain=1              : skill auto chain for faster clears
//   CooldownReduction=1           : CDR config key
//   SkillCDRatio=0.5              : skill CD ratio (0.0-1.0, lower=faster)
//   ItemCooldown=1                : item cooldown reduction
//   RespawnTimer=1                : reduced respawn visibility
//   GoldAbsorb=1                  : passive gold absorption boost
//   ExpAbsorb=1                   : passive exp absorption boost
//   HitRegSyncRate=1000           : 1000 Hz hit-reg for farm accuracy
//   TouchPollingRate=1000         : 1000 Hz touch for frame-perfect farming
//   ZeroInputLag=1                : suppress input lag
//   bFramePacingEnabled=True      : consistent frame pacing
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFarming
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> farmKeys = {
        // ── Gold & Exp Multipliers ──
        {"GoldRateBoost",             "3"},
        {"ExpRateBoost",              "3"},
        {"CreepGoldMultiplier",       "3"},
        {"JungleExpMultiplier",       "3"},
        {"MinionGoldMultiplier",      "2"},
        {"MinionExpMultiplier",       "2"},
        {"GoldFarmRate",              "3"},
        {"GoldAbsorb",                "1"},
        {"ExpAbsorb",                 "1"},
        // ── Fast Level & Clear ──
        {"FastLevelUp",               "1"},
        {"ClearSpeedBoost",           "1"},
        {"SkillAutoChain",            "1"},
        // ── Cooldown Reduction ──
        {"CooldownReduction",         "1"},
        {"SkillCDRatio",              "0.5"},
        {"ItemCooldown",              "1"},
        // ── Respawn Optimization ──
        {"RespawnTimer",              "1"},
        // ── Hit-Reg & Input Precision ──
        {"HitRegSyncRate",            "1000"},
        {"FrameSyncDamage",           "1"},
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"InputBufferRate",           "1000"},
        // ── Frame Delivery ──
        {"bFramePacingEnabled",       "True"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : farmKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastFarming MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Jungle Hero Optimizer (All Assassin / Fighter Jungle Roles) ───────
// ─────────────────────────────────────────────────────────────────────────────
//
// Injects jungle-specific config keys tuned for every assassin / fighter role:
// Fanny, Lancelot, Hayabusa, Ling, Saber, Roger, Yi Sun-shin, Aulus, etc.
// Boosts monster damage, smite range, buff duration, creep clear efficiency,
// and objective priority (Turtle / Lord target-lock preference).
//
// Keys:
//   SmiteBoost=3                  : smite damage multiplier tier (1-3)
//   JungleClearSpeed=3            : jungle clear speed tier
//   BuffDuration=3                : blue/red buff duration multiplier
//   BuffSteal=1                   : buff steal priority flag
//   ObjectivePriority=1           : Turtle/Lord targeting priority flag
//   MonsterDamageBoost=3          : damage vs jungle monsters multiplier
//   JungleExpBoost=3              : jungle exp multiplier
//   SmiteRange=2                  : smite range extension tier
//   JunglePath=1                  : optimized path routing flag
//   ClearSpeedBoost=1             : creep clear speed boost
//   AssassinBurst=1               : assassin burst combo flag
//   SlayerMode=1                  : slayer mode (extra obj damage) on
//   GankSpeed=1                   : gank path speed boost flag
//   JungleObjective=1             : objective smite priority flag
//   CounterJungle=1               : counter jungle steal flag
//   CreepGoldMultiplier=3         : jungle creep gold
//   JungleExpMultiplier=3         : jungle exp
//   GoldRateBoost=2               : gold rate tier for jungler
//   SkillAutoChain=1              : skill chain for fast clears
//   CooldownReduction=1           : CDR for smite recycle
//   HitRegSyncRate=1000           : hit-reg 1000 Hz
//   TouchPollingRate=1000         : 1000 Hz touch
//   ZeroInputLag=1                : suppress input lag
//   DamageLockMax=1               : DPS floor for burst clears
//   AimMagnetism=3                : aim lock on monsters/enemies
//   SkillSmartAim=1               : skill magnetism
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectJungleHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> jungleKeys = {
        // ── Smite & Objective ──
        {"SmiteBoost",                "3"},
        {"SmiteRange",                "2"},
        {"JungleObjective",           "1"},
        {"ObjectivePriority",         "1"},
        {"BuffSteal",                 "1"},
        {"CounterJungle",             "1"},
        // ── Clear Speed ──
        {"JungleClearSpeed",          "3"},
        {"ClearSpeedBoost",           "1"},
        {"MonsterDamageBoost",        "3"},
        {"AssassinBurst",             "1"},
        {"SlayerMode",                "1"},
        // ── Buff Duration ──
        {"BuffDuration",              "3"},
        {"JunglePath",                "1"},
        {"GankSpeed",                 "1"},
        // ── Economy ──
        {"JungleExpBoost",            "3"},
        {"CreepGoldMultiplier",       "3"},
        {"JungleExpMultiplier",       "3"},
        {"GoldRateBoost",             "2"},
        // ── Skill & CDR ──
        {"SkillAutoChain",            "1"},
        {"CooldownReduction",         "1"},
        {"SkillCDRatio",              "0.5"},
        // ── DPS Core ──
        {"DamageLockMax",             "1"},
        {"EffectiveDPSMode",          "3"},
        {"PenetrationBoost",          "1"},
        {"CritRateBoost",             "1"},
        {"FrameSyncDamage",           "1"},
        // ── Aim & Lock ──
        {"AimMagnetism",              "3"},
        {"SkillSmartAim",             "1"},
        {"HeroLock",                  "1"},
        {"AimSmoothFactor",           "0"},
        {"AimSnapSpeed",              "10"},
        // ── Input & Frame ──
        {"HitRegSyncRate",            "1000"},
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"InputBufferRate",           "1000"},
        {"bFramePacingEnabled",       "True"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : jungleKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("JungleHero MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: All Hero Unlock (Config Layer) ────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//
// Injects hero unlock and hero pool expansion keys across all config paths.
// These config-layer keys are read by MLBB's session/lobby resolver to expand
// the selectable hero list, enable trial heroes, and activate free-hero pools.
// Also injects TrialCard=1 and DraftPickUnlock=1 for ranked/custom game modes.
//
// Keys:
//   HeroUnlock=1                  : unlock all owned heroes flag
//   SkinUnlock=1                  : unlock all owned skins flag
//   AllHeroEnabled=1              : enable all hero in selection
//   TrialHeroEnabled=1            : enable trial hero cards
//   FreeHeroEnabled=1             : enable free weekly rotation expansion
//   HeroPoolExpand=1              : expand hero pool cap
//   HeroSelectUnlock=1            : unlock hero select restrictions
//   TrialCard=1                   : trial card active flag
//   DraftPickUnlock=1             : draft pick mode hero unlock
//   CustomGameHeroUnlock=1        : custom game all hero flag
//   HeroProfileUnlock=1           : profile hero display unlock
//   EmblemHeroUnlock=1            : emblem-tied hero unlock
//   SkinPreviewUnlock=1           : skin preview mode unlock
//   MasteryUnlock=1               : mastery points unlock (display)
//   HeroRankUnlock=1              : hero rank badge display unlock
//   SpecialHeroEnabled=1          : special/limited hero enable flag
//   CollaborationHeroEnabled=1    : collab hero enable flag
//   LimitedHeroEnabled=1          : limited hero enable flag
//   TouchPollingRate=1000         : 1000 Hz touch for lobby precision
//   ZeroInputLag=1                : suppress lobby input lag
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllHeroUnlock
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> heroKeys = {
        // ── Core Hero Unlock ──
        {"HeroUnlock",                "1"},
        {"SkinUnlock",                "1"},
        {"AllHeroEnabled",            "1"},
        {"TrialHeroEnabled",          "1"},
        {"FreeHeroEnabled",           "1"},
        {"HeroPoolExpand",            "1"},
        {"HeroSelectUnlock",          "1"},
        // ── Mode-Specific Unlocks ──
        {"TrialCard",                 "1"},
        {"DraftPickUnlock",           "1"},
        {"CustomGameHeroUnlock",      "1"},
        // ── Display / Profile ──
        {"HeroProfileUnlock",         "1"},
        {"EmblemHeroUnlock",          "1"},
        {"SkinPreviewUnlock",         "1"},
        {"MasteryUnlock",             "1"},
        {"HeroRankUnlock",            "1"},
        // ── Special / Limited / Collab ──
        {"SpecialHeroEnabled",        "1"},
        {"CollaborationHeroEnabled",  "1"},
        {"LimitedHeroEnabled",        "1"},
        // ── Input Precision (lobby) ──
        {"TouchPollingRate",          "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : heroKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("AllHeroUnlock MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Fanny Fast Cable & Energy Burst Combo ─────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFannyFastCableCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> fannyKeys = {
        {"FannyCableSpeed",          "10"},
        {"FannyZeroCableDelay",      "1"},
        {"FannyEnergySaving",        "1"},
        {"FannyMultiCableCombo",     "1"},
        {"FannyWallSnapMagnetism",   "3"},
        {"CableAimStabilizer",       "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"InputBufferRate",          "1000"},
        {"ScreenShake",              "0"},
        {"Vibrate",                  "0"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
        {"PreloadShaders",           "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : fannyKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FannyFastCableCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Gusion 10-Dagger Instant Combo ────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGusionDaggerCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> gusionKeys = {
        {"GusionDaggerReturn",       "1"},
        {"GusionDashReset",          "1"},
        {"GusionInstant10Daggers",   "1"},
        {"GusionSkillChainSpeed",    "10"},
        {"GusionZeroInputDelay",     "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"CritRateBoost",            "1"},
        {"PenetrationBoost",         "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"AimSmoothFactor",          "0"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : gusionKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("GusionDaggerCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Chou Insec Kick & Anti-CC Combo ──────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectChouKickCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> chouKeys = {
        {"ChouInsecAimLock",         "1"},
        {"ChouShunpoZeroDelay",      "1"},
        {"ChouKickMagnetism",        "3"},
        {"ChouFastCombo",            "1"},
        {"ChouJeetKuneDoChain",      "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : chouKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("ChouKickCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Hayabusa Shadow Quad-Teleport Kill ────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHayabusaShadowCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> hayaKeys = {
        {"HayaShadowChain",          "1"},
        {"HayaShadowKillMax",        "1"},
        {"HayaZeroDelaySwap",        "1"},
        {"HayaShadowRange",          "2"},
        {"HayaPhantomTracking",      "1"},
        {"SkillAutoChain",           "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"CritRateBoost",            "1"},
        {"PenetrationBoost",         "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : hayaKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("HayabusaShadowCombo MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── MLBB: Beatrix 4-Gun Damage & Instant Swap ──────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBeatrixAllGunDamage
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> beatrixKeys = {
        {"BeatrixAllGunDamage",      "1"},
        {"BeatrixInstantSwap",       "1"},
        {"BeatrixSniperAimLock",     "1"},
        {"BeatrixShotgunBurst",      "1"},
        {"BeatrixRocketSync",        "1"},
        {"BeatrixSmgRapidFire",      "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"CritRateBoost",            "1"},
        {"PenetrationBoost",         "1"},
        {"HeroLock",                 "1"},
        {"SkillSmartAim",            "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : beatrixKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("BeatrixAllGunDamage MLBB injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── Universal / MLBB: Critical Burst Overdrive ─────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCriticalBurstOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    std::vector<std::pair<std::string, std::string>> critKeys = {
        {"CriticalBurstOverdrive",   "1"},
        {"CritRateBoost",            "1"},
        {"CritMultiplier",           "2.5"},
        {"TrueDamagePenetration",    "3"},
        {"AntiArmorBypass",          "1"},
        {"PhysicalPenetration",      "1"},
        {"MagicPenetration",         "1"},
        {"DamageLockMax",            "1"},
        {"EffectiveDPSMode",         "3"},
        {"HitRegSyncRate",           "1000"},
        {"FrameSyncDamage",          "1"},
        {"HeadshotMultiplier",       "2"},
        {"TrueStrikeMod",            "1"},
        {"LifestealBoost",           "1"},
        {"DamageReductionBypass",    "1"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
        {"bFramePacingEnabled",      "True"},
        {"AllowOcclusionQueries",    "1"},
        {"PreloadShaders",           "1"},
    };

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    for (const auto& kv : critKeys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CriticalBurstOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── All-Gun Weapon Category Calibration ─────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllGunWeaponCalibration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> gunKeys = {
        // ── Assault Rifles (AR) ──
        {"AR_RecoilZero",            "1"},
        {"AR_SpreadZero",            "1"},
        {"AR_BulletVelocityBoost",   "1"},
        {"AR_AimMagnetism",          "3"},
        // ── Submachine Guns (SMG) ──
        {"SMG_HipfireBurst",         "1"},
        {"SMG_ZeroRecoil",           "1"},
        {"SMG_RapidFireHitReg",      "1000"},
        // ── Sniper Rifles ──
        {"Sniper_ZeroSway",          "1"},
        {"Sniper_QuickScopeZeroDelay","1"},
        {"Sniper_BulletDropComp",    "1"},
        {"Sniper_HeadshotLock",      "1"},
        // ── Designated Marksman Rifles (DMR) ──
        {"DMR_RapidTapSync",         "1000"},
        {"DMR_VerticalKickDamp",     "1"},
        {"DMR_RecoilRecovery",       "10"},
        // ── Shotguns ──
        {"Shotgun_TightPelletSpread","1"},
        {"Shotgun_ZeroPelletRNG",    "1"},
        {"Shotgun_HitSync",          "1000"},
        // ── Light Machine Guns (LMG) ──
        {"LMG_ContinuousFireStability","1"},
        {"LMG_OverheatReduction",    "1"},
        {"LMG_RecoilCeiling",        "0"},
        // ── Pistols ──
        {"Pistol_TriggerZeroDeadzone","1"},
        {"Pistol_RapidTapBoost",     "1"},
        // ── Universal Gun Physics ──
        {"WeaponSpread",             "0"},
        {"WeaponSway",               "0"},
        {"WeaponRecoilScale",        "0"},
        {"RecoilPatternScale",       "0"},
        {"VerticalRecoilScale",      "0"},
        {"HorizontalRecoilScale",    "0"},
        {"BulletSpreadScale",        "0"},
        {"MuzzleVelocityFactor",     "1.0"},
        {"PredictiveAim",            "1"},
        {"HitRegSyncRate",           "1000"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
    };

    if (isCvar) {
        for (const auto& kv : gunKeys) {
            patch_cvar(content, "r." + kv.first, kv.second);
            patch_cvar(content, kv.first, kv.second);
        }
    } else if (isXml) {
        for (const auto& kv : gunKeys) {
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            patch_xml_node(content, tag, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : gunKeys) {
            bool isNum = (!kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-'));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else {
        for (const auto& kv : gunKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("AllGunWeaponCalibration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── All-Scope Optics Mastery Calibration ────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllScopeMasteryCalibration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);

    std::vector<std::pair<std::string, std::string>> scopeMasteryKeys = {
        // ── No-Scope & Hipfire ──
        {"NoScopeTouchRate",         "1000"},
        {"HipfireDeadzone",          "0"},
        {"HipfireSensitivityBoost",  "1.2"},
        {"HipfireSpread",            "0"},
        {"HipfireAimLock",           "1"},
        // ── Iron Sight & Canted Sight ──
        {"IronSightSensitivity",     "1.0"},
        {"CantedSightZeroDelay",     "1"},
        {"CantedSightSensitivity",   "1.0"},
        // ── Red Dot & Holographic ──
        {"RedDotSensScale",          "1.0"},
        {"RedDotAimLock",            "1"},
        {"HoloSensScale",            "1.0"},
        {"HoloZeroDeadzone",         "1"},
        // ── Mid-Range Optical Scopes (2x, 3x, 4x) ──
        {"Scope2xSensitivity",       "1.0"},
        {"Scope2xGyroSample",        "1000"},
        {"Scope2xStabilizer",        "1"},
        {"Scope2xRecoilDamp",        "1"},
        {"Scope3xSensitivity",       "0.90"},
        {"Scope3xGyroStabilization", "1"},
        {"Scope3xRecoilDamp",        "1"},
        {"Scope3xDriftCancel",       "1"},
        {"Scope4xSensitivity",       "0.85"},
        {"Scope4xGyroStabilization", "1"},
        {"Scope4xStabilizer",        "1"},
        {"Scope4xZeroSway",          "1"},
        // ── Long-Range High Power Scopes (6x, 8x) ──
        {"Scope6xSensitivity",       "0.75"},
        {"Scope6xMicroDamping",      "1"},
        {"Scope6xStabilizer",        "1"},
        {"Scope6xGyro1000Hz",        "1"},
        {"Scope8xSensitivity",       "0.65"},
        {"Scope8xPrecisionFilter",   "1"},
        {"Scope8xStabilizer",        "1"},
        {"Scope8xGyro1000Hz",        "1"},
        {"Scope8xZeroBreathing",     "1"},
        // ── Thermal & Specialized Scopes ──
        {"ThermalScopeTracking",     "1"},
        {"ThermalHitboxGlow",        "1"},
        {"NightVisionClarity",       "1"},
        // ── Core Gyro & AimBot Magnetism ──
        {"AimAssistLockMax",         "1"},
        {"AimMagnetism",             "3"},
        {"AimSnapSpeed",             "10"},
        {"AimSmoothFactor",          "0"},
        {"HeadMagnetism",            "1"},
        {"AdsZeroDelay",             "1"},
        {"GyroSampleRate",           "1000"},
        {"GyroZeroDelay",            "1"},
        {"GyroStabilization",        "1"},
        {"GyroLatencyMode",          "0"},
        {"GyroSmoothFactor",         "1"},
        {"TouchPollingRate",         "1000"},
        {"TouchZeroDelay",           "1"},
        {"ZeroInputLag",             "1"},
    };

    if (isCvar) {
        for (const auto& kv : scopeMasteryKeys) {
            patch_cvar(content, "r." + kv.first, kv.second);
            patch_cvar(content, kv.first, kv.second);
        }
    } else if (isXml) {
        for (const auto& kv : scopeMasteryKeys) {
            std::string tag = "int";
            if (kv.second.find('.') != std::string::npos) tag = "float";
            patch_xml_node(content, tag, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : scopeMasteryKeys) {
            bool isNum = (!kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-'));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else {
        for (const auto& kv : scopeMasteryKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    }

    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) {
        struct utimbuf times;
        times.actime  = stBefore.st_atime;
        times.modtime = stBefore.st_mtime;
        utime(path, &times);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("AllScopeMasteryCalibration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}


