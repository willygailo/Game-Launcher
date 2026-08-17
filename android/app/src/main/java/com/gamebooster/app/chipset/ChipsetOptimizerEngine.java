package com.gamebooster.app.chipset;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.device.DeviceDetector;
import com.gamebooster.app.device.DeviceDetector.ChipsetVendor;

/**
 * ChipsetOptimizerEngine — Universal Chipset Detection & Tuning Orchestrator.
 * Automatically identifies the exact processor vendor & generation, then applies
 * tailored low-level driver properties, GPU turbo commands, touch sampling rates,
 * and hardware frequency governor locks across Qualcomm, MediaTek, Exynos, Tensor, Unisoc, and Kirin.
 */
public class ChipsetOptimizerEngine {

    private static final String TAG = "ChipsetOptimizerEngine";

    /**
     * Automatically detects device chipset and applies dedicated optimizations.
     *
     * @param context Android context
     * @param targetFps Desired frame rate target (e.g. 90, 120, 144, 165, 185)
     * @return true if chipset-specific tuning was applied
     */
    public static boolean applyChipsetOptimization(Context context, int targetFps) {
        ChipsetVendor vendor = DeviceDetector.detectChipsetVendor();
        String socDesc = DeviceDetector.getDetailedSocDescription();
        Log.i(TAG, "⚡ Detected SoC: " + socDesc + " [Vendor: " + vendor.name() + "] — Target: " + targetFps + " FPS");

        switch (vendor) {
            case QUALCOMM:
                return QualcommTuner.applySnapdragonBoost(targetFps);

            case MEDIATEK:
                return MediaTekTuner.applyMediaTekBoost(targetFps);

            case EXYNOS:
                return ExynosTuner.applyExynosBoost(targetFps);

            case TENSOR:
                return TensorTuner.applyTensorBoost(targetFps);

            case UNISOC:
                return UnisocTuner.applyUnisocBoost(targetFps);

            case KIRIN:
                return KirinTuner.applyKirinBoost(targetFps);

            case GENERIC:
            default:
                // Universal AOSP & Vulkan Pipeline fallback
                QualcommTuner.applySnapdragonBoost(targetFps);
                return MediaTekTuner.applyMediaTekBoost(targetFps);
        }
    }

    public static String getActiveChipsetSummary() {
        return DeviceDetector.getDetailedSocDescription();
    }
}
