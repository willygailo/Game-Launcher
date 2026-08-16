package com.gamebooster.app.ui.sensitivity;

/**
 * Advanced sensitivity & gyro recoil tuner calculating legal FOV, DPI, and scope scaling
 * profiles for PUBG Mobile, COD Mobile, Free Fire, and Mobile Legends.
 */
public class SensitivityCalculator {

    public enum GameProfile {
        PUBG_MOBILE("PUBG Mobile / BGMI"),
        COD_MOBILE("Call of Duty Mobile"),
        FREE_FIRE("Free Fire / Free Fire Max"),
        MLBB("Mobile Legends: Bang Bang");

        private final String label;
        GameProfile(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum RecoilMode {
        BALANCED("Balanced / Standard"),
        PRECISION_RECOIL("Low Recoil / Precision Micro-Aim"),
        PRO_GYRO_MAX("Pro Gyro 400% Ultra Response");

        private final String label;
        RecoilMode(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public static SensitivityModel calculate(int dpi, double screenSizeInches, GameProfile gameProfile, RecoilMode recoilMode) {
        SensitivityModel model = new SensitivityModel();

        // Baseline DPI scaling factor (Standard mobile baseline ~ 400 DPI)
        double dpiFactor = 400.0 / Math.max(160, dpi);
        double screenFactor = Math.max(4.5, screenSizeInches) / 6.5;

        double gameModifier = 1.0;
        switch (gameProfile) {
            case COD_MOBILE: gameModifier = 1.15; break;
            case FREE_FIRE: gameModifier = 1.25; break;
            case MLBB: gameModifier = 0.90; break;
            default: gameModifier = 1.0; break;
        }

        double recoilMultiplier = 1.0;
        double gyroRecoilMultiplier = 1.0;
        switch (recoilMode) {
            case PRECISION_RECOIL:
                recoilMultiplier = 0.85; // Lower sensitivity for stable recoil control
                gyroRecoilMultiplier = 1.35; // Higher gyro dampening for effortless vertical recoil control
                break;
            case PRO_GYRO_MAX:
                recoilMultiplier = 1.0;
                gyroRecoilMultiplier = 1.60;
                break;
            default:
                recoilMultiplier = 1.0;
                gyroRecoilMultiplier = 1.0;
                break;
        }

        double baseCoeff = 100.0 * dpiFactor * screenFactor * gameModifier * recoilMultiplier;

        model.freeLook = clamp((int) (baseCoeff * 1.2));
        model.noScope3rdPerson = clamp((int) (baseCoeff * 1.0)); // TPP sensitivity
        model.noScope1stPerson = clamp((int) (baseCoeff * 1.2)); // FPP sensitivity

        // Presets requested: TPP 100, FPP 150, Sprint 150, Aim Assist 100%
        model.tppFov = 100;
        model.fppFov = 150;
        model.sprintSensitivity = 150;
        model.aimAssistStrength = 100;

        // Focal length reduction scope ratios
        model.redDotHolo = clamp((int) (baseCoeff * 0.68));
        model.scope2x = clamp((int) (baseCoeff * 0.52));
        model.scope3x = clamp((int) (baseCoeff * 0.38));
        model.scope4x = clamp((int) (baseCoeff * 0.28));
        model.scope6x = clamp((int) (baseCoeff * 0.18));
        model.scope8x = clamp((int) (baseCoeff * 0.11));

        // Gyroscope dampening & recoil compensation values
        double gyroBase = 300.0 * gyroRecoilMultiplier;
        model.gyroNoScope = clampGyro((int) (gyroBase * 1.0));
        model.gyroRedDot = clampGyro((int) (gyroBase * 0.95));
        model.gyro2x = clampGyro((int) (gyroBase * 0.85));
        model.gyro3x = clampGyro((int) (gyroBase * 0.75));
        model.gyro4x = clampGyro((int) (gyroBase * 0.60));
        model.gyro6x = clampGyro((int) (gyroBase * 0.45));
        model.gyro8x = clampGyro((int) (gyroBase * 0.35));

        model.summary = String.format("%s profile (%s) - %d DPI, %.1f\" screen",
                gameProfile.getLabel(), recoilMode.getLabel(), dpi, screenSizeInches);

        return model;
    }

    public static SensitivityModel calculate(int dpi, double screenSizeInches, float gyroPreferenceMultiplier) {
        return calculate(dpi, screenSizeInches, GameProfile.PUBG_MOBILE, RecoilMode.BALANCED);
    }

    private static int clamp(int val) {
        return Math.max(1, Math.min(300, val));
    }

    private static int clampGyro(int val) {
        return Math.max(1, Math.min(400, val));
    }
}
