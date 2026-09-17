package com.gamebooster.app.booster;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.PrivilegeBridgeEngine;

import java.util.ArrayList;
import java.util.List;

public class EsportsAudioEnhancer {

    private static final String TAG = "EsportsAudio";
    private static final String PREF_NAME = "esports_audio_prefs";
    private static final String KEY_PRESET = "active_preset";
    private static final String KEY_ENABLED = "audio_boost_enabled";
    private static final String KEY_SPEAKER_BYPASS = "speaker_bypass_boost_enabled";

    private static Equalizer equalizer = null;
    private static LoudnessEnhancer loudnessEnhancer = null;
    private static BassBoost bassBoost = null;

    private static boolean isEnabled = false;
    private static boolean isSpeakerBypassEnabled = false;

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

    public static boolean isSpeakerBypassEnabled(Context context) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            isSpeakerBypassEnabled = prefs.getBoolean(KEY_SPEAKER_BYPASS, false);
        }
        return isSpeakerBypassEnabled;
    }

    public static boolean isSpeakerBypassEnabled() {
        return isSpeakerBypassEnabled;
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

    public static boolean setSpeakerBypassMode(Context context, boolean enable) {
        isSpeakerBypassEnabled = enable;
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_SPEAKER_BYPASS, enable).apply();
        }
        if (enable) {
            return applySpeakerBypassBoost(context);
        } else {
            return disableSpeakerBypassBoost();
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

    public static boolean applySpeakerBypassBoost(Context context) {
        try {
            // 1. Android DSP LoudnessEnhancer (API 19+) for Hardware Speaker Overdrive
            try {
                if (loudnessEnhancer == null) {
                    loudnessEnhancer = new LoudnessEnhancer(0);
                }
                loudnessEnhancer.setTargetGain(1500); // +15 dB overdrive gain
                loudnessEnhancer.setEnabled(true);
            } catch (Throwable t) {
                Log.w(TAG, "LoudnessEnhancer DSP init: " + t.getMessage());
            }

            // 2. Hardware BassBoost for deep spatial impact
            try {
                if (bassBoost == null) {
                    bassBoost = new BassBoost(1000, 0);
                }
                if (bassBoost.getStrengthSupported()) {
                    bassBoost.setStrength((short) 600); // 60% punchy bass
                }
                bassBoost.setEnabled(true);
            } catch (Throwable t) {
                Log.w(TAG, "BassBoost DSP init: " + t.getMessage());
            }

            // 3. Android System Safe Volume & Audio Limit Bypass (Root / Shizuku)
            List<String> bypassCommands = new ArrayList<>();
            bypassCommands.add("settings put global audio_safe_volume_state 2");
            bypassCommands.add("settings put secure unsafe_volume_music_active 1");
            bypassCommands.add("setprop audio.safemedia.bypass true");
            bypassCommands.add("setprop audio.safemedia.force 0");

            // 4. Hardware Loudspeaker DRC (Dynamic Range Compression) Elimination
            // Bypasses speaker clipping threshold to prevent footstep audio muffle during gunfights
            bypassCommands.add("setprop persist.vendor.audio.speaker.drc 0");
            bypassCommands.add("setprop persist.audio.speaker.boost 1");
            bypassCommands.add("setprop persist.audio.hifi.volume 1");
            bypassCommands.add("setprop persist.vendor.audio.hifi 1");
            bypassCommands.add("setprop persist.audio.fluence.speaker false");

            // 5. Low Latency FastTrack Audio HAL Dispatch
            bypassCommands.add("setprop audio.deep_buffer.media false");
            bypassCommands.add("setprop ro.audio.flinger_standbytime_ms 100");
            bypassCommands.add("setprop af.fast_track_multiplier 1");
            bypassCommands.add("setprop persist.sys.audio.latency 0");
            bypassCommands.add("setprop persist.audio.vr.enable 1");
            bypassCommands.add("setprop persist.vendor.audio.spatial.latency 0");
            bypassCommands.add("cmd media_session volume --stream 3 --set 15");

            PrivilegeBridgeEngine.executePrivilegedBatch(bypassCommands);

            // 6. Maximize Stream Volume if context is available
            if (context != null) {
                try {
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    if (am != null) {
                        int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
                    }
                } catch (Throwable ignored) {}
            }

            Log.i(TAG, "🔊 Speaker Audio Limiter Bypassed & Hardware Loudness Boost (+15dB) Applied");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply speaker bypass: " + e.getMessage(), e);
            return false;
        }
    }

    public static boolean disableSpeakerBypassBoost() {
        try {
            if (loudnessEnhancer != null) {
                try { loudnessEnhancer.setEnabled(false); } catch (Throwable ignored) {}
                try { loudnessEnhancer.release(); } catch (Throwable ignored) {}
                loudnessEnhancer = null;
            }
            if (bassBoost != null) {
                try { bassBoost.setEnabled(false); } catch (Throwable ignored) {}
                try { bassBoost.release(); } catch (Throwable ignored) {}
                bassBoost = null;
            }
            List<String> restoreCommands = new ArrayList<>();
            restoreCommands.add("setprop persist.vendor.audio.speaker.drc 1");
            restoreCommands.add("setprop persist.audio.speaker.boost 0");
            restoreCommands.add("setprop audio.deep_buffer.media true");
            PrivilegeBridgeEngine.executePrivilegedBatch(restoreCommands);
            Log.i(TAG, "Speaker Audio Bypass Disabled.");
            return true;
        } catch (Throwable e) {
            return false;
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
