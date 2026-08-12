package com.gamebooster.app.feature.sensitivity.model;

/**
 * Interactive calculator mapping device DPI, screen size, and gyro sensitivity
 * to recommended manual in-game sensitivity values for PUBG Mobile & COD Mobile.
 */
public class SensitivityCalculator {

    public static SensitivityModel calculate(int dpi, double screenSizeInches, float gyroPreferenceMultiplier) {
        SensitivityModel model = new SensitivityModel();

        // Baseline DPI scaling factor (Standard mobile baseline ~ 400 DPI)
        double dpiFactor = 400.0 / Math.max(160, dpi);

        // Baseline screen size scaling (Standard 6.5" phone)
        double screenFactor = Math.max(4.5, screenSizeInches) / 6.5;

        // Base sensitivity coefficient
        double baseCoeff = 100.0 * dpiFactor * screenFactor;

        model.freeLook = clamp((int) (baseCoeff * 1.2));
        model.noScope3rdPerson = clamp((int) (baseCoeff * 1.1));
        model.noScope1stPerson = clamp((int) (baseCoeff * 1.0));

        // Scope sensitivities (Focal length reduction principle)
        model.redDotHolo = clamp((int) (baseCoeff * 0.70));
        model.scope2x = clamp((int) (baseCoeff * 0.55));
        model.scope3x = clamp((int) (baseCoeff * 0.40));
        model.scope4x = clamp((int) (baseCoeff * 0.30));
        model.scope6x = clamp((int) (baseCoeff * 0.20));
        model.scope8x = clamp((int) (baseCoeff * 0.12));

        // Gyroscope sensitivities (scaled by user preference multiplier)
        double gyroBase = 300.0 * gyroPreferenceMultiplier;
        model.gyroNoScope = clampGyro((int) (gyroBase * 1.0));
        model.gyroRedDot = clampGyro((int) (gyroBase * 0.9));
        model.gyro2x = clampGyro((int) (gyroBase * 0.8));
        model.gyro3x = clampGyro((int) (gyroBase * 0.7));
        model.gyro4x = clampGyro((int) (gyroBase * 0.55));
        model.gyro6x = clampGyro((int) (gyroBase * 0.4));
        model.gyro8x = clampGyro((int) (gyroBase * 0.3));

        model.summary = String.format("Calculated for %d DPI on %.1f\" display (Gyro multiplier: %.1fx)",
                dpi, screenSizeInches, gyroPreferenceMultiplier);

        return model;
    }

    private static int clamp(int val) {
        return Math.max(1, Math.min(300, val));
    }

    private static int clampGyro(int val) {
        return Math.max(1, Math.min(400, val));
    }
}
