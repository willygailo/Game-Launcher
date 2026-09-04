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
#include <fstream>
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
        if (errno == EACCES || errno == EPERM) {
            LOGW("POSIX write_file_atomic EACCES/EPERM (Android 13-16 scoped storage) for %s - delegating to privileged Shizuku pipeline", path.c_str());
        } else {
            LOGE("Failed to open temporary file for atomic write: %s (errno=%d)", tmpPath.c_str(), errno);
        }
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
        LOGW("Atomic rename failed from %s to %s (errno=%d)", tmpPath.c_str(), path.c_str(), errno);
        unlink(tmpPath.c_str());
        return false;
    }
    return true;
}

static std::string read_file_posix(const std::string& path) {
    int fd = open(path.c_str(), O_RDONLY);
    if (fd < 0) {
        if (errno == EACCES || errno == EPERM) {
            LOGW("POSIX read_file_posix EACCES/EPERM (Android 13-16 scoped storage) for %s", path.c_str());
        }
        return "";
    }

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
                std::string tag = "int";
                if (v == "True" || v == "False") tag = "string";
                else if (v.find('.') != std::string::npos) tag = "float";
                patch_xml_node(content, tag, k, v);
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

// ─── Safe Backward Compatibility & 1000-Tier Dedicated Implementations ───────

static bool apply_keys_to_file(const std::string& pathStr, const char* path,
                               const std::vector<std::pair<std::string, std::string>>& keys,
                               const char* logTag) {
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml")  != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos
                   || pathStr.rfind("UserCustom.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJZC.ini") != std::string::npos
                   || pathStr.rfind("EnjoyCJ.ini") != std::string::npos
                   || pathStr.rfind("GraphicsSettings.ini") != std::string::npos);

    for (const auto& kv : keys) {
        if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
            patch_key_value(content, kv.first, kv.second);
        } else if (isXml) {
            std::string tag = "int";
            if (kv.second == "True" || kv.second == "False") tag = "string";
            else if (kv.second.find('.') != std::string::npos) tag = "float";
            patch_xml_node(content, tag, kv.first, kv.second);
        } else if (isJson) {
            bool isNum = (!kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-'));
            patch_json_node(content, kv.first, kv.second, isNum);
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
    LOGI("%s injected: %s [ok=%d]", logTag, pathStr.c_str(), ok);
    return ok;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat mult, jfloat hsMult, jint crit) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string multStr = std::to_string(mult > 0 ? mult : 1.5f);
    std::string hsStr = std::to_string(hsMult > 0 ? hsMult : 2.0f);
    std::string critStr = std::to_string(crit > 0 ? crit : 100);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageBoost", "1"}, {"DamageMultiplier", multStr}, {"DamageLockMax", "1"},
        {"HeadshotMultiplier", hsStr}, {"CritRateBoost", critStr}, {"EffectiveDPSMode", "3"},
        {"PenetrationBoost", "1"}, {"TrueDamageBoost", "1"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"FrameSyncDamage", "1"}, {"TouchPollingRate", "1000"},
        {"r.PUBGDamageLockMax", "1"}, {"r.PUBGDamageBoost", "1"}, {"r.PUBGHeadshotMultiplier", hsStr}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "DamageBoost");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat recoilScale, jint stability) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ZeroRecoil", "1"}, {"RecoilScale", "0"}, {"VerticalRecoilScale", "0"},
        {"HorizontalRecoilScale", "0"}, {"RecoilPatternScale", "0"}, {"WeaponSpread", "0"},
        {"WeaponSway", "0"}, {"BulletSpreadScale", "0"}, {"SpreadDecayRate", "10"},
        {"MuzzleSpread", "0"}, {"MovingSpreadFactor", "0"}, {"RecoilControlAssist", "1"},
        {"r.WeaponRecoilScale", "0"}, {"r.VerticalRecoilScale", "0"}, {"r.HorizontalRecoilScale", "0"},
        {"r.RecoilPatternScale", "0"}, {"r.WeaponSpread", "0"}, {"r.WeaponSway", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ZeroRecoil");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *env, jclass, jstring jPath, jint strength, jint precision) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimAssistEnabled", "1"}, {"AimAssistStrength", "100"}, {"AimMagnetism", "3"},
        {"AimAssistLockMax", "1"}, {"HeadMagnetism", "1"}, {"HeadBoneAimPriority", "1"},
        {"AimSnapSpeed", "10"}, {"AimSnapThreshold", "0"}, {"AimSmoothFactor", "0"},
        {"AdsZeroDelay", "1"}, {"PredictiveAim", "1"}, {"HeroLock", "1"}, {"SkillSmartAim", "1"},
        {"r.AimAssistEnabled", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"},
        {"r.AimSnapThreshold", "0"}, {"r.HeadBoneAimPriority", "1"}, {"r.PredictiveAim", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "AimAssist");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *env, jclass, jstring jPath, jfloat trackingStrength, jfloat hitboxMultiplier) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string hbStr = std::to_string(hitboxMultiplier > 0 ? hitboxMultiplier : 3.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"TrackingBullet", "1"}, {"TrackingStrength", "1000"}, {"BulletMagnetism", "1"},
        {"HitboxMultiplier", hbStr}, {"HitboxScale", hbStr}, {"BulletVelocityComp", "1"},
        {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"}, {"HitRegistrationRate", "1000"},
        {"ZeroBulletDrop", "1"}, {"BulletDropComp", "1"}, {"WeaponSpread", "0"},
        {"BulletSpreadScale", "0"}, {"MuzzleVelocityFactor", "1.0"}, {"TrueDamageBoost", "1"},
        {"r.PUBGBulletVelocityCompensation", "1"}, {"r.PUBGInstantHitReg", "1"},
        {"r.BulletSpreadScale", "0"}, {"r.WeaponSpread", "0"}, {"r.MuzzleVelocityFactor", "1.0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "TrackingBullet");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *env, jclass, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string defStr = std::to_string(defBoost > 0 ? defBoost : 3000.0f);
    std::string redStr = std::to_string(dmgReduction > 0 ? dmgReduction : 0.99f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ArmorDefBoost", "1"}, {"ArmorDefense", defStr}, {"PhysicalDefense", defStr},
        {"MagicDefense", defStr}, {"DamageReduction", redStr}, {"DamageReductionBypassImmune", "1"},
        {"ShieldMultiplier", "3.0"}, {"LifestealBoost", "1"}, {"SpellVampBoost", "1"},
        {"bFramePacingEnabled", "True"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ArmorDef");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSpeedBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat speedMultiplier, jfloat sprintBoost) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string spdStr = std::to_string(speedMultiplier > 0 ? speedMultiplier : 1.5f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"MovementSpeedBoost", "1"}, {"SpeedMultiplier", spdStr}, {"SprintSpeedMax", "1"},
        {"SlideDistanceMax", "1"}, {"SlideSpeedBoost", "1"}, {"JumpHeightBoost", "1"},
        {"FastTacticalSprint", "1"}, {"SprintDelayZero", "1"}, {"JoystickZeroDeadzone", "1"},
        {"JoystickResponseLevel", "3"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "SpeedBoost");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroDamage1000
  (JNIEnv *env, jclass, jstring jPath, jfloat, jfloat, jint, jint) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "1"}, {"DamageBoost", "1"}, {"EffectiveDPSMode", "3"},
        {"HeroBaseDamageMultiplier", "1"}, {"HeroSkillDamageMultiplier", "1"},
        {"HeroUltimateDamageMult", "1"}, {"HeroPassiveDamageMult", "1"},
        {"HeroCritDamageMult", "1"}, {"HeroMagicDamageMult", "1"}, {"HeroPhysicalDamageMult", "1"},
        {"HeroTrueDamageMult", "1"}, {"PenetrationBoost", "1"}, {"CritRateBoost", "1"},
        {"CritDamageMultiplier", "3.0"}, {"HeadshotMultiplier", "2.0"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"HitRegistrationRate", "1000"}, {"FrameSyncDamage", "1"},
        {"TrueDamageBoost", "1"}, {"DamageReductionBypass", "1"}, {"SpellVampBoost", "1"},
        {"LifestealBoost", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"r.PUBGDamageLockMax", "1"}, {"r.PUBGDamageBoost", "1"}, {"r.PUBGTrueDamageMod", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "HeroDamage1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat, jint) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ScopeZeroRecoil", "1"}, {"RecoilScale", "0"}, {"VerticalRecoilScale", "0"},
        {"HorizontalRecoilScale", "0"}, {"RecoilPatternScale", "0"}, {"WeaponSpread", "0"},
        {"WeaponSway", "0"}, {"BulletSpreadScale", "0"}, {"Scope2xStabilizer", "1"},
        {"Scope3xStabilizer", "1"}, {"Scope4xStabilizer", "1"}, {"Scope6xStabilizer", "1"},
        {"Scope8xStabilizer", "1"}, {"ScopeZeroSway", "1"}, {"ScopeBreathingDamp", "1"},
        {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"r.WeaponRecoilScale", "0"}, {"r.WeaponSpread", "0"}, {"r.WeaponSway", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ScopeZeroRecoil");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist1000
  (JNIEnv *env, jclass, jstring jPath, jint strength, jfloat precision) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimAssistLockMax", "1"}, {"AimAssistEnabled", "1"}, {"AimAssistStrength", "1000"},
        {"AimMagnetism", "1000"}, {"AimMagnetismLevel", "10"}, {"LockOnRange", "1.0"},
        {"AimSnapSpeed", "10"}, {"AimSnapThreshold", "0"}, {"AimSmoothFactor", "0"},
        {"AimStabilizer", "1"}, {"HeadMagnetism", "1"}, {"HeadBoneAimPriority", "1"},
        {"AdsZeroDelay", "1"}, {"PredictiveAim", "1"}, {"HeroLock", "1"}, {"SkillSmartAim", "1"},
        {"TargetPriority", "0"}, {"AimMethod", "1"}, {"WeaponSway", "0"}, {"WeaponSpread", "0"},
        {"WeaponRecoilScale", "0"}, {"TouchPollingRate", "1000"}, {"TouchSampleRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"GyroSampleRate", "1000"},
        {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"}, {"GyroLatencyMode", "0"},
        {"GyroSensitivityRatio", "2.5"},
        {"r.AimAssistEnabled", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"},
        {"r.AimSnapThreshold", "0"}, {"r.HeadBoneAimPriority", "1"}, {"r.PredictiveAim", "1"},
        {"r.GyroSampleRate", "1000"}, {"r.GyroZeroDelay", "1"}, {"r.GyroStabilization", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "AimAssist1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet1000
  (JNIEnv *env, jclass, jstring jPath, jfloat trackingStrength, jfloat hitboxMultiplier) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string hbStr = std::to_string(hitboxMultiplier > 0 ? hitboxMultiplier : 3.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"TrackingBullet", "1"}, {"TrackingBulletLockMax", "1000"}, {"TrackingStrength", "1000"},
        {"BulletTrackingStrength", "1000"}, {"HitboxMultiplier", hbStr}, {"HitboxScale", hbStr},
        {"BulletMagnetism", "1"}, {"BulletVelocityComp", "1"}, {"BulletVelocityBoost", "1"},
        {"MuzzleVelocityFactor", "1.0"}, {"InstantHitReg", "1"}, {"HitRegSyncRate", "1000"},
        {"HitRegistrationRate", "1000"}, {"FrameSyncDamage", "1"}, {"ZeroBulletDrop", "1"},
        {"BulletDropComp", "1"}, {"WeaponSpread", "0"}, {"BulletSpreadScale", "0"},
        {"MuzzleSpread", "0"}, {"MovingSpreadFactor", "0"}, {"TrueDamageBoost", "1"},
        {"PenetrationBoost", "1"}, {"DamageReductionBypass", "1"}, {"DamageLockMax", "1"},
        {"EffectiveDPSMode", "3"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"r.PUBGBulletVelocityCompensation", "1"}, {"r.PUBGInstantHitReg", "1"},
        {"r.PUBGTrueDamageMod", "1"}, {"r.BulletSpreadScale", "0"}, {"r.WeaponSpread", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "TrackingBullet1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef1000
  (JNIEnv *env, jclass, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string defStr = std::to_string(defBoost > 0 ? defBoost : 3000.0f);
    std::string redStr = std::to_string(dmgReduction > 0 ? dmgReduction : 0.99f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ArmorDefBoost", "1"}, {"ArmorDef1000", "1"}, {"ArmorDefense", defStr},
        {"PhysicalDefense", defStr}, {"MagicDefense", defStr}, {"DamageReduction", redStr},
        {"DamageReductionBypassImmune", "1"}, {"ShieldMultiplier", "3.0"}, {"LifestealBoost", "1"},
        {"SpellVampBoost", "1"}, {"RetaliationDamage", "3"}, {"bFramePacingEnabled", "True"},
        {"ZeroInputLag", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ArmorDef1000");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectShield1500
  (JNIEnv *env, jclass, jstring jPath, jfloat shieldMult, jfloat defBoost) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string shStr = std::to_string(shieldMult > 0 ? shieldMult : 3.0f);
    std::string defStr = std::to_string(defBoost > 0 ? defBoost : 3000.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"Shield1500", "1"}, {"ShieldMultiplier", shStr}, {"ShieldScale", shStr},
        {"PhysicalDefense", defStr}, {"MagicDefense", defStr}, {"DamageReduction", "0.99"},
        {"ShieldRegenRate", "10"}, {"ImmunityShield", "1"}, {"TankRetaliationDmg", "3"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "Shield1500");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDroneView
  (JNIEnv *env, jclass, jstring jPath, jint fov, jint height) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string fovStr = std::to_string(fov > 0 ? fov : 180);
    std::string htStr = std::to_string(height > 0 ? height : 180);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DroneView", "1"}, {"DroneFOV", fovStr}, {"MaxFOV", fovStr}, {"FieldOfView", fovStr},
        {"CameraHeight", htStr}, {"CameraDistance", htStr}, {"WideCameraAngle", "1"},
        {"MapVisibilityRange", "2.0"}, {"FogOfWarBypass", "1"}, {"AllowOcclusionQueries", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "DroneView");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimHeadLock
  (JNIEnv *env, jclass, jstring jPath, jfloat headMagnetism, jint snapSpeed) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string magStr = std::to_string(headMagnetism > 0 ? headMagnetism : 1.0f);
    std::string spdStr = std::to_string(snapSpeed > 0 ? snapSpeed : 10);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimHeadLock", "1"}, {"HeadMagnetism", magStr}, {"HeadBoneAimPriority", "1"},
        {"BoneIndex", "0"}, {"AimSnapSpeed", spdStr}, {"AimSnapThreshold", "0"},
        {"AimSmoothFactor", "0"}, {"AdsZeroDelay", "1"}, {"PredictiveAim", "1"},
        {"AimAssistStrength", "1000"}, {"AimMagnetism", "1000"}, {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}, {"GyroSampleRate", "1000"},
        {"r.HeadBoneAimPriority", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "AimHeadLock");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraDamageOverdrive
  (JNIEnv *env, jclass, jstring jPath, jfloat damageScale, jfloat critMultiplier, jfloat trueDamage) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string dmgStr = std::to_string(damageScale > 0 ? damageScale : 1.5f);
    std::string critStr = std::to_string(critMultiplier > 0 ? critMultiplier : 3.0f);
    std::string trueStr = std::to_string(trueDamage > 0 ? trueDamage : 1.0f);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"UltraDamageOverdrive", "1"}, {"DamageLockMax", "1"}, {"DamageBoost", "1"},
        {"DamageScale", dmgStr}, {"CritDamageMultiplier", critStr}, {"CritRateBoost", "1"},
        {"TrueDamageBoost", trueStr}, {"EffectiveDPSMode", "3"}, {"PenetrationBoost", "1"},
        {"ArmorPenMax", "1"}, {"MagicPenMax", "1"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"HitRegistrationRate", "1000"}, {"FrameSyncDamage", "1"},
        {"HeadshotMultiplier", "2.0"}, {"DamageReductionBypass", "1"}, {"BurstDamageWindow", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"r.PUBGDamageLockMax", "1"}, {"r.PUBGDamageBoost", "1"}, {"r.PUBGTrueDamageMod", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "UltraDamageOverdrive");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroAimLock
  (JNIEnv *env, jclass, jstring jPath, jint targetPriority, jfloat lockDistance) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string prioStr = std::to_string(targetPriority >= 0 ? targetPriority : 0);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"HeroAimLock", "1"}, {"HeroLock", "1"}, {"SkillSmartAim", "1"},
        {"TargetPriority", prioStr}, {"AimMethod", "1"}, {"AimAssistStrength", "1000"},
        {"AimMagnetism", "1000"}, {"AimSnapSpeed", "10"}, {"AimSnapThreshold", "0"},
        {"HeadMagnetism", "1"}, {"HeadBoneAimPriority", "1"}, {"AdsZeroDelay", "1"},
        {"PredictiveAim", "1"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"}, {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"},
        {"r.AimAssistEnabled", "1"}, {"r.AimAssistStrength", "100"}, {"r.AimMagnetism", "3"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "HeroAimLock");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
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
        {"AR_RecoilZero",             "1"},
        {"AR_SpreadZero",             "1"},
        {"AR_BulletVelocityBoost",    "1"},
        {"AR_AimMagnetism",           "3"},
        {"AR_MaxDamageLock",          "1"},
        {"AR_HeadMagnetism",          "1"},
        {"AR_AccuracyMax",            "1"},
        {"AR_EffectiveRangeMax",      "1"},
        {"AR_ZeroDeadzone",           "1"},
        {"AR_SprayPatternRecovery",   "10"},
        // ── Submachine Guns (SMG) ──
        {"SMG_HipfireBurst",          "1"},
        {"SMG_ZeroRecoil",            "1"},
        {"SMG_RapidFireHitReg",       "1000"},
        {"SMG_AimMagnetism",          "3"},
        {"SMG_SprayControlMax",       "1"},
        {"SMG_DamageMultiplier",      "1.0"},
        {"SMG_BulletVelocityMax",     "1"},
        {"SMG_ZeroSpread",            "1"},
        // ── Sniper Rifles ──
        {"Sniper_ZeroSway",           "1"},
        {"Sniper_QuickScopeZeroDelay","1"},
        {"Sniper_BulletDropComp",     "1"},
        {"Sniper_HeadshotLock",       "1"},
        {"Sniper_InstantHitReg",      "1"},
        {"Sniper_ScopeStabilizer",    "1"},
        {"Sniper_BreathHoldZero",     "1"},
        {"Sniper_MaxDamageLock",      "1"},
        // ── Designated Marksman Rifles (DMR) ──
        {"DMR_RapidTapSync",          "1000"},
        {"DMR_VerticalKickDamp",      "1"},
        {"DMR_RecoilRecovery",        "10"},
        {"DMR_ZeroSway",              "1"},
        {"DMR_ZeroDelay",             "1"},
        {"DMR_AimMagnetism",          "3"},
        {"DMR_MaxDPSLock",            "1"},
        // ── Shotguns ──
        {"Shotgun_TightPelletSpread", "1"},
        {"Shotgun_ZeroPelletRNG",     "1"},
        {"Shotgun_HitSync",           "1000"},
        {"Shotgun_PelletConcentration","1.0"},
        {"Shotgun_RangeBoost",        "1"},
        {"Shotgun_MaxDamageBurst",    "1"},
        // ── Light Machine Guns (LMG) ──
        {"LMG_ContinuousFireStability","1"},
        {"LMG_OverheatReduction",     "1"},
        {"LMG_RecoilCeiling",         "0"},
        {"LMG_ZeroBloom",             "1"},
        {"LMG_SpreadCap",             "0"},
        {"LMG_AimLock",               "1"},
        {"LMG_HitRegSync",            "1000"},
        // ── Pistols ──
        {"Pistol_TriggerZeroDeadzone", "1"},
        {"Pistol_RapidTapBoost",      "1"},
        {"Pistol_RecoilDamp",         "1"},
        {"Pistol_AimMagnetism",       "3"},
        {"Pistol_ZeroDelay",          "1"},
        // ── Universal Gun Physics & 2026 Engine Overdrive ──
        {"WeaponSpread",              "0"},
        {"WeaponSway",                "0"},
        {"WeaponRecoilScale",         "0"},
        {"RecoilPatternScale",        "0"},
        {"VerticalRecoilScale",       "0"},
        {"HorizontalRecoilScale",     "0"},
        {"BulletSpreadScale",         "0"},
        {"MuzzleVelocityFactor",      "1.0"},
        {"BulletVelocityComp",        "1"},
        {"PredictiveAim",             "1"},
        {"HitRegSyncRate",            "1000"},
        {"HitRegistrationRate",       "1000"},
        {"DamageLockMax",             "1"},
        {"DamageBoost",               "1"},
        {"EffectiveDPSMode",          "3"},
        {"PenetrationBoost",          "1"},
        {"CritRateBoost",             "1"},
        {"FrameSyncDamage",           "1"},
        {"AimAssistLockMax",          "1"},
        {"AimAssistEnabled",          "1"},
        {"AimAssistStrength",         "100"},
        {"AimMagnetism",              "3"},
        {"AimSnapSpeed",              "10"},
        {"AimSnapThreshold",          "0"},
        {"AimStabilizer",             "1"},
        {"HeadMagnetism",             "1"},
        {"HeadBoneAimPriority",       "1"},
        {"AdsZeroDelay",              "1"},
        {"AimSmoothFactor",           "0"},
        {"TouchPollingRate",          "1000"},
        {"TouchSampleRate",           "1000"},
        {"TouchZeroDelay",            "1"},
        {"ZeroInputLag",              "1"},
        {"GyroSampleRate",            "1000"},
        {"GyroZeroDelay",             "1"},
        {"GyroStabilization",         "1"},
        {"GyroLatencyMode",           "0"},
        {"GyroSensitivityRatio",      "2.5"},
        {"r.OneFrameThreadLag",       "0"},
        {"r.FinishCurrentFrame",      "0"},
        {"r.VSync",                   "0"},
        {"bFramePacingEnabled",       "1"},
        {"AllowOcclusionQueries",     "1"},
        {"PreloadShaders",            "1"}
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



// =============================================================================
// ─── PUBGM: No-Scope Hipfire Aimbot ──────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoScopeAimbot
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
    std::vector<std::pair<std::string, std::string>> keys = {
        {"HipfireSpread","0"},{"HipfireSpreadScale","0"},{"WeaponSpread","0"},
        {"BulletSpreadScale","0"},{"NoSpreadHipfire","1"},{"HipfireAimLock","1"},
        {"WeaponSway","0"},{"SwayAmplitude","0"},{"SwayFrequency","0"},
        {"BreathSway","0"},{"HeadMagnetism","1"},{"HeadBonePriority","1"},
        {"AimBoneTarget","0"},{"AimMagnetism","3"},{"AimSnapSpeed","10"},
        {"AimSmoothFactor","0"},{"AimAssistLockMax","1"},{"NoScopeAimbot","1"},
        {"HipfireAimbotEnable","1"},{"HipfireHeadLock","1"},{"InstantAimSnap","1"},
        {"AimDeadzone","0"},{"GyroSampleRate","1000"},{"GyroZeroDelay","1"},
        {"GyroStabilization","1"},{"GyroLatencyMode","0"},{"TouchPollingRate","1000"},
        {"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"HitRegSyncRate","1000"},
        {"FrameSyncDamage","1"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for (const auto& kv : keys) {
        if (isXml)        patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson)  patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar)  patch_cvar(content, kv.first, kv.second);
        else              patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime=stBefore.st_atime; t.modtime=stBefore.st_mtime; utime(path,&t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("NoScopeAimbot PUBGM injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: All-Scope Aimbot (2x-8x) ────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllScopeAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AdsZeroDelay","1"},{"AdsSnapEnable","1"},{"AdsTransitionTime","0"},
        {"HeadMagnetism","1"},{"HeadBonePriority","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"AimAssistLockMax","1"},{"AllScopeHeadLock","1"},{"ScopeAimbotEnable","1"},
        {"PredictiveAim","1"},{"WeaponSway","0"},{"BreathSway","0"},{"SwayAmplitude","0"},
        {"MicroJitterCancel","1"},
        {"Scope2xGyroSample","1000"},{"Scope2xHeadLock","1"},{"Scope2xZeroSway","1"},{"Scope2xAimSnap","10"},
        {"Scope3xGyroStabilization","1"},{"Scope3xHeadLock","1"},{"Scope3xZeroSway","1"},{"Scope3xAimSnap","10"},
        {"Scope4xGyroStabilization","1"},{"Scope4xHeadLock","1"},{"Scope4xZeroSway","1"},{"Scope4xAimSnap","10"},
        {"Scope6xGyro1000Hz","1"},{"Scope6xHeadLock","1"},{"Scope6xMicroDamping","1"},{"Scope6xAimSnap","10"},
        {"Scope8xGyro1000Hz","1"},{"Scope8xHeadLock","1"},{"Scope8xPrecisionFilter","1"},
        {"Scope8xZeroBreathing","1"},{"Scope8xAimSnap","10"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"HitRegSyncRate","1000"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for (const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("AllScopeAimbot PUBGM injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Long-Range Scope Headshot (6x/8x) ────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLongRangeScopeHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"BulletDropComp","1"},{"Sniper_BulletDropComp","1"},
        {"BulletVelocityFactor","1.0"},{"MuzzleVelocityFactor","1.0"},
        {"HoldBreathCancel","1"},{"BreathHoldTime","0"},{"BreathSway","0"},
        {"Scope8xZeroBreathing","1"},{"Scope6xZeroBreathing","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"LongRangeHeadshotLock","1"},{"Sniper_HeadshotLock","1"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"MicroJitterCancel","1"},
        {"Scope6xGyro1000Hz","1"},{"Scope6xMicroDamping","1"},{"Scope6xStabilizer","1"},{"Scope6xPrecisionFilter","1"},
        {"Scope8xGyro1000Hz","1"},{"Scope8xPrecisionFilter","1"},{"Scope8xStabilizer","1"},{"Scope8xMicroJitterCancel","1"},
        {"ThermalScopeTracking","1"},{"ThermalHitboxGlow","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("LongRangeScopeHeadshot PUBGM injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Mid-Range Auto Headshot (2x/3x/4x) ───────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMidRangeAutoHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"PredictiveAim","1"},{"TargetPrediction","1"},{"TargetLeadCompensation","1"},
        {"FrameSyncHeadshot","1"},{"HeadshotLockMidRange","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"VerticalRecoilScale","0"},{"VerticalKickDamp","1"},{"RecoilPatternScale","0"},{"WeaponRecoilScale","0"},
        {"Scope2xGyroSample","1000"},{"Scope2xHeadLock","1"},{"Scope2xRecoilDamp","1"},
        {"Scope3xGyroStabilization","1"},{"Scope3xHeadLock","1"},{"Scope3xRecoilDamp","1"},{"Scope3xDriftCancel","1"},
        {"Scope4xGyroStabilization","1"},{"Scope4xHeadLock","1"},{"Scope4xZeroSway","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("MidRangeAutoHeadshot PUBGM injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Fast Attack Speed (Fire Rate + Melee Max) ────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastAttackSpeed
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"WeaponFireRate","10"},{"FireRateBoost","10"},{"FireIntervalMin","0"},
        {"FullAutoFrameSync","1"},{"AutoFireSync","1"},{"ADSFireInterval","0"},{"FireRateUnlockMax","1"},
        {"MeleePunchSpeed","10"},{"MeleeAttackRate","10"},{"MeleeIntervalMin","0"},{"PubgmMeleeSpeedMax","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},{"AttackFrameSync","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"bFramePacingEnabled","True"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("PubgmFastAttackSpeed injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: No-Scope Hipfire Aimbot ───────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmNoScopeAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"HipfireSpread","0"},{"HipfireSpreadScale","0"},{"AR_SpreadZero","1"},
        {"SMG_HipfireBurst","1"},{"Shotgun_ZeroPelletRNG","1"},
        {"Pistol_TriggerZeroDeadzone","1"},{"LMG_RecoilCeiling","0"},
        {"WeaponSpread","0"},{"BulletSpreadScale","0"},{"NoSpreadHipfire","1"},
        {"HeadMagnetism","1"},{"HeadBonePriority","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"AimAssistLockMax","1"},{"HipfireHeadLock","1"},{"CodmNoScopeAimbot","1"},
        {"InstantAimSnap","1"},{"WeaponSway","0"},{"SwayAmplitude","0"},{"BreathSway","0"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmNoScopeAimbot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: All-Scope Aimbot (All Optic Classes) ──────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeAimbot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AdsZeroDelay","1"},{"AdsTransitionTime","0"},{"AdsSnapEnable","1"},{"CodmAdsZeroDelay","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"AllScopeHeadshotPriority","1"},{"ScopeAimbotEnable","1"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},{"AimAssistLockMax","1"},
        {"PredictiveAim","1"},{"RedDotAimLock","1"},{"HoloZeroDeadzone","1"},
        {"Scope2xHeadLock","1"},{"Scope3xHeadLock","1"},{"Scope4xHeadLock","1"},
        {"Scope6xHeadLock","1"},{"Scope8xHeadLock","1"},{"ThermalHeadshotLock","1"},
        {"WeaponSway","0"},{"BreathSway","0"},{"MicroJitterCancel","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"HitRegSyncRate","1000"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmAllScopeAimbot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: Long-Range Headshot Lock (Sniper/Marksman) ────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmLongRangeHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"BulletDropComp","1"},{"Sniper_BulletDropComp","1"},
        {"MuzzleVelocityFactor","1.0"},{"BulletVelocityBoost","1"},
        {"HoldBreathCancel","1"},{"BreathHoldTime","0"},{"BreathSway","0"},
        {"Scope8xZeroBreathing","1"},{"Scope6xZeroBreathing","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"Sniper_HeadshotLock","1"},{"CodmLongRangeHeadshotLock","1"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"MicroJitterCancel","1"},{"Scope6xMicroDamping","1"},{"Scope8xPrecisionFilter","1"},
        {"Sniper_ZeroSway","1"},{"GyroSampleRate","1000"},
        {"Scope6xGyro1000Hz","1"},{"Scope8xGyro1000Hz","1"},
        {"GyroZeroDelay","1"},{"GyroStabilization","1"},{"GyroLatencyMode","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmLongRangeHeadshot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: Mid-Range Auto Headshot (AR/SMG) ──────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMidRangeHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"PredictiveAim","1"},{"TargetPrediction","1"},{"TargetLeadCompensation","1"},
        {"FrameSyncHeadshot","1"},{"CodmMidRangeHeadshotLock","1"},{"HeadshotLockMidRange","1"},
        {"HeadBonePriority","1"},{"HeadMagnetism","1"},{"AimBoneTarget","0"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSmoothFactor","0"},
        {"VerticalRecoilScale","0"},{"VerticalKickDamp","1"},
        {"AR_RecoilZero","1"},{"SMG_ZeroRecoil","1"},{"SMG_RapidFireHitReg","1000"},
        {"RecoilPatternScale","0"},{"HorizontalRecoilScale","0"},
        {"Scope2xHeadLock","1"},{"Scope2xRecoilDamp","1"},
        {"Scope3xHeadLock","1"},{"Scope3xDriftCancel","1"},
        {"Scope4xHeadLock","1"},{"Scope4xZeroSway","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmMidRangeHeadshot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── CODM: Fast Fire Rate + Operator Attack Speed ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastAttackSpeed
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"WeaponFireRate","10"},{"FireRateBoost","10"},{"FireIntervalMin","0"},
        {"FullAutoFrameSync","1"},{"ADSFireInterval","0"},{"FireRateUnlockMax","1"},{"CodmFireRateMax","1"},
        {"OperatorSkillAttackRate","10"},{"OperatorCooldownMin","0"},
        {"OperatorSkillCastSpeed","10"},{"OperatorSkillFrameSync","1"},
        {"MeleePunchSpeed","10"},{"MeleeAttackRate","10"},{"MeleeIntervalMin","0"},{"CodmMeleeSpeedMax","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},{"AttackFrameSync","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"bFramePacingEnabled","True"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmFastAttackSpeed injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ultra Damage 2500+ All Hero ───────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUltraDamageAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"PhysicalDamageBase","2500"},{"MagicDamageBase","2500"},{"TrueDmgMultiplier","3"},
        {"BasicAttackDamage","2500"},{"SkillDamageBase","2500"},
        {"CritMultiplier","3"},{"CritRateBoost","1"},{"CritDamageMultiplier","3"},
        {"PenetrationBoost","1"},{"PhysicalPenBoost","2500"},{"MagicPenBoost","2500"},
        {"ArmorPenPercent","1"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},
        {"FrameSyncDamage","1"},{"HitRegSyncRate","1000"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"InputBufferRate","1000"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("MlbbUltraDamageAllHero injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Armor 3000+ All Hero ──────────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbArmorAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"PhysicalDefense","3000"},{"MagicDefense","3000"},{"DamageReduction","0.99"},
        {"DamageReductionMax","1"},{"PhysicalShield","5000"},{"MagicShield","5000"},
        {"ShieldBoost","1"},{"ShieldAbsorption","1"},{"PassiveShieldRegen","1"},
        {"MaxHPBoost","1"},{"HPRegenRate","10"},{"LifestealBoost","1"},{"SpellVampBoost","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("MlbbArmorAllHero injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fanny Auto Full Energy + Free Cable ───────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFannyAutoFullEnergy
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"FannyEnergyRegen","10"},{"FannyEnergyRegenMax","1"},{"FannyEnergyFull","1"},
        {"CableEnergyFree","1"},{"AutoEnergyRefill","1"},{"EnergyRegenRate","10"},{"FannyEnergyMax","1"},
        {"CableCooldown","0"},{"FannyCableCooldown","0"},{"SkillCDRatio","0"},{"CooldownReduction","1"},
        {"FannyMultiCableCombo","1"},{"FannyCableChain","1"},{"FannyCableInstantRecast","1"},
        {"FannyInstantRecall","1"},{"SkillAutoChain","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},
        {"ZeroInputDelay","1"},{"HitRegSyncRate","1000"},{"DamageLockMax","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("FannyAutoFullEnergy MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ling Fastest Combo + Auto Sword Chain ─────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLingFastestComboAutoSword
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"LingSwordAutoChain","1"},{"LingBlinkChainMax","1"},{"BlinkChainMax","1"},
        {"LingWallBlink","1"},{"WallJumpInstant","1"},{"LingInstantDash","1"},
        {"TempestInstantCast","1"},{"LingUltInstant","1"},
        {"LingTempestBladeSpeed","10"},{"LingSwordSpawnInstant","1"},
        {"ZeroInputDelay","1"},{"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"DamageLockMax","1"},{"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"SkillAutoChain","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("LingFastestComboAutoSword MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Gusion Ultra Overdrive ────────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGusionUltraOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"GusionDaggerChain","1"},{"GusionDaggerInstant","1"},{"GusionMultiDagger","1"},
        {"GusionInstantRecall","1"},{"GusionDaggerReturnZero","1"},{"GusionDaggerSpeed","10"},
        {"GusionUltraBurst","1"},{"GusionUltInstant","1"},{"GusionBurstComboInstant","1"},
        {"UltraDamageOverdrive","1"},
        {"PhysicalDamageBase","2500"},{"MagicDamageBase","2500"},{"SkillDamageBase","2500"},
        {"CritMultiplier","3"},{"PenetrationBoost","1"},{"DamageLockMax","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
        {"GusionArmorMax","3000"},{"PhysicalDefense","3000"},{"MagicDefense","3000"},
        {"DamageReduction","0.99"},{"PhysicalShield","5000"},{"MagicShield","5000"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"SkillAutoChain","1"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("GusionUltraOverdrive MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: All Hero Item + Skill Max Boost ───────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllHeroItemSkillBoost
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"ItemStatBoost","10"},{"ItemDamageBoost","10"},{"ItemDefenseBoost","10"},
        {"ItemHPBoost","10"},{"ItemAttackSpeed","10"},
        {"SkillDamageBoost","10"},{"SkillDamageBase","2500"},{"SkillAmplifyBoost","10"},{"SkillCritBoost","1"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"ItemCooldown","0"},{"CooldownZero","1"},
        {"LifestealBoost","1"},{"LifestealPercent","1"},{"SpellVampBoost","1"},{"OmniVamp","1"},
        {"MovementSpeedBoost","1"},{"MovementSpeedMax","1"},
        {"PenetrationBoost","1"},{"PhysicalPenBoost","2500"},{"MagicPenBoost","2500"},
        {"HitRegSyncRate","1000"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("AllHeroItemSkillBoost MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fast Attack Speed MAX All Hero ────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastAttackSpeedAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AttackSpeedBoost","10"},{"BasicAttackRate","10"},{"AttackSpeedCap","10"},
        {"AttackSpeedMax","1"},{"AttackSpeedUnlock","1"},
        {"AutoAttackInterval","0"},{"BasicAttackInterval","0"},{"AttackIntervalMin","0"},
        {"AutoAttackFrameSync","1"},{"AttackAnimSpeed","10"},{"AttackAnimRate","10"},{"AttackAnimBlend","0"},
        {"HitRegSyncRate","1000"},{"AttackFrameSync","1"},{"FrameSyncDamage","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"DamageLockMax","1"},{"EffectiveDPSMode","3"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("FastAttackSpeedAllHero MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Kagura Instant Umbrella Rapid Combo ───────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectKaguraCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"KaguraUmbrellaThrow","1"},{"KaguraUmbrellaInstant","1"},
        {"KaguraZeroReturnDelay","1"},{"KaguraReturnSpeed","10"},
        {"KaguraYinYangMax","1"},{"KaguraYinYangInstant","1"},
        {"KaguraSeimeiUmbrellaInstant","1"},{"KaguraSkillChain","1"},{"KaguraSkillAutoChain","1"},
        {"SkillAutoChain","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"DamageLockMax","1"},{"MagicDamageBase","2500"},{"SkillDamageBase","2500"},
        {"CritMultiplier","3"},{"PenetrationBoost","1"},{"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("KaguraCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Zilong Fastest Auto Slash + Spear Flip ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZilongAutoSlash
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"ZilongAutoSlash","1"},{"ZilongSlashChain","1"},{"ZilongSlashInterval","0"},
        {"ZilongSpearFlipInstant","1"},{"ZilongSpearFlip","1"},{"ZilongSpearSpeed","10"},
        {"ZilongDragonFlurry","1"},{"ZilongPassiveBoost","1"},{"ZilongUltSpeed","10"},
        {"AttackSpeedBoost","10"},{"BasicAttackRate","10"},{"AutoAttackInterval","0"},
        {"AttackSpeedMax","1"},{"AttackAnimSpeed","10"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("ZilongAutoSlash MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Saber Triple Strike Instant Combo ─────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSaberCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"SaberTripleStrikeInstant","1"},{"SaberTripleStrikeChain","1"},
        {"SaberStrikeInterval","0"},{"SaberStrikeSpeed","10"},
        {"SaberChaseZeroDelay","1"},{"SaberChaseInstant","1"},{"SaberFlickerInstant","1"},
        {"SaberUltLock","1"},{"SaberUltInstant","1"},{"SaberUltSpeed","10"},{"SaberUltTargetLock","1"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"SkillAutoChain","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("SaberCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Alucard Lifesteal Burst + Full Sustain ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAlucardLifestealCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"AlucardLifesteal","10"},{"AlucardLifestealMax","1"},
        {"AlucardOmniVamp","1"},{"AlucardOmniVampBoost","10"},
        {"AlucardResetChain","1"},{"AlucardSkillReset","1"},{"AlucardComboChain","1"},
        {"SkillAutoChain","1"},{"AlucardPhantomStepInstant","1"},{"AlucardPhantomStepZeroCD","1"},
        {"AlucardUltInstant","1"},{"AlucardUltLifestealBurst","1"},
        {"LifestealBoost","1"},{"LifestealPercent","1"},{"SpellVampBoost","1"},
        {"HPRegenRate","10"},{"PassiveShieldRegen","1"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("AlucardLifestealCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Yi Sun-shin Weapon Switch + Ship Ult Global Lock ──────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectYiSunShinCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"YiSunShinAutoSwitch","1"},{"YiSunShinMeleeCrit","1"},{"YiSunShinRangedBleed","1"},
        {"YiSunShinShipBuffInstant","1"},{"YiSunShinMountainShockerInstant","1"},{"YiSunShinUltGlobalLock","1"},
        {"AttackSpeedBoost","10"},{"BasicAttackRate","10"},{"AttackSpeedCap","10"},
        {"AttackSpeedMax","1"},{"AttackSpeedUnlock","1"},
        {"AutoAttackInterval","0"},{"BasicAttackInterval","0"},{"AttackIntervalMin","0"},
        {"AutoAttackFrameSync","1"},{"AttackAnimSpeed","10"},{"AttackAnimRate","10"},
        {"DamageLockMax","1"},{"PhysicalDamageBase","2500"},
        {"CritRateBoost","1"},{"CritMultiplier","3"},{"PenetrationBoost","1"},
        {"EffectiveDPSMode","3"},{"FrameSyncDamage","1"},{"CooldownReduction","1"},{"SkillCDRatio","0"},
        {"HitRegSyncRate","1000"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"bFramePacingEnabled","True"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("YiSunShinCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Chou Freestyle Instant Kick + Shunpo Immune ───────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectChouFreestyleCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"ChouJeetKuneDoInstant","1"},{"ChouJeetKuneDoChain","1"},{"ChouShunpoImmune","1"},
        {"ChouShunpoReset","1"},{"ChouDragonKickInstant","1"},{"ChouDragonKickLock","1"},
        {"SkillAutoChain","1"},{"SkillZeroDelay","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},
        {"PhysicalDamageBase","2500"},{"CritRateBoost","1"},{"CritMultiplier","3"},
        {"PenetrationBoost","1"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"HitRegSyncRate","1000"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("ChouFreestyleCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}


// =============================================================================
// ─── MLBB: Lancelot Infinite Triangular Dash ─────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLancelotDashCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"LancelotPunctureReset","1"},{"LancelotDashChainMax","1"},{"LancelotThornedRoseInstant","1"},
        {"LancelotPhantomExecutionBurst","1"},{"LancelotImmuneFrame","1"},
        {"SkillAutoChain","1"},{"SkillZeroDelay","1"},{"AimMagnetism","3"},{"SkillSmartAim","1"},
        {"PhysicalDamageBase","2500"},{"CritRateBoost","1"},{"CritMultiplier","3"},
        {"PenetrationBoost","1"},{"DamageLockMax","1"},{"EffectiveDPSMode","3"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"HitRegSyncRate","1000"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("LancelotDashCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Franco 100% Magnet Hook + Suppress ────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFrancoHookCombo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"FrancoHookMagnet","1"},{"FrancoHookSpeed","10"},{"FrancoHookRangeMax","1"},
        {"FrancoBloodyHuntInstant","1"},{"FrancoSuppressInstant","1"},{"FrancoFlickerHook","1"},
        {"AimMagnetism","3"},{"SkillSmartAim","1"},{"HeroLock","1"},
        {"DamageReduction","0.99"},{"PhysicalDefense","3000"},{"MagicDefense","3000"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"HitRegSyncRate","1000"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("FrancoHookCombo MLBB injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── Free Fire: Auto Drag Headshot Magnetism + Zero Bloom ─────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireAutoHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"DragHeadshotAssist","1"},{"HeadshotSensitivityMultiplier","3.0"},{"CrosshairBloom","0"},
        {"SpreadZero","1"},{"RecoilControlAssist","1"},{"HeadMagnetism","1"},
        {"AimBoneTarget","0"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("FreeFireAutoHeadshot injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── Free Fire: Instant 360 Gloo Wall + Fast Reload ──────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireFastGlooWall
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"InstantGlooWall","1"},{"GlooWallDeployDelay","0"},{"FastGlooCrouch","1"},
        {"ReloadSpeedBoost","10"},{"WeaponSwitchZeroDelay","1"},{"SprintDelayZero","1"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("FreeFireFastGlooWall injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── Blood Strike: Zero Recoil + Slide Cancel ────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeZeroRecoil
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"RecoilCompFactor","0"},{"SpreadZero","1"},{"SlideCancelSync","1"},
        {"FastTacticalSprint","1"},{"HipfireSpread","0"},{"HeadMagnetism","1"},
        {"AimBoneTarget","0"},{"TouchPollingRate","1000"},{"TouchZeroDelay","1"},
        {"HitRegSyncRate","1000"},{"ZeroInputLag","1"},{"FrameSyncDamage","1"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("BloodStrikeZeroRecoil injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── Delta Force: Precision Aim + Bullet Drop Calculator ─────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForcePrecisionAim
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"BulletDropComp","1"},{"MuzzleVelocityFactor","1.0"},{"ZeroSwaySniper","1"},
        {"ThermalScopeLock","1"},{"HeadBonePriority","1"},{"AimMagnetism","3"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"HitRegSyncRate","1000"},
        {"ZeroInputLag","1"},{"FrameSyncDamage","1"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("DeltaForcePrecisionAim injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// ─── HOK: Auto Smite Objective + Predictive Skill Aim ────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHokAutoSmiteObjective
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys={
        {"HokSmiteObjectivePriority","1"},{"HokSmiteStealMax","1"},{"HokPredictiveSkillAim","1"},
        {"SkillSmartAim","1"},{"HeroLock","1"},{"ObjectiveTargetLock","1"},
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"HitRegSyncRate","1000"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
    };
    for(const auto& kv:keys){
        if(isXml) patch_xml_node(content,"string",kv.first,kv.second);
        else if(isJson) patch_json_node(content,kv.first,kv.second,true);
        else if(isCvar) patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("HokAutoSmiteObjective injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// MLBB: All Hero Max Damage 2026 Overdrive
// Full-stack MLBB damage + aim + skill lock for all hero classes.
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroMaxDamage2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        // Core damage
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"FrameSyncDamage","1"},
        {"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},{"InstantHitReg","1"},
        {"TrueStrikeMod","1"},{"TrueDamageBoost","1"},{"DamageReductionBypass","1"},
        {"LifestealBoost","1"},{"SkillDamageBoost","1"},{"BasicAttackBoost","1"},
        {"SpellVampBoost","1"},{"HeadshotMultiplier","2"},{"SADamageMod","3"},
        {"SEADamageBoost","1"},{"DamagePlus","1"},
        // Per-hero engine-level multipliers (covers all 120+ heroes)
        {"HeroBaseDamageMultiplier","1"},{"HeroSkillDamageMultiplier","1"},
        {"HeroUltimateDamageMult","1"},{"HeroPassiveDamageMult","1"},
        {"HeroCritDamageMult","1"},{"HeroMagicDamageMult","1"},
        {"HeroPhysicalDamageMult","1"},{"HeroTrueDamageMult","1"},
        // Hero role boosts
        {"AssassinDamageMod","3"},{"FighterDamageMod","3"},{"MageMagicDamageMod","3"},
        {"MarksmanRangeDamageMod","3"},{"SupportHealMod","3"},{"TankShieldDamageRet","3"},
        {"JungleDamageMod","3"},{"RoamSupportDmgMod","3"},
        // Aim & hero lock
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSnapThreshold","0"},
        {"AimSmoothFactor","0"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"HeroLock","1"},{"SkillSmartAim","1"},{"AdsZeroDelay","1"},
        {"TargetPriority","0"},{"AimMethod","1"},{"SkillAutoChain","1"},
        {"SkillAutoCombo","1"},{"ObjectiveTargetLock","1"},
        // CD reduction
        {"CooldownReduction","1"},{"SkillCDRatio","0"},{"UltCDReduction","1"},{"ItemCDReduction","1"},
        // Input precision
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"JoystickZeroDeadzone","1"},{"JoystickResponseLevel","3"},
        // Frame delivery
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},{"PreloadShaders","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"HighFPSMode","3"},{"FrameRateLevel","5"},
        // Stealth
        {"ScreenShake","0"},{"Vibrate","0"},{"DamageText","1"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second=="True"||kv.second=="False")t="string";else if(kv.second.find('.')!=std::string::npos)t="float";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("MlbbAllHeroMaxDamage2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// MLBB: Ultimate Damage Overdrive 2026 — Burst All Skills + Attack Speed Unlock
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUltimateDamageOverdrive2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        {"UltimateWindowExtend","1"},{"UltAutoActivateWindow","1"},{"UltSkillDamageMult","1"},
        {"UltCooldownBypass","1"},{"UltCDReduction","1"},
        {"AttackSpeedMax","1"},{"AttackSpeedCap","3.0"},{"AttackSpeedBoost","1"},
        {"AnimationSpeedMultiplier","1.5"},{"ComboChainSpeed","10"},
        {"DamageLockMax","1"},{"EffectiveDPSMode","3"},{"TrueDamageBoost","1"},
        {"BurstDamageWindow","1"},{"CritRateBoost","1"},{"CritDamageMultiplier","3.0"},
        {"PenetrationBoost","1"},{"ArmorPenMax","1"},{"MagicPenMax","1"},
        {"HitRegSyncRate","1000"},{"FrameSyncDamage","1"},{"InstantHitReg","1"},
        {"AssassinBurstDmgBoost","3"},{"FighterComboMultiplier","3"},{"MageUltDmgBoost","3"},
        {"MarksmanCritBoost","3"},{"SniperOneHitBoost","3"},{"TankRetaliationDmg","3"},
        {"AimAssistLockMax","1"},{"HeroLock","1"},{"SkillSmartAim","1"},
        {"AimMagnetism","3"},{"HeadMagnetism","1"},{"AimSnapSpeed","10"},
        {"AimSmoothFactor","0"},{"TargetPriority","0"},
        {"TouchPollingRate","1000"},{"TouchZeroDelay","1"},{"ZeroInputLag","1"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},
        {"ScreenShake","0"},{"Vibrate","0"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second=="True"||kv.second=="False")t="string";else if(kv.second.find('.')!=std::string::npos)t="float";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("MlbbUltimateDamageOverdrive2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// PUBGM: All Weapon Max Damage 2026 — UE4 CVars + Plain INI full stack
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAllWeaponMaxDamage2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos
        ||pathStr.rfind("UserCustom.ini")!=std::string::npos
        ||pathStr.rfind("EnjoyCJZC.ini")!=std::string::npos
        ||pathStr.rfind("EnjoyCJ.ini")!=std::string::npos);
    // UE4 CVar keys
    std::vector<std::pair<std::string,std::string>> cv={
        {"r.PUBGDamageLockMax","1"},{"r.PUBGDamageBoost","1"},{"r.PUBGEffectiveDPSMode","3"},
        {"r.PUBGBulletVelocityCompensation","1"},{"r.PUBGInstantHitReg","1"},
        {"r.PUBGPenetrationBoost","1"},{"r.PUBGCritRateBoost","1"},
        {"r.PUBGHeadshotMultiplier","2.0"},{"r.PUBGTrueDamageMod","1"},
        {"r.WeaponRecoilScale","0"},{"r.VerticalRecoilScale","0"},{"r.HorizontalRecoilScale","0"},
        {"r.RecoilPatternScale","0"},{"r.WeaponSpread","0"},{"r.WeaponSway","0"},
        {"r.BulletSpreadScale","0"},{"r.MuzzleVelocityFactor","1.0"},
        {"r.AimAssistEnabled","1"},{"r.AimAssistStrength","100"},{"r.AimMagnetism","3"},
        {"r.AimSnapThreshold","0"},{"r.HeadBoneAimPriority","1"},{"r.PredictiveAim","1"},
        {"r.GyroSampleRate","1000"},{"r.GyroSensitivityRatio","2.5"},{"r.GyroZeroDelay","1"},
        {"r.GyroLatencyMode","0"},{"r.GyroStabilization","1"},{"r.GyroSmoothFactor","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"r.AllowOcclusionQueries","1"},{"r.PUBGDeviceFPS","9"},
        {"r.PUBGMaxFPS","165"},{"r.ResolutionScale","120"},
    };
    // Plain keys for XML/JSON/INI
    std::vector<std::pair<std::string,std::string>> pl={
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"HeadshotMultiplier","2"},
        {"FrameSyncDamage","1"},{"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},
        {"InstantHitReg","1"},{"BulletVelocityComp","1"},{"TrueDamageMod","1"},
        {"AR_RecoilZero","1"},{"AR_SpreadZero","1"},{"AR_MaxDamageLock","1"},{"AR_AimMagnetism","3"},
        {"SMG_ZeroRecoil","1"},{"SMG_ZeroSpread","1"},{"SMG_RapidFireHitReg","1000"},
        {"Sniper_ZeroSway","1"},{"Sniper_HeadshotLock","1"},{"Sniper_BulletDropComp","1"},
        {"Sniper_InstantHitReg","1"},{"Sniper_MaxDamageLock","1"},
        {"DMR_ZeroSway","1"},{"DMR_MaxDPSLock","1"},
        {"Shotgun_ZeroPelletRNG","1"},{"Shotgun_MaxDamageBurst","1"},
        {"LMG_ZeroBloom","1"},{"LMG_AimLock","1"},{"LMG_HitRegSync","1000"},
        {"Pistol_AimMagnetism","3"},{"Pistol_ZeroDelay","1"},
        {"WeaponSpread","0"},{"WeaponSway","0"},{"WeaponRecoilScale","0"},
        {"RecoilPatternScale","0"},{"VerticalRecoilScale","0"},{"HorizontalRecoilScale","0"},
        {"BulletSpreadScale","0"},{"MuzzleVelocityFactor","1.0"},
        {"MuzzleSpread","0"},{"MovingSpreadFactor","0"},
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"AimSnapSpeed","10"},{"AimSnapThreshold","0"},
        {"AimSmoothFactor","0"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"AdsZeroDelay","1"},{"PredictiveAim","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},{"PreloadShaders","1"},
        {"FrameRateLevel","9"},{"ResolutionScale","120"},{"HighFPSMode","3"},
    };
    if(isCvar){for(const auto& kv:cv)patch_cvar(content,kv.first,kv.second);for(const auto& kv:pl)patch_key_value(content,kv.first,kv.second);}
    else if(isXml){for(const auto& kv:pl){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}}
    else if(isJson){for(const auto& kv:pl){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}}
    else{for(const auto& kv:cv)patch_cvar(content,kv.first,kv.second);for(const auto& kv:pl)patch_key_value(content,kv.first,kv.second);}
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("PubgmAllWeaponMaxDamage2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// PUBGM: Ultra Aimbot 2026 — Head Lock + Bullet Magnetism + 1000Hz Gyro
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmUltraAimbot2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("UserCustom.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        {"r.AimAssistEnabled","1"},{"r.AimAssistStrength","100"},{"r.AimMagnetism","3"},
        {"r.AimSnapThreshold","0"},{"r.HeadBoneAimPriority","1"},{"r.PredictiveAim","1"},
        {"r.PUBGBulletVelocityCompensation","1"},
        {"r.GyroSampleRate","1000"},{"r.GyroSensitivityRatio","2.5"},{"r.GyroZeroDelay","1"},
        {"r.GyroLatencyMode","0"},{"r.GyroStabilization","1"},{"r.GyroSmoothFactor","1"},
        {"r.WeaponSpread","0"},{"r.WeaponSway","0"},{"r.WeaponRecoilScale","0"},
        {"r.RecoilPatternScale","0"},{"r.VerticalRecoilScale","0"},{"r.HorizontalRecoilScale","0"},
        {"r.BulletSpreadScale","0"},{"r.MuzzleVelocityFactor","1.0"},
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"LockOnRange","1.0"},{"AimSnapSpeed","10"},
        {"AimSnapThreshold","0"},{"AimStabilizer","1"},{"HeadMagnetism","1"},
        {"HeadBoneAimPriority","1"},{"AdsZeroDelay","1"},{"AimSmoothFactor","0"},
        {"PredictiveAim","1"},{"BulletVelocityComp","1"},{"BulletMagnetism","1"},
        {"Scope2xSensitivity","1.0"},{"Scope2xStabilizer","1"},{"Scope2xRecoilDamp","1"},
        {"Scope3xSensitivity","0.90"},{"Scope3xStabilizer","1"},{"Scope3xRecoilDamp","1"},
        {"Scope4xSensitivity","0.85"},{"Scope4xStabilizer","1"},{"Scope4xZeroSway","1"},
        {"Scope6xSensitivity","0.75"},{"Scope6xStabilizer","1"},{"Scope6xGyro1000Hz","1"},
        {"Scope8xSensitivity","0.65"},{"Scope8xStabilizer","1"},{"Scope8xZeroBreathing","1"},
        {"ThermalScopeTracking","1"},{"ThermalHitboxGlow","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},{"InstantHitReg","1"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
    };
    for(const auto& kv:k){
        if(isCvar){patch_cvar(content,kv.first,kv.second);patch_key_value(content,kv.first,kv.second);}
        else if(isXml){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("PubgmUltraAimbot2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// CODM: Max Damage All Weapon 2026 — all weapon classes full stack
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMaxDamageAllWeapon2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("GraphicsSettings.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"HeadshotMultiplier","2"},
        {"TrueDamageBoost","1"},{"FrameSyncDamage","1"},
        {"HitRegSyncRate","1000"},{"HitRegistrationRate","1000"},{"InstantHitReg","1"},
        {"BulletVelocityComp","1"},{"MuzzleVelocityFactor","1.0"},
        {"AR_MaxDamageLock","1"},{"AR_RecoilZero","1"},{"AR_SpreadZero","1"},
        {"AR_AimMagnetism","3"},{"AR_AccuracyMax","1"},
        {"SMG_ZeroRecoil","1"},{"SMG_ZeroSpread","1"},{"SMG_RapidFireHitReg","1000"},{"SMG_DamageMultiplier","1.0"},
        {"Sniper_ZeroSway","1"},{"Sniper_HeadshotLock","1"},{"Sniper_InstantHitReg","1"},
        {"Sniper_MaxDamageLock","1"},{"Sniper_BulletDropComp","1"},
        {"DMR_ZeroSway","1"},{"DMR_MaxDPSLock","1"},{"DMR_RecoilRecovery","10"},
        {"Shotgun_ZeroPelletRNG","1"},{"Shotgun_MaxDamageBurst","1"},{"Shotgun_HitSync","1000"},
        {"LMG_ZeroBloom","1"},{"LMG_AimLock","1"},{"LMG_HitRegSync","1000"},{"LMG_RecoilCeiling","0"},
        {"Pistol_AimMagnetism","3"},{"Pistol_ZeroDelay","1"},{"Pistol_RecoilDamp","1"},
        {"RecoilScale","0"},{"VerticalRecoilScale","0"},{"HorizontalRecoilScale","0"},
        {"RecoilPatternScale","0"},{"RecoilMultiplier","0"},
        {"WeaponSpread","0"},{"WeaponSway","0"},{"BulletSpreadScale","0"},
        {"SpreadDecayRate","10"},{"MuzzleSpread","0"},{"MovingSpreadFactor","0"},{"JumpSpreadFactor","0"},
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"AimSnapSpeed","10"},{"AimSnapThreshold","0"},{"AimSmoothFactor","0"},
        {"AdsZeroDelay","1"},{"PredictiveAim","1"},
        {"Scope2xStabilizer","1"},{"Scope4xStabilizer","1"},{"Scope6xStabilizer","1"},
        {"Scope8xStabilizer","1"},{"ScopeBreathingDamp","1"},{"ScopeSwayDamp","1"},
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"JoystickZeroDeadzone","1"},{"JoystickResponseLevel","3"},
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},{"PreloadShaders","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"FrameRateLevel","9"},{"ResolutionScale","120"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmMaxDamageAllWeapon2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// =============================================================================
// CODM: Ultra Config Cheat 2026 — Full Stack: Damage + Aim + Speed + Graphics
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmUltraConfigCheat2026
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path), content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat=(stat(path,&stBefore)==0);
    bool isXml=(pathStr.rfind(".xml")!=std::string::npos||content.find("<map>")!=std::string::npos);
    bool isJson=(pathStr.rfind(".json")!=std::string::npos||(!content.empty()&&content.front()=='{'));
    bool isCvar=(content.find("+CVars=")!=std::string::npos||pathStr.rfind("GraphicsSettings.ini")!=std::string::npos);
    std::vector<std::pair<std::string,std::string>> k={
        // Damage
        {"DamageLockMax","1"},{"DamageBoost","1"},{"EffectiveDPSMode","3"},
        {"PenetrationBoost","1"},{"CritRateBoost","1"},{"HeadshotMultiplier","2"},
        {"TrueDamageBoost","1"},{"FrameSyncDamage","1"},
        {"HitRegSyncRate","1000"},{"InstantHitReg","1"},
        // Aim
        {"AimAssistLockMax","1"},{"AimAssistEnabled","1"},{"AimAssistStrength","100"},
        {"AimMagnetism","3"},{"HeadMagnetism","1"},{"HeadBoneAimPriority","1"},
        {"AimSnapSpeed","10"},{"AimSnapThreshold","0"},{"AimSmoothFactor","0"},
        {"AdsZeroDelay","1"},{"PredictiveAim","1"},
        // No recoil / no spread
        {"RecoilScale","0"},{"VerticalRecoilScale","0"},{"HorizontalRecoilScale","0"},
        {"RecoilPatternScale","0"},{"RecoilMultiplier","0"},
        {"WeaponSpread","0"},{"WeaponSway","0"},{"BulletSpreadScale","0"},
        {"SpreadDecayRate","10"},{"MuzzleSpread","0"},
        {"MovingSpreadFactor","0"},{"JumpSpreadFactor","0"},
        // Speed & movement
        {"MovementSpeedBoost","1"},{"SprintSpeedMax","1"},{"SlideDistanceMax","1"},
        {"SlideSpeedBoost","1"},{"JumpHeightBoost","1"},
        // Graphics unlock 2026
        {"FrameRateLevel","9"},{"ResolutionScale","120"},
        {"HDR10Plus","1"},{"UltraExtreme2026","1"},
        {"VulkanPipelineCache","1"},{"AsyncCompute","1"},{"VRS","1"},
        {"PreloadShaders","1"},{"bPreloadShaders","True"},
        {"ShaderPrecompile","1"},{"ShaderWarmupAtLaunch","1"},
        // Gyro & touch
        {"GyroSampleRate","1000"},{"GyroZeroDelay","1"},{"GyroStabilization","1"},
        {"GyroLatencyMode","0"},{"GyroSensitivityRatio","2.5"},
        {"TouchPollingRate","1000"},{"TouchSampleRate","1000"},{"TouchZeroDelay","1"},
        {"ZeroInputLag","1"},{"InputBufferRate","1000"},
        {"JoystickZeroDeadzone","1"},{"JoystickResponseLevel","3"},
        // Frame & engine
        {"bFramePacingEnabled","True"},{"AllowOcclusionQueries","1"},
        {"r.OneFrameThreadLag","0"},{"r.FinishCurrentFrame","0"},{"r.VSync","0"},
        {"ScreenShake","0"},{"Vibrate","0"},
    };
    for(const auto& kv:k){
        if(isXml){std::string t="int";if(kv.second.find('.')!=std::string::npos)t="float";else if(kv.second=="True"||kv.second=="False")t="string";patch_xml_node(content,t,kv.first,kv.second);}
        else if(isJson){bool n=!kv.second.empty()&&(isdigit((unsigned char)kv.second[0])||kv.second[0]=='-');patch_json_node(content,kv.first,kv.second,n);}
        else if(isCvar)patch_cvar(content,kv.first,kv.second);
        else patch_key_value(content,kv.first,kv.second);
    }
    bool ok=write_file_atomic(pathStr,content);
    if(ok&&hasStat){struct utimbuf t;t.actime=stBefore.st_atime;t.modtime=stBefore.st_mtime;utime(path,&t);}
    env->ReleaseStringUTFChars(jPath,path);
    LOGI("CodmUltraConfigCheat2026 injected: %s [ok=%d]",pathStr.c_str(),ok);
    return ok?JNI_TRUE:JNI_FALSE;
}

// ─── 2026 Master 10000+ Damage & Attack Speed Overdrive Suite Implementations ─

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"PhysicalDamageBase", "10000"},
        {"MagicDamageBase", "10000"}, {"TrueDamageBase", "10000"}, {"TrueDamageBoost", "10000"},
        {"EffectiveDPSMode", "3"}, {"PenetrationBoost", "10000"}, {"ArmorPenMax", "10000"},
        {"MagicPenMax", "10000"}, {"CritRateBoost", "100"}, {"CritDamageMultiplier", "5.0"},
        {"AttackSpeedBoost", "10000"}, {"AttackSpeedCap", "10.0"}, {"AttackSpeedUnlock", "1"},
        {"AttackSpeedMax", "1"}, {"BasicAttackRate", "10"}, {"AutoAttackInterval", "0"},
        {"AttackAnimSpeed", "10.0"}, {"InstantBasicAttack", "1"}, {"HitRegSyncRate", "1000"},
        {"HitRegistrationRate", "1000"}, {"InstantHitReg", "1"}, {"SpellVampBoost", "100"},
        {"LifestealBoost", "100"}, {"EnergyRegenBoost", "100"}, {"ManaRegenBoost", "100"},
        {"SkillCDRatio", "0"}, {"SkillInstantReset", "1"}, {"FastRecall", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "MlbbDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamageBoost", "10000"},
        {"BulletVelocityBoost", "10000"}, {"MuzzleVelocityFactor", "10.0"}, {"FireRateBoost", "10000"},
        {"FireRateMultiplier", "10.0"}, {"RapidFireHitReg", "1000"}, {"TriggerZeroDelay", "1"},
        {"BurstIntervalZero", "1"}, {"HitboxMultiplier", "5.0"}, {"HitboxScale", "5.0"},
        {"BulletMagnetism", "1"}, {"BulletVelocityComp", "1"}, {"InstantHitReg", "1"},
        {"HitRegSyncRate", "1000"}, {"ZeroBulletDrop", "1"}, {"BulletDropComp", "1"},
        {"TrueDamageBoost", "10000"}, {"PenetrationBoost", "10000"}, {"ZeroRecoil", "1"},
        {"RecoilScale", "0"}, {"WeaponSpread", "0"}, {"BulletSpreadScale", "0"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"},
        {"r.PUBGDamageBoost", "10000"}, {"r.PUBGFireRateBoost", "10000"},
        {"r.PUBGBulletVelocityCompensation", "1"}, {"r.PUBGInstantHitReg", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "PubgmDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamage", "10000"},
        {"DPSOverdrive", "10000"}, {"FireRateMultiplier", "10.0"}, {"CycleTimeReduction", "1.0"},
        {"BoltActionSpeedBoost", "5.0"}, {"ADSFireRateSync", "1000"}, {"TriggerZeroDeadzone", "1"},
        {"HitRegSyncRate", "1000"}, {"InstantHitReg", "1"}, {"PenetrationBoost", "10000"},
        {"TrueDamageBoost", "10000"}, {"ZeroRecoil", "1"}, {"RecoilScale", "0"},
        {"WeaponSpread", "0"}, {"SlideCancelInstant", "1"}, {"QuickDrawZeroDelay", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "CodmDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"GunDamageMultiplier", "10000"},
        {"FireRateOverclock", "10000"}, {"FireRateBoost", "10.0"}, {"AutoHeadshotDamage", "10000"},
        {"HeadshotMultiplier", "5.0"}, {"QuickShotZeroDelay", "1"}, {"GlooWallDeploySpeed", "10.0"},
        {"HitRegSyncRate", "1000"}, {"InstantHitReg", "1"}, {"ZeroSpread", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FreeFireDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHokDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"PhysicalPowerBase", "10000"},
        {"MagicPowerBase", "10000"}, {"AttackSpeedCap", "10.0"}, {"AttackSpeedBoost", "10000"},
        {"AutoAttackInterval", "0"}, {"AttackAnimSpeed", "10.0"}, {"AutoSmiteDamage", "10000"},
        {"InstantBasicAttack", "1"}, {"HitRegSyncRate", "1000"}, {"SpellVampBoost", "100"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "HokDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWildRiftDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"AttackDamageBase", "10000"},
        {"AbilityPowerBase", "10000"}, {"AttackSpeedMaxRatio", "10.0"}, {"AttackSpeedBoost", "10000"},
        {"AutoAttackWindupZero", "1"}, {"TrueDamageMultiplier", "10000"}, {"CritRateBoost", "100"},
        {"InstantBasicAttack", "1"}, {"HitRegSyncRate", "1000"}, {"OmnivampBoost", "100"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "WildRiftDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastReloadQuickSwap
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"FastReload", "1"}, {"ReloadSpeedMultiplier", "10.0"}, {"ReloadDurationReduction", "0.99"},
        {"InstantChambering", "1"}, {"QuickSwap", "1"}, {"WeaponSwapZeroDelay", "1"},
        {"HolsterSpeedBoost", "10.0"}, {"DrawSpeedBoost", "10.0"}, {"SprintToFireZeroDelay", "1"},
        {"AdsZeroDelay", "1"}, {"TouchPollingRate", "1000"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastReloadQuickSwap");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWallPiercingArmorShredder
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"WallPiercing", "1"}, {"MaterialPenetrationMax", "1"}, {"CoverPenetrationMultiplier", "5.0"},
        {"ArmorShredder", "1"}, {"ArmorReductionMultiplier", "1.0"}, {"DamageReductionBypass", "1"},
        {"TrueDamagePenetration", "10000"}, {"ShieldPiercing", "1"}, {"HitRegSyncRate", "1000"},
        {"ZeroBulletDrop", "1"}, {"r.PUBGTrueDamageMod", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "WallPiercingArmorShredder");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroPingNetworkOverclock
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"ZeroPingNetwork", "1"}, {"NetworkJitterBufferZero", "1"}, {"ClientTickRate", "128"},
        {"ServerPacketSyncRate", "1000"}, {"InterpolationDelay", "0"}, {"ExtrapolationSmoothing", "1"},
        {"PacketLossComp", "1"}, {"HitRegSyncRate", "1000"}, {"InstantHitReg", "1"},
        {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ZeroPingNetworkOverclock");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtreme240FpsGraphics
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"TargetFPS", "240"}, {"MaxFrameRate", "240"}, {"FrameRateLimit", "240"},
        {"FrameRateLevel", "10"}, {"HighFPSMode", "4"}, {"Unlock240Hz", "1"},
        {"Unlock185Hz", "1"}, {"Unlock165Hz", "1"}, {"Unlock144Hz", "1"},
        {"Vsync", "0"}, {"bFramePacingEnabled", "1"}, {"ResolutionScale", "140"},
        {"HDR10Plus", "1"}, {"UltraExtreme2026", "1"}, {"VulkanPipelineCache", "1"},
        {"AsyncCompute", "1"}, {"VRS", "1"}, {"PreloadShaders", "1"},
        {"r.PUBGDeviceFPS", "10"}, {"r.PUBGMaxFPS", "240"}, {"r.VSync", "0"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "UltraExtreme240FpsGraphics");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalDamage10000AttackSpeedMax
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "10000"}, {"DamageBoost", "10000"}, {"WeaponDamage", "10000"},
        {"PhysicalDamageBase", "10000"}, {"MagicDamageBase", "10000"}, {"TrueDamageBase", "10000"},
        {"AttackSpeedBoost", "10000"}, {"AttackSpeedCap", "10.0"}, {"AttackSpeedMax", "1"},
        {"FireRateMultiplier", "10.0"}, {"FireRateBoost", "10000"}, {"PenetrationBoost", "10000"},
        {"CritRateBoost", "100"}, {"CritDamageMultiplier", "5.0"}, {"HitRegSyncRate", "1000"},
        {"InstantHitReg", "1"}, {"ZeroRecoil", "1"}, {"ZeroSpread", "1"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"}, {"ZeroInputLag", "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "UniversalDamage10000AttackSpeedMax");
    env->ReleaseStringUTFChars(jPath, path);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─────────────────────────────────────────────────────────────────────────────
// 2026 SKILL ECONOMY OVERDRIVE SUITE
// Fast Cooldown · Fast Full Mana · Fast Full Energy · Fast HP Regen
// Fast Stamina/Fury · Zero Skill Cost · Max Ult Charge · Master Suite
// ─────────────────────────────────────────────────────────────────────────────

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown
  (JNIEnv *env, jclass, jstring jPath, jfloat cdrRatio) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    // cdrRatio: 0.001 = near-instant cooldown (0.1% of normal)
    char cdrBuf[32]; snprintf(cdrBuf, sizeof(cdrBuf), "%.4f", (float)cdrRatio);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkillCDR",               cdrBuf},
        {"CooldownMultiplier",     cdrBuf},
        {"SkillCooldown",          "0.0"},
        {"SkillCooldownReduction", "1.0"},
        {"ItemCooldown",           "0.0"},
        {"ItemActiveCooldown",     "0.0"},
        {"PassiveCDReduction",     "1.0"},
        {"GlobalCooldown",         cdrBuf},
        {"UltCooldownReduction",   "1.0"},
        {"HeroUltCD",              cdrBuf},
        {"PassiveCooldown",        "0.0"},
        {"CastTimeReduction",      "1.0"},
        {"TalentCooldown",         "0.0"},
        {"EquipmentCooldown",      "0.0"},
        // MLBB keys
        {"SkillCD",                "0"},
        {"SP_SkillCooldown",       "0.0"},
        // CODM keys
        {"OperatorSkillCooldown",  cdrBuf},
        {"TacticalCooldown",       cdrBuf},
        {"LethalCooldown",         cdrBuf},
        {"FieldUpgradeCooldown",   cdrBuf},
        // PUBGM keys
        {"VehicleCooldown",        "0.0"},
        {"AdrenalineCooldown",     "0.0"},
        // Legacy & general speed keys
        {"CooldownReduction",      "1"},
        {"SkillCDRatio",           "0"},
        {"SkillInstantReset",      "1"},
        {"FastCooldownMax",        "1"},
        {"EnergyRegenBoost",       "10"},
        {"ManaRegenBoost",         "10"},
        {"ZeroInputLag",           "1"},
        {"TouchPollingRate",       "1000"},
        {"TouchZeroDelay",         "1"}
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastCooldown2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastCooldown2026 injected: %s [cdr=%.4f ok=%d]", pathStr.c_str(), (float)cdrRatio, ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullMana
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"ManaRegen",          "10000"},
        {"MaxMana",            "99999"},
        {"StartMana",          "99999"},
        {"ManaCostMultiplier", "0.0"},
        {"SkillManaCost",      "0"},
        {"UltManaCost",        "0"},
        {"ManaPerSecond",      "10000"},
        {"ManaRestoreRate",    "10000"},
        {"InstantManaFull",    "1"},
        {"ManaOnKill",         "10000"},
        {"ManaOnHit",          "10000"},
        // MLBB mana keys
        {"HeroMaxMana",        "99999"},
        {"ManaGain",           "10000"},
        {"SP_MaxMana",         "99999"},
        {"SP_ManaRegen",       "10000"},
        // HoYo/Genshin keys
        {"SkillEnergyCost",    "0"},
        {"EnergyRecharge",     "10000"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastFullMana2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastFullMana2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullEnergy
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"EnergyRegen",          "10000"},
        {"MaxEnergy",            "99999"},
        {"StartEnergy",          "99999"},
        {"EnergyCostMultiplier", "0.0"},
        {"SkillEnergyCost",      "0"},
        {"UltEnergyCost",        "0"},
        {"EnergyPerSecond",      "10000"},
        {"EnergyRestoreRate",    "10000"},
        {"InstantEnergyFull",    "1"},
        {"EnergyOnKill",         "10000"},
        {"EnergyOnHit",          "10000"},
        // MLBB/HOK energy keys
        {"SP_MaxEnergy",         "99999"},
        {"SP_EnergyRegen",       "10000"},
        {"EnergyGain",           "10000"},
        // Farlight/ArenaBreakout shield-energy
        {"ShieldEnergyRegen",    "10000"},
        {"MaxShieldEnergy",      "99999"},
        // PUBGM adrenaline / boost energy
        {"BoostEnergyRegen",     "10000"},
        {"AdrenalineEffect",     "10000"},
        {"BoostDecayRate",       "0.0"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastFullEnergy2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastFullEnergy2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastHpRegen
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"HpRegen",              "10000"},
        {"HpRegenPerSecond",     "10000"},
        {"PassiveRegenTick",     "10000"},
        {"RegenDelay",           "0.0"},
        {"HealMultiplier",       "10.0"},
        {"LifeStealRate",        "1.0"},
        {"LifeStealMax",         "1.0"},
        {"BloodthirstRate",      "10000"},
        {"VampirismRate",        "10000"},
        {"HpOnKill",             "10000"},
        {"HpOnHit",              "10000"},
        {"SelfHealMultiplier",   "10.0"},
        {"OutOfCombatRegen",     "10000"},
        // MLBB hero HP regen
        {"HeroHPRegen",          "10000"},
        {"SP_HpRegen",           "10000"},
        // PUBGM health bar regen
        {"HealthRegen",          "10000"},
        {"AutoHealRate",         "10000"},
        // CODM operator passive regen
        {"OperatorHealRate",     "10000"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastHpRegen2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastHpRegen2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastStaminaFuryRegen
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"StaminaRegen",       "10000"},
        {"MaxStamina",         "99999"},
        {"StaminaDecayRate",   "0.0"},
        {"SprintStaminaCost",  "0.0"},
        {"DodgeCost",          "0.0"},
        {"JumpCost",           "0.0"},
        {"FuryRegen",          "10000"},
        {"MaxFury",            "99999"},
        {"FuryDecay",          "0.0"},
        {"RageRegen",          "10000"},
        {"MaxRage",            "99999"},
        {"RageDecay",          "0.0"},
        {"MomentumRegen",      "10000"},
        {"FlowRegen",          "10000"},
        // MLBB fighter rage/fury
        {"SP_FuryRegen",       "10000"},
        {"HeroFuryMax",        "99999"},
        // PUBGM sprint stamina
        {"SprintDuration",     "99999"},
        {"SprintRecovery",     "10000"},
        // CODM operator charge
        {"OperatorCharge",     "10000"},
        {"ChargeDecayRate",    "0.0"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "FastStaminaFuryRegen2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastStaminaFuryRegen2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroSkillCost
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkillCost",         "0"},
        {"UltCost",           "0"},
        {"AbilityCost",       "0"},
        {"SpellCost",         "0"},
        {"ThrowableCost",     "0"},
        {"ConsumableCost",    "0"},
        {"AmmoConsumption",   "0"},
        {"ResourceCost",      "0"},
        {"StaminaCost",       "0"},
        {"EnergyCost",        "0"},
        {"ManaCost",          "0"},
        {"FuryCost",          "0"},
        {"RageCost",          "0"},
        {"HeatCost",          "0"},
        // MLBB zero skill mana cost
        {"SP_SkillManaCost",  "0"},
        {"SP_UltManaCost",    "0"},
        // CODM zero costs
        {"OperatorSkillCost", "0"},
        {"TacticalCost",      "0"},
        {"LethalCost",        "0"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "ZeroSkillCost2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("ZeroSkillCost2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMaxUltCharge
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"UltChargeRate",       "10000"},
        {"UltChargeMax",        "99999"},
        {"StartUltCharge",      "99999"},
        {"UltPassiveRegen",     "10000"},
        {"SuperChargeRate",     "10000"},
        {"UltReadyInstant",     "1"},
        {"ChargeDecayRate",     "0.0"},
        {"UltChargeOnKill",     "10000"},
        {"UltChargeOnHit",      "10000"},
        {"UltChargeOnDamage",   "10000"},
        // MLBB ult recharge
        {"HeroUltRecharge",     "10000"},
        {"SP_UltChargeRate",    "10000"},
        // CODM scorestreak charge
        {"ScorestreakCharge",   "10000"},
        {"KillstreakCharge",    "10000"},
        // HOK/WildRift ult charge
        {"AbilityChargeRate",   "10000"},
        {"UltimateChargeRate",  "10000"},
    };
    bool ok = apply_keys_to_file(pathStr, path, keys, "MaxUltCharge2026");
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MaxUltCharge2026 injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─── Master One-Shot Skill Economy Injector ──────────────────────────────────
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSkillEconomyMasterSuite
  (JNIEnv *env, jclass cls, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    // Re-use path string across all sub-injectors
    bool r1 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown(env, cls, jPath, 0.001f);
    bool r2 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullMana(env, cls, jPath);
    bool r3 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullEnergy(env, cls, jPath);
    bool r4 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastHpRegen(env, cls, jPath);
    bool r5 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastStaminaFuryRegen(env, cls, jPath);
    bool r6 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroSkillCost(env, cls, jPath);
    bool r7 = Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMaxUltCharge(env, cls, jPath);
    bool anyOk = r1 || r2 || r3 || r4 || r5 || r6 || r7;
    LOGI("SkillEconomyMasterSuite2026: CDR=%d Mana=%d Energy=%d HP=%d Stamina=%d ZeroCost=%d Ult=%d",
         r1,r2,r3,r4,r5,r6,r7);
    return anyOk ? JNI_TRUE : JNI_FALSE;
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

    std::string content;
    if (path.find(".ini") != std::string::npos) {
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
    } else if (path.find(".json") != std::string::npos) {
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
    } else if (path.find(".xml") != std::string::npos) {
        if (content.find("<map>") != std::string::npos) {
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
            std::ostringstream ss;
            ss << "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
               << "<map>\n"
               << "  <string name=\"SystemInfo_graphicsDeviceName\">" << gpu << "</string>\n"
               << "  <string name=\"SystemInfo_graphicsDeviceVendor\">Qualcomm</string>\n"
               << "  <string name=\"SystemInfo_deviceModel\">" << soc << "</string>\n"
               << "  <int name=\"SystemInfo_systemMemorySize\" value=\"" << ram << "\" />\n"
               << "  <int name=\"SystemInfo_graphicsMemorySize\" value=\"8192\" />\n"
               << "  <int name=\"TargetFrameRate\" value=\"" << hz << "\" />\n"
               << "  <int name=\"MaxRefreshRate\" value=\"" << hz << "\" />\n"
               << "  <int name=\"Unlock185Hz\" value=\"1\" />\n"
               << "  <int name=\"Unlock165Hz\" value=\"1\" />\n"
               << "  <int name=\"Unlock144Hz\" value=\"1\" />\n"
               << "  <int name=\"Unlock120Hz\" value=\"1\" />\n"
               << "</map>\n";
            content = ss.str();
        }
    } else {
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

// =============================================================================
// ─── Fast Loot & Instant Weapon Swap ─────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastLootAndWeaponSwap
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"AutoPickUpSpeed", "100"}, {"bAutoPickUpSpeedPriority", "True"},
        {"PickUpSearchRadius", "2500"}, {"QuickLootThreshold", "0"},
        {"LootBoxRenderPriority", "1"}, {"WeaponSwitchZeroDelay", "1"},
        {"QuickDrawLatencyReduction", "0"}, {"bFastEquipEnabled", "True"},
        {"InstantLootResponse", "1"}, {"FastWeaponSwapSpeedMultiplier", "2.0"},
        {"ZeroLootDelay", "1"}, {"bFastItemPickup", "True"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastLootAndWeaponSwap injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Instant Sprint Turbo ────────────────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectInstantSprintTurbo
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SprintSensitivity", "100"}, {"JoystickDeadZone", "0"},
        {"SprintDelayZero", "1"}, {"InstantSprintThreshold", "0.01"},
        {"FastSprintResponse", "1"}, {"SprintForwardDeadzone", "0"},
        {"ZeroSprintTransitionLag", "1"}, {"bInstantSprintActive", "True"},
        {"TouchAnalogSensitivity", "2.0"}, {"RunLockZeroLatency", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("InstantSprintTurbo injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Multi-Range Headshot Precision Calibration ──────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMultiRangeHeadshotCalibration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"CloseRangeAimSensMultiplier", "1.5"}, {"HipFireFovStability", "1"},
        {"TouchZeroFriction", "1"}, {"MidRangeRecoilStability", "1"},
        {"AimFovSmoothCurve", "0"}, {"CrosshairSwayElimination", "1"},
        {"LongRangeMicroAimPrecision", "1000"}, {"SniperScopeZeroLatency", "1"},
        {"SteadyAimFovLock", "1"}, {"SubPixelAimCalibration", "1"},
        {"GyroMicroSensitivityBoost", "1.4"}, {"HeadshotHitboxAimMagnetism", "1"},
        {"DynamicAimAcceleration", "0"}, {"bMultiRangeHeadshotEnabled", "True"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MultiRangeHeadshotCalibration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Jungle Fast Farm All Hero ─────────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbJungleFastFarmAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    std::vector<std::pair<std::string,std::string>> keys = {
        {"MonsterTargetPriority", "1"}, {"CreepLockPriority", "1"},
        {"SmartRetributionHpThreshold", "1"}, {"TargetLowestHpMonster", "1"},
        {"JungleClearSpeedBoost", "1"}, {"RetributionInstantCast", "1"},
        {"FastCampPathingSens", "10"}, {"AutoObjectiveSmiteLock", "1"},
        {"CreepAttackPriority", "1"}, {"JungleAttackSpeedRatio", "2.0"},
        {"ZeroJungleDelay", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbJungleFastFarmAllHero injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ling Fastest Sword (Tempest of Blades) ────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbLingFastestSword
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    std::vector<std::pair<std::string,std::string>> keys = {
        {"LingSwordPathResponsiveness", "10"}, {"LingSwordAutoLock", "1"},
        {"LingSwordZeroDelay", "1"}, {"SwordTouchSampling", "1000"},
        {"TempestOfBladesFastSword", "1"}, {"LingDashResetZeroLatency", "1"},
        {"LingWallJumpSpeed", "5"}, {"LingSwordMagnetism", "1"},
        {"Ling4SwordInstantCombo", "1"}, {"LingEnergyRestoreFast", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbLingFastestSword injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fanny Fastest Cable (Zero Delay & Wall Snap) ──────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFannyFastestCable
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    std::vector<std::pair<std::string,std::string>> keys = {
        {"FannyZeroCableDelay", "1"}, {"FannyCableSpeed", "10"},
        {"FannyMultiCableInstantCast", "1"}, {"CableWallSnapSens", "5.0"},
        {"SkillCastResponseTime", "0"}, {"FannyDualCableInstant", "1"},
        {"FannyWallSnapMagnetism", "3"}, {"FannyEnergySaving", "1"},
        {"FannyInstantRecall", "1"}, {"FannyStraightCableSpeed", "10"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbFannyFastestCable injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal Zero-Delay Skill Tap & Combo All Hero ─────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalZeroDelaySkillTapAllHero
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkillQueueInstant", "1"}, {"SmartSkillCastZeroDelay", "1"},
        {"AutoAttackAnimationCancel", "1"}, {"ComboChainBufferMs", "0"},
        {"TouchSamplingRate", "1000"}, {"ZeroDelaySkillTap", "1"},
        {"InstantSkillCancelThreshold", "0"}, {"HeroTargetLockPriority", "1"},
        {"FastSkillReleaseSpeed", "10"}, {"InputQueueBypass", "1"},
        {"bZeroLatencyInput", "True"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("UniversalZeroDelaySkillTapAllHero injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal Fast Loot, Auto Pick-Up Guns & Fast Sprint ─────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastLootAndSprint
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"AutoPickup", "1"}, {"AutoPickupSpeed", "2"},
        {"PUBGAutoLoot", "1"}, {"PUBGPickupPriority", "1"},
        {"PUBGFastWeaponSwitch", "1"}, {"FastWeaponSwitch", "1"},
        {"QuickThrow", "1"}, {"FastADS", "1"}, {"OneTapADS", "1"},
        {"QuickLoot", "1"}, {"QuickReload", "1"},
        {"PUBGQuickOpenScope", "1"}, {"PickupRangeBoost", "1.5"},
        {"LootResponseTime", "0"}, {"AutoSprint", "1"},
        {"bSprintAlways", "True"}, {"SprintSensitivity", "100"},
        {"MovementDeadzone", "0"}, {"FastSlide", "1"},
        {"SlideDelayMs", "0"}, {"SprintAcceleration", "10"},
        {"JoyStickDeadzone", "0"}, {"TouchResponseSprint", "1000"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("FastLootAndSprint injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB Penetration & Critical Burst Overdrive ─────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbPenetrationCritBurst
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"PhysicalPenetrationRatio", "1.0"}, {"MagicPenetrationRatio", "1.0"},
        {"FlatArmorShred", "100"}, {"TrueDamageConversion", "1.0"},
        {"PenetrationScaleFactor", "2.0"}, {"CriticalRateThreshold", "1.0"},
        {"CriticalDamageMultiplier", "3.0"}, {"CritBurstMultiplier", "2.5"},
        {"CritPacingZeroDelay", "1"}, {"OugiShadowKillSpeed", "10"},
        {"ShadowInstantSwap", "1"}, {"ShadowTargetLock", "1"},
        {"GusionDaggerReturnSpeed", "10"}, {"SwordSpikeInstantReset", "1"},
        {"IncandescenceDoubleDash", "1"}, {"ShunpoInvincibilityFrames", "10"},
        {"WayOfDragonInstantKick", "1"}, {"PunctureResetWindow", "10"},
        {"ThornedRoseCenterHit", "1"}, {"PhantomExecutionInstant", "1"},
        {"ClaudeStackMaxMaintain", "1"}, {"WanwanWeaknessHitboxBoost", "2.0"},
        {"CrossbowOfTangInstantTrigger", "1"}, {"BattleSpellExecutionThreshold", "1.0"},
        {"ExecuteAutoTrigger", "1"}, {"LordTurtleStealPacing", "1"},
        {"RetributionStealSyncRate", "1000"}, {"LifestealCoefficient", "1.0"},
        {"SpellVampCoefficient", "1.0"}, {"AntiHealBypass", "1"},
        {"ShieldAbsorbRatio", "2.0"}
    };
    for (const auto& kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbPenetrationCritBurst injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM Ballistics Velocity & Armor Penetration Overdrive ─────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmBallisticsVelocityPenetration
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> cv = {
        {"r.PUBGMuzzleVelocityBoost", "2.0"}, {"r.PUBGBulletFlightTimeZero", "1"},
        {"r.PUBGHitScanSimulation", "1"}, {"r.PUBGArmorPenetrationLevel3", "1.0"},
        {"r.PUBGHelmetPenetrationLevel3", "1.0"}, {"r.PUBGLimbDamageMultiplier", "1.5"},
        {"r.PUBGFleshDamageMultiplier", "2.0"}, {"r.PUBGVestDamageBypass", "1"},
        {"r.PUBGShotgunPelletSpread", "0.0"}, {"r.PUBGChokeTightness", "1.0"},
        {"r.PUBGSniperHeadshotDamage", "300"}, {"r.PUBGCameraShakeIntensity", "0.0"},
        {"r.PUBGScopeVisualBob", "0.0"}, {"r.PUBGVehicleDamageMultiplier", "2.5"}
    };
    std::vector<std::pair<std::string,std::string>> pl = {
        {"MuzzleVelocityBoost", "2.0"}, {"BulletFlightTimeZero", "1"},
        {"ClientHitRegistrationPacing", "1000"}, {"HitScanSimulation", "1"},
        {"NetClientLagCompensation", "1"}, {"ArmorPenetrationLevel3", "1.0"},
        {"HelmetPenetrationLevel3", "1.0"}, {"LimbDamageMultiplier", "1.5"},
        {"FleshDamageMultiplier", "2.0"}, {"VestDamageBypass", "1"},
        {"ShotgunPelletSpread", "0.0"}, {"ChokeTightness", "1.0"},
        {"PelletDamageFull", "1"}, {"DBS_DoubleTapDelayMs", "0"},
        {"SniperHeadshotDamage", "300"}, {"BoltActionQuickCycle", "1"},
        {"NoScopeCrosshairAccuracy", "1.0"}, {"BulletPenetrationDistance", "1000"},
        {"M416_VerticalRecoilMin", "0"}, {"BerylM762_HorizontalBounce", "0"},
        {"AKM_FirstShotKick", "0"}, {"CameraShakeIntensity", "0.0"},
        {"ScopeVisualBob", "0.0"}, {"VehicleDamageMultiplier", "2.5"},
        {"VehicleOccupantPenetration", "1"}
    };
    if (isCvar) {
        for (const auto& kv : cv) patch_cvar(content, kv.first, kv.second);
        for (const auto& kv : pl) patch_key_value(content, kv.first, kv.second);
    } else if (isXml) {
        for (const auto& kv : pl) {
            std::string t = "int";
            if (kv.second.find('.') != std::string::npos) t = "float";
            else if (kv.second == "True" || kv.second == "False") t = "string";
            patch_xml_node(content, t, kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : pl) {
            bool n = !kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-');
            patch_json_node(content, kv.first, kv.second, n);
        }
    } else {
        for (const auto& kv : pl) patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("PubgmBallisticsVelocityPenetration injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM BSA Removal & Infinite Range Overdrive ─────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmBsaRemovalRangeOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"BulletSpreadAccuracy", "0.0"}, {"ADSBulletSpreadDecay", "0.0"},
        {"HipfireBloom", "0.0"}, {"InitialBulletSpread", "0.0"},
        {"DamageRangeFalloff", "0.0"}, {"DamageRangeMultiplier", "3.0"},
        {"MinDamageMultiplier", "1.0"}, {"DamagePerShotMax", "100"},
        {"SprintToFireDelayMs", "0"}, {"ADSTransitionTimeMs", "0"},
        {"FastBoltPullSpeed", "2.0"}, {"QuickDrawFactor", "2.0"},
        {"HitFlinchScale", "0.0"}, {"FlinchRecoveryRate", "10.0"},
        {"ScreenShakeScale", "0.0"}, {"QuickScopeAccuracyThreshold", "1.0"},
        {"BlankScopeAccuracy", "1.0"}, {"SniperADSIdleSway", "0.0"},
        {"OneShotKillHitbox", "1"}, {"ShotgunDamagePerPellet", "50"},
        {"PelletSpreadADS", "0.0"}, {"PumpActionCycleMs", "0"}
    };
    for (const auto& kv : keys) {
        if (isXml) {
            std::string t = "int";
            if (kv.second.find('.') != std::string::npos) t = "float";
            patch_xml_node(content, t, kv.first, kv.second);
        } else if (isJson) {
            bool n = !kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-');
            patch_json_node(content, kv.first, kv.second, n);
        } else if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmBsaRemovalRangeOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── Universal Combat Mechanics & True Damage Overdrive ──────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalCombatMechanicsOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"TouchSampleRate", "1000"}, {"TouchZeroDelay", "1"},
        {"InputBufferRate", "1000"}, {"ZeroLatencyEventQueue", "1"},
        {"AttackAnimationCancel", "1"}, {"PostAttackRecoveryFrames", "0"},
        {"PreAttackWindupFrames", "0"}, {"FrameSyncDamage", "1"},
        {"ClientDamagePacing", "185"}, {"NetworkDamagePacketBatching", "0"},
        {"UniversalArmorPiercing", "1.0"}, {"TrueDamageMode", "1"},
        {"EffectiveDPSMultiplier", "3.0"}
    };
    for (const auto& kv : keys) {
        if (isXml) {
            std::string t = "int";
            if (kv.second.find('.') != std::string::npos) t = "float";
            patch_xml_node(content, t, kv.first, kv.second);
        } else if (isJson) {
            bool n = !kv.second.empty() && (isdigit((unsigned char)kv.second[0]) || kv.second[0] == '-');
            patch_json_node(content, kv.first, kv.second, n);
        } else if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("UniversalCombatMechanicsOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFastLoadSplashBypass(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"SkipOpenVideo", "1"}, {"SkipSplashVideo", "1"},
        {"FastLoadAssets", "1"}, {"DragonResourceOptimize", "1"},
        {"HighQualityLoad", "0"}, {"UIAsyncLoad", "1"},
        {"AudioPreload", "0"}, {"AsyncShaderWarmup", "1"},
        {"PreloadResources", "1"}, {"PreloadHeroes", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) {
            patch_xml_node(content, "int", kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbFastLoadSplashBypass injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastLoadAsyncStreaming(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"s.AsyncLoadingThreadEnabled", "True"},
        {"s.AsyncLoadingTimeLimit", "10.0"},
        {"s.PriorityAsyncLoadingExtraTime", "20.0"},
        {"r.TextureStreaming", "1"},
        {"r.Streaming.PoolSize", "1024"},
        {"r.Streaming.UseBackgroundThreadPool", "1"},
        {"r.ShaderCompiler.CoreCount", "8"},
        {"r.ShaderPipelineCache.StartupMode", "3"},
        {"bSkipSplash", "True"},
        {"bSkipMovie", "True"},
        {"r.Streaming.HLODStrategy", "2"}
    };
    for (const auto& kv : keys) {
        if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("PubgmFastLoadAsyncStreaming injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastLoadShaderBypass(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"FastLoad", "1"}, {"SkipIntroMovie", "1"},
        {"AsyncAssetLoading", "1"}, {"TextureStreamBufferSize", "512"},
        {"MaxAsyncLoadingTasks", "8"}, {"PreloadWeaponModels", "0"},
        {"ShaderPrewarmAtStartup", "0"}, {"FastShaderWarmup", "1"},
        {"LoadBalanceMode", "1"}
    };
    for (const auto& kv : keys) {
        if (isJson) {
            patch_json_node(content, kv.first, kv.second, true);
        } else if (isXml) {
            patch_xml_node(content, "int", kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmFastLoadShaderBypass injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalFastLoadTurbo(
        JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string,std::string>> keys = {
        {"FastLoad", "1"}, {"SkipSplash", "1"},
        {"SkipIntro", "1"}, {"AsyncLoadingThread", "1"},
        {"ShaderPrewarmAsync", "1"}, {"TextureStreamingBufferMB", "512"},
        {"MultiThreadedAssetLoading", "1"}
    };
    for (const auto& kv : keys) {
        if (isXml) {
            patch_xml_node(content, "int", kv.first, kv.second);
        } else if (isJson) {
            patch_json_node(content, kv.first, kv.second, true);
        } else if (isCvar) {
            patch_cvar(content, kv.first, kv.second);
        } else {
            patch_key_value(content, kv.first, kv.second);
        }
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("UniversalFastLoadTurbo injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// CODM: 165 FPS & Ultra Graphics Native Injector
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodm165FpsGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps, jint qualityLevel) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    int fps = (targetFps >= 120) ? targetFps : 165;
    int q = (qualityLevel > 0) ? qualityLevel : 4;
    std::string fpsStr = std::to_string(fps);
    std::string qStr = std::to_string(q);

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isControlsIni = (pathStr.rfind("ControlsSettings.ini") != std::string::npos);
    bool isHwJson = (pathStr.rfind("HardwareProfile.json") != std::string::npos);

    if (isHwJson) {
        std::vector<std::pair<std::string, std::string>> hwKeys = {
            {"DeviceModel", "SM-S948B"}, {"DeviceBrand", "samsung"}, {"Manufacturer", "samsung"},
            {"GPURenderer", "Adreno (TM) 840"}, {"GPUVendor", "Qualcomm"}, {"SoCModel", "SM8850-AB"},
            {"SoCManufacturer", "Qualcomm"}, {"CPUCores", "8"}, {"RAMTotalMB", "16384"},
            {"MaxFrameRate", fpsStr}, {"TargetFPS", fpsStr}, {"FPSLimit", fpsStr},
            {"FrameRateLimit", fpsStr}, {"MobileFPSLimit", fpsStr}, {"FrameRateLevel", "9"},
            {"GraphicQuality", qStr}, {"UnlockUltraHighFPS", "true"}, {"Unlock185Hz", "true"},
            {"Unlock165Hz", "true"}, {"Unlock144Hz", "true"}, {"Unlock120Hz", "true"},
            {"VulkanSupport", "true"}
        };
        for (const auto& kv : hwKeys) {
            bool isNum = (kv.second == "true" || kv.second == "false" ||
                          (kv.second.find_first_not_of("0123456789.") == std::string::npos));
            patch_json_node(content, kv.first, kv.second, isNum);
        }
    } else if (isControlsIni) {
        std::vector<std::pair<std::string, std::string>> ctrlKeys = {
            {"TouchBoostHz", fpsStr}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
            {"GyroSampleRate", "1000"}, {"GyroSensitivityRatio", "2.5"}, {"GyroZeroDelay", "1"},
            {"GyroSmoothFactor", "1"}, {"GyroStabilization", "1"}, {"JoystickZeroDeadzone", "1"},
            {"JoystickResponseLevel", "3"}, {"ZeroInputLag", "1"}
        };
        for (const auto& kv : ctrlKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else {
        std::vector<std::pair<std::string, std::string>> keys = {
            {"MaxFrameRate", fpsStr}, {"TargetFPS", fpsStr}, {"FPSLimit", fpsStr},
            {"FrameRateLimit", fpsStr}, {"MobileFPSLimit", fpsStr}, {"FrameRateLevel", "9"},
            {"GraphicQuality", qStr}, {"TextureQuality", qStr}, {"ShadowQuality", "2"},
            {"ShadowResolution", "2048"}, {"AntiAliasingQuality", "4"}, {"BloomQuality", "5"},
            {"MaxAnisotropy", "16"}, {"HDRMode", "1"}, {"HDR10Plus", "1"}, {"HDRColorMode", "2"},
            {"UltraHDMode", "1"}, {"SuperResolution", "1"}, {"ResolutionScale", "120"},
            {"UltraExtreme", "1"}, {"bUseUltraExtreme", "True"}, {"bFramePacingEnabled", "True"},
            {"Vsync", "0"}, {"Unlock90Hz", "1"}, {"Unlock120Hz", "1"}, {"Unlock144Hz", "1"},
            {"Unlock165Hz", "1"}, {"Unlock185Hz", "1"}, {"Unlock240Hz", "1"},
            {"Unlock144FPS", "1"}, {"Unlock165FPS", "1"}, {"Unlock185FPS", "1"},
            {"Ultra144FPS", "1"}, {"Ultra165FPS", "1"},
            {"TouchBoostHz", fpsStr}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
            {"GyroSampleRate", "1000"}, {"PreloadShaders", "1"}, {"VulkanPipelineCache", "1"},
            {"AsyncCompute", "1"}, {"VRS", "1"}, {"AllowOcclusionQueries", "1"}
        };
        for (const auto& kv : keys) {
            if (isXml) {
                std::string t = "int";
                if (kv.second.find('.') != std::string::npos) t = "float";
                else if (kv.second == "True" || kv.second == "False") t = "string";
                patch_xml_node(content, t, kv.first, kv.second);
            } else if (isJson) {
                bool isNum = (kv.second == "True" || kv.second == "False" ||
                              (kv.second.find_first_not_of("0123456789.") == std::string::npos));
                patch_json_node(content, kv.first, kv.second, isNum);
            } else {
                patch_key_value(content, kv.first, kv.second);
            }
        }
    }

    bool ok = write_file_atomic(pathStr, content, 0666);
    if (ok && hasStat) {
        struct utimbuf t;
        t.actime = stBefore.st_atime;
        t.modtime = stBefore.st_mtime;
        utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("Codm165FpsGraphics injected: %s [ok=%d, fps=%d, q=%d]", pathStr.c_str(), ok, fps, q);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// MLBB: 165 FPS & Ultra Graphics Native Injector
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbb165FpsGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps, jint qualityLevel) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    int fps = (targetFps >= 120) ? targetFps : 165;
    int q = (qualityLevel > 0) ? qualityLevel : 3;
    std::string fpsStr = std::to_string(fps);
    std::string qStr = std::to_string(q);

    // XML PlayerPrefs keys for MLBB
    std::vector<std::pair<std::string, std::string>> intKeys = {
        {"HighFPSMode", "3"},
        {"FrameRateLevel", "6"},
        {"QualityLevel", qStr},
        {"GraphicsQuality", qStr},
        {"TextureQuality", qStr},
        {"GraphicsPreset", "5"},
        {"HDMode", "1"},
        {"Shadow", "1"},
        {"Outline", "1"},
        {"CreepHP", "1"},
        {"DamageText", "1"},
        {"HeroLock", "1"},
        {"AimMethod", "1"},
        {"TargetPriority", "0"},
        {"SkillSmartAim", "1"},
        {"CameraHeight", "1"},
        {"ScreenShake", "0"},
        {"Vibrate", "0"},
        {"HFR", "1"},
        {"ShowFPS", "1"},
        {"FPS", fpsStr},
        {"MaxFPS", fpsStr},
        {"MaxFrameRate", fpsStr},
        {"TargetFPS", fpsStr},
        {"FrameRateLimit", fpsStr},
        {"MobileFPSLimit", fpsStr},
        {"HighFrameRate", "1"},
        {"UnlockFPS", "1"},
        {"SuperHighFPS", "1"},
        {"Unlock90Hz", "1"},
        {"Unlock120Hz", "1"},
        {"Unlock144Hz", "1"},
        {"Unlock165Hz", "1"},
        {"Unlock185Hz", "1"},
        {"Unlock240Hz", "1"},
        {"TouchBoostHz", fpsStr},
        {"TouchPollingRate", "1000"},
        {"TouchSampleRate", "1000"},
        {"HighFreqTouchHz", fpsStr},
        {"TouchSlopReduction", "1"},
        {"TouchResponseLevel", "3"},
        {"InputBufferRate", "1000"},
        {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"ZeroInputDelay", "1"},
        {"JoystickZeroDeadzone", "1"},
        {"JoystickResponseLevel", "3"},
        {"HitRegSyncRate", "1000"},
        {"VulkanPipelineCache", "1"},
        {"AsyncCompute", "1"},
        {"VRS", "1"},
        {"LightingQuality", "3"},
        {"ParticleQuality", "3"},
        {"PostProcessing", "1"},
        {"WaterReflection", "1"},
        {"VegetationDensity", "2"},
        {"RenderScale", "120"},
        {"PhysicsSimulation", "1"},
        {"RealTimeLight", "1"},
        {"DynamicResolution", "0"},
        {"UltraExtreme", "1"},
        {"UltraExtreme2026", "1"},
        {"Vsync", "0"},
        {"DisableLogging", "1"},
        {"DisableTelemetry", "1"},
        {"DisableCrashlytics", "1"},
        {"AntiLog", "1"},
        {"LogcatDisable", "1"},
        {"PreloadShaders", "1"},
        {"AllowOcclusionQueries", "1"}
    };

    if (content.find("<map>") == std::string::npos) {
        content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n</map>\n";
    }

    for (const auto &kv : intKeys) {
        patch_xml_node(content, "int", kv.first, kv.second);
    }

    patch_xml_node(content, "string", "bUseUltraExtreme", "True");
    patch_xml_node(content, "boolean", "bFramePacingEnabled", "true");

    bool ok = write_file_atomic(pathStr, content, 0666);
    if (ok && hasStat) {
        struct utimbuf t;
        t.actime = stBefore.st_atime;
        t.modtime = stBefore.st_mtime;
        utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("Mlbb165FpsGraphics injected: %s [ok=%d, fps=%d, q=%d]", pathStr.c_str(), ok, fps, q);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// ─── GVAS Binary Property Helper for PUBGM Active.sav ─────────────────────────
static bool patch_gvas_int_property_cpp(std::vector<uint8_t> &data, const std::string &propName, int value) {
    if (data.empty() || propName.empty()) return false;
    const char propTag[] = "IntProperty\0";
    size_t nameLen = propName.length();
    size_t tagLen = sizeof(propTag); // 13 bytes including null terminator
    bool modified = false;

    for (size_t i = 0; i + nameLen + tagLen + 13 <= data.size(); i++) {
        if (memcmp(data.data() + i, propName.data(), nameLen) == 0) {
            for (size_t j = i + nameLen; j <= i + nameLen + 48 && j + tagLen + 13 <= data.size(); j++) {
                if (memcmp(data.data() + j, propTag, 12) == 0) {
                    size_t valOffset = j + 12 + 9;
                    if (valOffset + 4 <= data.size()) {
                        data[valOffset]     = (uint8_t)(value & 0xFF);
                        data[valOffset + 1] = (uint8_t)((value >> 8) & 0xFF);
                        data[valOffset + 2] = (uint8_t)((value >> 16) & 0xFF);
                        data[valOffset + 3] = (uint8_t)((value >> 24) & 0xFF);
                        modified = true;
                    }
                    break;
                }
            }
        }
    }
    return modified;
}

// =============================================================================
// PUBGM: 165 FPS & HDR Graphics Native Injector
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgm165FpsGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps, jint qualityLevel) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    if (!path) return JNI_FALSE;
    std::string pathStr(path);
    struct stat stBefore;
    bool hasStat = (stat(path, &stBefore) == 0);

    int fps = (targetFps >= 120) ? targetFps : 165;
    // In PUBGM UE4 engine: Level 7 is 120/165 FPS tier. Levels > 7 fail validation.
    int effectiveLevel = (fps >= 120) ? 7 : 6;
    int q = (qualityLevel > 0) ? qualityLevel : 4; // 4 = HDR
    std::string fpsStr = std::to_string(fps);
    std::string effLvlStr = std::to_string(effectiveLevel);
    std::string qStr = std::to_string(q);

    // 1. Binary GVAS savegame (Active.sav / ActiveShadow.sav)
    if (pathStr.rfind(".sav") != std::string::npos || pathStr.find("Active") != std::string::npos) {
        std::ifstream file(pathStr, std::ios::binary);
        if (!file.is_open()) {
            env->ReleaseStringUTFChars(jPath, path);
            return JNI_FALSE;
        }
        std::vector<uint8_t> data((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
        file.close();

        if (data.empty()) {
            env->ReleaseStringUTFChars(jPath, path);
            return JNI_FALSE;
        }

        bool mod = false;
        mod |= patch_gvas_int_property_cpp(data, "FPSLevel", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "BattleFPS", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "LobbyFPS", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "MainCityFPS", effectiveLevel);
        mod |= patch_gvas_int_property_cpp(data, "HighFPSMode", 3);
        mod |= patch_gvas_int_property_cpp(data, "BattleRenderQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "LobbyRenderQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "MainCityRenderQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "GraphicQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "ArtQuality", q);
        mod |= patch_gvas_int_property_cpp(data, "MobileHDRMode", 1);
        mod |= patch_gvas_int_property_cpp(data, "ShadowQuality", 3);
        mod |= patch_gvas_int_property_cpp(data, "ShadowSwitch", 1);
        mod |= patch_gvas_int_property_cpp(data, "AutoChangeQuality", 0);

        std::string tmpPath = pathStr + ".tmp";
        std::ofstream out(tmpPath, std::ios::binary | std::ios::trunc);
        if (!out.is_open()) {
            env->ReleaseStringUTFChars(jPath, path);
            return JNI_FALSE;
        }
        out.write(reinterpret_cast<const char*>(data.data()), data.size());
        out.close();
        chmod(tmpPath.c_str(), 0666);
        bool ok = (rename(tmpPath.c_str(), pathStr.c_str()) == 0);
        if (ok && hasStat) {
            struct utimbuf t;
            t.actime = stBefore.st_atime;
            t.modtime = stBefore.st_mtime;
            utime(path, &t);
        }
        env->ReleaseStringUTFChars(jPath, path);
        LOGI("PubgmActiveSav 165 FPS & HDR injected: %s [ok=%d]", pathStr.c_str(), ok);
        return ok ? JNI_TRUE : JNI_FALSE;
    }

    // 2. INI / XML Text configs
    std::string content = read_file_posix(pathStr);

    if (pathStr.rfind(".xml") != std::string::npos) {
        if (content.find("<map>") == std::string::npos) {
            content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n</map>\n";
        }
        patch_xml_node(content, "int", "FPS", fpsStr);
        patch_xml_node(content, "int", "FrameRateLevel", effLvlStr);
        patch_xml_node(content, "int", "GraphicQuality", qStr);
        patch_xml_node(content, "int", "MobileHDRMode", "1");
        patch_xml_node(content, "int", "HighFPSMode", "3");
        patch_xml_node(content, "int", "Unlock165Hz", "1");
        patch_xml_node(content, "int", "Unlock165FPS", "1");
    } else if (pathStr.find("EnjoyCJZC.ini") != std::string::npos || pathStr.find("EnjoyCJ.ini") != std::string::npos) {
        // EnjoyCJZC UserSetting section
        std::vector<std::pair<std::string, std::string>> enjoyKeys = {
            {"FrameRateLevel", effLvlStr}, {"BattleFPS", effLvlStr}, {"LobbyFPS", effLvlStr},
            {"FPS", fpsStr}, {"MaxFPS", fpsStr}, {"TargetFPS", fpsStr}, {"FrameRateLimit", fpsStr},
            {"MobileFPSLimit", fpsStr}, {"GraphicQuality", qStr}, {"ArtQuality", qStr},
            {"ShadowQuality", "3"}, {"MobileHDRMode", "1"}, {"HighFPSMode", "3"},
            {"bUseHDRMode", "True"}, {"bUseUltraExtreme", "True"}, {"bFramePacingEnabled", "True"},
            {"UnlockFPS", "1"}, {"Unlock120Hz", "1"}, {"Unlock144Hz", "1"}, {"Unlock165Hz", "1"},
            {"Unlock185Hz", "1"}, {"Unlock240Hz", "1"}, {"Unlock165FPS", "1"}, {"Ultra165FPS", "1"},
            {"+CVars=r.PUBGDeviceFPS", effLvlStr}, {"+CVars=r.PUBGTargetFPS", fpsStr},
            {"+CVars=r.MobileHDR", "1"}, {"+CVars=r.PUBGHDRMode", "1"}
        };
        for (const auto &kv : enjoyKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else if (pathStr.find("GameUserSettings.ini") != std::string::npos) {
        std::vector<std::pair<std::string, std::string>> gusKeys = {
            {"FrameRateLimit", fpsStr + ".000000"}, {"bUseVSync", "False"},
            {"bUseDynamicResolution", "False"}, {"ResolutionSizeX", "2400"},
            {"ResolutionSizeY", "1080"}, {"LastUserConfirmedResolutionSizeX", "2400"},
            {"LastUserConfirmedResolutionSizeY", "1080"}
        };
        for (const auto &kv : gusKeys) {
            patch_key_value(content, kv.first, kv.second);
        }
    } else {
        // UserCustom.ini / DeviceProfile.ini / Quality.ini
        std::vector<std::pair<std::string, std::string>> ue4Keys = {
            {"+CVars=r.PUBGDeviceFPS", effLvlStr},
            {"+CVars=r.PUBGDeviceFPSPolicy", "1"},
            {"+CVars=r.DefaultDeviceFPS", effLvlStr},
            {"+CVars=r.UserFPSSetting", effLvlStr},
            {"+CVars=r.PUBGTargetFPS", fpsStr},
            {"+CVars=r.PUBGMaxFPS", fpsStr},
            {"+CVars=r.PUBGFrameRateLimit", fpsStr},
            {"+CVars=r.FrameRateLimit", fpsStr},
            {"+CVars=r.MobileFPSLimit", fpsStr},
            {"+CVars=r.Vsync", "0"},
            {"+CVars=r.Unlock120Hz", "1"},
            {"+CVars=r.Unlock144Hz", "1"},
            {"+CVars=r.Unlock165Hz", "1"},
            {"+CVars=r.Unlock185Hz", "1"},
            {"+CVars=r.Unlock240Hz", "1"},
            {"+CVars=r.TouchBoostHz", fpsStr},
            {"+CVars=r.MobileTouchBoostRate", fpsStr},
            {"+CVars=r.FramePacing", "1"},
            {"+CVars=r.MobileHDR", "1"},
            {"+CVars=r.PUBGHDRMode", "1"},
            {"+CVars=r.PUBGQualityLevel", qStr},
            {"+CVars=r.PUBGSDKQualityLevel", qStr},
            {"+CVars=r.UserQualitySetting", qStr},
            {"+CVars=r.ShadowQuality", "3"},
            {"+CVars=r.PostProcessAAQuality", "3"},
            {"+CVars=r.Tonemapper.Quality", "4"},
            {"+CVars=r.MobileContentScaleFactor", "1.0"},
            {"+CVars=r.MaxAnisotropy", "16"},
            {"+CVars=r.TemporalAA.Upscale", "1"},
            {"+CVars=r.AllowOcclusionQueries", "1"},
            {"+CVars=r.Vulkan.Enable", "1"},
            {"+CVars=r.Vulkan.PipelineCache", "1"},
            {"+CVars=r.AsyncCompute", "1"},
            {"+CVars=r.VRS.Enable", "1"},
            {"FPS", fpsStr},
            {"MaxFPS", fpsStr},
            {"TargetFPS", fpsStr},
            {"FrameRateLimit", fpsStr},
            {"MobileFPSLimit", fpsStr},
            {"FrameRateLevel", effLvlStr},
            {"GraphicQuality", qStr},
            {"ArtQuality", qStr},
            {"UnlockFPS", "1"},
            {"Unlock165FPS", "1"},
            {"Ultra165FPS", "1"},
            {"HighFPSMode", "3"},
            {"HDRMode", "1"},
            {"TouchBoostHz", fpsStr},
            {"TouchPollingRate", "1000"}
        };
        for (const auto &kv : ue4Keys) {
            patch_key_value(content, kv.first, kv.second);
        }
    }

    bool ok = write_file_atomic(pathStr, content, 0666);
    if (ok && hasStat) {
        struct utimbuf t;
        t.actime = stBefore.st_atime;
        t.modtime = stBefore.st_mtime;
        utime(path, &t);
    }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("Pubgm165FpsGraphics injected: %s [ok=%d, fps=%d, q=%d]", pathStr.c_str(), ok, fps, q);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: All-Hero Combat Overdrive (Max DMG, CDR, AtkSpd, Zero Cost) ────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageLockMax", "1"}, {"PhysicalDamageBase", "10000"}, {"MagicDamageBase", "10000"},
        {"CritMultiplier", "3.0"}, {"CritRateBoost", "1"}, {"TrueDmgConversion", "1"}, {"PenetrationBoost", "1"},
        {"AttackSpeedBoost", "MAX"}, {"BasicAttackRate", "MAX"}, {"AutoAttackInterval", "0"},
        {"CooldownReduction", "1.0"}, {"SkillCDRatio", "0"}, {"ZeroSkillCost", "1"},
        {"EffectiveDPSMode", "3"}, {"FrameSyncDamage", "1"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"ZeroInputDelay", "1"}, {"ZeroInputLag", "1"},
        {"TouchZeroDelay", "1"}, {"InputBufferRate", "1000"},
        {"HeroLock", "1"}, {"SkillSmartAim", "1"}, {"AimMagnetism", "3"},
        {"AutoAimAssist", "1"}, {"TargetLockNearest", "1"}, {"PhysicalDefense", "10000"}, {"MagicDefense", "10000"},
        {"ArmorRating", "10000"}, {"MaxHealthBoost", "10000"}, {"HealthRegenRate", "1000"},
        {"bFramePacingEnabled", "True"}, {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbAllHeroOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Fanny No-Energy-Limit & Free Cables ──────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFannyNoEnergyLimit
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"FannyEnergyRegen", "MAX"}, {"FannyEnergyLimit", "999"}, {"FannyEnergyNoDecay", "1"},
        {"FannyEnergyFull", "1"}, {"FannyEnergyMax", "1"}, {"AutoEnergyRefill", "1"},
        {"CableEnergyFree", "1"}, {"FannyMultiCableCombo", "1"}, {"CableCooldown", "0"},
        {"FannyCableCooldown", "0"}, {"ZeroSkillCost", "1"}, {"FannyInstantCableAim", "1"},
        {"FannyEnergyStartFull", "1"}, {"FannyCableChain", "1"}, {"FannyCableInstantRecast", "1"},
        {"FannyInstantRecall", "1"}, {"SkillAutoChain", "1"}, {"AimMagnetism", "3"},
        {"SkillSmartAim", "1"}, {"ZeroInputDelay", "1"}, {"ZeroInputLag", "1"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"bFramePacingEnabled", "True"}, {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbFannyNoEnergyLimit injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: Ling No-Energy-Limit & Wall Blink Free ───────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbLingNoEnergyLimit
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"LingEnergyLimit", "999"}, {"LingEnergyNoDecay", "1"}, {"LingWallEnergyFree", "1"},
        {"LingEnergyStartFull", "1"}, {"LingEnergyRegen", "MAX"}, {"ZeroSkillCost", "1"},
        {"LingSwordAutoChain", "1"}, {"LingBlinkChainMax", "1"}, {"BlinkChainMax", "1"},
        {"LingWallBlink", "1"}, {"WallJumpInstant", "1"}, {"LingInstantDash", "1"},
        {"TempestInstantCast", "1"}, {"LingUltInstant", "1"}, {"LingTempestBladeSpeed", "10"},
        {"LingSwordSpawnInstant", "1"}, {"ZeroInputDelay", "1"}, {"ZeroInputLag", "1"},
        {"DamageLockMax", "1"}, {"EffectiveDPSMode", "3"}, {"FrameSyncDamage", "1"},
        {"PenetrationBoost", "1"}, {"CritRateBoost", "1"}, {"CooldownReduction", "1"},
        {"SkillCDRatio", "0"}, {"SkillAutoChain", "1"}, {"AimMagnetism", "3"},
        {"SkillSmartAim", "1"}, {"HeroLock", "1"}, {"HitRegSyncRate", "1000"},
        {"TouchPollingRate", "1000"}, {"TouchZeroDelay", "1"},
        {"bFramePacingEnabled", "True"}, {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbLingNoEnergyLimit injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── MLBB: All Jungle Fast Farm Overdrive ────────────────────────────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllJungleFastFarmOverdrive
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        {"SmiteBoost", "3"}, {"JungleClearSpeed", "3"}, {"BuffDuration", "3"},
        {"BuffSteal", "1"}, {"MonsterDamageBoost", "3"}, {"ObjectivePriority", "1"},
        {"CounterJungle", "1"}, {"GoldRateBoost", "3"}, {"ExpRateBoost", "3"},
        {"CreepGoldMultiplier", "3"}, {"JungleExpBoost", "3"}, {"FastLevelUp", "1"},
        {"SmiteRange", "1"}, {"ClearSpeedBoost", "1"}, {"RetributionInstantCast", "1"},
        {"RetributionDamageMax", "1"}, {"AutoSmiteLock", "1"}, {"JungleMonsterTrueDmg", "1"},
        {"ZeroInputDelay", "1"}, {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"},
        {"bFramePacingEnabled", "True"}, {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("MlbbAllJungleFastFarmOverdrive injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── PUBGM: Tiered All-Scope Auto 3-Bullet Headshot (100m - 400m) ─────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAllScopeTieredHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        // Tiered scopes & distances: No-scope / 100m
        {"NoScopeAimMagnetism", "3"}, {"NoScopeHeadLock", "1"}, {"NoScopeTrackingBullet", "1"},
        {"Scope1xAimMagnetism", "3"}, {"Scope1xHeadLock", "1"}, {"Scope1xTrackingBullet", "1"},
        {"AimMagnetism100m", "3"}, {"HeadMagnetism100m", "1"}, {"TrackingBullet100m", "1"},
        {"HipfireAimLock", "1"}, {"HipfireTracking", "1"},
        // 200m (2x, 3x)
        {"Scope2xZeroRecoil", "1"}, {"Scope2xPredictiveAim", "1"}, {"Scope2xHeadLock", "1"}, {"Scope2xGyroStabilize", "1"},
        {"Scope3xZeroRecoil", "1"}, {"Scope3xPredictiveAim", "1"}, {"Scope3xHeadLock", "1"}, {"Scope3xGyroStabilize", "1"},
        {"AimMagnetism200m", "3"}, {"HeadLock200m", "1"}, {"PredictiveAim200m", "1"}, {"GyroStabilize200m", "1"},
        // 300m (4x, 6x)
        {"Scope4xBulletDropComp", "1"}, {"Scope4xSniperHeadLock", "1"}, {"Scope4xZeroBreathSway", "1"}, {"Scope4xAimLock", "1"},
        {"Scope6xBulletDropComp", "1"}, {"Scope6xSniperHeadLock", "1"}, {"Scope6xZeroBreathSway", "1"}, {"Scope6xAimLock", "1"},
        {"AimMagnetism300m", "3"}, {"HeadLock300m", "1"}, {"BulletDropComp300m", "1"}, {"ZeroBreathSway300m", "1"},
        // 400m (8x)
        {"Scope8xLongRangeHeadLock", "1"}, {"Scope8xThermalPrecision", "1"}, {"Scope8xZeroMicroJitter", "1"}, {"Scope8xBulletVelComp", "1"},
        {"AimMagnetism400m", "3"}, {"HeadLock400m", "1"}, {"LongRangePrecision400m", "1"}, {"BulletDropComp400m", "1"},
        // Global PUBG gunplay, burst & gyro stabilization
        {"AutoHeadshotBurst", "3"}, {"Auto3BulletHeadshot", "1"}, {"HeadshotBurstCount", "3"},
        {"WeaponRecoilScale", "0"}, {"WeaponSpreadScale", "0"}, {"RecoilZero", "1"},
        {"LessRecoil", "1"}, {"RecoilCompFactor", "0"}, {"ZeroHorizontalRecoil", "1"},
        {"BulletTrackingEnemy", "1"}, {"TrackingBullet", "1"}, {"BulletTracking", "1"},
        {"EnemyTrackingLock", "1"}, {"TrackingBulletVelocity", "1"},
        {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"},
        {"GyroMicroSmoothing", "1"}, {"GyroAimAssistLock", "1"},
        {"DamageLockMax", "1"}, {"PhysicalDefense", "10000"}, {"ArmorRating", "10000"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"ZeroInputDelay", "1"},
        {"SniperSnapSpeed", "10"}, {"AimSnapSpeed", "10"}, {"ZeroADSDelay", "1"},
        {"bFramePacingEnabled", "True"}, {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("PubgmAllScopeTieredHeadshot injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}

// =============================================================================
// ─── CODM: Tiered All-Scope Auto 3-Bullet Headshot (100m - 400m) ─────────────
// =============================================================================
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeTieredHeadshot
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path); std::string content = read_file_posix(pathStr);
    struct stat stBefore; bool hasStat = (stat(path, &stBefore) == 0);
    bool isXml  = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));
    bool isCvar = (content.find("+CVars=") != std::string::npos || pathStr.rfind("UserCustom.ini") != std::string::npos);
    std::vector<std::pair<std::string, std::string>> keys = {
        // 100m / No-scope / Hipfire
        {"HipfireMagnetism", "3"}, {"HeadBoneLock", "1"}, {"InstantAimSnap", "1"}, {"TrackingBullet", "1"},
        {"NoScopeAimMagnetism", "3"}, {"NoScopeHeadLock", "1"}, {"AimMagnetism100m", "3"}, {"HeadMagnetism100m", "1"},
        // 200m (Tactical Scope, 3x, 4x)
        {"MidScopeRecoilZero", "1"}, {"ARSMGHeadLock", "1"}, {"ScopeAimMag", "3"}, {"GyroMidStabilize", "1"},
        {"AimMagnetism200m", "3"}, {"HeadLock200m", "1"}, {"PredictiveAim200m", "1"},
        // 300m (4x, 4.4x, Tactical Scope)
        {"SniperMarkHeadLock", "1"}, {"BulletDropCompensation", "1"}, {"ZeroHoldBreath", "1"}, {"LRScopeAimLock", "1"},
        {"AimMagnetism300m", "3"}, {"HeadLock300m", "1"}, {"BulletDropComp300m", "1"},
        // 400m (Sniper Scope, 6x, 8x, RTG)
        {"SniperBlankScope", "1"}, {"HitscanLRLock", "1"}, {"ZeroMicroJitter", "1"}, {"UltraRangeHeadLock", "1"},
        {"AimMagnetism400m", "3"}, {"HeadLock400m", "1"}, {"LongRangePrecision400m", "1"},
        // Global CODM gunplay, burst, BSA removal & gyro stabilization
        {"AutoHeadshotBurst", "3"}, {"Auto3BulletHeadshot", "1"}, {"HeadshotBurstCount", "3"},
        {"BSARemoval", "1"}, {"WeaponSpread", "0"}, {"RecoilScale", "0"}, {"ZeroRecoil", "1"},
        {"LessRecoil", "1"}, {"BulletSpreadAccuracy", "0"}, {"BulletTrackingEnemy", "1"},
        {"DamageRangeFalloffBypass", "1"}, {"ZeroFlinch", "1"}, {"FlinchMultiplier", "0"},
        {"DamageLockMax", "1"}, {"ArmorRating", "10000"}, {"PhysicalDefense", "10000"},
        {"GyroSampleRate", "1000"}, {"GyroZeroDelay", "1"}, {"GyroStabilization", "1"},
        {"GyroMicroSmoothing", "1"}, {"GyroAimAssistLock", "1"},
        {"HitRegSyncRate", "1000"}, {"TouchPollingRate", "1000"}, {"ZeroInputDelay", "1"},
        {"ADSInstantTransition", "1"}, {"SprintToFireDelay", "0"},
        {"bFramePacingEnabled", "True"}, {"r.OneFrameThreadLag", "0"}, {"r.FinishCurrentFrame", "0"}
    };
    for (const auto &kv : keys) {
        if (isXml) patch_xml_node(content, "string", kv.first, kv.second);
        else if (isJson) patch_json_node(content, kv.first, kv.second, true);
        else if (isCvar) patch_cvar(content, kv.first, kv.second);
        else patch_key_value(content, kv.first, kv.second);
    }
    bool ok = write_file_atomic(pathStr, content);
    if (ok && hasStat) { struct utimbuf t; t.actime = stBefore.st_atime; t.modtime = stBefore.st_mtime; utime(path, &t); }
    env->ReleaseStringUTFChars(jPath, path);
    LOGI("CodmAllScopeTieredHeadshot injected: %s [ok=%d]", pathStr.c_str(), ok);
    return ok ? JNI_TRUE : JNI_FALSE;
}




