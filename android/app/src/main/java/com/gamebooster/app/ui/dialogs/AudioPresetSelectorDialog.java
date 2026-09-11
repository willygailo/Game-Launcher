package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.EsportsAudioEnhancer;
import com.gamebooster.app.core.AppExecutors;

public class AudioPresetSelectorDialog {

    private static Dialog activeDialog;

    public static void show(Context context) {
        if (context == null) return;

        AppExecutors.getInstance().postToMainThread(() -> {
            try {
                if (!(context instanceof Activity)) return;
                Activity act = (Activity) context;
                if (act.isFinishing() || act.isDestroyed()) return;

                if (activeDialog != null && activeDialog.isShowing()) {
                    try { activeDialog.dismiss(); } catch (Throwable ignored) {}
                }

                Dialog dialog = new Dialog(act);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                View view = LayoutInflater.from(context).inflate(R.layout.dialog_audio_preset, (ViewGroup) null, false);
                dialog.setContentView(view);

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setDimAmount(0.50f);
                }

                TextView tvStatus = view.findViewById(R.id.tv_audio_active_status);
                LinearLayout btnFootsteps = view.findViewById(R.id.btn_preset_footsteps);
                LinearLayout btnGunshots = view.findViewById(R.id.btn_preset_gunshots);
                LinearLayout btnSpatial = view.findViewById(R.id.btn_preset_spatial);
                LinearLayout btnVoice = view.findViewById(R.id.btn_preset_voice);

                TextView indFootsteps = view.findViewById(R.id.indicator_preset_footsteps);
                TextView indGunshots = view.findViewById(R.id.indicator_preset_gunshots);
                TextView indSpatial = view.findViewById(R.id.indicator_preset_spatial);
                TextView indVoice = view.findViewById(R.id.indicator_preset_voice);

                Button btnDismiss = view.findViewById(R.id.btn_audio_dismiss);

                Runnable updateSelectionUi = () -> {
                    EsportsAudioEnhancer.AudioPreset active = EsportsAudioEnhancer.getActivePreset(context);

                    if (indFootsteps != null) {
                        boolean isAct = active == EsportsAudioEnhancer.AudioPreset.FOOTSTEP_RADAR;
                        indFootsteps.setText(isAct ? "● ACTIVE" : "SELECT");
                        indFootsteps.setTextColor(isAct ? Color.parseColor("#00FF66") : Color.parseColor("#94A3B8"));
                    }
                    if (indGunshots != null) {
                        boolean isAct = active == EsportsAudioEnhancer.AudioPreset.GUNSHOT_LOCALIZATION;
                        indGunshots.setText(isAct ? "● ACTIVE" : "SELECT");
                        indGunshots.setTextColor(isAct ? Color.parseColor("#00FF66") : Color.parseColor("#94A3B8"));
                    }
                    if (indSpatial != null) {
                        boolean isAct = active == EsportsAudioEnhancer.AudioPreset.SPATIAL_VIRTUALIZER;
                        indSpatial.setText(isAct ? "● ACTIVE" : "SELECT");
                        indSpatial.setTextColor(isAct ? Color.parseColor("#00FF66") : Color.parseColor("#94A3B8"));
                    }
                    if (indVoice != null) {
                        boolean isAct = active == EsportsAudioEnhancer.AudioPreset.SQUAD_VOICE_CLARITY;
                        indVoice.setText(isAct ? "● ACTIVE" : "SELECT");
                        indVoice.setTextColor(isAct ? Color.parseColor("#00FF66") : Color.parseColor("#94A3B8"));
                    }
                };

                updateSelectionUi.run();

                View.OnClickListener makeListener = (v) -> {
                    EsportsAudioEnhancer.AudioPreset preset = EsportsAudioEnhancer.AudioPreset.FOOTSTEP_RADAR;
                    if (v == btnGunshots) preset = EsportsAudioEnhancer.AudioPreset.GUNSHOT_LOCALIZATION;
                    else if (v == btnSpatial) preset = EsportsAudioEnhancer.AudioPreset.SPATIAL_VIRTUALIZER;
                    else if (v == btnVoice) preset = EsportsAudioEnhancer.AudioPreset.SQUAD_VOICE_CLARITY;

                    final EsportsAudioEnhancer.AudioPreset sel = preset;
                    AppExecutors.getInstance().executeCommand(() -> {
                        EsportsAudioEnhancer.setActivePreset(context, sel);
                        EsportsAudioEnhancer.setEsportsAudioMode(context, true);
                        AppExecutors.getInstance().postToMainThread(() -> {
                            updateSelectionUi.run();
                            Toast.makeText(context, "🎧 Audio Equalizer: " + sel.title + " Locked!", Toast.LENGTH_SHORT).show();
                        });
                    });
                };

                if (btnFootsteps != null) btnFootsteps.setOnClickListener(makeListener);
                if (btnGunshots != null) btnGunshots.setOnClickListener(makeListener);
                if (btnSpatial != null) btnSpatial.setOnClickListener(makeListener);
                if (btnVoice != null) btnVoice.setOnClickListener(makeListener);

                if (btnDismiss != null) {
                    btnDismiss.setOnClickListener(v -> dialog.dismiss());
                }

                dialog.setCanceledOnTouchOutside(true);
                activeDialog = dialog;
                dialog.show();
            } catch (Throwable ignored) {}
        });
    }
}
