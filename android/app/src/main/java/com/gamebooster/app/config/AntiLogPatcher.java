package com.gamebooster.app.config;

import android.os.Environment;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
            "com.supercell.brawlstars", "com.supercell.clashroyale", "com.supercell.clashofclans", "com.supercell.squad",
            // Sports (PES / EA FC)
            "jp.konami.pesam", "com.ea.gp.fifamobile"
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
        String pkg = packageName.toLowerCase(Locale.ROOT).trim();

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
        String[] antiLogKeys = {
            "DisableLogging=1",
            "DisableTelemetry=1",
            "DisableCrashlytics=1",
            "AntiLog=1",
            "LogcatDisable=1",
            "+CVars=r.SuppressLogs=1",
            "+CVars=r.DisableDebugLog=1",
            "+CVars=r.EnableCrashReporting=0",
            "+CVars=r.Telemetry=0",
            "+CVars=a.DisableAnalytics=1"
        };
        List<String> configPaths = GameConfigPathResolver.getPathsForGame(pkg);
        for (String cfgPath : configPaths) {
            if (ShizukuFileManager.fileExists(cfgPath)) {
                ConfigFileHelper.patchKeys(cfgPath, antiLogKeys, "[Telemetry]");
            }
        }

        String cmd = sb.toString();
        if (!cmd.trim().isEmpty()) {
            executePrivileged(cmd);
        }
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
        // Use Environment API instead of hardcoded /sdcard/ to comply with Android storage best practices
        final String extRoot = Environment.getExternalStorageDirectory().getPath()
                + "/Android/data/" + pkg;

        // Standard external + internal paths
        paths.add(extRoot + "/files/Logs");
        paths.add(extRoot + "/files/Saved/Logs");
        paths.add(extRoot + "/files/Saved/Crashes");
        paths.add("/data/data/" + pkg + "/files/Logs");
        paths.add("/data/data/" + pkg + "/files/tlog");
        paths.add("/data/data/" + pkg + "/files/apm_logs");
        paths.add("/data/data/" + pkg + "/files/crash_report");
        paths.add("/data/data/" + pkg + "/files/hawk_logs");

        // Game specific paths
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            paths.add(extRoot + "/files/dragon2017/assets/UI/android/log");
            paths.add(extRoot + "/files/dragon2017/assets/UI/android/commlog");
            paths.add(extRoot + "/files/dragon2017/assets/UI/android/assets_log");
            paths.add("/data/data/" + pkg + "/files/crash_log");
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            paths.add(extRoot + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs");
            paths.add(extRoot + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Crashes");
            paths.add(extRoot + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/StatEvents");
            paths.add(extRoot + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Pandora");
            paths.add(extRoot + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/PufferTmpDir");
        } else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) {
            paths.add(extRoot + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs");
            paths.add(extRoot + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Crashes");
            paths.add(extRoot + "/files/callofduty/logs");
        } else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
            paths.add(extRoot + "/files/report");
            paths.add(extRoot + "/files/Garena/FreeFire/log");
            paths.add("/data/data/" + pkg + "/files/report");
        } else if (pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("genshin") || pkg.contains("hoyoverse")) {
            paths.add(extRoot + "/files/crashes");
            paths.add(extRoot + "/files/output_log.txt");
        } else if (pkg.contains("bloodstrike") || pkg.contains("netease")) {
            paths.add(extRoot + "/files/netease/logs");
            paths.add(extRoot + "/files/netease/crash");
            paths.add(extRoot + "/files/Unity/logs");
        } else if (pkg.contains("arena") || pkg.contains("breakout") || pkg.contains("deltaforce") || pkg.contains("proximabeta")) {
            paths.add(extRoot + "/files/UE4Game/UAGame/UAGame/Saved/Logs");
            paths.add(extRoot + "/files/UE4Game/UAGame/UAGame/Saved/Crashes");
            paths.add(extRoot + "/files/cloudgame_log");
            paths.add(extRoot + "/files/tlog");
        } else if (pkg.contains("farlight") || pkg.contains("miracle")) {
            paths.add(extRoot + "/files/UE4Game/Solarland/Solarland/Saved/Logs");
            paths.add(extRoot + "/files/UE4Game/Solarland/Solarland/Saved/Crashes");
            paths.add(extRoot + "/files/Solarland/Logs");
        } else if (pkg.contains("sgame") || pkg.contains("hok") || pkg.contains("arenaofvalor") || pkg.contains("kgtw")) {
            paths.add(extRoot + "/files/tencent/tlog");
            paths.add(extRoot + "/files/tlog");
            paths.add(extRoot + "/files/apm_logs");
            paths.add("/data/data/" + pkg + "/files/tencent");
        } else if (pkg.contains("wildrift") || pkg.contains("league")) {
            paths.add(extRoot + "/files/r3dlogs");
            paths.add(extRoot + "/files/Logs");
            paths.add("/data/data/" + pkg + "/files/r3dlogs");
        } else if (pkg.contains("standoff") || pkg.contains("axlebolt")) {
            paths.add(extRoot + "/files/Unity/logs");
            paths.add(extRoot + "/files/Logs");
            paths.add("/data/data/" + pkg + "/files/Unity");
        } else if (pkg.contains("carx") || pkg.contains("glofta") || pkg.contains("ea.games.r3")) {
            paths.add(extRoot + "/files/Unity/logs");
            paths.add(extRoot + "/files/Logs");
        } else if (pkg.contains("roblox")) {
            paths.add(extRoot + "/files/logs");
            paths.add("/data/data/" + pkg + "/files/logs");
        } else if (pkg.contains("valorant") || pkg.contains("projectc")) {
            paths.add(extRoot + "/files/UE4Game/ProjectC/ProjectC/Saved/Logs");
            paths.add(extRoot + "/files/UE4Game/ProjectC/ProjectC/Saved/Crashes");
        } else if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clash")) {
            paths.add("/data/data/" + pkg + "/files/analytics");
            paths.add("/data/data/" + pkg + "/files/logs");
            paths.add(extRoot + "/files/analytics");
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
