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
#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <memory>
#include <unordered_map>
#include <android/log.h>

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
    } else {
        if (!content.empty() && content.back() != '\n') {
            content += "\n";
        }
        content += key + "=" + value + "\n";
        return true;
    }
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

