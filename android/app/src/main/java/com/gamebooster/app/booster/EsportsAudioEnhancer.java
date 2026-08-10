package com.gamebooster.app.booster;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class EsportsAudioEnhancer {

    private static final String TAG = "EsportsAudio";
    private static Equalizer equalizer = null;
    private static AudioPreset currentPreset = AudioPreset.OFF;

    public enum AudioPreset {
        FOOTSTEP_BOOST("Footstep Cue Boost"),
        ESPORTS_BALANCED("eSports Balanced Clarity"),
        BASS_BOOST("Explosion Bass Boost"),
        OFF("Stock Audio");

        public final String label;

        AudioPreset(String label) {
            this.label = label;
        }
    }

    public static AudioPreset getCurrentPreset() {
        return currentPreset;
    }

    public static boolean isEnabled() {
        return currentPreset != AudioPreset.OFF;
    }

    public static boolean applyAudioPreset(Context context, AudioPreset preset) {
        currentPreset = preset;

        if (preset == AudioPreset.OFF) {
            return disableAudioBoost();
        }

        try {
            // Apply system shell audio equalizer tuning
            CommandExecutor.executeSystemCommand("cmd media_session volume --stream 3 --set 15");
            CommandExecutor.executeSystemCommand("setprop persist.audio.soundfx.type 2");
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 1");
            ShizukuExecutor.executeShizukuCommand("setprop persist.audio.soundfx.type 2");
            ShizukuExecutor.executeShizukuCommand("setprop persist.audio.clarity 1");

            if (equalizer == null) {
                equalizer = new Equalizer(0, 0);
            }
            equalizer.setEnabled(true);

            short bands = equalizer.getNumberOfBands();
            short[] range = equalizer.getBandLevelRange();
            short maxRange = range[1];

            for (short i = 0; i < bands; i++) {
                int centerFreq = equalizer.getCenterFreq(i); // In mHz

                switch (preset) {
                    case FOOTSTEP_BOOST:
                        // 1.5kHz - 4.5kHz = Footsteps & Gunshot Cues
                        if (centerFreq >= 1500000 && centerFreq <= 4500000) {
                            equalizer.setBandLevel(i, (short) (maxRange * 0.85)); // Boost 85%
                        } else {
                            equalizer.setBandLevel(i, (short) 0);
                        }
                        break;

                    case ESPORTS_BALANCED:
                        // Mild clarity boost on high frequencies
                        if (centerFreq >= 3000000) {
                            equalizer.setBandLevel(i, (short) (maxRange * 0.40));
                        } else {
                            equalizer.setBandLevel(i, (short) 0);
                        }
                        break;

                    case BASS_BOOST:
                        // 60Hz - 250Hz = Low end bass
                        if (centerFreq <= 250000) {
                            equalizer.setBandLevel(i, (short) (maxRange * 0.90));
                        } else {
                            equalizer.setBandLevel(i, (short) 0);
                        }
                        break;

                    default:
                        equalizer.setBandLevel(i, (short) 0);
                        break;
                }
            }
            Log.i(TAG, "⚡ Esports Audio Preset Applied: " + preset.label);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "AudioEffect API fallback to shell tuning: " + e.getMessage());
            CommandExecutor.executeSystemCommand("settings put system sound_effects_enabled 1");
            return true;
        }
    }

    public static boolean setEsportsAudioMode(Context context, boolean enable) {
        return applyAudioPreset(context, enable ? AudioPreset.FOOTSTEP_BOOST : AudioPreset.OFF);
    }

    private static boolean disableAudioBoost() {
        try {
            if (equalizer != null) {
                equalizer.setEnabled(false);
                equalizer.release();
                equalizer = null;
            }
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 0");
            ShizukuExecutor.executeShizukuCommand("setprop persist.audio.clarity 0");
            currentPreset = AudioPreset.OFF;
            Log.i(TAG, "Esports Audio Equalizer Disabled.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
