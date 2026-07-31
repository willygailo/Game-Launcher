package com.gamespace.app.utils;

import android.widget.TextView;

import com.gamespace.app.core.EngineMode;
import com.gamespace.app.data.CommandExecutor;

public class EngineUIHelper {

    public static void refreshEngineStatus(TextView tvStatus) {
        if (tvStatus == null) return;
        EngineMode mode = CommandExecutor.getActiveEngineMode();
        tvStatus.setText("Active Engine: " + mode.getDisplayName());
        tvStatus.setTextColor(mode.getColorHex());
    }
}
