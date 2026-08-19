package com.gamebooster.app.spoofer;

import com.gamebooster.app.device.DeviceDetector;

/**
 * SpoofSanityChecker — pre-apply feature-set validation (Phase 2.4).
 *
 * Spoofing a profile whose GPU/SoC family differs from the real device is a
 * known ban vector (multi-GPU/GL-vendor detection): e.g. advertising an Apple
 * A18 Pro GPU on a Mali-powered device. This checker runs BEFORE any mask is
 * written; a mismatching profile is blocked with an explanation instead of
 * being applied. All logic is pure — unit-testable on the JVM.
 */
public final class SpoofSanityChecker {

    public enum GpuFamily { ADRENO, MALI, APPLE, POWERVR, NVIDIA, AMD, UNKNOWN }

    public static final class SanityResult {
        public final boolean allowed;
        public final String reason;
        /** Non-null when the apply is allowed but carries a caveat (e.g. GPU mismatch on a soft-AC game). */
        public final String warning;

        private SanityResult(boolean allowed, String reason) {
            this(allowed, reason, null);
        }

        private SanityResult(boolean allowed, String reason, String warning) {
            this.allowed = allowed;
            this.reason = reason;
            this.warning = warning;
        }
    }

    private static final String BLOCK_TAG = "known ban vector — apply blocked";

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
     * Infers the SoC vendor a profile impersonates from its socModel string,
     * or null when the profile does not name a recognizable vendor.
     */
    static DeviceDetector.ChipsetVendor inferProfileSoCVendor(SpoofProfile profile) {
        if (profile == null) return null;
        String soc = profile.socModel != null ? profile.socModel.toLowerCase() : "";
        if (soc.contains("dimensity")) return DeviceDetector.ChipsetVendor.MEDIATEK;
        if (soc.contains("exynos")) return DeviceDetector.ChipsetVendor.EXYNOS;
        if (soc.contains("tensor")) return DeviceDetector.ChipsetVendor.TENSOR;
        if (soc.contains("kirin")) return DeviceDetector.ChipsetVendor.KIRIN;
        if (soc.contains("unisoc")) return DeviceDetector.ChipsetVendor.UNISOC;
        if (soc.contains("snapdragon") || soc.contains("sm8")) return DeviceDetector.ChipsetVendor.QUALCOMM;
        if (soc.contains("apple") || soc.matches("a1[0-9].*")) return DeviceDetector.ChipsetVendor.APPLE;
        return null;
    }

    /**
     * Pre-apply check: blocks a spoof profile when the device's real GPU or SoC
     * family provably differs from what the profile advertises. When either side
     * is undetectable the apply is allowed (with an explanatory warning).
     *
     * @param deviceChipset the real chipset vendor detected on this device
     * @param profile       the spoof profile about to be applied
     */
    public static SanityResult check(DeviceDetector.ChipsetVendor deviceChipset, SpoofProfile profile) {
        return checkForGame(deviceChipset, profile, GameSpoofSafetyRegistry.RiskTier.HIGH_RISK);
    }

    /**
     * Per-game aware pre-apply check. The blocking threshold follows the
     * anti-cheat risk tier of the target game (see GameSpoofSafetyRegistry):
     *
     * - HIGH_RISK (kernel-level AC, e.g. Tencent ACE): GPU/SoC family mismatch
     *   blocks the apply — cross-vendor GL spoofing is a provable ban vector.
     * - MEDIUM_RISK / LOW_RISK (soft AC or none): mismatch is allowed with a
     *   warning so the spoof stays "fully working" on non-Snapdragon devices.
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
        if (mismatch && riskTier == GameSpoofSafetyRegistry.RiskTier.HIGH_RISK) {
            String mismatchDesc = gpuMismatch && socMismatch
                    ? "GPU and SoC feature sets differ"
                    : gpuMismatch
                            ? "GPU feature set mismatch"
                            : "SoC feature set mismatch";
            String detail = gpuMismatch
                    ? "this device renders with " + deviceGpu + " but profile \"" + profileName + "\" advertises " + profileGpu
                    : "this device runs " + deviceChipset + " but profile \"" + profileName + "\" impersonates " + inferProfileSoCVendor(profile);
            return new SanityResult(false, mismatchDesc + ": " + detail + " (GL vendor swap across GPU families is a "
                    + BLOCK_TAG + ". Choose a profile matching your device GPU or disable spoofing.)");
        }

        if (mismatch) {
            String warning = "Applied with warning — " + (gpuMismatch
                    ? "device GPU " + deviceGpu + " differs from profile " + profileGpu
                    : "device chipset " + deviceChipset + " differs from profile " + inferProfileSoCVendor(profile))
                    + ". Not recommended for kernel-anti-cheat titles.";
            return new SanityResult(true, "Spoof allowed — " + GameSpoofSafetyRegistry.describe(riskTier), warning);
        }

        String gpuNote = deviceGpu == GpuFamily.UNKNOWN
                ? "not verifiable (device GPU undetectable)"
                : "compatible (" + deviceGpu + ")";
        return new SanityResult(true, "Spoof allowed — GPU feature set " + gpuNote
                + " for profile \"" + profileName + "\"");
    }
}