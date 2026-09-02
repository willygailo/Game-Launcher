package com.gamebooster.app.shizuku;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.gamebooster.app.BuildConfig;

import java.util.Collections;
import java.util.List;

import rikka.shizuku.Shizuku;

public class ShizukuUserServiceConnector {

    private static final String TAG = "ShizukuUserService";
    private static final ShizukuUserServiceConnector INSTANCE = new ShizukuUserServiceConnector();

    private IUserService userServiceInstance = null;
    private boolean isBinding = false;
    private long bindingStartedAt = 0L;

    private static final long BIND_STUCK_TIMEOUT_MS = 15000L;

    private final IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.w(TAG, "IUserService binder died. Cleaning up reference and attempting auto-rebind.");
            if (userServiceInstance != null) {
                userServiceInstance.asBinder().unlinkToDeath(deathRecipient, 0);
            }
            userServiceInstance = null;
            isBinding = false;
            ShizukuConnectionManager.getInstance().onBinderDead();
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "IUserService connected successfully under privileged shell UID.");
            userServiceInstance = IUserService.Stub.asInterface(service);
            try {
                service.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to link death recipient to UserService binder", e);
            }
            isBinding = false;
            ShizukuConnectionManager.getInstance().onBinderReceived();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "IUserService disconnected / unbound.");
            userServiceInstance = null;
            isBinding = false;
            ShizukuConnectionManager.getInstance().onBinderDead();
        }
    };

    private final Shizuku.UserServiceArgs serviceArgs = new Shizuku.UserServiceArgs(
            new ComponentName(BuildConfig.APPLICATION_ID, UserService.class.getName()))
            .daemon(true)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE);

    public static ShizukuUserServiceConnector getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isServiceConnected() {
        try {
            return userServiceInstance != null && userServiceInstance.asBinder() != null && userServiceInstance.asBinder().isBinderAlive();
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isServiceConnected(long waitTimeoutMs) {
        if (isServiceConnected()) {
            return true;
        }
        if (waitTimeoutMs <= 0 || android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            return isServiceConnected();
        }
        long deadline = System.currentTimeMillis() + waitTimeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isServiceConnected()) {
                return true;
            }
            try {
                Thread.sleep(40);
            } catch (InterruptedException ignored) {}
        }
        return isServiceConnected();
    }

    public synchronized void bindService() {
        if (isServiceConnected()) {
            return;
        }
        if (isBinding) {
            // Give Shizuku sufficient time to launch the daemon before forcing a rebind
            if (System.currentTimeMillis() - bindingStartedAt < BIND_STUCK_TIMEOUT_MS) {
                return;
            }
            Log.w(TAG, "Bind stuck > " + BIND_STUCK_TIMEOUT_MS + "ms — forcing clean rebind");
            try {
                Shizuku.unbindUserService(serviceArgs, serviceConnection, true);
            } catch (Throwable ignored) {}
            isBinding = false;
        }
        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Binding Shizuku UserService via AIDL (processSuffix=service, daemon=true, version=" + BuildConfig.VERSION_CODE + ")...");
                isBinding = true;
                bindingStartedAt = System.currentTimeMillis();
                try {
                    Shizuku.peekUserService(serviceArgs, serviceConnection);
                } catch (Throwable ignored) {}
                if (!isServiceConnected()) {
                    Shizuku.bindUserService(serviceArgs, serviceConnection);
                }
            } else {
                Log.d(TAG, "Shizuku not ready for bind (ping=" + (Shizuku.pingBinder()) + ")");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Failed to bind Shizuku UserService: " + e.getMessage(), e);
            isBinding = false;
            ShizukuConnectionManager.getInstance().onBindFailure();
        }
    }

    public synchronized void unbindService() {
        if (userServiceInstance != null) {
            try {
                userServiceInstance.asBinder().unlinkToDeath(deathRecipient, 0);
                Shizuku.unbindUserService(serviceArgs, serviceConnection, true);
                Log.d(TAG, "Shizuku UserService unbound.");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding Shizuku UserService", e);
            } finally {
                userServiceInstance = null;
                isBinding = false;
            }
        }
    }

    private void ensureBound() {
        if (!isServiceConnected()) {
            bindService();
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                int retries = 8;
                while (!isServiceConnected() && retries > 0) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                    retries--;
                }
            }
        }
    }

    public String executeCommand(String command) {
        ensureBound();
        if (userServiceInstance != null) {
            String direct = executeCommandDirect(command);
            if (direct != null) return direct;
        }
        return ShizukuExecutor.executeShizukuCommand(command);
    }

    /**
     * Executes on the AIDL user service WITHOUT any fallback — used by
     * {@link ShizukuExecutor} to avoid infinite mutual fallback recursion.
     */
    public String executeCommandDirect(String command) {
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.execCommand(command);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in execCommand", e);
                userServiceInstance = null;
            }
        }
        return null;
    }

    public List<String> executeBatchCommands(List<String> commands) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.execBatchCommands(commands);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in execBatchCommands", e);
                userServiceInstance = null;
            }
        }
        return Collections.emptyList();
    }

    public List<String> execBatchCommands(List<String> commands) {
        return executeBatchCommands(commands);
    }

    public boolean writeDirectFile(String path, String content, String mode) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.writeDirectFile(path, content, mode);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in writeDirectFile — fallback to shell", e);
                userServiceInstance = null;
            }
        }
        return false;
    }

    public String readDirectFile(String path) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.readDirectFile(path);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in readDirectFile — fallback to shell", e);
                userServiceInstance = null;
            }
        }
        return null;
    }

    public boolean deleteDirectFile(String path) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.deleteDirectFile(path);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in deleteDirectFile", e);
                userServiceInstance = null;
            }
        }
        return false;
    }

    public boolean makeDirectories(String path) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.makeDirectories(path);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in makeDirectories", e);
                userServiceInstance = null;
            }
        }
        return false;
    }

    public boolean fileExists(String path) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.fileExists(path);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in fileExists", e);
                userServiceInstance = null;
            }
        }
        return false;
    }

    public long getAvailableMemoryBytes() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.getAvailableMemoryBytes();
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in getAvailableMemoryBytes", e);
            }
        }
        return -1L;
    }

    public void forceDisplayRefreshRate(int hz) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.forceDisplayRefreshRate(hz);
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in forceDisplayRefreshRate", e);
            }
        }
        ShizukuExecutor.executeShizukuCommand("settings put system peak_refresh_rate " + hz + ".0; settings put system min_refresh_rate " + hz + ".0");
    }

    public void trimCachesAndDropCaches() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.trimCachesAndDropCaches();
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in trimCachesAndDropCaches", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("pm trim-caches 2000M; sync; echo 3 > /proc/sys/vm/drop_caches");
    }

    public void setCpuGpuPerformanceGovernors() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.setCpuGpuPerformanceGovernors();
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setCpuGpuPerformanceGovernors", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > \"$cpu\" 2>/dev/null; done; setprop debug.adreno.turbo 1; setprop debug.mali.sched.priority -20");
    }

    public void restoreCpuGpuGovernors() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.restoreCpuGpuGovernors();
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in restoreCpuGpuGovernors", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > \"$cpu\" 2>/dev/null; done; cmd power set-mode 2 0; cmd power set-mode 0 0; setprop debug.adreno.turbo 0");
    }

    public void optimize5GAndWifi() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.optimize5GAndWifi();
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in optimize5GAndWifi", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("cmd wifi force-low-latency-mode enabled; cmd wifi force-hi-perf-mode enabled; settings put global wifi_scan_always_enabled 0; settings put global mobile_data_always_on 1");
    }

    public boolean applyHardwareMask(String buildProps, String mockCpuInfo, String mockMemInfo) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.applyHardwareMask(buildProps, mockCpuInfo, mockMemInfo);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in applyHardwareMask", e);
            }
        }
        return false;
    }

    public boolean patchGameConfigFile(String targetPath, String content, String chmodMode) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.patchGameConfigFile(targetPath, content, chmodMode);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in patchGameConfigFile", e);
            }
        }
        return writeDirectFile(targetPath, content, chmodMode);
    }

    public void setGameModeApi(String packageName, int targetFps) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.setGameModeApi(packageName, targetFps);
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setGameModeApi", e);
            }
        }
        final int fps = targetFps > 0 ? targetFps : 185;
        ShizukuExecutor.executeShizukuCommands("cmd game mode performance " + packageName + "; cmd game set --fps " + fps + " " + packageName + "; cmd window set-app-refresh-rate " + packageName + " " + fps);
    }

    public void enforceAppOpsAndPermissions(String packageName) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.enforceAppOpsAndPermissions(packageName);
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in enforceAppOpsAndPermissions", e);
            }
        }
        ShizukuPermissionEnforcer.enforceGamePermissions(packageName);
    }

    public void applyThermalAndKernelBoost() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.applyThermalAndKernelBoost();
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in applyThermalAndKernelBoost", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("setprop debug.thermal.throttle.disable 1; setprop debug.performance.tuning 1; setprop debug.hwui.renderer vulkan");
    }

    public boolean setCpuAffinity(int pid, int mask) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.setCpuAffinity(pid, mask);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setCpuAffinity", e);
            }
        }
        int cpuMask = mask > 0 ? mask : 0xF0;
        String hexMask = Integer.toHexString(cpuMask);
        String res = ShizukuExecutor.executeShizukuCommand("taskset -p " + hexMask + " " + pid + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setProcessPriority(int pid, int niceLevel) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.setProcessPriority(pid, niceLevel);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setProcessPriority", e);
            }
        }
        int nice = (niceLevel >= -20 && niceLevel <= 19) ? niceLevel : -20;
        String res = ShizukuExecutor.executeShizukuCommand("renice -n " + nice + " -p " + pid + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean suppressHeadsUpNotifications(boolean suppress) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.suppressHeadsUpNotifications(suppress);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in suppressHeadsUpNotifications", e);
            }
        }
        int val = suppress ? 0 : 1;
        String res = ShizukuExecutor.executeShizukuCommand("settings put global heads_up_notifications_enabled " + val + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setGamingDnd(boolean enable) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.setGamingDnd(enable);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setGamingDnd", e);
            }
        }
        String filter = enable ? "priority" : "all";
        String dndMode = enable ? "on" : "off";
        String res = ShizukuExecutor.executeShizukuCommand("cmd notification set_interruption_filter " + filter + " 2>/dev/null; cmd notification set_dnd_mode " + dndMode + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean executeZramCompaction() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.executeZramCompaction();
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in executeZramCompaction", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("fstrim -v /data 2>/dev/null; fstrim -v /cache 2>/dev/null; sync; echo 3 > /proc/sys/vm/drop_caches 2>/dev/null; echo 1 > /proc/sys/vm/compact_memory 2>/dev/null; echo 1 > /sys/block/zram0/compact 2>/dev/null; cmd activity purge-cached-processes 2>/dev/null; cmd activity kill-all 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setNetworkQoS(boolean prioritize) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.setNetworkQoS(prioritize);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setNetworkQoS", e);
            }
        }
        String netVal = prioritize ? "true" : "false";
        String bgVal = prioritize ? "false" : "true";
        String res = ShizukuExecutor.executeShizukuCommand("cmd netpolicy set restrict-background " + netVal + " 2>/dev/null; cmd connectivity set-background-data " + bgVal + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean freezeApp(String packageName) {
        if (packageName == null) return false;
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.freezeApp(packageName);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in freezeApp", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("am force-stop " + packageName + " 2>/dev/null; pm suspend --user 0 " + packageName + " 2>/dev/null; cmd package suspend --user 0 " + packageName + " 2>/dev/null; cmd appops set " + packageName + " RUN_IN_BACKGROUND ignore 2>/dev/null; cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null; am set-standby-bucket " + packageName + " restricted 2>/dev/null; am set-standby-bucket " + packageName + " 45 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean unfreezeApp(String packageName) {
        if (packageName == null) return false;
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.unfreezeApp(packageName);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in unfreezeApp", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("pm unsuspend --user 0 " + packageName + " 2>/dev/null; cmd package unsuspend --user 0 " + packageName + " 2>/dev/null; cmd appops set " + packageName + " RUN_IN_BACKGROUND allow 2>/dev/null; cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null; am set-standby-bucket " + packageName + " active 2>/dev/null; am set-standby-bucket " + packageName + " 10 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean speedCompileGame(String packageName) {
        if (packageName == null) return false;
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.speedCompileGame(packageName);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in speedCompileGame", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("cmd package compile -m speed -f " + packageName + " 2>/dev/null; pm compile -m speed -f " + packageName + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setResolutionScale(int width, int height) {
        if (width <= 0 || height <= 0) return false;
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.setResolutionScale(width, height);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setResolutionScale", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("wm size " + width + "x" + height + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public void resetResolutionScale() {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                userServiceInstance.resetResolutionScale();
                return;
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in resetResolutionScale", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("wm size reset 2>/dev/null; wm density reset 2>/dev/null");
    }

    public boolean setGameGpuDriver(String packageName, String driverType) {
        if (packageName == null) return false;
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.setGameGpuDriver(packageName, driverType);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setGameGpuDriver", e);
            }
        }
        return false;
    }

    public boolean purgeAppLogsAndTraces(String packageName) {
        if (packageName == null) return false;
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.purgeAppLogsAndTraces(packageName);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in purgeAppLogsAndTraces", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("rm -rf /sdcard/Android/data/" + packageName + "/cache/* /sdcard/Android/data/" + packageName + "/files/*.log /sdcard/Android/data/" + packageName + "/files/dragon2017/assets/Logs/* 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setTouchSamplingRate(int rateHz) {
        ensureBound();
        if (userServiceInstance != null) {
            try {
                return userServiceInstance.setTouchSamplingRate(rateHz);
            } catch (Exception e) {
                Log.e(TAG, "RemoteException in setTouchSamplingRate", e);
            }
        }
        final int rate = rateHz > 0 ? rateHz : 1000;
        String res = ShizukuExecutor.executeShizukuCommand("setprop persist.sys.touch.report_rate " + rate + "; setprop debug.input.max_events_per_sec " + rate + "; setprop view.touch_slop 0");
        return res != null && !res.startsWith("ERROR");
    }
}
