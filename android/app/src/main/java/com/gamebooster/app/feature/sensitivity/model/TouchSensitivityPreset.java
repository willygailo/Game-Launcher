package com.gamebooster.app.feature.sensitivity.model;

/**
 * Model representing game touch sensitivity & input tuning presets.
 */
public class TouchSensitivityPreset {

    public enum PresetType {
        FPS_CLAW_COMPETITIVE,
        MOBA_FAST_COMBO,
        SNIPER_STEADY_SCOPE,
        BALANCED_DEFAULT
    }

    public final PresetType type;
    public final String name;
    public final String description;
    public final int touchSlop;
    public final float pressureScale;
    public final int maxEventsPerSec;
    public final float gyroLpfAlpha;

    public TouchSensitivityPreset(PresetType type, String name, String description,
                                  int touchSlop, float pressureScale, int maxEventsPerSec, float gyroLpfAlpha) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.touchSlop = touchSlop;
        this.pressureScale = pressureScale;
        this.maxEventsPerSec = maxEventsPerSec;
        this.gyroLpfAlpha = gyroLpfAlpha;
    }

    public static TouchSensitivityPreset getPreset(PresetType type) {
        switch (type) {
            case FPS_CLAW_COMPETITIVE:
                return new TouchSensitivityPreset(
                        PresetType.FPS_CLAW_COMPETITIVE,
                        "FPS 4-Finger Claw Competitive",
                        "Zero slop, 1000/s max event sampling rate, instantaneous touch & zero aim delay for PUBGM/CODM",
                        0, 0.0001f, 1000, 0.85f
                );
            case MOBA_FAST_COMBO:
                return new TouchSensitivityPreset(
                        PresetType.MOBA_FAST_COMBO,
                        "MOBA Fast Combo Skill Cast",
                        "Low touch slop (2), ultra-fast drag prediction for MLBB skill trajectory targeting",
                        2, 0.001f, 500, 0.75f
                );
            case SNIPER_STEADY_SCOPE:
                return new TouchSensitivityPreset(
                        PresetType.SNIPER_STEADY_SCOPE,
                        "Sniper Steady Scope Aim",
                        "Balanced touch slop (4) with ultra-smooth 3-axis gyroscope LPF jitter suppression for long-range sniping",
                        4, 0.01f, 300, 0.35f
                );
            case BALANCED_DEFAULT:
            default:
                return new TouchSensitivityPreset(
                        PresetType.BALANCED_DEFAULT,
                        "Balanced Stock Preset",
                        "Standard AOSP touch slop and system defaults",
                        8, 1.0f, 150, 0.8f
                );
        }
    }
}
