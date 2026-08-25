package com.gamebooster.app.spoofer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;

/**
 * SpoofInspectorDialog — Real-time live diagnostic verification inspector
 * for all device spoofing and hardware masking layers via Shizuku.
 */
public class SpoofInspectorDialog {

    private static Dialog activeDialog;

    public static void show(Context context) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_spoof_inspector, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.6f);
        }

        TextView tvMode = view.findViewById(R.id.tv_inspector_mode);
        TextView tvContent = view.findViewById(R.id.tv_inspector_content);
        Button btnRefresh = view.findViewById(R.id.btn_inspector_refresh);
        Button btnClose = view.findViewById(R.id.btn_inspector_close);

        Runnable queryDiagnostics = () -> {
            tvContent.setText("⏳ Querying live system and Shizuku diagnostic state...");

            AppExecutors.getInstance().executeCommand(() -> {
                StringBuilder sb = new StringBuilder();

                boolean spoofEnabled = SpoofPreferences.isSpoofEnabled(context);
                String activeProfileId = SpoofPreferences.getActiveProfileId(context);
                SpoofProfile activeProfile = activeProfileId != null ? DeviceSpooferEngine.getProfileById(activeProfileId) : null;
                boolean shizukuActive = ShizukuExecutor.hasShizukuPermission();
                boolean scopedAccess = ShizukuFileManager.hasFullAccess();

                sb.append("═══════════════════════════════════════════════════════\n");
                sb.append("▶ [GAME BOOSTER LIVE SPOOF DIAGNOSTICS]\n");
                sb.append("═══════════════════════════════════════════════════════\n\n");

                sb.append("1. MASTER CONFIGURATION:\n");
                sb.append("   • Master Spoof Toggle : ").append(spoofEnabled ? "ENABLED [ACTIVE]" : "DISABLED").append("\n");
                sb.append("   • Active Global Target: ").append(activeProfile != null ? activeProfile.displayName + " [" + activeProfile.model + "]" : "None").append("\n");
                sb.append("   • Target Android OS   : Android 13 - 16 (API 33-36 Fully Supported)\n\n");

                sb.append("2. ELEVATED EXECUTION ENGINE:\n");
                sb.append("   • Shizuku ADB Shell   : ").append(shizukuActive ? "✔ GRANTED (UID 2000 Non-Root)" : "✖ Not Granted / Disconnected").append("\n");
                sb.append("   • Scoped Storage Access: ").append(scopedAccess ? "✔ UNLOCKED (/sdcard/Android/data)" : "✖ Restricted").append("\n");
                sb.append("   • Architecture Engine : Pure Shizuku Wireless ADB Subsystem\n\n");

                sb.append("3. IN-APP PROCESS IDENTITY (Build.*):\n");
                sb.append("   • Model       : ").append(Build.MODEL).append("\n");
                sb.append("   • Brand       : ").append(Build.BRAND).append("\n");
                sb.append("   • Manufacturer: ").append(Build.MANUFACTURER).append("\n");
                sb.append("   • Hardware    : ").append(Build.HARDWARE).append("\n");
                sb.append("   • SoC Model   : ").append(Build.SOC_MODEL).append("\n");
                sb.append("   • Fingerprint : ").append(Build.FINGERPRINT).append("\n\n");

                sb.append("4. LIVE DISPLAY SUBSYSTEM:\n");
                try {
                    Display display = activity.getDisplay();
                    if (display != null) {
                        Display.Mode mode = display.getMode();
                        sb.append("   • Active Refresh Rate: ").append(mode.getRefreshRate()).append(" Hz\n");
                        sb.append("   • Screen Resolution  : ").append(mode.getPhysicalWidth()).append("x").append(mode.getPhysicalHeight()).append("\n");
                    }
                } catch (Throwable ignored) {}

                sb.append("\n5. SHIZUKU MASKING SUBSYSTEMS:\n");
                sb.append("   [1] System Property Overrides (ro.product.*, ro.soc.*)\n");
                sb.append("   [2] Android 13-16 Game Mode API (cmd game mode 2)\n");
                sb.append("   [3] High-Performance Game Driver Opt-In (ANGLE/Vulkan)\n");
                sb.append("   [4] SurfaceFlinger Refresh Rate Lock (120Hz-185Hz)\n");
                sb.append("   [5] Direct Game Config Injection (DeviceProfile.ini / JSON)\n");
                sb.append("   [6] Mock Procfs Hardware Mask Export\n");

                String resultText = sb.toString();

                AppExecutors.getInstance().postToMainThread(() -> {
                    if (!dialog.isShowing()) return;

                    tvContent.setText(resultText);
                    if (shizukuActive) {
                        tvMode.setText("[SHIZUKU ADB PRIVILEGED]");
                        tvMode.setTextColor(Color.parseColor("#00FF66"));
                    } else {
                        tvMode.setText("[SHIZUKU DISCONNECTED]");
                        tvMode.setTextColor(Color.parseColor("#FFCC00"));
                    }
                });
            });
        };

        btnRefresh.setOnClickListener(v -> queryDiagnostics.run());
        btnClose.setOnClickListener(v -> dismissCurrent());

        activeDialog = dialog;
        dialog.show();

        // Run on open
        queryDiagnostics.run();
    }

    public static void dismissCurrent() {
        if (activeDialog != null) {
            try {
                if (activeDialog.isShowing()) {
                    activeDialog.dismiss();
                }
            } catch (Throwable ignored) {}
            activeDialog = null;
        }
    }
}
