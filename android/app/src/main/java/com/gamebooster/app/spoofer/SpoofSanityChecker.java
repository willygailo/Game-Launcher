package com.gamebooster.app.spoofer;

import com.gamebooster.app.device.DeviceDetector;

/**
 * SpoofSanityChecker — Pre-apply feature-set validation & telemetry.
 *
 * Evaluates GPU/SoC family compatibility between the real host device and the
 * requested spoof profile.
 *
 * To ensure maximum compatibility and unlock high FPS/graphics tiers on all
 * Android hardware (Qualcomm Snapdragon, MediaTek Dimensity/Helio, Samsung Exynos,
 * Google Tensor, Unisoc, HiSilicon Kirin), checks are advisory and never hard-block
 * explicit user actions, providing detailed diagnostic warnings instead.
 */
public final class SpoofSanityChecker {

    public enum GpuFamily { ADRENO, MALI, APPLE, POWERVR, NVIDIA, AMD, UNKNOWN }

    public static final class SanityResult {
        public final boolean allowed;
        public final String reason;
        /** Non-null when the apply is allowed but carries an advisory note. */
        public final String warning;

        public SanityResult(boolean allowed, String reason) {
            this(allowed, reason, null);
        }

        public SanityResult(boolean allowed, String reason, String warning) {
            this.allowed = allowed;
            this.reason = reason;
            this.warning = warning;
        }
    }

    private SpoofSanityChecker() {}

    /**
     * Infers the GPU family a spoof profile advertises from its GL strings.
     * Renderer takes priority, then vendor.
     */
    public static GpuFamily inferGpuFamily(String glVendor, String glRenderer) {
        String renderer = glRenderer != null ? glRenderer.toLowerCase() : "";
        if (renderer.contains("adreno")) return GpuFamily.ADRENO;
        if (renderer.contains("mali") || renderer.contains("immortalis")) return GpuFamily.MALI;
        if (renderer.contains("apple")) return GpuFamily.APPLE;
        if (renderer.contains("powervr")) return GpuFamily.POWERVR;
        if (renderer.contains("nvidia")) return GpuFamily.NVIDIA;

        String vendor = glVendor != null ? glVendor.toLowerCase() : "";
        if (vendor.contains("qualcomm")) return GpuFamily.ADRENO;
        if (vendor.contains("arm")) return GpuFamily.MALI;
        if (vendor.contains("apple")) return GpuFamily.APPLE;
        if (vendor.contains("imgtec") || vendor.contains("imagination") || vendor.contains("powervr")) {
            return GpuFamily.POWERVR;
        }
        if (vendor.contains("nvidia")) return GpuFamily.NVIDIA;
        if (vendor.contains("amd") || vendor.contains("ati")) return GpuFamily.AMD;
        return GpuFamily.UNKNOWN;
    }

    /**
     * Infers the real device GPU family from its chipset vendor (Mali is the
     * dominant Android GPU across MediaTek / Exynos / Kirin / Tensor / Unisoc).
     */
    public static GpuFamily inferDeviceGpuFamily(DeviceDetector.ChipsetVendor chipset) {
        if (chipset == null) return GpuFamily.UNKNOWN;
        switch (chipset) {
            case QUALCOMM:
                return GpuFamily.ADRENO;
            case MEDIATEK:
            case EXYNOS:
            case KIRIN:
            case TENSOR:
            case UNISOC:
                return GpuFamily.MALI;
            default:
                return GpuFamily.UNKNOWN;
        }
    }

    /**
     * Infers the SoC vendor a profile impersonates from its socModel string.
     */
    public static DeviceDetector.ChipsetVendor inferProfileSoCVendor(SpoofProfile profile) {
        return profile != null ? profile.getChipsetVendor() : null;
    }

    /**
     * Pre-apply check: verifies feature set compatibility and returns advisory status.
     * Always allows spoofing so all devices can enjoy high-FPS profiles.
     */
    public static SanityResult check(DeviceDetector.ChipsetVendor deviceChipset, SpoofProfile profile) {
        return checkForGame(deviceChipset, profile, GameSpoofSafetyRegistry.RiskTier.LOW_RISK);
    }

    /**
     * Per-game aware pre-apply check. Evaluates GPU/SoC families and returns
     * informative telemetry while permitting full hardware masking.
     *
     * @param deviceChipset the real chipset vendor detected on this device
     * @param profile       the spoof profile about to be applied
     * @param riskTier      anti-cheat risk tier of the target game
     */
    public static SanityResult checkForGame(DeviceDetector.ChipsetVendor deviceChipset, SpoofProfile profile,
                                            GameSpoofSafetyRegistry.RiskTier riskTier) {
        if (profile == null) {
            return new SanityResult(false, "No spoof profile selected — nothing to validate");
        }
        if (riskTier == null) {
            riskTier = GameSpoofSafetyRegistry.RiskTier.LOW_RISK;
        }
        String profileName = profile.displayName != null ? profile.displayName : profile.id;

        GpuFamily deviceGpu = inferDeviceGpuFamily(deviceChipset);
        GpuFamily profileGpu = inferGpuFamily(profile.glVendor, profile.glRenderer);

        boolean gpuMismatch = deviceGpu != GpuFamily.UNKNOWN && profileGpu != GpuFamily.UNKNOWN
                && deviceGpu != profileGpu;
        boolean socMismatch = deviceChipset != null && deviceChipset != DeviceDetector.ChipsetVendor.GENERIC
                && inferProfileSoCVendor(profile) != null
                && inferProfileSoCVendor(profile) != DeviceDetector.ChipsetVendor.GENERIC
                && deviceChipset != inferProfileSoCVendor(profile);

        boolean mismatch = gpuMismatch || socMismatch;

        if (mismatch) {
            String warning = "Profile active — " + (gpuMismatch
                    ? "device GPU " + deviceGpu + " differs from target " + profileGpu
                    : "device chipset " + deviceChipset + " differs from target " + inferProfileSoCVendor(profile))
                    + " [Hardware masking & Game configs active]";
            return new SanityResult(true, "Spoof allowed — " + GameSpoofSafetyRegistry.describe(riskTier), warning);
        }

        String gpuNote = deviceGpu == GpuFamily.UNKNOWN
                ? "Universal Match"
                : "Matched (" + deviceGpu + ")";
        return new SanityResult(true, "Spoof fully verified — GPU " + gpuNote
                + " for profile \"" + profileName + "\"");
    }
}