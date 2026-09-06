package com.gamebooster.app.shizuku;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ShizukuKeepAliveWatchdogTest {

    @Test
    public void testWatchdogSingleton() {
        ShizukuKeepAliveWatchdog w1 = ShizukuKeepAliveWatchdog.getInstance();
        ShizukuKeepAliveWatchdog w2 = ShizukuKeepAliveWatchdog.getInstance();
        assertNotNull(w1);
        assertEquals(w1, w2);
    }

    @Test
    public void testShizukuPackageConstant() {
        assertEquals("moe.shizuku.privileged.api", ShizukuKeepAliveWatchdog.SHIZUKU_PKG);
    }

    @Test
    public void testInitialWatchdogState() {
        ShizukuKeepAliveWatchdog watchdog = ShizukuKeepAliveWatchdog.getInstance();
        assertFalse(watchdog.isWatchdogActive());
    }

    @Test
    public void testBuildImmunityCommandsContainsOomShielding() {
        ShizukuKeepAliveWatchdog watchdog = ShizukuKeepAliveWatchdog.getInstance();
        List<String> cmds = watchdog.buildImmunityCommands();
        assertNotNull(cmds);
        assertFalse(cmds.isEmpty());

        boolean hasOomNegative1000 = false;
        boolean hasShizukuPkg = false;
        boolean hasShizukuServer = false;

        for (String cmd : cmds) {
            if (cmd.contains("oom_score_adj") && cmd.contains("-1000")) {
                hasOomNegative1000 = true;
            }
            if (cmd.contains(ShizukuKeepAliveWatchdog.SHIZUKU_PKG)) {
                hasShizukuPkg = true;
            }
            if (cmd.contains("shizuku_server")) {
                hasShizukuServer = true;
            }
        }

        assertTrue("Immunity commands must include -1000 oom_score_adj", hasOomNegative1000);
        assertTrue("Immunity commands must target Shizuku package", hasShizukuPkg);
        assertTrue("Immunity commands must target shizuku_server", hasShizukuServer);
    }

    @Test
    public void testBuildImmunityCommandsContainsBatteryAndStandbyImmunity() {
        ShizukuKeepAliveWatchdog watchdog = ShizukuKeepAliveWatchdog.getInstance();
        List<String> cmds = watchdog.buildImmunityCommands();

        boolean hasDeviceidleWhitelist = false;
        boolean hasActiveStandbyBucket = false;
        boolean hasAppOpsRunInBackground = false;

        for (String cmd : cmds) {
            if (cmd.contains("deviceidle whitelist") && cmd.contains(ShizukuKeepAliveWatchdog.SHIZUKU_PKG)) {
                hasDeviceidleWhitelist = true;
            }
            if (cmd.contains("set-standby-bucket") && cmd.contains("active")) {
                hasActiveStandbyBucket = true;
            }
            if (cmd.contains("appops set") && cmd.contains("RUN_IN_BACKGROUND allow")) {
                hasAppOpsRunInBackground = true;
            }
        }

        assertTrue("Must whitelist Shizuku in deviceidle", hasDeviceidleWhitelist);
        assertTrue("Must pin Shizuku standby bucket to active", hasActiveStandbyBucket);
        assertTrue("Must grant RUN_IN_BACKGROUND AppOp to Shizuku", hasAppOpsRunInBackground);
    }

    @Test
    public void testBuildImmunityCommandsContainsPhantomProcessAndFreezerBypass() {
        ShizukuKeepAliveWatchdog watchdog = ShizukuKeepAliveWatchdog.getInstance();
        List<String> cmds = watchdog.buildImmunityCommands();

        boolean hasMaxPhantomProcs = false;
        boolean hasDisablePhantomMonitor = false;
        boolean hasDisableAppFreezer = false;

        for (String cmd : cmds) {
            if (cmd.contains("max_phantom_processes 2147483647")) {
                hasMaxPhantomProcs = true;
            }
            if (cmd.contains("settings_enable_monitor_phantom_procs false")) {
                hasDisablePhantomMonitor = true;
            }
            if (cmd.contains("cached_apps_freezer disabled")) {
                hasDisableAppFreezer = true;
            }
        }

        assertTrue("Must raise max_phantom_processes to 2B", hasMaxPhantomProcs);
        assertTrue("Must disable monitor phantom procs", hasDisablePhantomMonitor);
        assertTrue("Must disable cached apps freezer", hasDisableAppFreezer);
    }

    @Test
    public void testBuildImmunityCommandsContainsWirelessDebuggingKeepAlive() {
        ShizukuKeepAliveWatchdog watchdog = ShizukuKeepAliveWatchdog.getInstance();
        List<String> cmds = watchdog.buildImmunityCommands();

        boolean hasAdbWifi = false;
        boolean hasWifiSleep = false;
        boolean hasAdbTimeout = false;

        for (String cmd : cmds) {
            if (cmd.contains("adb_wifi_enabled 1")) {
                hasAdbWifi = true;
            }
            if (cmd.contains("wifi_sleep_policy 2")) {
                hasWifiSleep = true;
            }
            if (cmd.contains("adb_authorization_timeout 0")) {
                hasAdbTimeout = true;
            }
        }

        assertTrue("Must keep adb_wifi_enabled", hasAdbWifi);
        assertTrue("Must set wifi_sleep_policy to 2 (never)", hasWifiSleep);
        assertTrue("Must set adb_authorization_timeout to 0", hasAdbTimeout);
    }
}
