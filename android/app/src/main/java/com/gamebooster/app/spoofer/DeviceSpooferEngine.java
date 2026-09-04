package com.gamebooster.app.spoofer;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.config.GameConfigPathResolver;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.List;
import java.util.Map;

/**
 * DeviceSpooferEngine — Real-World Working Device & Hardware Spoofing Engine.
 *
 * Implements a 6-layer Hardware Masking & Spoofing architecture via Shizuku (temporary root):
 *
 * 1. CPU / SoC Masking: Cores, Frequency, Architecture, Features, /proc/cpuinfo, SoC model/manufacturer.
 * 2. GPU / Graphics Masking: GL Renderer, GL Vendor, Vulkan Driver, ANGLE, HWUI backend.
 * 3. RAM / Memory Masking: Total/Available RAM MB, /proc/meminfo, Dalvik VM heap overrides.
 * 4. Model & OS Build Identity: Model, Brand, Manufacturer, Board, Fingerprint, Display ID, Release/SDK.
 * 5. In-App Java Reflection: Overrides static Build fields so app telemetry/diagnostics report masked device.
 * 6. Dynamic Game Hardware Profile Injection: Writes engine hardware profiles across dynamically resolved
 *    game paths using GameConfigPathResolver.
 */
public class DeviceSpooferEngine {

    private static final String TAG = "DeviceSpooferEngine";

    /** Currently active spoof profile ID, null if no spoof is applied. */
    private static String activeProfileId = null;

    /** Reason the last spoof apply was blocked by the pre-apply sanity check, null when allowed. */
    private static String lastSanityBlockReason = null;

    /** Warning attached to the last successful apply (e.g. GPU mismatch on a soft-AC game), null when clean. */
    private static String lastSanityWarning = null;

    public static String getLastSanityBlockReason() {
        return lastSanityBlockReason;
    }

    public static String getLastSanityWarning() {
        return lastSanityWarning;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Profile access (delegates to registry)
    // ─────────────────────────────────────────────────────────────────────────

    public static Map<String, SpoofProfile> getAllProfiles() {
        return SpoofProfileRegistry.getAllProfiles();
    }

    public static Map<String, List<SpoofProfile>> getAllByBrand() {
        return SpoofProfileRegistry.getAllByBrand();
    }

    public static List<String> getBrandNames() {
        return SpoofProfileRegistry.getBrandNames();
    }

    public static SpoofProfile getProfileById(String id) {
        return SpoofProfileRegistry.getById(id);
    }

    public static List<SpoofProfile> getProfilesByBrand(String brand) {
        return SpoofProfileRegistry.getByBrand(brand);
    }

    public static String getActiveProfileId() {
        return activeProfileId;
    }

    public static SpoofProfile getActiveProfile() {
        return activeProfileId != null ? getProfileById(activeProfileId) : null;
    }

    public static SpoofProfile getDefaultProfile() {
        SpoofProfile p = SpoofProfileRegistry.getById("samsung_s26_ultra");
        if (p == null) p = SpoofProfileRegistry.getById("asus_rog9_pro_edition");
        if (p == null) p = SpoofProfileRegistry.getById("blackshark_5_pro");
        if (p == null) {
            Map<String, SpoofProfile> all = SpoofProfileRegistry.getAllProfiles();
            if (!all.isEmpty()) return all.values().iterator().next();
        }
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Smart profile recommendation per game package
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the recommended spoof profile for a given game package name.
     * Picks the best-fit device to unlock the highest FPS/graphics tier:
     * - PUBGM / BGMI / Free Fire → REDMAGIC 10 Pro+ (165Hz eSports)
     * - CODM / Warzone / Blood Strike → Samsung Galaxy S26 Ultra (Snapdragon 8 Elite / Adreno 840)
     * - MLBB / HOK / Wild Rift / Roblox → ASUS ROG Phone 9 Pro (185Hz / 165Hz Extreme)
     * - Genshin / Honkai / ZZZ / Wuthering Waves → Xiaomi 15 Ultra (Vulkan Ultra)
     */
    public static SpoofProfile getRecommendedProfile(String packageName) {
        if (packageName == null) return SpoofProfileRegistry.getById("samsung_s26_ultra");
        String pkg = packageName.toLowerCase();

        // 1. PUBGM / BGMI / Free Fire / Battle Royale (eSports high refresh rate)
        if (pkg.contains("tencent.ig") || pkg.contains("pubg") || pkg.contains("imobile") || 
            pkg.contains("krmobile") || pkg.contains("vng.pubgmobile") || pkg.contains("freefire") || 
            pkg.contains("arenabreakout") || pkg.contains("farlight84")) {
            SpoofProfile p = SpoofProfileRegistry.getById("redmagic_10_pro_plus");
            return p != null ? p : SpoofProfileRegistry.getById("samsung_s26_ultra");
        }

        // 2. CODM / Warzone / Blood Strike / Tactical FPS (Snapdragon 8 Elite / Adreno 840)
        if (pkg.contains("callofduty") || pkg.contains("codm") || pkg.contains("bloodstrike") || 
            pkg.contains("standoff2") || pkg.contains("deltaforce")) {
            return SpoofProfileRegistry.getById("samsung_s26_ultra");
        }

        // 3. MLBB / HOK / Arena of Valor / Wild Rift / Roblox (185Hz / Ultra-low Touch Latency)
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends") || pkg.contains("sgame") || 
            pkg.contains("wildrift") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || 
            pkg.contains("kgvn") || pkg.contains("kgid") || pkg.contains("roblox")) {
            SpoofProfile p = SpoofProfileRegistry.getById("asus_rog9_pro_edition");
            return p != null ? p : SpoofProfileRegistry.getById("samsung_s26_ultra");
        }

        // 4. Genshin Impact / Honkai: Star Rail / Zenless Zone Zero / Wuthering Waves (Vulkan Ultra Tier 5)
        if (pkg.contains("genshin") || pkg.contains("hkrpg") || pkg.contains("honkai") || 
            pkg.contains("cognosphere") || pkg.contains("mihoyo") || pkg.contains("hoyoverse") || 
            pkg.contains("nap") || pkg.contains("wutheringwaves")) {
            SpoofProfile p = SpoofProfileRegistry.getById("xiaomi_15_ultra");
            return p != null ? p : SpoofProfileRegistry.getById("samsung_s26_ultra");
        }

        // Default Flagship Profile
        return SpoofProfileRegistry.getById("samsung_s26_ultra");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Apply
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Applies the active user-selected spoof profile for the given game package ONLY IF enabled by user.
     * Respects user permission: if user did not explicitly turn on Device Spoofing and select a profile,
     * this will NEVER automatically set or force any spoof profile.
     */
    public static boolean applySpoofing(Context context, String packageName) {
        if (context == null) return false;
        boolean enabled = SpoofPreferences.isSpoofEnabled(context);
        if (!enabled) {
            // User did not enable spoofing -> DO NOT AUTO SET
            return false;
        }
        // Check per-package override first, fall back to global active profile
        String activeId = SpoofPreferences.resolveProfileId(context, packageName);
        if (activeId == null || activeId.trim().isEmpty()) {
            // No profile selected for this package or globally -> DO NOT AUTO SET
            return false;
        }
        SpoofProfile profile = getProfileById(activeId);
        if (profile == null) {
            return false;
        }
        return applyProfile(context, profile, packageName);
    }

    /**
     * Applies a specific SpoofProfile across all real-world layers via Shizuku / Root:
     * Delegates to HardwareMaskEngine for full 6-layer hardware masking.
     */
    public static boolean applyProfile(Context context, SpoofProfile profile, String packageName) {
        if (profile == null) {
            Log.e(TAG, "Cannot apply null spoof profile.");
            return false;
        }

        // Phase 2.5: per-game aware pre-apply sanity check. The strict block is
        // reserved for kernel-anti-cheat titles (Tencent ACE etc.); soft-AC and
        // AC-free games apply with a warning so the spoof works on any device.
        try {
            com.gamebooster.app.device.DeviceDetector.ChipsetVendor deviceChipset =
                    com.gamebooster.app.device.DeviceDetector.detectChipsetVendor();
            GameSpoofSafetyRegistry.RiskTier riskTier =
                    GameSpoofSafetyRegistry.riskTierFor(packageName);
            SpoofSanityChecker.SanityResult sanity =
                    SpoofSanityChecker.checkForGame(deviceChipset, profile, riskTier);
            if (!sanity.allowed) {
                lastSanityBlockReason = sanity.reason;
                lastSanityWarning = null;
                Log.w(TAG, "Spoof BLOCKED for " + profile.id + " (" + riskTier + "): " + sanity.reason);
                return false;
            }
            lastSanityBlockReason = null;
            lastSanityWarning = sanity.warning;
            Log.i(TAG, "Pre-apply sanity check passed: " + sanity.reason);
        } catch (Throwable t) {
            Log.w(TAG, "Sanity check non-fatal, proceeding: " + t.getMessage());
        }

        try {
            boolean success = HardwareMaskEngine.applyFullHardwareMask(context, profile, packageName);
            if (success) {
                activeProfileId = profile.id;
                if (context != null) {
                    SpoofPreferences.setSpoofEnabled(context, true);
                    SpoofPreferences.setActiveProfileId(context, profile.id);
                }
                // Post-apply read-back: log how many spoof layers are live in-app
                try {
                    SpoofValidator.SpoofValidationResult validation =
                            SpoofValidator.validate(context, profile);
                    Log.i(TAG, "Post-apply validation: " + validation);
                } catch (Throwable t) {
                    Log.w(TAG, "Post-apply validation non-fatal: " + t.getMessage());
                }
            }
            return success;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply full real-world device spoofing: " + profile.id, e);
            return false;
        }
    }

    /**
     * Safely updates in-memory android.os.Build fields via reflection.
     */
    public static void applyInAppBuildSpoof(SpoofProfile profile) {
        HardwareMaskEngine.applyInAppReflectionMask(profile);
    }

    /**
     * Exports fake /proc/cpuinfo, /proc/meminfo, and property override files to disk.
     */
    public static void exportProcMockFiles(SpoofProfile profile) {
        HardwareMaskEngine.exportMockProcfsPayloads(profile);
    }

    /**
     * Injects hardware profile into all known games installed or registered on the system.
     */
    public static void injectAllInstalledGamesHardwareProfile(SpoofProfile profile) {
        HardwareMaskEngine.injectAllInstalledGamesHardwareProfile(profile);
    }

    /**
     * Applies the active spoof profile across ALL registered games dynamically.
     */
    public static boolean applyActiveProfileToAllGames(Context context) {
        if (context == null) return false;
        boolean enabled = SpoofPreferences.isSpoofEnabled(context);
        if (!enabled) return false;
        String activeId = SpoofPreferences.getActiveProfileId(context);
        if (activeId == null || activeId.trim().isEmpty()) return false;
        SpoofProfile profile = getProfileById(activeId);
        if (profile == null) return false;

        boolean anySuccess = false;
        for (String pkg : com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet()) {
            boolean ok = applyProfile(context, profile, pkg);
            if (ok) anySuccess = true;
        }
        return anySuccess;
    }

    /**
     * Forcefully applies a specific spoof profile ID to a target package.
     */
    public static boolean forceApplySpoof(Context context, String profileId, String packageName) {
        if (profileId == null || profileId.trim().isEmpty()) return false;
        SpoofProfile profile = getProfileById(profileId.trim());
        if (profile == null) return false;
        if (context != null) {
            SpoofPreferences.setSpoofEnabled(context, true);
            if (packageName != null && !packageName.trim().isEmpty()) {
                SpoofPreferences.setProfileIdForPackage(context, packageName.trim(), profile.id);
            } else {
                SpoofPreferences.setActiveProfileId(context, profile.id);
            }
        }
        return applyProfile(context, profile, packageName);
    }

    /**
     * Applies a specific spoof profile ID across installed games and target titles
     * without altering global Android OS identity or SafetyNet/Play Integrity.
     */
    public static int forceApplyGlobalSpoof(Context context, String profileId) {
        if (profileId == null || profileId.trim().isEmpty()) return 0;
        SpoofProfile profile = getProfileById(profileId.trim());
        if (profile == null) return 0;
        if (context != null) {
            SpoofPreferences.setSpoofEnabled(context, true);
            SpoofPreferences.setActiveProfileId(context, profile.id);
            SpoofPreferences.setSpoofAllApps(context, true);
        }
        return HardwareMaskEngine.maskAllInstalledApplications(context, profile);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Reset
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resets the active game boost profile and restores hardware masking properties.
     */
    public static void resetSpoofing() {
        try {
            HardwareMaskEngine.resetHardwareMask();
            activeProfileId = null;
            Log.i(TAG, "Device spoofing reset completed.");
        } catch (Throwable ignored) {}
    }
}
