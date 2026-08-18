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

        // 1. PUBGM / BGMI / Free Fire / Battle Royale
        if (pkg.contains("tencent.ig") || pkg.contains("pubg") || pkg.contains("imobile") || 
            pkg.contains("krmobile") || pkg.contains("vng.pubgmobile") || pkg.contains("freefire") || 
            pkg.contains("arenabreakout") || pkg.contains("farlight84")) {
            return SpoofProfileRegistry.getById("samsung_s26_ultra");
        }

        // 2. CODM / Warzone / Blood Strike / Tactical FPS
        if (pkg.contains("callofduty") || pkg.contains("codm") || pkg.contains("bloodstrike") || 
            pkg.contains("standoff2") || pkg.contains("deltaforce")) {
            return SpoofProfileRegistry.getById("samsung_s26_ultra");
        }

        // 3. MLBB / HOK / Arena of Valor / Wild Rift / Roblox
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends") || pkg.contains("sgame") || 
            pkg.contains("wildrift") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || 
            pkg.contains("kgvn") || pkg.contains("kgid") || pkg.contains("roblox")) {
            return SpoofProfileRegistry.getById("samsung_s26_ultra");
        }

        // 4. Genshin Impact / Honkai: Star Rail / Zenless Zone Zero / Wuthering Waves
        if (pkg.contains("genshin") || pkg.contains("hkrpg") || pkg.contains("honkai") || 
            pkg.contains("cognosphere") || pkg.contains("mihoyo") || pkg.contains("hoyoverse") || 
            pkg.contains("nap") || pkg.contains("wutheringwaves")) {
            return SpoofProfileRegistry.getById("xiaomi_15_ultra");
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

        try {
            boolean success = HardwareMaskEngine.applyFullHardwareMask(context, profile, packageName);
            if (success) {
                activeProfileId = profile.id;
                if (context != null) {
                    SpoofPreferences.setSpoofEnabled(context, true);
                    SpoofPreferences.setActiveProfileId(context, profile.id);
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
        if (profile == null) return;
        try {
            for (String pkg : com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet()) {
                HardwareMaskEngine.injectTailoredGameHardwareConfigs(pkg, profile);
            }
        } catch (Throwable t) {
            Log.w(TAG, "injectAllInstalledGamesHardwareProfile error: " + t.getMessage());
        }
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
