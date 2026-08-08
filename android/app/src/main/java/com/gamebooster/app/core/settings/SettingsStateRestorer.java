package com.gamebooster.app.core.settings;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.booster.CpuGovernorChannel;
import com.gamebooster.app.booster.EsportsAudioEnhancer;
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.config.ManualSettingsPreferences;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.gamespace.AutoGameMonitorService;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.tweaks.TweakManagerRepository;

/**
 * SettingsStateRestorer — Restores and locks all user-configured performance settings
 * when the app starts or Shizuku connects. Ensures settings stay ON permanently and never auto-turn off.
 */
public class SettingsStateRestorer {

    private static final String TAG = "SettingsStateRestorer";

    public static void restoreAllSettings(Context context) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        AppExecutors.getInstance().executeCommand(() -> {
            boolean masterEnabled = ManualSettingsPreferences.isMasterBoostEnabled(appContext);
            if (!masterEnabled) {
                Log.i(TAG, "Master Boost is OFF. Reverting all tweaks to AOSP defaults...");
                TweakManagerRepository.revertAllTweaks(appContext);
                return;
            }

            Log.i(TAG, "Restoring and enforcing persistent Esports gaming performance settings...");

            // 1. Enforce Zero Touch Latency
            TouchLatencyChannel.enableUltraTouchResponse();

            // 2. Restore GPU Renderer Mode (Vulkan/Skia)
            String gpuMode = ManualSettingsPreferences.getGpuMode(appContext);
            PerformanceChannel.setGpuRenderMode("vulkan".equalsIgnoreCase(gpuMode));

            // 3. Restore CPU Governor (Performance/Schedutil)
            String cpuMode = ManualSettingsPreferences.getCpuMode(appContext);
            CpuGovernorChannel.setGovernor("performance".equalsIgnoreCase(cpuMode) ? "extreme" : "schedutil");

            // 4. Restore Google ANGLE Driver & System Game Driver
            boolean angleEnabled = ManualSettingsPreferences.isAngleModeEnabled(appContext);
            if (angleEnabled) {
                GpuTweaksChannel.setAngleMode(true);
            }

            boolean gameDriverEnabled = ManualSettingsPreferences.isGameDriverEnabled(appContext);
            if (gameDriverEnabled) {
                GpuTweaksChannel.setGameDriverMode(true);
            }

            // 5. Restore Network Optimization (Tethering HW Offload & GNSS Raw)
            boolean tetherHw = ManualSettingsPreferences.isTetherHwEnabled(appContext);
            if (tetherHw) {
                NetworkOptimizer.setTetheringHwAcceleration(true);
            }

            boolean forceGnss = ManualSettingsPreferences.isForceGnssEnabled(appContext);
            if (forceGnss) {
                NetworkOptimizer.setForceFullGnss(true);
            }

            // 6. Restore Hardware Device Spoofing if active
            if (SpoofPreferences.isSpoofEnabled(appContext)) {
                String activeProfileId = SpoofPreferences.getActiveProfileId(appContext);
                if (activeProfileId != null) {
                    com.gamebooster.app.spoofer.SpoofProfile activeProf = DeviceSpooferEngine.getProfileById(activeProfileId);
                    if (activeProf != null) {
                        DeviceSpooferEngine.applyProfile(appContext, activeProf, null);
                    }
                }
            }

            // 7. Restore applied system tweak items
            TweakManagerRepository.restoreAppliedTweaksAsync(appContext);

            // 8. Restore Esports Footstep Audio Enhancement
            if (EsportsAudioEnhancer.isEnabled()) {
                EsportsAudioEnhancer.setEsportsAudioMode(appContext, true);
            }

            // 9. Restore Gaming DND Mode
            if (GameSpaceDndManager.isDndActive(appContext)) {
                GameSpaceDndManager.setGamingDndMode(appContext, true);
            }

            // 10. Restore Auto Game Launch Monitor Service
            if (AutoGameMonitorService.isRunning()) {
                AutoGameMonitorService.start(appContext);
            }

            Log.i(TAG, "Persistent Esports gaming settings restoration COMPLETE.");
        });
    }
}
