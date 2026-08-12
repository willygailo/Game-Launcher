package com.gamebooster.app.feature.performance.display;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;

/**
 * IpadViewScalerEngine is deprecated and disabled.
 * Screen DPI density scaling has been permanently removed to prevent screen distortion and UI glitches.
 */
public class IpadViewScalerEngine {

    private static final String TAG = "IpadViewScalerEngine";

    public enum IpadViewMode {
        DISABLED("Normal View", 1.0f);

        public final String label;
        public final float densityMultiplier;

        IpadViewMode(String label, float densityMultiplier) {
            this.label = label;
            this.densityMultiplier = densityMultiplier;
        }
    }

    public static void init(Context context) {
        // Disabled
    }

    public static boolean applyIpadView(Context context, IpadViewMode mode) {
        return restoreDefaultView();
    }

    /**
     * Ensures display density is always reset to system default.
     */
    public static boolean restoreDefaultView() {
        if (!ShizukuExecutor.isShizukuAvailable()) return false;
        try {
            Log.d(TAG, "Ensuring default display density (wm density reset)...");
            String result = ShizukuExecutor.executeShizukuCommand("wm density reset");
            return result != null && !result.startsWith("ERROR");
        } catch (Throwable e) {
            Log.e(TAG, "Failed to reset display density", e);
            return false;
        }
    }

    public static boolean applyForGame(Context context, String packageName) {
        return restoreDefaultView();
    }

    public static boolean revertForAppExit(Context context) {
        return restoreDefaultView();
    }
}
