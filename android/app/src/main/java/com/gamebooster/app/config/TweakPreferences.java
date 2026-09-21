package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import com.gamebooster.app.tweaks.TweakItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TweakPreferences {

    private static final String PREF_NAME = "game_booster_tweak_prefs";
    private static final String KEY_PREFIX_TWEAK = "tweak_applied_";

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveTweakState(Context context, String tweakId, boolean applied) {
        if (context == null || tweakId == null) return;
        getPrefs(context).edit().putBoolean(KEY_PREFIX_TWEAK + tweakId, applied).apply();
    }

    public static boolean isTweakApplied(Context context, String tweakId) {
        if (context == null || tweakId == null) return false;
        return getPrefs(context).getBoolean(KEY_PREFIX_TWEAK + tweakId, false);
    }

    public static Set<String> getAppliedTweakIds(Context context) {
        Set<String> appliedIds = new HashSet<>();
        if (context == null) return appliedIds;
        SharedPreferences prefs = getPrefs(context);
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_PREFIX_TWEAK) && Boolean.TRUE.equals(prefs.getAll().get(key))) {
                appliedIds.add(key.substring(KEY_PREFIX_TWEAK.length()));
            }
        }
        return appliedIds;
    }

    public static void loadSavedStates(Context context, List<TweakItem> tweaks) {
        if (context == null || tweaks == null) return;
        SharedPreferences prefs = getPrefs(context);
        for (TweakItem tweak : tweaks) {
            boolean applied = prefs.getBoolean(KEY_PREFIX_TWEAK + tweak.getId(), false);
            tweak.setApplied(applied);
        }
    }

    /**
     * Bidirectionally synchronizes a specific tweak ID with its corresponding top switch in ManualSettingsPreferences.
     */
    public static void syncTweakToManualSetting(Context context, String tweakId, boolean applied) {
        if (context == null || tweakId == null) return;
        switch (tweakId) {
            case "universal_1000hz_digitizer_lock":
            case "touch_sampling_rate_1000hz_lock":
                ManualSettingsPreferences.setTouch1000HzLockEnabled(context, applied);
                break;
            case "tcp_bbr_v3_congestion_control":
            case "tcp_bbr_gaming_low_latency":
                ManualSettingsPreferences.setTcpBbrBuffersEnabled(context, applied);
                break;
            case "android_phantom_freezer_kill":
            case "android_13_16_phantom_freezer_kill":
                ManualSettingsPreferences.setPhantomFreezerKillEnabled(context, applied);
                break;
            case "vulkan_skiavk_render_thread_rt":
            case "skia_vulkan_pipeline":
                ManualSettingsPreferences.setVulkanSkiaVkEnabled(context, applied);
                break;
            case "uclamp_cpuset_top_app_boost":
                ManualSettingsPreferences.setUclampBoostEnabled(context, applied);
                break;
            case "memory_16kb_vm_shield":
                ManualSettingsPreferences.setMemory16kbShieldEnabled(context, applied);
                break;
            case "disable_data_saver":
                ManualSettingsPreferences.setDisableDataSaverEnabled(context, applied);
                break;
            case "disable_battery_saver":
                ManualSettingsPreferences.setDisableBatterySaverEnabled(context, applied);
                break;
            case "wifi_power_save_disable":
                ManualSettingsPreferences.setDisableWifiSaverEnabled(context, applied);
                break;
            case "wlan_fast_roaming_boost":
            case "wifi7_low_latency_gaming_mode":
                ManualSettingsPreferences.setWifiLowLatencyEnabled(context, applied);
                break;
            case "art_dexopt_speed_compile":
                ManualSettingsPreferences.setAotSpeedEnabled(context, applied);
                break;
            case "game_driver_mlbb_codm_pubgm_preference":
                ManualSettingsPreferences.setGameDriverEnabled(context, applied);
                break;
            case "android_13_16_adpf_fixed_perf":
            case "android_15_16_adpf_headroom_v2":
                ManualSettingsPreferences.setAdpfEngineEnabled(context, applied);
                break;
            default:
                break;
        }
    }

    /**
     * Synchronizes a ManualSettingsPreferences change into the corresponding TweakPreferences entry.
     */
    public static void syncManualSettingToTweak(Context context, String manualKey, boolean enabled) {
        if (context == null || manualKey == null) return;
        switch (manualKey) {
            case "touch_1000hz":
                saveTweakState(context, "universal_1000hz_digitizer_lock", enabled);
                break;
            case "tcp_bbr":
                saveTweakState(context, "tcp_bbr_v3_congestion_control", enabled);
                break;
            case "phantom_freezer":
                saveTweakState(context, "android_phantom_freezer_kill", enabled);
                break;
            case "vulkan_skiavk":
                saveTweakState(context, "vulkan_skiavk_render_thread_rt", enabled);
                break;
            case "uclamp_boost":
                saveTweakState(context, "uclamp_cpuset_top_app_boost", enabled);
                break;
            case "memory_16kb":
                saveTweakState(context, "memory_16kb_vm_shield", enabled);
                break;
            case "data_saver":
                saveTweakState(context, "disable_data_saver", enabled);
                break;
            case "battery_saver":
                saveTweakState(context, "disable_battery_saver", enabled);
                break;
            case "wifi_saver":
                saveTweakState(context, "wifi_power_save_disable", enabled);
                break;
            case "wifi_low_latency":
                saveTweakState(context, "wifi7_low_latency_gaming_mode", enabled);
                break;
            case "art_speed":
                saveTweakState(context, "art_dexopt_speed_compile", enabled);
                break;
            case "game_driver":
                saveTweakState(context, "game_driver_mlbb_codm_pubgm_preference", enabled);
                break;
            case "adpf_engine":
                saveTweakState(context, "android_15_16_adpf_headroom_v2", enabled);
                break;
            default:
                break;
        }
    }
}
