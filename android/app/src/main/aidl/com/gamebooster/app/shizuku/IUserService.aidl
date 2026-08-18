package com.gamebooster.app.shizuku;

import java.util.List;

interface IUserService {
    void destroy() = 16777114; // Reserved Shizuku method ID for destroy
    String execCommand(String command) = 1;
    int getUid() = 2;
    List<String> execBatchCommands(in List<String> commands) = 3;
    boolean writeDirectFile(String path, String content, String mode) = 4;
    String readDirectFile(String path) = 5;
    boolean deleteDirectFile(String path) = 6;
    boolean makeDirectories(String path) = 7;
    boolean fileExists(String path) = 8;
    long getAvailableMemoryBytes() = 9;
    void forceDisplayRefreshRate(int hz) = 10;
    void trimCachesAndDropCaches() = 11;
    void setCpuGpuPerformanceGovernors() = 12;
    void optimize5GAndWifi() = 13;
    boolean applyHardwareMask(String buildProps, String mockCpuInfo, String mockMemInfo) = 14;
    boolean patchGameConfigFile(String targetPath, String content, String chmodMode) = 15;
    void setGameModeApi(String packageName, int targetFps) = 16;
    void enforceAppOpsAndPermissions(String packageName) = 17;
    void applyThermalAndKernelBoost() = 18;
    boolean isPrivilegedReady() = 19;
}
