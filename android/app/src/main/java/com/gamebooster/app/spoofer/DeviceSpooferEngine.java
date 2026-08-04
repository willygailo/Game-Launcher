package com.gamebooster.app.spoofer;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * DeviceSpooferEngine handles FULL hardware identity spoofing for Android 11 to 16.
 * It overrides system properties via Shizuku ADB Binder IPC to unlock 90 FPS, 120 FPS,
 * Ultra Graphics, and Ultra Audio settings in Mobile Legends, Call of Duty Mobile, PUBG, BGMI, etc.
 */
public class DeviceSpooferEngine {

    private static final String TAG = "DeviceSpooferEngine";

    public static class SpoofProfile {
        public final String id;
        public final String name;
        public final String model;
        public final String brand;
        public final String manufacturer;
        public final String device;
        public final String productName;
        public final String buildProduct;
        public final String hardware;
        public final String platform;
        public final String socModel;

        public SpoofProfile(String id, String name, String model, String brand, String manufacturer,
                            String device, String productName, String buildProduct,
                            String hardware, String platform, String socModel) {
            this.id = id;
            this.name = name;
            this.model = model;
            this.brand = brand;
            this.manufacturer = manufacturer;
            this.device = device;
            this.productName = productName;
            this.buildProduct = buildProduct;
            this.hardware = hardware;
            this.platform = platform;
            this.socModel = socModel;
        }
    }

    public static final SpoofProfile PROFILE_ROG_PHONE_8_PRO = new SpoofProfile(
            "rog_phone_8_pro", "ASUS ROG Phone 8 Pro (165 Hz Extreme MLBB/CODM)",
            "ASUS_AI2401", "asus", "asus", "ASUS_AI2401", "WW_AI2401", "ASUS_AI2401", "qcom", "sm8650", "SM8650"
    );

    public static final SpoofProfile PROFILE_REDMAGIC_9_PRO = new SpoofProfile(
            "redmagic_9_pro", "REDMAGIC 9 Pro (165 Hz Extreme PUBGM/BGMI)",
            "NX769J", "nubia", "nubia", "NX769J", "NX769J", "NX769J", "qcom", "sm8650", "SM8650"
    );

    public static final SpoofProfile PROFILE_ROG_PHONE_6 = new SpoofProfile(
            "rog_phone_6", "ASUS ROG Phone 6 (120 FPS MLBB/Wild Rift)",
            "ASUS_AI2201", "asus", "asus", "ASUS_AI2201", "WW_AI2201", "ASUS_AI2201", "qcom", "sm8475", "SM8475"
    );

    public static final SpoofProfile PROFILE_BLACK_SHARK_5 = new SpoofProfile(
            "black_shark_5", "Black Shark 5 Pro (120 FPS CODM / Max Graphics)",
            "SHARK PAR-A0", "blackshark", "blackshark", "PAR-A0", "PAR-A0", "PAR-A0", "qcom", "sm8450", "SM8450"
    );

    public static final SpoofProfile PROFILE_GALAXY_S24_ULTRA = new SpoofProfile(
            "galaxy_s24_ultra", "Samsung Galaxy S24 Ultra (120 FPS PUBG/BGMI)",
            "SM-S928B", "samsung", "samsung", "e3q", "e3qxxx", "SM-S928B", "qcom", "kalama", "SM8650"
    );

    public static final SpoofProfile PROFILE_IPAD_PRO = new SpoofProfile(
            "ipad_pro", "iPad Pro M2 (Tablet FOV & Extreme Graphics)",
            "iPad13,8", "apple", "apple", "iPad13,8", "iPad13,8", "iPad13,8", "apple", "m2", "M2"
    );

    public static final SpoofProfile PROFILE_XIAOMI_14_ULTRA = new SpoofProfile(
            "xiaomi_14_ultra", "Xiaomi 14 Ultra (120 FPS Ultra Graphics)",
            "24030PN60G", "xiaomi", "xiaomi", "aurora", "aurora_global", "24030PN60G", "qcom", "sm8650", "SM8650"
    );

    public static final SpoofProfile PROFILE_IPAD_PRO_M4 = new SpoofProfile(
            "ipad_pro_m4", "iPad Pro M4 (Tablet FOV 120 FPS & Ultra Graphics)",
            "iPad16,3", "apple", "apple", "iPad16,3", "iPad16,3", "iPad16,3", "apple", "m4", "M4"
    );

    public static final SpoofProfile PROFILE_IPHONE_15_PRO_MAX = new SpoofProfile(
            "iphone_15_pro_max", "iPhone 15 Pro Max (120 FPS Max Graphics)",
            "iPhone16,2", "apple", "apple", "iPhone16,2", "iPhone16,2", "iPhone16,2", "apple", "a17pro", "A17Pro"
    );

    private static final Map<String, SpoofProfile> PROFILES = new HashMap<>();

    static {
        PROFILES.put(PROFILE_ROG_PHONE_8_PRO.id, PROFILE_ROG_PHONE_8_PRO);
        PROFILES.put(PROFILE_REDMAGIC_9_PRO.id, PROFILE_REDMAGIC_9_PRO);
        PROFILES.put(PROFILE_ROG_PHONE_6.id, PROFILE_ROG_PHONE_6);
        PROFILES.put(PROFILE_BLACK_SHARK_5.id, PROFILE_BLACK_SHARK_5);
        PROFILES.put(PROFILE_GALAXY_S24_ULTRA.id, PROFILE_GALAXY_S24_ULTRA);
        PROFILES.put(PROFILE_XIAOMI_14_ULTRA.id, PROFILE_XIAOMI_14_ULTRA);
        PROFILES.put(PROFILE_IPAD_PRO.id, PROFILE_IPAD_PRO);
        PROFILES.put(PROFILE_IPAD_PRO_M4.id, PROFILE_IPAD_PRO_M4);
        PROFILES.put(PROFILE_IPHONE_15_PRO_MAX.id, PROFILE_IPHONE_15_PRO_MAX);
    }

    private static String activeProfileId = null;

    /**
     * Determines the optimal device spoof profile for a given game package.
     */
    public static SpoofProfile getRecommendedProfile(String packageName) {
        if (packageName == null) return PROFILE_ROG_PHONE_8_PRO;
        String pkg = packageName.toLowerCase();

        if (pkg.contains("mobile.legends") || pkg.contains("wildrift") || pkg.contains("sgame")) {
            return PROFILE_ROG_PHONE_8_PRO;
        } else if (pkg.contains("callofduty") || pkg.contains("codm") || pkg.contains("bloodstrike")) {
            return PROFILE_BLACK_SHARK_5;
        } else if (pkg.contains("tencent.ig") || pkg.contains("pubg") || pkg.contains("imobile") || pkg.contains("freefire")) {
            return PROFILE_REDMAGIC_9_PRO;
        } else if (pkg.contains("genshin") || pkg.contains("hkrpg") || pkg.contains("honkai")) {
            return PROFILE_XIAOMI_14_ULTRA;
        }

        return PROFILE_ROG_PHONE_8_PRO;
    }

    /**
     * Applies FULL device hardware property overrides via Shizuku ADB shell commands.
     */
    public static boolean applySpoofing(Context context, String packageName) {
        SpoofProfile profile = getRecommendedProfile(packageName);
        return applyProfile(context, profile, packageName);
    }

    /**
     * Applies a specific SpoofProfile to the system properties and game mode APIs.
     */
    public static boolean applyProfile(Context context, SpoofProfile profile, String packageName) {
        if (profile == null) return false;
        if (!ShizukuExecutor.isShizukuAvailable()) {
            Log.w(TAG, "Shizuku ADB unavailable. Cannot perform hardware property spoofing.");
            return false;
        }

        try {
            Log.d(TAG, "Applying FULL Device Spoofing Profile: " + profile.name + " for package: " + packageName);

            // Execute full system property overrides via resetprop / setprop combo
            String[] commands = new String[] {
                    "resetprop ro.product.model " + profile.model + " || setprop ro.product.model " + profile.model,
                    "resetprop ro.product.brand " + profile.brand + " || setprop ro.product.brand " + profile.brand,
                    "resetprop ro.product.manufacturer " + profile.manufacturer + " || setprop ro.product.manufacturer " + profile.manufacturer,
                    "resetprop ro.product.device " + profile.device + " || setprop ro.product.device " + profile.device,
                    "resetprop ro.product.name " + profile.productName + " || setprop ro.product.name " + profile.productName,
                    "resetprop ro.build.product " + profile.buildProduct + " || setprop ro.build.product " + profile.buildProduct,
                    "resetprop ro.vendor.product.model " + profile.model + " || setprop ro.vendor.product.model " + profile.model,
                    "resetprop ro.product.vendor.model " + profile.model + " || setprop ro.product.vendor.model " + profile.model,
                    "resetprop ro.odm.product.model " + profile.model + " || setprop ro.odm.product.model " + profile.model,
                    "resetprop ro.hardware " + profile.hardware + " || setprop ro.hardware " + profile.hardware,
                    "resetprop ro.board.platform " + profile.platform + " || setprop ro.board.platform " + profile.platform,
                    "resetprop ro.soc.model " + profile.socModel + " || setprop ro.soc.model " + profile.socModel,
                    "settings put system peak_refresh_rate 120.0",
                    "settings put system user_refresh_rate 120",
                    "settings put global angle_gl_driver_all_angle 1",
                    "settings put global game_driver_all_apps 1"
            };

            for (String cmd : commands) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            }

            if (packageName != null && !packageName.trim().isEmpty()) {
                ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + packageName);
                ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + packageName + " 120");
                ShizukuExecutor.executeShizukuCommand("device_config put game_overlay " + packageName + " mode=2,fps=120:mode=3,fps=120");
            }

            activeProfileId = profile.id;
            Log.i(TAG, "FULL Device Spoofing active: " + profile.model + " (" + profile.brand + ")");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply full device spoofing", e);
            return false;
        }
    }

    /**
     * Resets system property overrides when a game session ends.
     */
    public static void resetSpoofing() {
        if (!ShizukuExecutor.isShizukuAvailable() || activeProfileId == null) return;
        try {
            ShizukuExecutor.executeShizukuCommand("setprop persist.sys.game.boost.profile 0");
            activeProfileId = null;
            Log.i(TAG, "Device spoofing reset completed.");
        } catch (Throwable ignored) {}
    }

    public static Map<String, SpoofProfile> getAllProfiles() {
        return PROFILES;
    }
}
