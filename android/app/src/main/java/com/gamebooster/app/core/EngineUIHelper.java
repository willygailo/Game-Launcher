package com.gamebooster.app.core;

import com.gamebooster.app.engine.EngineMode;

import android.widget.TextView;

import com.gamebooster.app.engine.EngineMode;
import com.gamebooster.app.engine.CommandExecutor;

public class EngineUIHelper {

    public static void refreshEngineStatus(TextView tvStatus) {
        if (tvStatus == null) return;
        EngineMode mode = CommandExecutor.getActiveEngineMode();
        tvStatus.setText("Active Engine: " + mode.getDisplayName());
        tvStatus.setTextColor(mode.getColorHex());
    }
}
