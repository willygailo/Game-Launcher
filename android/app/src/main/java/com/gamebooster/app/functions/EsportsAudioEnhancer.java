package com.gamebooster.app.functions;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.gamebooster.app.root.CommandExecutor;

public class EsportsAudioEnhancer {

    private static final String TAG = "EsportsAudio";
    private static Equalizer equalizer = null;
    private static boolean isEnabled = false;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static boolean setEsportsAudioMode(Context context, boolean enable) {
        isEnabled = enable;
        if (enable) {
            return enableFootstepAudioBoost(context);
        } else {
            return disableAudioBoost();
        }
    }

    private static boolean enableFootstepAudioBoost(Context context) {
        try {
            // Apply system shell audio equalizer tuning
            CommandExecutor.executeSystemCommand("cmd media_session volume --stream 3 --set 15");
            CommandExecutor.executeSystemCommand("setprop persist.audio.soundfx.type 2");
            CommandExecutor.executeSystemCommand("setprop persist.audio.clarity 1");

            if (equalizer == null) {
                equalizer = new Equalizer(0, 0);
            }
            equalizer.setEnabled(true);

            short bands = equalizer.getNumberOfBands();
            for (short i = 0; i < bands; i++) {
                int centerFreq = equalizer.getCenterFreq(i); // In mHz
                // 2kHz - 4kHz (2,000,000 mHz - 4,000,000 mHz) = Footsteps & Gunshot Cues
                if (centerFreq >= 1500000 && centerFreq <= 4500000) {
                    short maxRange = equalizer.getBandLevelRange()[1];
                    equalizer.setBandLevel(i, (short) (maxRange * 0.8)); // Boost 80%
                }
            }
            Log.i(TAG, "⚡ Esports Footstep Audio Equalizer Enabled!");
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
            Log.i(TAG, "Esports Audio Equalizer Disabled.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
