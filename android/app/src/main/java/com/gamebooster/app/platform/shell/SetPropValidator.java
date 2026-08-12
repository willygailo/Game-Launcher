package com.gamebooster.app.platform.shell;

import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * SetPropValidator — Validates Android system property keys against Android API level support ranges.
 *
 * <p>Prevents executing properties that are not supported on the current Android version,
 * which would silently fail or cause unexpected behavior on Android 13–16 devices.
 *
 * <p>Categories validated:
 * <ul>
 *   <li>SurfaceFlinger properties (Android 12-16 differences)</li>
 *   <li>GPU / HWUI rendering properties</li>
 *   <li>Touch input properties</li>
 *   <li>Game Mode API (Android 12+)</li>
 *   <li>Persist vs debug namespace rules</li>
 * </ul>
 */
public class SetPropValidator {

    private static final String TAG = "SetPropValidator";

    // -----------------------------------------------------------------------------------------
    // Validation Result
    // -----------------------------------------------------------------------------------------

    public static class ValidationResult {
        /** Whether the property is supported on the current device's API level */
        public final boolean supported;
        /** The property key that was validated */
        public final String key;
        /** Min API level required (-1 = any) */
        public final int minApi;
        /** Max API level supported (-1 = no upper limit) */
        public final int maxApi;
        /** Human-readable note about this property's compatibility */
        public final String note;

        ValidationResult(boolean supported, String key, int minApi, int maxApi, String note) {
            this.supported = supported;
            this.key = key;
            this.minApi = minApi;
            this.maxApi = maxApi;
            this.note = note;
        }
    }

    // -----------------------------------------------------------------------------------------
    // Property Registry
    // -----------------------------------------------------------------------------------------

    /** Inner class describing a property's API support range */
    private static class PropInfo {
        final int minApi;
        final int maxApi; // -1 = no upper limit
        final String note;

        PropInfo(int minApi, int maxApi, String note) {
            this.minApi = minApi;
            this.maxApi = maxApi;
            this.note = note;
        }

        PropInfo(int minApi, String note) {
            this(minApi, -1, note);
        }
    }

    /** Static registry mapping property prefix/name → supported API range */
    private static final Map<String, PropInfo> PROP_REGISTRY = new HashMap<>();

    static {
        // SurfaceFlinger frame rate control
        PROP_REGISTRY.put("debug.sf.fps_limit",
                new PropInfo(21, 35, "SurfaceFlinger FPS limit; replaced by Game Mode API on API 36+"));
        PROP_REGISTRY.put("debug.sf.hw",
                new PropInfo(21, "SurfaceFlinger hardware overlay flag"));
        PROP_REGISTRY.put("debug.sf.latch_unsignaled",
                new PropInfo(21, "SurfaceFlinger buffer latch tuning"));
        PROP_REGISTRY.put("debug.sf.disable_backpressure",
                new PropInfo(21, "SurfaceFlinger backpressure control"));
        PROP_REGISTRY.put("debug.sf.early_app_phase_offset_ns",
                new PropInfo(29, "SurfaceFlinger early phase offset (Android 10+)"));
        PROP_REGISTRY.put("debug.sf.early_phase_offset_ns",
                new PropInfo(29, "SurfaceFlinger early phase offset (Android 10+)"));

        // GPU / HWUI rendering
        PROP_REGISTRY.put("debug.hwui.renderer",
                new PropInfo(21, "HWUI renderer backend (vulkan/opengl/skiagl/skiavk)"));
        PROP_REGISTRY.put("debug.renderengine.backend",
                new PropInfo(29, "RenderEngine backend (Android 10+)"));
        PROP_REGISTRY.put("debug.egl.hw",
                new PropInfo(21, 29, "EGL hardware acceleration flag (deprecated API 30+)"));
        PROP_REGISTRY.put("debug.angle.backend",
                new PropInfo(30, "ANGLE graphics backend (Android 11+)"));
        PROP_REGISTRY.put("persist.hwui.renderer",
                new PropInfo(21, "Persistent HWUI renderer backend"));
        PROP_REGISTRY.put("persist.debug.sf.hw",
                new PropInfo(21, "Persistent SurfaceFlinger HW flag"));

        // Persist FPS / performance
        PROP_REGISTRY.put("persist.sys.NV_FPSLIMIT",
                new PropInfo(21, "Persistent NVIDIA/Qualcomm FPS cap"));
        PROP_REGISTRY.put("persist.sys.NV_POWERMODE",
                new PropInfo(21, "NVIDIA performance power mode"));
        PROP_REGISTRY.put("persist.sys.gamemode.fps",
                new PropInfo(31, "Game Mode FPS target (Android 12+)"));
        PROP_REGISTRY.put("persist.sys.perf.topAppRenderThreadBoost.enable",
                new PropInfo(28, "Top-app render thread boost (Android 9+)"));
        PROP_REGISTRY.put("persist.sys.bg_apps_limit",
                new PropInfo(21, "Background app process limit"));

        // Touch / Input
        PROP_REGISTRY.put("debug.input.max_events_per_sec",
                new PropInfo(21, "Input event rate cap — touch sampling"));
        PROP_REGISTRY.put("view.touch_slop",
                new PropInfo(21, "Touch slop deadzone (pixels)"));
        PROP_REGISTRY.put("persist.sys.touch.response_time",
                new PropInfo(21, "OEM touch response time override"));
        PROP_REGISTRY.put("persist.sys.touch.sensitivity",
                new PropInfo(21, "OEM touch sensitivity override"));
        PROP_REGISTRY.put("persist.sys.touch_prediction",
                new PropInfo(21, "Touch prediction engine"));
        PROP_REGISTRY.put("persist.vendor.qti.input.touch_boost",
                new PropInfo(21, "Qualcomm touch boost (QTI-specific)"));

        // Thermal
        PROP_REGISTRY.put("persist.vendor.thermal.enable",
                new PropInfo(21, "Vendor thermal service enable — OEM-specific, avoid on Samsung/MediaTek"));

        // Game Mode API
        PROP_REGISTRY.put("debug.game_mode",
                new PropInfo(31, "Android 12+ Game Mode debug flag"));
    }

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------

    /**
     * Validates a setprop key against the current device's Android API level.
     *
     * @param propKey The full property key (e.g., "debug.sf.fps_limit")
     * @return ValidationResult with supported flag and details
     */
    public static ValidationResult validate(String propKey) {
        if (propKey == null || propKey.isEmpty()) {
            return new ValidationResult(false, propKey, -1, -1, "Null or empty prop key");
        }

        int currentApi = Build.VERSION.SDK_INT;

        // Check exact key match first
        PropInfo info = PROP_REGISTRY.get(propKey);

        // Check prefix match if exact not found (e.g., vendor-specific variants)
        if (info == null) {
            for (Map.Entry<String, PropInfo> entry : PROP_REGISTRY.entrySet()) {
                if (propKey.startsWith(entry.getKey())) {
                    info = entry.getValue();
                    break;
                }
            }
        }

        if (info == null) {
            // Unknown prop — allow by default but log a debug warning
            Log.d(TAG, "Unknown prop key '" + propKey + "' — allowing (not in registry)");
            return new ValidationResult(true, propKey, -1, -1, "Unknown prop — allowed by default");
        }

        boolean minOk = (info.minApi == -1 || currentApi >= info.minApi);
        boolean maxOk = (info.maxApi == -1 || currentApi <= info.maxApi);
        boolean supported = minOk && maxOk;

        if (!supported) {
            Log.w(TAG, "SKIPPING prop '" + propKey + "': requires API "
                    + info.minApi + (info.maxApi > 0 ? "–" + info.maxApi : "+")
                    + " but device is API " + currentApi + ". Note: " + info.note);
        }

        return new ValidationResult(supported, propKey, info.minApi, info.maxApi, info.note);
    }

    /**
     * Returns true if the setprop key is safe to use on the current Android version.
     * Convenience wrapper for {@link #validate(String)}.
     *
     * @param propKey The property key
     * @return true if supported on current API level
     */
    public static boolean isSupported(String propKey) {
        return validate(propKey).supported;
    }

    /**
     * Filters a setprop command string — returns null if the prop key is not supported
     * on the current Android API level, allowing batch builders to skip unsupported commands.
     *
     * @param setpropCommand Full "setprop key value" command string
     * @return The command unchanged if supported, null if should be skipped
     */
    public static String filterCommand(String setpropCommand) {
        if (setpropCommand == null) return null;
        if (!setpropCommand.startsWith("setprop ")) return setpropCommand; // not a setprop, pass through

        String[] parts = setpropCommand.split("\\s+", 3);
        if (parts.length < 2) return setpropCommand;

        String key = parts[1];
        return isSupported(key) ? setpropCommand : null;
    }

    /**
     * Returns the current Android API level for display.
     */
    public static int getCurrentApiLevel() {
        return Build.VERSION.SDK_INT;
    }
}
