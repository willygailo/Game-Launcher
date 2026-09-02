package com.gamebooster.app.booster;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;

public class EsportsAudioEnhancer {

    private static final String TAG = "EsportsAudio";
    private static final String PREF_NAME = "esports_audio_prefs";
    private static final String KEY_PRESET = "active_preset";
    private static final String KEY_ENABLED = "audio_boost_enabled";
    private static Equalizer equalizer = null;
    private static boolean isEnabled = false;

    public enum AudioPreset {
        FOOTSTEP_RADAR("Footstep Radar (2kHz–4kHz)", "Amplifies enemy footsteps, crawling, and equipment handling sounds in PUBG, CODM, and Blood Strike.", 1800000, 4200000, 0.95f),
        GUNSHOT_LOCALIZATION("Gunshot Localization (500Hz–1.5kHz)", "Sharpens gunshot trajectory and directionality for rapid 3D positioning.", 500000, 1600000, 0.85f),
        SPATIAL_VIRTUALIZER("3D Spatial Soundstage", "Expands ambient soundstage and clarifies high-frequency audio cues.", 1000000, 8000000, 0.75f),
        SQUAD_VOICE_CLARITY("Squad Voice Clarity (300Hz–3kHz)", "Boosts voice communications and discord squad audio over loud in-game explosions.", 300000, 3000000, 0.80f);

        public final String title;
        public final String description;
        public final int minFreqMhz;
        public final int maxFreqMhz;
        public final float boostPercent;

        AudioPreset(String title, String description, int minFreqMhz, int maxFreqMhz, float boostPercent) {
            this.title = title;
            this.description = description;
            this.minFreqMhz = minFreqMhz;
            this.maxFreqMhz = maxFreqMhz;
            this.boostPercent = boostPercent;
        }
    }

    public static boolean isEnabled(Context context) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            isEnabled = prefs.getBoolean(KEY_ENABLED, isEnabled);
        }
        return isEnabled;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static AudioPreset getActivePreset(Context context) {
        if (context == null) return AudioPreset.FOOTSTEP_RADAR;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(KEY_PRESET, AudioPreset.FOOTSTEP_RADAR.name());
        try {
            return AudioPreset.valueOf(name);
        } catch (Exception e) {
            return AudioPreset.FOOTSTEP_RADAR;
        }
    }

    public static void setActivePreset(Context context, AudioPreset preset) {
        if (context == null || preset == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PRESET, preset.name()).apply();
        if (isEnabled(context)) {
            applyPreset(context, preset);
        }
    }

    public static boolean setEsportsAudioMode(Context context, boolean enable) {
        isEnabled = enable;
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_ENABLED, enable).apply();
        }
        if (enable) {
            AudioPreset preset = getActivePreset(context);
            return applyPreset(context, preset);
        } else {
            return disableAudioBoost();
        }
    }

    public static boolean applyPreset(Context context, AudioPreset preset) {
        if (preset == null) preset = AudioPreset.FOOTSTEP_RADAR;
        try {
            // 1. Hardware Equalizer Session 0 (Global Output Mix)
            if (equalizer == null) {
                equalizer = new Equalizer(1000, 0);
            }
            equalizer.setEnabled(true);

            short bands = equalizer.getNumberOfBands();
            short[] range = equalizer.getBandLevelRange(); // e.g., [-1500, +1500] mB
            short maxBoost = range != null && range.length > 1 ? range[1] : (short) 1000;

            for (short i = 0; i < bands; i++) {
                int centerFreq = equalizer.getCenterFreq(i); // In mHz
                if (centerFreq >= preset.minFreqMhz && centerFreq <= preset.maxFreqMhz) {
                    equalizer.setBandLevel(i, (short) (maxBoost * preset.boostPercent));
                } else {
                    equalizer.setBandLevel(i, (short) 0);
                }
            }

            // 2. System and Vendor Property Level Audio Optimizations
            CommandExecutor.executeSystemCommand("setprop persist.audio.soundfx.type 2");
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 1");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.audio.game.mode 1");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.audio.spatializer.mode 1");
            CommandExecutor.executeSystemCommand("cmd media_session volume --stream 3 --set 15");

            Log.i(TAG, "⚡ Esports Audio Preset Applied: " + preset.title + " (Bands=" + bands + ")");
            return true;
        } catch (Throwable e) {
            Log.w(TAG, "AudioEffect Equalizer fallback: " + e.getMessage());
            CommandExecutor.executeSystemCommand("settings put system sound_effects_enabled 1");
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 1");
            return true;
        }
    }

    public static boolean disableAudioBoost() {
        try {
            if (equalizer != null) {
                try { equalizer.setEnabled(false); } catch (Throwable ignored) {}
                try { equalizer.release(); } catch (Throwable ignored) {}
                equalizer = null;
            }
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 0");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.audio.game.mode 0");
            Log.i(TAG, "Esports Audio Equalizer Disabled.");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
