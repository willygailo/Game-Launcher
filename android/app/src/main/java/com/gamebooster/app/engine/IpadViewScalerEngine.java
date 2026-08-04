package com.gamebooster.app.engine;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * IpadViewScalerEngine dynamically scales the display density (DPI) and aspect resolution
 * via Shizuku ADB shell commands to provide an iPad/Tablet FOV (Field of View) experience
 * in games like PUBG Mobile, CODM, Mobile Legends, Free Fire, and Blood Strike without modifying game binaries.
 */
public class IpadViewScalerEngine {

    private static final String TAG = "IpadViewScalerEngine";

    public enum IpadViewMode {
        DISABLED("Normal View", 1.0f),
        IPAD_MEDIUM("iPad View 1.5x (Balanced FOV)", 0.80f),
        IPAD_ULTRA("iPad View 2.0x (Extreme FOV)", 0.68f);

        public final String label;
        public final float densityMultiplier;

        IpadViewMode(String label, float densityMultiplier) {
            this.label = label;
            this.densityMultiplier = densityMultiplier;
        }
    }

    private static int originalDensity = -1;

    /**
     * Captures default display density if not already saved.
     */
    public static void init(Context context) {
        if (originalDensity <= 0 && context != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            originalDensity = metrics.densityDpi;
            Log.i(TAG, "Captured original screen density: " + originalDensity + " DPI");
        }
    }

    /**
     * Applies an iPad View scaling profile using Shizuku ADB commands.
     *
     * @param context App context
     * @param mode Target IpadViewMode (IPAD_MEDIUM or IPAD_ULTRA)
     * @return true if applied successfully via Shizuku
     */
    public static boolean applyIpadView(Context context, IpadViewMode mode) {
        init(context);

        if (!ShizukuExecutor.isShizukuAvailable()) {
            Log.w(TAG, "Shizuku ADB unavailable. Cannot apply iPad View scaling.");
            return false;
        }

        if (mode == IpadViewMode.DISABLED) {
            return restoreDefaultView();
        }

        try {
            int targetDensity = Math.round(originalDensity * mode.densityMultiplier);
            Log.d(TAG, "Applying " + mode.label + " -> Target DPI: " + targetDensity);

            String cmd = "wm density " + targetDensity;
            String result = ShizukuExecutor.executeShizukuCommand(cmd);
            boolean success = result != null && !result.startsWith("ERROR");

            if (success) {
                Log.i(TAG, "iPad View scaling applied: " + targetDensity + " DPI (" + mode.name() + ")");
            }
            return success;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply iPad View scaling", e);
            return false;
        }
    }

    /**
     * Restores default screen density when leaving game session.
     */
    public static boolean restoreDefaultView() {
        if (!ShizukuExecutor.isShizukuAvailable()) return false;
        try {
            Log.d(TAG, "Restoring default display density...");
            String cmd = (originalDensity > 0) ? ("wm density " + originalDensity) : "wm density reset";
            String result = ShizukuExecutor.executeShizukuCommand(cmd);
            boolean success = result != null && !result.startsWith("ERROR");
            if (success) {
                Log.i(TAG, "Display density successfully restored to default.");
            }
            return success;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to restore default display density", e);
            return false;
        }
    }

    /**
     * Checks manual preferences and applies iPad view scaling specifically when a supported game package (MLBB/PUBGM/CODM) is active.
     */
    public static boolean applyForGame(Context context, String packageName) {
        if (context == null || packageName == null) return restoreDefaultView();
        
        boolean globalEnabled = com.gamebooster.app.config.ManualSettingsPreferences.isIpadViewEnabled(context);
        if (!globalEnabled) {
            return restoreDefaultView();
        }

        String pkg = packageName.toLowerCase().trim();
        boolean isMlbb = pkg.contains("mobile.legends") || pkg.contains("mobilelegends");
        boolean isPubg = pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile");
        boolean isCodm = pkg.contains("cod") || pkg.contains("callofduty");

        if (isMlbb) {
            boolean enabled = com.gamebooster.app.config.ManualSettingsPreferences.isIpadViewGameEnabled(context, "mlbb");
            if (!enabled) return restoreDefaultView();
        } else if (isPubg) {
            boolean enabled = com.gamebooster.app.config.ManualSettingsPreferences.isIpadViewGameEnabled(context, "pubg");
            if (!enabled) return restoreDefaultView();
        } else if (isCodm) {
            boolean enabled = com.gamebooster.app.config.ManualSettingsPreferences.isIpadViewGameEnabled(context, "codm");
            if (!enabled) return restoreDefaultView();
        }

        String modeStr = com.gamebooster.app.config.ManualSettingsPreferences.getIpadViewMode(context);
        IpadViewMode mode = "IPAD_ULTRA".equalsIgnoreCase(modeStr) ? IpadViewMode.IPAD_ULTRA : IpadViewMode.IPAD_MEDIUM;
        return applyIpadView(context, mode);
    }

    public static boolean revertForAppExit(Context context) {
        return restoreDefaultView();
    }

    public static int getOriginalDensity() {
        return originalDensity;
    }
}
