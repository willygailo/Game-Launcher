package com.gamebooster.app.booster;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

public class AudioBoosterEngine {

    private static final String TAG = "AudioBoosterEngine";
    private static Equalizer equalizerInstance = null;

    public enum SoundProfile {
        OFF("Standard Audio", "No EQ modification"),
        FOOTSTEP_AIM("Esports Footsteps & Aim", "Amplifies 1kHz-4kHz footstep frequency for PUBGM/CODM"),
        BASS_EXPLOSION("Bass & Explosion", "Deep bass response for cinematic action"),
        TREBLE_CLEAR("Clear Voice Chat", "Enhances squad voice clarity");

        public final String label;
        public final String description;

        SoundProfile(String label, String description) {
            this.label = label;
            this.description = description;
        }
    }

    public static String applyAudioProfile(Context context, SoundProfile profile) {
        if (context == null || profile == null) return "Invalid parameters";

        if (profile == SoundProfile.OFF) {
            if (equalizerInstance != null) {
                try {
                    equalizerInstance.setEnabled(false);
                    equalizerInstance.release();
                } catch (Exception ignored) {}
                equalizerInstance = null;
            }
            if (ShizukuExecutor.isShizukuAvailable()) {
                ShizukuExecutor.executeShizukuCommand("cmd media_session reset");
            }
            return "✅ Audio EQ disabled (System Standard)";
        }

        try {
            if (equalizerInstance == null) {
                equalizerInstance = new Equalizer(0, 0);
            }
            equalizerInstance.setEnabled(true);
            short numberOfBands = equalizerInstance.getNumberOfBands();

            short[] bandLevels = new short[numberOfBands];
            if (profile == SoundProfile.FOOTSTEP_AIM) {
                // Boost mid-high frequencies (1kHz - 4kHz footstep spectrum)
                for (short i = 0; i < numberOfBands; i++) {
                    int freq = equalizerInstance.getCenterFreq(i) / 1000;
                    if (freq >= 1000 && freq <= 4500) {
                        bandLevels[i] = (short) Math.min(equalizerInstance.getBandLevelRange()[1], 600); // +6dB
                    } else {
                        bandLevels[i] = 0;
                    }
                    equalizerInstance.setBandLevel(i, bandLevels[i]);
                }
            } else if (profile == SoundProfile.BASS_EXPLOSION) {
                for (short i = 0; i < numberOfBands; i++) {
                    int freq = equalizerInstance.getCenterFreq(i) / 1000;
                    if (freq <= 250) {
                        bandLevels[i] = (short) Math.min(equalizerInstance.getBandLevelRange()[1], 800); // +8dB
                    } else {
                        bandLevels[i] = 0;
                    }
                    equalizerInstance.setBandLevel(i, bandLevels[i]);
                }
            }

            // Route ADB sound priority tweaks if Shizuku available
            if (ShizukuExecutor.isShizukuAvailable()) {
                ShizukuExecutor.executeShizukuCommand("setprop persist.audio.game_mode 1");
                ShizukuExecutor.executeShizukuCommand("setprop debug.audio.latency 5");
            }

            Log.i(TAG, "Applied audio EQ profile: " + profile.label);
            return "⚡ Applied " + profile.label + " Audio EQ Profile";
        } catch (Exception e) {
            Log.e(TAG, "Error configuring Audio Equalizer", e);
            return "⚠️ Audio EQ hardware access limited (" + e.getMessage() + ")";
        }
    }
}
