package com.gamebooster.app.core;

import com.gamebooster.app.feature.performance.display.EngineMode;

import android.widget.TextView;

import com.gamebooster.app.feature.performance.display.EngineMode;
import com.gamebooster.app.platform.shell.CommandExecutor;

public class EngineUIHelper {

    public static void refreshEngineStatus(TextView tvStatus) {
        if (tvStatus == null) return;
        EngineMode mode = CommandExecutor.getActiveEngineMode();
        tvStatus.setText("Active Engine: " + mode.getDisplayName());
        tvStatus.setTextColor(mode.getColorHex());
    }
}
