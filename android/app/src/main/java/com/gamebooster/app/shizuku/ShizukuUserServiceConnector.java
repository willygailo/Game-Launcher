package com.gamebooster.app.shizuku;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.gamebooster.app.BuildConfig;
import com.gamebooster.app.core.AppExecutors;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

public class ShizukuUserServiceConnector {

    private static final String TAG = "ShizukuUserService";
    private static final ShizukuUserServiceConnector INSTANCE = new ShizukuUserServiceConnector();

    private volatile IUserService userServiceInstance = null;
    private volatile boolean isBinding = false;
    private volatile long bindingStartedAt = 0L;
    private volatile int cachedServicePid = -1;

    private static final long BIND_STUCK_TIMEOUT_MS = 4000L;
    private final AtomicBoolean rebindScheduled = new AtomicBoolean(false);

    private int consecutiveFailures = 0;

    public synchronized void resetUserServiceState() {
        consecutiveFailures = 0;
        isBinding = false;
    }

    public int getServicePid() {
        if (cachedServicePid > 0) return cachedServicePid;
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                int pid = instance.getPid();
                if (pid > 0) {
                    cachedServicePid = pid;
                    return pid;
                }
            } catch (Throwable ignored) {}
        }
        return -1;
    }

    public void scheduleSilentRebind() {
        if (rebindScheduled.compareAndSet(false, true)) {
            AppExecutors.getInstance().executeCommand(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
                rebindScheduled.set(false);
                if (!isServiceConnected() && Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Executing silent auto-rebind for IUserService daemon...");
                    bindService();
                }
            });
        }
    }

    private void handleRemoteException(String op, Exception e) {
        Log.w(TAG, "RemoteException in " + op + " (switching to elevated shell fallback): " + e.getMessage());
        userServiceInstance = null;
        cachedServicePid = -1;
        isBinding = false;
        scheduleSilentRebind();
    }

    private final IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.w(TAG, "IUserService binder died. Secondary daemon process was recycled by OS. Triggering auto-rebind...");
            if (userServiceInstance != null) {
                try {
                    userServiceInstance.asBinder().unlinkToDeath(deathRecipient, 0);
                } catch (Throwable ignored) {}
            }
            userServiceInstance = null;
            cachedServicePid = -1;
            isBinding = false;
            scheduleSilentRebind();
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "IUserService connected successfully under privileged shell UID.");
            userServiceInstance = IUserService.Stub.asInterface(service);
            consecutiveFailures = 0;
            isBinding = false;
            try {
                service.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to link death recipient to UserService binder", e);
            }
            try {
                int pid = userServiceInstance.getPid();
                cachedServicePid = pid;
                Log.i(TAG, "IUserService live PID=" + pid + " (shielding via Watchdog)");
                ShizukuKeepAliveWatchdog.getInstance().onUserServiceConnected(pid);
            } catch (Throwable t) {
                Log.w(TAG, "Failed to query UserService PID", t);
            }
            ShizukuConnectionManager.getInstance().onBinderReceived();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "IUserService disconnected / unbound. Auto-recovering...");
            userServiceInstance = null;
            cachedServicePid = -1;
            isBinding = false;
            scheduleSilentRebind();
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
            IUserService instance = userServiceInstance;
            if (instance != null && instance.asBinder() != null && instance.asBinder().isBinderAlive()) {
                try {
                    return instance.ping();
                } catch (RemoteException re) {
                    Log.w(TAG, "UserService binder alive but ping() failed (daemon zombie): " + re.getMessage());
                    handleRemoteException("ping", re);
                    return false;
                }
            }
            return false;
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
            consecutiveFailures = 0;
            return;
        }
        if (isBinding) {
            if (System.currentTimeMillis() - bindingStartedAt < BIND_STUCK_TIMEOUT_MS) {
                return;
            }
            consecutiveFailures++;
            Log.w(TAG, "Bind stuck > " + BIND_STUCK_TIMEOUT_MS + "ms (attempt " + consecutiveFailures + "). Resetting bind attempt...");
            try {
                // Pass false for remove so Shizuku does not terminate the daemon process
                Shizuku.unbindUserService(serviceArgs, serviceConnection, false);
            } catch (Throwable ignored) {}
            isBinding = false;
        }
        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Binding Shizuku UserService via AIDL (attempt " + (consecutiveFailures + 1) + ")...");
                isBinding = true;
                bindingStartedAt = System.currentTimeMillis();
                Shizuku.bindUserService(serviceArgs, serviceConnection);
            } else {
                Log.d(TAG, "Shizuku not ready for bind (ping=" + (Shizuku.pingBinder()) + ")");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Failed to bind Shizuku UserService: " + e.getMessage(), e);
            isBinding = false;
            consecutiveFailures++;
            ShizukuConnectionManager.getInstance().onBindFailure();
            scheduleSilentRebind();
        }
    }

    public synchronized void unbindService() {
        if (userServiceInstance != null) {
            try {
                userServiceInstance.asBinder().unlinkToDeath(deathRecipient, 0);
            } catch (Throwable ignored) {}
            try {
                Shizuku.unbindUserService(serviceArgs, serviceConnection, false);
                Log.d(TAG, "Shizuku UserService unbound cleanly.");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding Shizuku UserService", e);
            } finally {
                userServiceInstance = null;
                cachedServicePid = -1;
                isBinding = false;
            }
        }
    }

    private void ensureBound() {
        if (!isServiceConnected()) {
            bindService();
            if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                int retries = 6;
                while (!isServiceConnected() && retries > 0) {
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException ignored) {}
                    retries--;
                }
            }
        }
    }

    public String executeCommand(String command) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
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
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.execCommand(command);
            } catch (Exception e) {
                handleRemoteException("execCommand", e);
            }
        }
        return null;
    }

    public List<String> executeBatchCommands(List<String> commands) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.execBatchCommands(commands);
            } catch (Exception e) {
                handleRemoteException("execBatchCommands", e);
            }
        }
        return Collections.emptyList();
    }

    public List<String> execBatchCommands(List<String> commands) {
        return executeBatchCommands(commands);
    }

    public boolean writeDirectFile(String path, String content, String mode) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.writeDirectFile(path, content, mode);
            } catch (Exception e) {
                handleRemoteException("writeDirectFile", e);
            }
        }
        return ShizukuFileManager.writeFile(path, content, mode).success;
    }

    public String readDirectFile(String path) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.readDirectFile(path);
            } catch (Exception e) {
                handleRemoteException("readDirectFile", e);
            }
        }
        return ShizukuFileManager.readFile(path);
    }

    public boolean deleteDirectFile(String path) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.deleteDirectFile(path);
            } catch (Exception e) {
                handleRemoteException("deleteDirectFile", e);
            }
        }
        return ShizukuFileManager.deleteFile(path).success;
    }

    public boolean makeDirectories(String path) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.makeDirectories(path);
            } catch (Exception e) {
                handleRemoteException("makeDirectories", e);
            }
        }
        return ShizukuFileManager.makeDirectory(path);
    }

    public boolean fileExists(String path) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.fileExists(path);
            } catch (Exception e) {
                handleRemoteException("fileExists", e);
            }
        }
        return ShizukuFileManager.fileExists(path);
    }

    public long getAvailableMemoryBytes() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.getAvailableMemoryBytes();
            } catch (Exception e) {
                handleRemoteException("getAvailableMemoryBytes", e);
            }
        }
        return -1L;
    }

    public void forceDisplayRefreshRate(int hz) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.forceDisplayRefreshRate(hz);
                return;
            } catch (Exception e) {
                handleRemoteException("forceDisplayRefreshRate", e);
            }
        }
        ShizukuExecutor.executeShizukuCommand("settings put system peak_refresh_rate " + hz + ".0; settings put system min_refresh_rate " + hz + ".0");
    }

    public void trimCachesAndDropCaches() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.trimCachesAndDropCaches();
                return;
            } catch (Exception e) {
                handleRemoteException("trimCachesAndDropCaches", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("pm trim-caches 2000M; sync; echo 3 > /proc/sys/vm/drop_caches");
    }

    public void setCpuGpuPerformanceGovernors() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.setCpuGpuPerformanceGovernors();
                return;
            } catch (Exception e) {
                handleRemoteException("setCpuGpuPerformanceGovernors", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > \"$cpu\" 2>/dev/null; done; setprop debug.adreno.turbo 1; setprop debug.mali.sched.priority -20");
    }

    public void restoreCpuGpuGovernors() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.restoreCpuGpuGovernors();
                return;
            } catch (Exception e) {
                handleRemoteException("restoreCpuGpuGovernors", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo schedutil > \"$cpu\" 2>/dev/null; done; cmd power set-mode 2 0; cmd power set-mode 0 0; setprop debug.adreno.turbo 0");
    }

    public void optimize5GAndWifi() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.optimize5GAndWifi();
                return;
            } catch (Exception e) {
                handleRemoteException("optimize5GAndWifi", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("cmd wifi force-low-latency-mode enabled; cmd wifi force-hi-perf-mode enabled; settings put global wifi_scan_always_enabled 0; settings put global mobile_data_always_on 1");
    }

    public boolean applyHardwareMask(String buildProps, String mockCpuInfo, String mockMemInfo) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.applyHardwareMask(buildProps, mockCpuInfo, mockMemInfo);
            } catch (Exception e) {
                handleRemoteException("applyHardwareMask", e);
            }
        }
        return false;
    }

    public boolean patchGameConfigFile(String targetPath, String content, String chmodMode) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.patchGameConfigFile(targetPath, content, chmodMode);
            } catch (Exception e) {
                handleRemoteException("patchGameConfigFile", e);
            }
        }
        return writeDirectFile(targetPath, content, chmodMode);
    }

    public void setGameModeApi(String packageName, int targetFps) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.setGameModeApi(packageName, targetFps);
                return;
            } catch (Exception e) {
                handleRemoteException("setGameModeApi", e);
            }
        }
        final int fps = targetFps > 0 ? targetFps : 185;
        ShizukuExecutor.executeShizukuCommands("cmd game mode performance " + packageName + "; cmd game set --fps " + fps + " " + packageName + "; cmd window set-app-refresh-rate " + packageName + " " + fps);
    }

    public void enforceAppOpsAndPermissions(String packageName) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.enforceAppOpsAndPermissions(packageName);
                return;
            } catch (Exception e) {
                handleRemoteException("enforceAppOpsAndPermissions", e);
            }
        }
        ShizukuPermissionEnforcer.enforceGamePermissions(packageName);
    }

    public void applyThermalAndKernelBoost() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.applyThermalAndKernelBoost();
                return;
            } catch (Exception e) {
                handleRemoteException("applyThermalAndKernelBoost", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("setprop debug.thermal.throttle.disable 1; setprop debug.performance.tuning 1; setprop debug.hwui.renderer vulkan");
    }

    public boolean setCpuAffinity(int pid, int mask) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.setCpuAffinity(pid, mask);
            } catch (Exception e) {
                handleRemoteException("setCpuAffinity", e);
            }
        }
        int cpuMask = mask > 0 ? mask : 0xF0;
        String hexMask = Integer.toHexString(cpuMask);
        String res = ShizukuExecutor.executeShizukuCommand("taskset -p " + hexMask + " " + pid + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setProcessPriority(int pid, int niceLevel) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.setProcessPriority(pid, niceLevel);
            } catch (Exception e) {
                handleRemoteException("setProcessPriority", e);
            }
        }
        int nice = (niceLevel >= -20 && niceLevel <= 19) ? niceLevel : -20;
        String res = ShizukuExecutor.executeShizukuCommand("renice -n " + nice + " -p " + pid + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean suppressHeadsUpNotifications(boolean suppress) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.suppressHeadsUpNotifications(suppress);
            } catch (Exception e) {
                handleRemoteException("suppressHeadsUpNotifications", e);
            }
        }
        int val = suppress ? 0 : 1;
        String res = ShizukuExecutor.executeShizukuCommand("settings put global heads_up_notifications_enabled " + val + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setGamingDnd(boolean enable) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.setGamingDnd(enable);
            } catch (Exception e) {
                handleRemoteException("setGamingDnd", e);
            }
        }
        String filter = enable ? "priority" : "all";
        String dndMode = enable ? "on" : "off";
        String res = ShizukuExecutor.executeShizukuCommand("cmd notification set_interruption_filter " + filter + " 2>/dev/null; cmd notification set_dnd_mode " + dndMode + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean executeZramCompaction() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.executeZramCompaction();
            } catch (Exception e) {
                handleRemoteException("executeZramCompaction", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("fstrim -v /data 2>/dev/null; fstrim -v /cache 2>/dev/null; sync; echo 3 > /proc/sys/vm/drop_caches 2>/dev/null; echo 1 > /proc/sys/vm/compact_memory 2>/dev/null; echo 1 > /sys/block/zram0/compact 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setNetworkQoS(boolean prioritize) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.setNetworkQoS(prioritize);
            } catch (Exception e) {
                handleRemoteException("setNetworkQoS", e);
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
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.freezeApp(packageName);
            } catch (Exception e) {
                handleRemoteException("freezeApp", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("am force-stop " + packageName + " 2>/dev/null; pm suspend --user 0 " + packageName + " 2>/dev/null; cmd package suspend --user 0 " + packageName + " 2>/dev/null; cmd appops set " + packageName + " RUN_IN_BACKGROUND ignore 2>/dev/null; cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND ignore 2>/dev/null; am set-standby-bucket " + packageName + " restricted 2>/dev/null; am set-standby-bucket " + packageName + " 45 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean unfreezeApp(String packageName) {
        if (packageName == null) return false;
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.unfreezeApp(packageName);
            } catch (Exception e) {
                handleRemoteException("unfreezeApp", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("pm unsuspend --user 0 " + packageName + " 2>/dev/null; cmd package unsuspend --user 0 " + packageName + " 2>/dev/null; cmd appops set " + packageName + " RUN_IN_BACKGROUND allow 2>/dev/null; cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null; am set-standby-bucket " + packageName + " active 2>/dev/null; am set-standby-bucket " + packageName + " 10 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean speedCompileGame(String packageName) {
        if (packageName == null) return false;
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.speedCompileGame(packageName);
            } catch (Exception e) {
                handleRemoteException("speedCompileGame", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("cmd package compile -m speed -f " + packageName + " 2>/dev/null; pm compile -m speed -f " + packageName + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setResolutionScale(int width, int height) {
        if (width <= 0 || height <= 0) return false;
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.setResolutionScale(width, height);
            } catch (Exception e) {
                handleRemoteException("setResolutionScale", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("wm size " + width + "x" + height + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public void resetResolutionScale() {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                instance.resetResolutionScale();
                return;
            } catch (Exception e) {
                handleRemoteException("resetResolutionScale", e);
            }
        }
        ShizukuExecutor.executeShizukuCommands("wm size reset 2>/dev/null; wm density reset 2>/dev/null");
    }

    public boolean setGameGpuDriver(String packageName, String driverType) {
        if (packageName == null) return false;
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.setGameGpuDriver(packageName, driverType);
            } catch (Exception e) {
                handleRemoteException("setGameGpuDriver", e);
            }
        }
        return false;
    }

    public boolean purgeAppLogsAndTraces(String packageName) {
        if (packageName == null) return false;
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.purgeAppLogsAndTraces(packageName);
            } catch (Exception e) {
                handleRemoteException("purgeAppLogsAndTraces", e);
            }
        }
        String res = ShizukuExecutor.executeShizukuCommand("rm -rf /sdcard/Android/data/" + packageName + "/cache/* /sdcard/Android/data/" + packageName + "/files/*.log /sdcard/Android/data/" + packageName + "/files/dragon2017/assets/Logs/* 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }

    public boolean setTouchSamplingRate(int rateHz) {
        ensureBound();
        IUserService instance = userServiceInstance;
        if (instance != null) {
            try {
                return instance.setTouchSamplingRate(rateHz);
            } catch (Exception e) {
                handleRemoteException("setTouchSamplingRate", e);
            }
        }
        final int rate = rateHz > 0 ? rateHz : 1000;
        String res = ShizukuExecutor.executeShizukuCommand("setprop persist.sys.touch.report_rate " + rate + "; setprop debug.input.max_events_per_sec " + rate + "; setprop view.touch_slop 2");
        return res != null && !res.startsWith("ERROR");
    }
}
