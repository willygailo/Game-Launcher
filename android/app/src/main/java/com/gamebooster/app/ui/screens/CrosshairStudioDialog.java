package com.gamebooster.app.ui.screens;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gamebooster.app.R;
import com.gamebooster.app.overlay.CrosshairOverlayService;

public class CrosshairStudioDialog extends DialogFragment {

    public static CrosshairStudioDialog newInstance() {
        return new CrosshairStudioDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(0xFF0F172A);

        Button btnToggleCrosshair = new Button(requireContext());
        btnToggleCrosshair.setText("🎯 TOGGLE PRECISION CROSSHAIR OVERLAY");
        btnToggleCrosshair.setTextColor(0xFF000000);
        btnToggleCrosshair.setBackgroundColor(0xFF00F0FF);
        btnToggleCrosshair.setTypeface(null, android.graphics.Typeface.BOLD);

        btnToggleCrosshair.setOnClickListener(v -> {
            Context context = getContext();
            if (context != null) {
                if (CrosshairOverlayService.isRunning()) {
                    CrosshairOverlayService.stopOverlay(context);
                    Toast.makeText(context, "Crosshair Overlay Stopped", Toast.LENGTH_SHORT).show();
                } else {
                    CrosshairOverlayService.startOverlay(context);
                    Toast.makeText(context, "🎯 Precision Crosshair Target Active!", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        });

        layout.addView(btnToggleCrosshair);

        builder.setView(layout)
                .setTitle("🎯 Reticle Target Studio")
                .setNegativeButton("Close", (dialog, id) -> dismiss());

        return builder.create();
    }
}
