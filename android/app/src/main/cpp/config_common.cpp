#include "config_common.h"
#include <cctype>
#include <sstream>
#include <iostream>
#include <fstream>
#include <algorithm>
#include <memory>
#include <unordered_map>

// ─── Fast Internal CRC32 Implementation ──────────────────────────────────────
uint32_t calculate_crc32(const uint8_t* data, size_t length) {
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
bool make_parent_dirs(const std::string& path) {
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

bool write_file_atomic(const std::string& path, const std::string& content, mode_t mode) {
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

std::string read_file_posix(const std::string& path) {
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

bool patch_key_value(std::string& content, const std::string& key, const std::string& value) {
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

bool patch_cvar(std::string& content, const std::string& cvar, const std::string& value) {
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

bool patch_xml_node(std::string& content, const std::string& tag, const std::string& key, const std::string& value) {
    // CRITICAL PROTECTION: If content is non-empty and does NOT have <map>, refuse to touch it!
    if (!content.empty() && content.find("<map>") == std::string::npos) {
        return false;
    }

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
        if (content.empty()) {
            content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n" + entry + "</map>\n";
        } else {
            content += entry;
        }
    }
    return true;
}

bool patch_json_node(std::string& content, const std::string& key, const std::string& value, bool isNumeric) {
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
int detect_cpu_cluster_mask(bool bigCoresOnly) {
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

// ─── Unified Key-Value Injection Engine Core ─────────────────────────────────
bool apply_keys_to_file(const std::string& pathStr, const char* path,
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
            std::string valLower = kv.second;
            std::transform(valLower.begin(), valLower.end(), valLower.begin(), ::tolower);
            std::string tag = "int";
            std::string valFinal = kv.second;
            if (valLower == "true" || valLower == "false") {
                tag = "boolean";
                valFinal = valLower;
            } else if (kv.second.find('.') != std::string::npos) {
                tag = "float";
            }
            patch_xml_node(content, tag, kv.first, valFinal);
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

// ─── GVAS Binary Property Helper for PUBGM Active.sav ─────────────────────────
bool patch_gvas_int_property_cpp(std::vector<uint8_t> &data, const std::string &propName, int value) {
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

bool patch_gvas_multiple_int_properties_cpp(std::vector<uint8_t> &data, const std::vector<std::pair<std::string, int>> &props) {
    if (data.empty() || props.empty()) return false;
    bool anyModified = false;
    for (const auto& p : props) {
        if (patch_gvas_int_property_cpp(data, p.first, p.second)) {
            anyModified = true;
        }
    }
    return anyModified;
}

// ─── Zero-Allocation Memory Mapping & Live Process Memory Hex Patching ───────

bool native_fast_hex_patch_mmap(const char* filepath,
                                const uint8_t* pattern, size_t patternLen,
                                const uint8_t* replacement, size_t replaceLen) {
    if (!filepath || !pattern || patternLen == 0 || !replacement || replaceLen == 0) {
        return false;
    }

    int fd = open(filepath, O_RDWR);
    if (fd < 0) {
        LOGW("native_fast_hex_patch_mmap: Failed to open %s (errno=%d)", filepath, errno);
        return false;
    }

    struct stat st;
    if (fstat(fd, &st) < 0 || st.st_size < static_cast<off_t>(patternLen)) {
        close(fd);
        return false;
    }

    size_t fileSize = static_cast<size_t>(st.st_size);
    void* mapAddr = mmap(nullptr, fileSize, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (mapAddr == MAP_FAILED) {
        close(fd);
        LOGW("native_fast_hex_patch_mmap: mmap failed for %s (errno=%d)", filepath, errno);
        return false;
    }

    // Advise kernel for sequential streaming scan
    madvise(mapAddr, fileSize, MADV_SEQUENTIAL | MADV_WILLNEED);

    uint8_t* bytes = static_cast<uint8_t*>(mapAddr);
    bool modified = false;
    size_t copyBytes = (replaceLen <= patternLen) ? replaceLen : patternLen;

    for (size_t i = 0; i + patternLen <= fileSize; ++i) {
        if (bytes[i] == pattern[0] && memcmp(bytes + i, pattern, patternLen) == 0) {
            memcpy(bytes + i, replacement, copyBytes);
            modified = true;
            i += patternLen - 1; // Advance past patched sequence
        }
    }

    if (modified) {
        msync(mapAddr, fileSize, MS_SYNC);
        LOGI("native_fast_hex_patch_mmap: Successfully patched in-memory file: %s", filepath);
    }

    munmap(mapAddr, fileSize);
    close(fd);
    return modified;
}

int64_t native_direct_memory_search(const char* filepath,
                                    const uint8_t* pattern, size_t patternLen) {
    if (!filepath || !pattern || patternLen == 0) {
        return -1;
    }

    int fd = open(filepath, O_RDONLY);
    if (fd < 0) {
        return -1;
    }

    struct stat st;
    if (fstat(fd, &st) < 0 || st.st_size < static_cast<off_t>(patternLen)) {
        close(fd);
        return -1;
    }

    size_t fileSize = static_cast<size_t>(st.st_size);
    void* mapAddr = mmap(nullptr, fileSize, PROT_READ, MAP_SHARED, fd, 0);
    if (mapAddr == MAP_FAILED) {
        close(fd);
        return -1;
    }

    madvise(mapAddr, fileSize, MADV_SEQUENTIAL | MADV_WILLNEED);

    const uint8_t* bytes = static_cast<const uint8_t*>(mapAddr);
    int64_t foundOffset = -1;

    for (size_t i = 0; i + patternLen <= fileSize; ++i) {
        if (bytes[i] == pattern[0] && memcmp(bytes + i, pattern, patternLen) == 0) {
            foundOffset = static_cast<int64_t>(i);
            break;
        }
    }

    munmap(mapAddr, fileSize);
    close(fd);
    return foundOffset;
}

int native_scan_and_patch_process_memory(pid_t pid,
                                        const char* moduleFilter,
                                        const uint8_t* pattern, size_t patternLen,
                                        const uint8_t* replacement, size_t replaceLen) {
    if (pid <= 0 || !pattern || patternLen == 0 || !replacement || replaceLen == 0) {
        return 0;
    }

    std::string mapsPath = "/proc/" + std::to_string(pid) + "/maps";
    std::ifstream mapsFile(mapsPath);
    if (!mapsFile.is_open()) {
        LOGW("native_scan_and_patch_process_memory: Cannot open %s", mapsPath.c_str());
        return 0;
    }

    std::string memPath = "/proc/" + std::to_string(pid) + "/mem";
    int memFd = open(memPath.c_str(), O_RDWR);

    int patchCount = 0;
    std::string line;
    const size_t CHUNK_SIZE = 65536; // 64KB scan window
    std::vector<uint8_t> buffer(CHUNK_SIZE + patternLen);

    while (std::getline(mapsFile, line)) {
        if (line.empty()) continue;

        // Apply module filter if specified
        if (moduleFilter && moduleFilter[0] != '\0') {
            if (line.find(moduleFilter) == std::string::npos) {
                continue;
            }
        }

        uintptr_t start = 0, end = 0;
        char perms[5] = {0};
        unsigned long long sTmp = 0, eTmp = 0;
        if (sscanf(line.c_str(), "%llx-%llx %4s", &sTmp, &eTmp, perms) < 3) {
            continue;
        }
        start = static_cast<uintptr_t>(sTmp);
        end = static_cast<uintptr_t>(eTmp);

        // Only scan readable segments
        if (perms[0] != 'r') continue;

        size_t regionSize = end - start;
        size_t offset = 0;

        while (offset < regionSize) {
            size_t toRead = std::min(CHUNK_SIZE, regionSize - offset);
            struct iovec localIov, remoteIov;
            localIov.iov_base = buffer.data();
            localIov.iov_len = toRead;
            remoteIov.iov_base = reinterpret_cast<void*>(start + offset);
            remoteIov.iov_len = toRead;

            ssize_t bytesRead = process_vm_readv(pid, &localIov, 1, &remoteIov, 1, 0);
            if (bytesRead <= 0 && memFd >= 0) {
                // Fallback to pread64 on /proc/<pid>/mem
                bytesRead = pread64(memFd, buffer.data(), toRead, static_cast<off64_t>(start + offset));
            }

            if (bytesRead > static_cast<ssize_t>(patternLen)) {
                size_t validBytes = static_cast<size_t>(bytesRead);
                for (size_t i = 0; i + patternLen <= validBytes; ++i) {
                    if (buffer[i] == pattern[0] && memcmp(buffer.data() + i, pattern, patternLen) == 0) {
                        uintptr_t targetAddr = start + offset + i;
                        size_t writeSize = (replaceLen <= patternLen) ? replaceLen : patternLen;

                        struct iovec localWrite, remoteWrite;
                        localWrite.iov_base = const_cast<uint8_t*>(replacement);
                        localWrite.iov_len = writeSize;
                        remoteWrite.iov_base = reinterpret_cast<void*>(targetAddr);
                        remoteWrite.iov_len = writeSize;

                        ssize_t written = process_vm_writev(pid, &localWrite, 1, &remoteWrite, 1, 0);
                        if (written <= 0 && memFd >= 0) {
                            written = pwrite64(memFd, replacement, writeSize, static_cast<off64_t>(targetAddr));
                        }

                        if (written > 0) {
                            patchCount++;
                            LOGI("native_scan_and_patch_process_memory: Patched pid=%d at addr=0x%llx", pid, (unsigned long long)targetAddr);
                        }
                        i += patternLen - 1;
                    }
                }
            }

            offset += toRead;
        }
    }

    if (memFd >= 0) {
        close(memFd);
    }
    return patchCount;
}
