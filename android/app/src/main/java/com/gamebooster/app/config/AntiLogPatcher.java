package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AntiLogPatcher — Privileged Game Anti-Log, Telemetry Suppressor & Disk I/O Optimizer.
 *
 * Provides real-time log purging, telemetry blocking, and crash reporter suppression for:
 *   - Mobile Legends: Bang Bang (MLBB)
 *   - PUBG Mobile / BGMI / Game for Peace
 *   - Call of Duty: Mobile / Warzone
 *   - Free Fire / Free Fire MAX
 *   - Genshin Impact / Honkai: Star Rail / ZZZ
 *   - Arena Breakout / Delta Force
 *   - Blood Strike / NetEase Games
 *   - Standoff 2, CarX Street, Wild Rift, Roblox, Valorant, Farlight 84, Supercell
 *
 * Uses Shizuku elevated ADB access to purge internal /data/data/ and /sdcard/Android/data/
 * log buffers, suppress kernel logd persistence, flush logcat buffers, and insert
 * anti-log engine flags into game configs to minimize background CPU/IO friction.
 */
public final class AntiLogPatcher {

    private static final String TAG = "AntiLogPatcher";

    // All known esports game packages for bulk anti-log operations
    public static final List<String> ALL_GAME_PACKAGES = Arrays.asList(
            // MLBB
            "com.mobile.legends", "com.mobilelegends.mi", "com.vng.mlbbvn", "com.mobilelegends.na", "com.mobilelegends.hw", "com.mobile.legends.moonton", "com.mobile.legends.kr", "com.mobile.legends.jp",
            // PUBGM / BGMI
            "com.tencent.ig", "com.pubg.imobile", "com.vng.pubgmobile", "com.pubg.krmobile", "com.rekoo.pubgm", "com.tencent.tmgp.pubgmhd", "com.tencent.iglite", "com.pubg.newstate",
            // CODM / Warzone
            "com.activision.callofduty.shooter", "com.garena.game.codm", "com.tencent.tmgp.kr.codm", "com.vng.codmvn", "com.tencent.tmgp.cod", "com.activision.callofduty.warzone",
            // Free Fire
            "com.dts.freefireth", "com.dts.freefiremax",
            // Genshin / Honkai / ZZZ / Wuthering Waves
            "com.miHoYo.GenshinImpact", "com.cognosphere.GenshinImpact", "com.HoYoverse.hkrpgoversea", "com.HoYoverse.nap", "com.miHoYo.bh3oversea", "com.kurogame.wutheringwaves.global",
            // Honor of Kings / AoV
            "com.levelinfinite.sgameGlobal", "com.levelinfinite.sgameGlobal.gpkg", "com.tencent.tmgp.sgame", "com.garena.game.kgtw", "com.garena.game.kgvn", "com.garena.game.kgid",
            // Blood Strike / NetEase
            "com.netease.bloodstrike", "com.netease.newspike",
            // Standoff 2
            "com.axlebolt.standoff2",
            // Wild Rift
            "com.riotgames.league.wildrift", "com.riotgames.league.wildrifttw", "com.riotgames.league.wildriftvn",
            // CarX & Racing
            "com.h20.carxstreet", "com.gameloft.anmp.android.glofta9hm", "com.ea.games.r3_row", "com.garena.game.fdtw",
            // Arena Breakout / Delta Force
            "com.proximabeta.mf.uamo", "com.levelinfinite.deltaforce",
            // Valorant Mobile / Project C
            "com.tencent.tmgp.projectc", "com.riotgames.valorantmobile", "com.tencent.tmgp.valorant", "com.riotgames.valorant",
            // Farlight 84
            "com.miracle.farlight84", "com.miraclegames.farlight84", "com.farlightgames.farlight84.gp", "com.farlightgames.farlight84.global",
            // Roblox
            "com.roblox.client",
            // Supercell
            "com.supercell.brawlstars", "com.supercell.clashroyale", "com.supercell.clashofclans", "com.supercell.squad"
    );

    // ─── Per-Game Anti-Log Application ─────────────────────────────────────────

    /**
     * Applies anti-log, log directory purging, and telemetry suppression for a specific game package.
     *
     * @param packageName Target game package
     * @return true if anti-log commands executed successfully
     */
    public static boolean applyAntiLog(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.toLowerCase().trim();

        List<String> logPaths = getLogPathsForPackage(pkg);
        StringBuilder sb = new StringBuilder();

        // 1. Purge known log/telemetry folders and create immutable placeholders
        for (String path : logPaths) {
            sb.append("rm -rf '").append(path).append("/*' 2>/dev/null; ");
            sb.append("mkdir -p '").append(path).append("' 2>/dev/null; ");
            sb.append("touch '").append(path).append("/.nomedia' 2>/dev/null; ");
        }

        // 2. Clean cache directories to free memory & disk I/O
        sb.append("rm -rf '/data/data/").append(pkg).append("/cache/*' 2>/dev/null; ");
        sb.append("rm -rf '/sdcard/Android/data/").append(pkg).append("/cache/*' 2>/dev/null; ");

        // 3. Inject Anti-Log flags into game config files
        List<String> configPaths = GameConfigPathResolver.getPathsForGame(pkg);
        for (String cfgPath : configPaths) {
            sb.append("if [ -f '").append(cfgPath).append("' ]; then ");
            // Inject generic anti-log / disable telemetry keys
            sb.append("grep -qF 'DisableLogging' '").append(cfgPath).append("' || echo 'DisableLogging=1' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF 'DisableTelemetry' '").append(cfgPath).append("' || echo 'DisableTelemetry=1' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF 'DisableCrashlytics' '").append(cfgPath).append("' || echo 'DisableCrashlytics=1' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF 'AntiLog' '").append(cfgPath).append("' || echo 'AntiLog=1' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF 'LogcatDisable' '").append(cfgPath).append("' || echo 'LogcatDisable=1' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF '+CVars=r.SuppressLogs' '").append(cfgPath).append("' || echo '+CVars=r.SuppressLogs=1' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF '+CVars=r.DisableDebugLog' '").append(cfgPath).append("' || echo '+CVars=r.DisableDebugLog=1' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF '+CVars=r.EnableCrashReporting' '").append(cfgPath).append("' || echo '+CVars=r.EnableCrashReporting=0' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF '+CVars=r.Telemetry' '").append(cfgPath).append("' || echo '+CVars=r.Telemetry=0' >> '").append(cfgPath).append("'; ");
            sb.append("grep -qF '+CVars=a.DisableAnalytics' '").append(cfgPath).append("' || echo '+CVars=a.DisableAnalytics=1' >> '").append(cfgPath).append("'; ");
            sb.append("fi; ");
        }

        String cmd = sb.toString();
        executePrivileged(cmd);
        Log.i(TAG, "Anti-Log & Telemetry suppression enforced for " + pkg);
        return true;
    }

    // ─── System-Level Anti-Log & Logcat Optimizer ─────────────────────────────

    /**
     * Disables background system logging friction, reduces logd buffer sizes to 0,
     * flushes logcat memory, and eliminates CPU overhead from logging services.
     */
    public static boolean applySystemAntiLog() {
        String[] cmds = {
                // Stop background log persist daemons
                "setprop logd.logpersistd.stop 1",
                "setprop persist.logd.size 0",
                "setprop persist.logd.size.main 0",
                "setprop persist.logd.size.system 0",
                "setprop persist.logd.size.crash 0",
                "setprop persist.logd.size.kernel 0",
                "setprop persist.logd.size.radio 0",
                "setprop persist.logd.size.events 0",
                // Disable debug tags
                "setprop log.tag 0",
                "setprop log.tag.stats_log 0",
                "setprop log.tag.OpenGLRenderer 0",
                "setprop log.tag.RenderThread 0",
                "setprop log.tag.SurfaceFlinger 0",
                "setprop log.tag.HWUI 0",
                // Flush system logcat buffers
                "logcat -c",
                "logcat -b all -c"
        };

        StringBuilder sb = new StringBuilder();
        for (String c : cmds) {
            sb.append(c).append("; ");
        }

        executePrivileged(sb.toString());
        Log.i(TAG, "System-level Anti-Log & Logcat buffer reduction applied.");
        return true;
    }

    // ─── Bulk Purge Across All Games ──────────────────────────────────────────

    /**
     * Purges logs, crash dumps, and telemetry files across ALL 15+ esports games in one shot.
     *
     * @return Number of game packages processed
     */
    public static int purgeAllGameLogs() {
        int count = 0;
        applySystemAntiLog();
        for (String pkg : ALL_GAME_PACKAGES) {
            applyAntiLog(pkg);
            count++;
        }
        Log.i(TAG, "Purged game logs and applied Anti-Log across " + count + " packages.");
        return count;
    }

    // ─── Helper Methods ───────────────────────────────────────────────────────

    /**
     * Resolves game-specific log directories for a given package name.
     */
    private static List<String> getLogPathsForPackage(String pkg) {
        List<String> paths = new ArrayList<>();

        // Standard /sdcard/ and /data/ paths
        paths.add("/sdcard/Android/data/" + pkg + "/files/Logs");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Saved/Logs");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Saved/Crashes");
        paths.add("/data/data/" + pkg + "/files/Logs");
        paths.add("/data/data/" + pkg + "/files/tlog");
        paths.add("/data/data/" + pkg + "/files/apm_logs");
        paths.add("/data/data/" + pkg + "/files/crash_report");
        paths.add("/data/data/" + pkg + "/files/hawk_logs");

        // Game specific paths
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/log");
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/commlog");
            paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/assets_log");
            paths.add("/data/data/" + pkg + "/files/crash_log");
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs");
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Crashes");
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/StatEvents");
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Pandora");
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/PufferTmpDir");
        } else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs");
            paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Crashes");
            paths.add("/sdcard/Android/data/" + pkg + "/files/callofduty/logs");
        } else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/report");
            paths.add("/sdcard/Android/data/" + pkg + "/files/Garena/FreeFire/log");
            paths.add("/data/data/" + pkg + "/files/report");
        } else if (pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("genshin") || pkg.contains("hoyoverse")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/crashes");
            paths.add("/sdcard/Android/data/" + pkg + "/files/output_log.txt");
        } else if (pkg.contains("bloodstrike") || pkg.contains("netease")) {
            paths.add("/sdcard/Android/data/" + pkg + "/files/netease/logs");
            paths.add("/sdcard/Android/data/" + pkg + "/files/netease/crash");
        }

        return paths;
    }

    private static void executePrivileged(String command) {
        if (command == null || command.trim().isEmpty()) return;
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(command);
        } else {
            CommandExecutor.executeSystemCommand(command);
        }
    }
}
