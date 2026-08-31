package com.gamebooster.app.config;

/**
 * FpsUnlockTier — Centralized FPS tier definitions for all game config patchers.
 *
 * Supported tiers: 60 / 90 / 120 / 144 / 165 / 185 / 240 fps.
 *
 * FPS Level mapping (PUBGM/UE4 standard):
 *   5 = 60fps, 6 = 90fps, 7 = 120fps, 8 = 144fps, 9 = 165fps, 10 = 185fps, 11 = 240fps
 */
public enum FpsUnlockTier {

    FPS_60(60, 5, "60fps"),
    FPS_90(90, 6, "90fps"),
    FPS_120(120, 7, "120fps"),
    FPS_144(144, 8, "144fps"),
    FPS_165(165, 9, "165fps"),
    FPS_185(185, 10, "185fps"),
    FPS_240(240, 11, "240fps");

    /** The raw target FPS value. */
    public final int fps;

    /** The internal device/frame-rate level integer. */
    public final int level;

    /** Human-readable label for UI display. */
    public final String label;

    FpsUnlockTier(int fps, int level, String label) {
        this.fps   = fps;
        this.level = level;
        this.label = label;
    }

    // ─── Lookup Helpers ──────────────────────────────────────────────────────

    /**
     * Resolves a requested FPS value to the nearest supported tier at-or-below
     * the request. Invalid (<= 0) values fall back to the top tier FPS_240.
     */
    public static FpsUnlockTier fromFps(int targetFps) {
        if (targetFps <= 0) return FPS_240;
        FpsUnlockTier best = values()[0];
        for (FpsUnlockTier tier : values()) {
            if (tier.fps <= targetFps) best = tier;
        }
        return best;
    }

    /**
     * Returns the tier matching the given level integer, or FPS_240 if unknown.
     */
    public static FpsUnlockTier fromLevel(int level) {
        for (FpsUnlockTier tier : values()) {
            if (tier.level == level) return tier;
        }
        return FPS_240;
    }

    /**
     * Returns a valid, tier-aligned FPS value for the requested target.
     * Use this in patchers instead of hard-coding a fixed FPS.
     */
    public static int resolveTargetFps(int targetFps) {
        return fromFps(targetFps).fps;
    }

    /**
     * Returns all available FPS tier values as an int array.
     */
    public static int[] getAllFpsValues() {
        int[] values = new int[values().length];
        for (int i = 0; i < values().length; i++) values[i] = values()[i].fps;
        return values;
    }

    /**
     * Returns all available FPS tier labels as a String array.
     */
    public static String[] getAllLabels() {
        String[] labels = new String[values().length];
        for (int i = 0; i < values().length; i++) labels[i] = values()[i].label;
        return labels;
    }

    // ─── Unlock Flag Generators ──────────────────────────────────────────────

    /**
     * Returns INI-style unlock flags for all Hz tiers up to and including this tier.
     */
    public String getUnlockFlags() {
        StringBuilder sb = new StringBuilder();
        for (int hz : HZ_UNLOCK_FLAGS) {
            if (hz > fps) break;
            sb.append("Unlock").append(hz).append("Hz=1\n");
        }
        return sb.toString();
    }

    /**
     * Returns UE4/UE5 CVar-style unlock flags for all Hz tiers up to and including this tier.
     */
    public String getUE4UnlockCVars() {
        StringBuilder sb = new StringBuilder();
        for (int hz : HZ_UNLOCK_FLAGS) {
            if (hz > fps) break;
            sb.append("+CVars=r.Unlock").append(hz).append("Hz=1\n");
        }
        return sb.toString();
    }

    /**
     * Returns JSON-style unlock flags for all Hz tiers up to and including this tier.
     */
    public String getJsonUnlockFlags() {
        StringBuilder sb = new StringBuilder();
        for (int hz : HZ_UNLOCK_FLAGS) {
            if (hz > fps) break;
            sb.append("  \"Unlock").append(hz).append("Hz\": 1,\n");
        }
        return sb.toString();
    }

    /**
     * Returns XML PlayerPrefs-style unlock flags for all Hz tiers up to and including this tier.
     */
    public String getXmlUnlockFlags() {
        StringBuilder sb = new StringBuilder();
        for (int hz : HZ_UNLOCK_FLAGS) {
            if (hz > fps) break;
            sb.append("  <int name=\"Unlock").append(hz).append("Hz\" value=\"1\" />\n");
        }
        return sb.toString();
    }

    /**
     * Returns INI-style Ultra Extreme graphics flags combined with FPS uncap.
     * 2026 Edition: includes UltraExtreme2026, VRS, HDR10Plus, and Vulkan flags.
     */
    public String getUltraExtremeFlags() {
        return getUnlockFlags() +
                "UltraExtreme=1\n" +
                "UltraExtreme2026=1\n" +
                "bUseUltraExtreme=True\n" +
                "GraphicsQuality=5\n" +
                "GraphicQuality=4\n" +
                "GraphicLevel=4\n" +
                "GraphicsPreset=5\n" +
                "HDRMode=1\n" +
                "HDR10Plus=1\n" +
                "UltraHDMode=1\n" +
                "HDRColorMode=2\n" +
                "SuperResolution=1\n" +
                "RenderScale=120\n" +
                "ShadowQuality=2\n" +
                "LightingQuality=3\n" +
                "ParticleQuality=3\n" +
                "PostProcessing=1\n" +
                "WaterReflection=1\n" +
                "AntiAliasingQuality=4\n" +
                "ResolutionScale=120\n" +
                "ScreenScale=120\n" +
                "VulkanEnabled=1\n" +
                "VulkanPipelineCache=1\n" +
                "FPS=" + fps + "\n" +
                "MaxFPS=" + fps + "\n" +
                "TargetFPS=" + fps + "\n" +
                "UnlockFPS=1\n" +
                "Unlock" + fps + "FPS=1\n";
    }

    /**
     * Returns UE4/UE5 CVar-style Ultra Extreme graphics flags + 2026 performance CVars.
     * Includes: AsyncCompute, VRS, OneFrameThreadLag, LOD boost, shader precompile.
     */
    public String getUE4UltraExtremeCVars() {
        return getUE4UnlockCVars() +
                // ── FPS CVars ──
                "+CVars=r.PUBGDeviceFPS=" + level + "\n" +
                "+CVars=r.PUBGMaxFPS=" + fps + "\n" +
                "+CVars=r.PUBGFrameRateLimit=" + fps + "\n" +
                "+CVars=r.FrameRateLimit=" + fps + "\n" +
                "+CVars=r.MobileFPSLimit=" + fps + "\n" +
                // ── 2026 Graphics CVars ──
                "+CVars=r.PUBGQualityLevel=4\n" +
                "+CVars=r.PUBGSDKQualityLevel=4\n" +
                "+CVars=r.MobileHDR=1\n" +
                "+CVars=r.Tonemapper.Quality=4\n" +
                "+CVars=r.HDR.Display.OutputDevice=1\n" +
                // ── 2026 Performance CVars ──
                "+CVars=r.OneFrameThreadLag=0\n" +
                "+CVars=r.FinishCurrentFrame=0\n" +
                "+CVars=r.AsyncCompute=1\n" +
                "+CVars=r.VRS.Enable=1\n" +
                "+CVars=r.LODHysteresis=0\n" +
                "+CVars=r.LODDistanceFactor=2.5\n" +
                "+CVars=r.EnableAsyncPipelineCompilation=1\n" +
                "+CVars=a.URO.Enable=0\n" +
                "+CVars=r.GPUCrashDebugging=0\n" +
                "+CVars=r.Streaming.LimitPoolSizeToVRAM=0\n";
    }

    // ─── UltraExtreme 144fps SuperSmooth Helpers ─────────────────────────────

    /**
     * Returns true if this tier is 144fps or above (UltraHighFps threshold).
     */
    public boolean isUltraHighFps() {
        return fps >= 144;
    }

    /**
     * Returns UE4/UE5 CVar-style frame-pacing, texture quality, shadow, AA, VRS, and AsyncCompute boost flags.
     * 2026 Edition: full competitive SuperSmooth block.
     */
    public String getSuperSmoothCVars() {
        return "+CVars=r.Vsync=0\n"
             + "+CVars=r.FramePacing=1\n"
             + "+CVars=r.VelocityBlur=1\n"
             + "+CVars=r.TemporalAA.Upscale=1\n"
             + "+CVars=r.MobileContentScaleFactor=1.0\n"
             + "+CVars=r.MobileReduceLoadedMips=0\n"
             + "+CVars=r.MaxAnisotropy=16\n"
             + "+CVars=r.BloomQuality=5\n"
             + "+CVars=r.DepthOfFieldQuality=4\n"
             + "+CVars=r.Shadow.MaxResolution=2048\n"
             + "+CVars=r.Shadow.CSM.MaxMobileCascades=4\n"
             + "+CVars=r.ReflectionCaptureResolution=256\n"
             + "+CVars=r.AllowOcclusionQueries=1\n"
             + "+CVars=r.TouchBoostHz=" + fps + "\n"
             + "+CVars=r.MobileTouchBoostRate=" + fps + "\n"
             // ── 2026 SuperSmooth additions ──
             + "+CVars=r.OneFrameThreadLag=0\n"
             + "+CVars=r.FinishCurrentFrame=0\n"
             + "+CVars=r.AsyncCompute=1\n"
             + "+CVars=r.VRS.Enable=1\n"
             + "+CVars=r.LODHysteresis=0\n"
             + "+CVars=r.LODDistanceFactor=2.5\n"
             + "+CVars=r.EnableAsyncPipelineCompilation=1\n"
             + "+CVars=a.URO.Enable=0\n"
             + "+CVars=r.GPUCrashDebugging=0\n";
    }

    /**
     * Returns INI-style max graphics quality flags (2026 Edition).
     * Compatible with all game config sections that accept plain key=value pairs.
     */
    public String getGraphicsMaxFlags() {
        return "UltraExtreme=1\n"
             + "UltraExtreme2026=1\n"
             + "bUseUltraExtreme=True\n"
             + "GraphicsQuality=5\n"
             + "GraphicQuality=4\n"
             + "GraphicLevel=4\n"
             + "GraphicsPreset=5\n"
             + "ResolutionQuality=120\n"
             + "ResolutionScale=120\n"
             + "RenderScale=120\n"
             + "ScreenScale=120\n"
             + "HDRMode=1\n"
             + "HDR10Plus=1\n"
             + "UltraHDMode=1\n"
             + "HDRColorMode=2\n"
             + "SuperResolution=1\n"
             + "bUseHDRMode=True\n"
             + "bUseHighQualityBloom=True\n"
             + "BloomQuality=5\n"
             + "AntiAliasingQuality=4\n"
             + "bUseAntiAliasing=True\n"
             + "ShadowQuality=2\n"
             + "ShadowDistance=3\n"
             + "ShadowResolution=2048\n"
             + "LightingQuality=3\n"
             + "ParticleQuality=3\n"
             + "PostProcessing=1\n"
             + "WaterReflection=1\n"
             + "TextureQuality=4\n"
             + "MaxAnisotropy=16\n"
             + "bReduceLoadedMips=False\n"
             + "bFramePacingEnabled=True\n"
             + "VulkanEnabled=1\n"
             + "VulkanPipelineCache=1\n"
             + "AsyncCompute=1\n"
             + "VRS=1\n"
             + "Vsync=0\n"
             + "TouchBoostHz=" + fps + "\n"
             + "TouchPollingRate=1000\n";
    }

    /**
     * Returns a combined INI block for UltraExtreme: unlock flags + max graphics + FPS keys.
     * 2026 Edition with full UltraExtreme graphics and AsyncCompute.
     */
    public String getUltraExtreme144Flags() {
        return getUnlockFlags()
             + getGraphicsMaxFlags()
             + "FPS=" + fps + "\n"
             + "MaxFPS=" + fps + "\n"
             + "TargetFPS=" + fps + "\n"
             + "FrameRateLimit=" + fps + "\n"
             + "MobileFPSLimit=" + fps + "\n"
             + "UnlockFPS=1\n"
             + "Unlock" + fps + "FPS=1\n"
             + "Ultra" + fps + "FPS=1\n"
             + "FrameRateLevel=" + level + "\n"
             + "HighFPSMode=3\n"
             + "SuperHighFPS=1\n";
    }

    private static final int[] HZ_UNLOCK_FLAGS = {90, 120, 144, 165, 185, 240};
}
