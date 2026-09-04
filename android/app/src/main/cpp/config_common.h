#ifndef GAMEBOOSTER_CONFIG_COMMON_H
#define GAMEBOOSTER_CONFIG_COMMON_H

#include <jni.h>
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
#include <ctime>
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
#include <utility>

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

// ─── Fast Internal CRC32 ─────────────────────────────────────────────────────
uint32_t calculate_crc32(const uint8_t* data, size_t length);

// ─── Direct POSIX File & Directory Helpers ────────────────────────────────────
bool make_parent_dirs(const std::string& path);
bool write_file_atomic(const std::string& path, const std::string& content, mode_t mode = 0666);
std::string read_file_posix(const std::string& path);

// ─── Structural In-Memory Parsers (Zero-Corruption) ──────────────────────────
bool patch_key_value(std::string& content, const std::string& key, const std::string& value);
bool patch_cvar(std::string& content, const std::string& cvar, const std::string& value);
bool patch_xml_node(std::string& content, const std::string& tag, const std::string& key, const std::string& value);
bool patch_json_node(std::string& content, const std::string& key, const std::string& value, bool isNumeric = false);

// ─── Real CPU Core Topology & Affinity ───────────────────────────────────────
int detect_cpu_cluster_mask(bool bigCoresOnly = true);

// ─── Unified Key-Value Injection Engine Core ─────────────────────────────────
bool apply_keys_to_file(const std::string& pathStr, const char* path,
                        const std::vector<std::pair<std::string, std::string>>& keys,
                        const char* logTag);

// ─── GVAS Binary Property Helper for PUBGM Active.sav ─────────────────────────
bool patch_gvas_int_property_cpp(std::vector<uint8_t> &data, const std::string &propName, int value);

#endif // GAMEBOOSTER_CONFIG_COMMON_H
