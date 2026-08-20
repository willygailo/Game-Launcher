package com.gamebooster.app.anticheat;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.AntiLogPatcher;
import com.gamebooster.app.config.GameConfigPathResolver;
import com.gamebooster.app.device.DeviceDetector;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import com.gamebooster.app.spoofer.GameSpoofSafetyRegistry;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofSanityChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GameAntiCheatBypassEngine — Automated Anti-Cheat Detection, Log Nullification &
 * Stealth Bypass Verification Engine for all major esports titles:
 *
 *   - PUBG Mobile / BGMI / New State (Tencent ACE / MTP / TP3)
 *   - Mobile Legends: Bang Bang (Moonton Anti-Hack / Unity Guard)
 *   - Call of Duty: Mobile / Warzone (Activision RICOCHET / Tencent ACE)
 *   - Honor of Kings / Arena of Valor (Tencent ACE / TiMi Security)
 *   - Genshin Impact / Star Rail / ZZZ / Wuthering Waves (HoYoProtect / mhyprot)
 *   - Free Fire / Free Fire MAX (Garena Hack Protection / CRC Monitor)
 *   - Blood Strike (NetEase NetProtect / NeoX Anti-Cheat)
 *   - Wild Rift (Riot Integrity Service)
 *   - Standoff 2 (Axlebolt Anti-Cheat)
 *   - Arena Breakout / Delta Force (Tencent ACE / MoreFun Security)
 *   - CarX Street / Asphalt / Speed Drifters (Unity CRC / Gameloft Security)
 *   - Roblox (Hyperion / Byfron Mobile Guard)
 *   - Valorant Mobile / Project C (Tencent ACE / Riot Mobile)
 *   - Farlight 84 (Farlight Anti-Cheat / UE4 Guard)
 *   - Supercell (FairPlay Integrity)
 *
 * Prevents anti-cheat flags, account bans, or config detection by:
 *   1. Auto-purging and freezing crash report dumps and telemetry folders.
 *   2. Injecting anti-log & anti-telemetry stealth flags directly into game configs.
 *   3. Enforcing safe file permissions (chmod 664 / 660) to eliminate permission anomalies.
 *   4. Validating hardware spoof sanity against kernel anti-cheat detection thresholds.
 *   5. Flushing system logcat buffers and muting background logging friction.
 */
public final class GameAntiCheatBypassEngine {

    private static final String TAG = "AntiCheatBypassEngine";

    public enum AntiCheatType {
        TENCENT_ACE("Tencent ACE / MTP / TP3 (Kernel-level)"),
        MOONTON_GUARD("Moonton Anti-Hack / Unity Security"),
        ACTIVISION_RICOCHET("Activision Security / RICOCHET Mobile"),
        GARENA_PROTECT("Garena Hack Protection & CRC Monitor"),
        HOYO_PROTECT("HoYoProtect / mhyprot Integrity"),
        NETEASE_NETPROTECT("NetEase NetProtect / NeoX Anti-Cheat"),
        RIOT_INTEGRITY("Riot Mobile Integrity Service"),
        AXLEBOLT_AC("Axlebolt Anti-Cheat"),
        HYPERION_BYFRON("Hyperion / Byfron Mobile Guard"),
        SUPERCELL_FAIRPLAY("Supercell FairPlay Integrity"),
        UNITY_GENERIC("Unity Engine CRC / Anti-Tamper"),
        GENERIC_PROTECT("Standard Android Game Protection");

        public final String displayName;

        AntiCheatType(String displayName) {
            this.displayName = displayName;
        }
    }

    public static final class BypassResult {
        public final boolean success;
        public final AntiCheatType antiCheatType;
        public final String gamePackage;
        public final List<String> neutralizedPaths;
        public final String message;
        public final boolean hardwareSanityPassed;
        public final String sanityWarning;

        public BypassResult(boolean success, AntiCheatType antiCheatType, String gamePackage,
                            List<String> neutralizedPaths, String message,
                            boolean hardwareSanityPassed, String sanityWarning) {
            this.success = success;
            this.antiCheatType = antiCheatType;
            this.gamePackage = gamePackage;
            this.neutralizedPaths = neutralizedPaths;
            this.message = message;
            this.hardwareSanityPassed = hardwareSanityPassed;
            this.sanityWarning = sanityWarning;
        }

        @Override
        public String toString() {
            return "[" + antiCheatType.displayName + "] " + message +
                    (sanityWarning != null ? " (Warning: " + sanityWarning + ")" : "");
        }
    }

    private GameAntiCheatBypassEngine() {}

    /**
     * Identifies the anti-cheat security model used by the given target package.
     */
    public static AntiCheatType detectAntiCheat(String packageName) {
        if (packageName == null) return AntiCheatType.GENERIC_PROTECT;
        String pkg = packageName.toLowerCase().trim();

        // 1. Tencent ACE / TP3 / MTP
        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") ||
            pkg.contains("vng.pubgmobile") || pkg.contains("sgame") || pkg.contains("tmgp") ||
            pkg.contains("arenabreakout") || pkg.contains("uamo") || pkg.contains("deltaforce") ||
            pkg.contains("projectc") || pkg.contains("valorant")) {
            return AntiCheatType.TENCENT_ACE;
        }

        // 2. Moonton
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends") || pkg.contains("moonton")) {
            return AntiCheatType.MOONTON_GUARD;
        }

        // 3. Activision / Call of Duty
        if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("activision") || pkg.contains("warzone")) {
            return AntiCheatType.ACTIVISION_RICOCHET;
        }

        // 4. Garena
        if (pkg.contains("freefire") || pkg.contains("dts.freefire") || pkg.contains("garena")) {
            return AntiCheatType.GARENA_PROTECT;
        }

        // 5. HoYoverse / Kuro
        if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") ||
            pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") ||
            pkg.contains("wutheringwaves")) {
            return AntiCheatType.HOYO_PROTECT;
        }

        // 6. NetEase
        if (pkg.contains("bloodstrike") || pkg.contains("newspike") || pkg.contains("netease")) {
            return AntiCheatType.NETEASE_NETPROTECT;
        }

        // 7. Riot
        if (pkg.contains("wildrift") || pkg.contains("riotgames")) {
            return AntiCheatType.RIOT_INTEGRITY;
        }

        // 8. Standoff 2 (Axlebolt)
        if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
            return AntiCheatType.AXLEBOLT_AC;
        }

        // 9. Roblox (Hyperion)
        if (pkg.contains("roblox")) {
            return AntiCheatType.HYPERION_BYFRON;
        }

        // 10. Supercell
        if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans")) {
            return AntiCheatType.SUPERCELL_FAIRPLAY;
        }

        // 11. CarX / Asphalt / Racing
        if (pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row")) {
            return AntiCheatType.UNITY_GENERIC;
        }

        return AntiCheatType.GENERIC_PROTECT;
    }

    /**
     * Prepares, neutralizes, and executes full anti-cheat bypass check for target game.
     *
     * @param context Application context
     * @param packageName Target game package
     * @return BypassResult containing security status and actions taken
     */
    public static BypassResult applyBypassAndNeutralize(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new BypassResult(false, AntiCheatType.GENERIC_PROTECT, "", new ArrayList<>(),
                    "Invalid package name", false, null);
        }

        String pkg = packageName.toLowerCase().trim();
        AntiCheatType acType = detectAntiCheat(pkg);
        List<String> neutralized = new ArrayList<>();

        // 1. Step 1: Pre-Apply Hardware Sanity Check (if spoofing enabled)
        boolean sanityOk = true;
        String sanityWarning = null;
        if (context != null && SpoofPreferences.isSpoofEnabled(context)) {
            String profileId = SpoofPreferences.resolveProfileId(context, pkg);
            if (profileId != null && !profileId.trim().isEmpty()) {
                SpoofProfile profile = com.gamebooster.app.spoofer.DeviceSpooferEngine.getProfileById(profileId);
                if (profile != null) {
                    DeviceDetector.ChipsetVendor realVendor = DeviceDetector.detectChipsetVendor();
                    GameSpoofSafetyRegistry.RiskTier riskTier = GameSpoofSafetyRegistry.riskTierFor(pkg);
                    SpoofSanityChecker.SanityResult sanity = SpoofSanityChecker.checkForGame(realVendor, profile, riskTier);
                    sanityOk = sanity.allowed;
                    sanityWarning = sanity.warning;
                }
            }
        }

        // 2. Step 2: System-level Anti-Log & Logcat buffer reduction
        AntiLogPatcher.applySystemAntiLog();

        // 3. Step 3: Purge and freeze all game-specific telemetry and crash reporter folders
        List<String> logPaths = getAntiCheatLogPaths(pkg, acType);
        StringBuilder sb = new StringBuilder();

        for (String path : logPaths) {
            neutralized.add(path);
            sb.append("rm -rf '").append(path).append("/*' 2>/dev/null; ");
            sb.append("mkdir -p '").append(path).append("' 2>/dev/null; ");
            sb.append("touch '").append(path).append("/.nomedia' 2>/dev/null; ");
        }

        // Clear internal crash cache
        sb.append("rm -rf '/data/data/").append(pkg).append("/cache/*' 2>/dev/null; ");
        sb.append("rm -rf '/data/data/").append(pkg).append("/code_cache/*' 2>/dev/null; ");
        sb.append("rm -rf '/sdcard/Android/data/").append(pkg).append("/cache/*' 2>/dev/null; ");

        // 4. Step 4: Inject Anti-Cheat Stealth Flags into all configuration files
        List<String> cfgPaths = GameConfigPathResolver.getPathsForGame(pkg);
        for (String cfg : cfgPaths) {
            sb.append("if [ -f '").append(cfg).append("' ]; then ");
            // Inject anti-detection & telemetry suppression flags
            sb.append("grep -qF 'DisableLogging' '").append(cfg).append("' || echo 'DisableLogging=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF 'DisableTelemetry' '").append(cfg).append("' || echo 'DisableTelemetry=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF 'DisableCrashlytics' '").append(cfg).append("' || echo 'DisableCrashlytics=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF 'AntiLog' '").append(cfg).append("' || echo 'AntiLog=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF 'LogcatDisable' '").append(cfg).append("' || echo 'LogcatDisable=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF '+CVars=r.SuppressLogs' '").append(cfg).append("' || echo '+CVars=r.SuppressLogs=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF '+CVars=r.DisableDebugLog' '").append(cfg).append("' || echo '+CVars=r.DisableDebugLog=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF '+CVars=r.EnableCrashReporting' '").append(cfg).append("' || echo '+CVars=r.EnableCrashReporting=0' >> '").append(cfg).append("'; ");
            sb.append("grep -qF '+CVars=r.Telemetry' '").append(cfg).append("' || echo '+CVars=r.Telemetry=0' >> '").append(cfg).append("'; ");
            sb.append("grep -qF '+CVars=a.DisableAnalytics' '").append(cfg).append("' || echo '+CVars=a.DisableAnalytics=1' >> '").append(cfg).append("'; ");
            sb.append("grep -qF '+CVars=r.LogFilter' '").append(cfg).append("' || echo '+CVars=r.LogFilter=0' >> '").append(cfg).append("'; ");
            sb.append("grep -qF 'bDisableAnalytics' '").append(cfg).append("' || echo 'bDisableAnalytics=True' >> '").append(cfg).append("'; ");
            sb.append("grep -qF 'bDisableBugReporting' '").append(cfg).append("' || echo 'bDisableBugReporting=True' >> '").append(cfg).append("'; ");

            // 5. Step 5: Normalize file permissions to standard app permissions (chmod 664)
            // to avoid anti-cheat permission anomaly alarms (e.g. flagging chmod 777)
            sb.append("chmod 664 '").append(cfg).append("' 2>/dev/null; ");
            sb.append("fi; ");
        }

        executePrivileged(sb.toString());

        Log.i(TAG, "Anti-Cheat auto-bypass and stealth protection enforced for " + pkg + " [" + acType.displayName + "]");
        return new BypassResult(true, acType, pkg, neutralized,
                "Auto-bypass active: telemetry muted, crash dumps neutralized, stealth permissions enforced",
                sanityOk, sanityWarning);
    }

    /**
     * Resolves game-specific telemetry, log, and crash reporting directories.
     */
    public static List<String> getAntiCheatLogPaths(String pkg, AntiCheatType acType) {
        List<String> paths = new ArrayList<>();

        // Universal Android app telemetry and log buffers
        paths.add("/sdcard/Android/data/" + pkg + "/files/Logs");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Saved/Logs");
        paths.add("/sdcard/Android/data/" + pkg + "/files/Saved/Crashes");
        paths.add("/data/data/" + pkg + "/files/Logs");
        paths.add("/data/data/" + pkg + "/files/tlog");
        paths.add("/data/data/" + pkg + "/files/apm_logs");
        paths.add("/data/data/" + pkg + "/files/crash_report");
        paths.add("/data/data/" + pkg + "/files/hawk_logs");
        paths.add("/data/data/" + pkg + "/files/bugly");
        paths.add("/data/data/" + pkg + "/files/tpns");

        // Game engine / Anti-cheat specific telemetry paths
        switch (acType) {
            case TENCENT_ACE:
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Crashes");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/StatEvents");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Pandora");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/PufferTmpDir");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/GVoice");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/LightData");
                paths.add("/sdcard/Android/data/" + pkg + "/files/Tencent/tlog");
                paths.add("/sdcard/Android/data/" + pkg + "/files/Tencent/logs");
                paths.add("/sdcard/Android/data/" + pkg + "/files/Tencent/crash");
                paths.add("/data/data/" + pkg + "/files/msdk");
                paths.add("/data/data/" + pkg + "/files/beacon");
                break;

            case MOONTON_GUARD:
                paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/log");
                paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/commlog");
                paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/assets_log");
                paths.add("/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/crashes");
                paths.add("/data/data/" + pkg + "/files/crash_log");
                break;

            case ACTIVISION_RICOCHET:
                paths.add("/sdcard/Android/data/" + pkg + "/files/callofduty/logs");
                paths.add("/sdcard/Android/data/" + pkg + "/files/callofduty/crashes");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs");
                paths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Crashes");
                break;

            case GARENA_PROTECT:
                paths.add("/sdcard/Android/data/" + pkg + "/files/report");
                paths.add("/sdcard/Android/data/" + pkg + "/files/Garena/FreeFire/log");
                paths.add("/sdcard/Android/data/" + pkg + "/files/Garena/FreeFire/crash");
                paths.add("/data/data/" + pkg + "/files/report");
                break;

            case HOYO_PROTECT:
                paths.add("/sdcard/Android/data/" + pkg + "/files/crashes");
                paths.add("/sdcard/Android/data/" + pkg + "/files/output_log.txt");
                paths.add("/sdcard/Android/data/" + pkg + "/files/sdk_log.txt");
                paths.add("/data/data/" + pkg + "/files/crash_report");
                break;

            case NETEASE_NETPROTECT:
                paths.add("/sdcard/Android/data/" + pkg + "/files/netease/logs");
                paths.add("/sdcard/Android/data/" + pkg + "/files/netease/crash");
                paths.add("/sdcard/Android/data/" + pkg + "/files/netease/report");
                break;

            case RIOT_INTEGRITY:
                paths.add("/sdcard/Android/data/" + pkg + "/files/RiotGames/Logs");
                paths.add("/sdcard/Android/data/" + pkg + "/files/RiotGames/Telemetry");
                break;

            case AXLEBOLT_AC:
                paths.add("/sdcard/Android/data/" + pkg + "/files/Logs");
                paths.add("/sdcard/Android/data/" + pkg + "/files/Crashes");
                break;

            default:
                break;
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
