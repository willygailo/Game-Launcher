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
    private static Equalizer equalizer = null;
    private static boolean isEnabled = false;

    public enum AudioPreset {
        FOOTSTEP_RADAR("Footstep Radar (2kHz–4kHz)", "Amplifies enemy footsteps, crawling, and equipment handling sounds in PUBG, CODM, and Blood Strike.", 1800000, 4200000, 0.90f),
        GUNSHOT_LOCALIZATION("Gunshot Localization (500Hz–1.5kHz)", "Sharpens gunshot trajectory and directionality for rapid 3D positioning.", 500000, 1600000, 0.85f),
        SPATIAL_VIRTUALIZER("3D Spatial Soundstage", "Expands ambient soundstage and clarifies high-frequency audio cues.", 1000000, 8000000, 0.70f),
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
        if (isEnabled) {
            applyPreset(context, preset);
        }
    }

    public static boolean setEsportsAudioMode(Context context, boolean enable) {
        isEnabled = enable;
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
            // Apply system shell audio equalizer tuning
            CommandExecutor.executeSystemCommand("cmd media_session volume --stream 3 --set 15");
            CommandExecutor.executeSystemCommand("setprop persist.audio.soundfx.type 2");
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 1");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.audio.game.mode 1");

            if (equalizer == null) {
                equalizer = new Equalizer(0, 0);
            }
            equalizer.setEnabled(true);

            short bands = equalizer.getNumberOfBands();
            for (short i = 0; i < bands; i++) {
                int centerFreq = equalizer.getCenterFreq(i); // In mHz
                if (centerFreq >= preset.minFreqMhz && centerFreq <= preset.maxFreqMhz) {
                    short maxRange = equalizer.getBandLevelRange()[1];
                    equalizer.setBandLevel(i, (short) (maxRange * preset.boostPercent));
                } else {
                    equalizer.setBandLevel(i, (short) 0);
                }
            }
            Log.i(TAG, "⚡ Esports Audio Preset Applied: " + preset.title);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "AudioEffect API fallback to shell tuning: " + e.getMessage());
            CommandExecutor.executeSystemCommand("settings put system sound_effects_enabled 1");
            return true;
        }
    }

    private static boolean disableAudioBoost() {
        try {
            if (equalizer != null) {
                equalizer.setEnabled(false);
                equalizer.release();
                equalizer = null;
            }
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 0");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.audio.game.mode 0");
            Log.i(TAG, "Esports Audio Equalizer Disabled.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
