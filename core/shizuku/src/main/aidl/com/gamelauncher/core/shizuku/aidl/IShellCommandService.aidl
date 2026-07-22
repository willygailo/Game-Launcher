// core/shizuku/src/main/aidl/com/gamelauncher/core/shizuku/aidl/IShellCommandService.aidl
package com.gamelauncher.core.shizuku.aidl;

/**
 * AIDL interface for privileged shell command execution via Shizuku UserService.
 */
interface IShellCommandService {
    boolean setPeakRefreshRate(float hz);
    boolean setMinRefreshRate(float hz);
    boolean setThermalOverride(boolean disabled);
    boolean writeSetting(String namespace, String key, String value);
    String readSetting(String namespace, String key);
    boolean setDeviceConfig(String namespace, String key, String value);
    String readDeviceConfig(String namespace, String key);
    boolean grantPermission(String packageName, String permissionName);
    boolean setAppOp(String packageName, String opName, String mode);
    void destroy();
}
