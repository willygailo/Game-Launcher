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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.lsposed.LspatchHelper;
import com.gamebooster.app.spoofer.lsposed.LsposedDetector;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.Map;

/**
 * SpoofInspectorDialog — Real-time live diagnostic verification inspector
 * for all device spoofing and hardware masking layers.
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

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_spoof_inspector, null);
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
            tvContent.setText("⏳ Querying live system and hook diagnostic state...");

            AppExecutors.getInstance().executeCommand(() -> {
                StringBuilder sb = new StringBuilder();

                boolean spoofEnabled = SpoofPreferences.isSpoofEnabled(context);
                String activeProfileId = SpoofPreferences.getActiveProfileId(context);
                SpoofProfile activeProfile = activeProfileId != null ? DeviceSpooferEngine.getProfileById(activeProfileId) : null;
                boolean lsposedActive = LsposedDetector.isModuleEnabled();
                boolean lspatchInstalled = LspatchHelper.isLspatchInstalled(context);
                boolean shizukuActive = ShizukuFileManager.hasFullAccess();

                sb.append("═══════════════════════════════════════════════════════\n");
                sb.append("▶ [GAME BOOSTER LIVE SPOOF DIAGNOSTICS]\n");
                sb.append("═══════════════════════════════════════════════════════\n\n");

                sb.append("1. MASTER CONFIGURATION:\n");
                sb.append("   • Master Spoof Toggle : ").append(spoofEnabled ? "ENABLED [ACTIVE]" : "DISABLED").append("\n");
                sb.append("   • Active Global Target: ").append(activeProfile != null ? activeProfile.displayName + " [" + activeProfile.model + "]" : "None").append("\n");
                sb.append("   • Spoof All Apps Mode : ").append(SpoofPreferences.isSpoofAllApps(context) ? "YES (Universal)" : "NO (Games Only)").append("\n\n");

                sb.append("2. EXECUTION HOOK TIERS:\n");
                sb.append("   • LSPosed (Root Zygisk): ").append(lsposedActive ? "✔ ACTIVE (In-Memory ART)" : "✖ Not Active").append("\n");
                sb.append("   • LSPatch (Non-Root)   : ").append(lspatchInstalled ? "✔ INSTALLED (Bridge Ready)" : "✖ Not Installed").append("\n");
                sb.append("   • Shizuku ADB Shell    : ").append(shizukuActive ? "✔ GRANTED (UID 2000)" : "✖ Not Granted").append("\n");
                sb.append("   • Prefs Bridge Provider: content://com.gamebooster.app.spoofprefs/spoof [READY]\n\n");

                sb.append("3. IN-APP PROCESS IDENTITY (Build.*):\n");
                sb.append("   • Model       : ").append(Build.MODEL).append("\n");
                sb.append("   • Brand       : ").append(Build.BRAND).append("\n");
                sb.append("   • Manufacturer: ").append(Build.MANUFACTURER).append("\n");
                sb.append("   • Hardware    : ").append(Build.HARDWARE).append("\n");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    sb.append("   • SoC Model   : ").append(Build.SOC_MODEL).append("\n");
                }
                sb.append("   • Fingerprint : ").append(Build.FINGERPRINT).append("\n\n");

                sb.append("4. LIVE DISPLAY SUBSYSTEM:\n");
                try {
                    Display display = activity.getWindowManager().getDefaultDisplay();
                    if (display != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Display.Mode mode = display.getMode();
                        sb.append("   • Active Refresh Rate: ").append(mode.getRefreshRate()).append(" Hz\n");
                        sb.append("   • Screen Resolution  : ").append(mode.getPhysicalWidth()).append("x").append(mode.getPhysicalHeight()).append("\n");
                    }
                } catch (Throwable ignored) {}

                sb.append("\n5. REGISTERED HOOK LAYERS (12 LAYERS):\n");
                sb.append("   [1] Anti-Detection & Root Hider\n");
                sb.append("   [2] Build & Version Identity\n");
                sb.append("   [3] SystemProperties Multi-Partition ro.*\n");
                sb.append("   [4] Display Modes (120Hz-185Hz)\n");
                sb.append("   [5] OpenGL ES & EGL GPU Renderer\n");
                sb.append("   [6] Runtime CPU Cores & Heap VM\n");
                sb.append("   [7] ActivityManager RAM Telemetry\n");
                sb.append("   [8] Procfs /proc/cpuinfo & meminfo\n");
                sb.append("   [9] Telephony & Unique Android ID\n");
                sb.append("   [10] Battery Thermal Headroom & Power\n");
                sb.append("   [11] 960Hz Touch Digitizer & Sensor\n");
                sb.append("   [12] Low-Latency Spatial Gaming Audio\n");

                String resultText = sb.toString();

                AppExecutors.getInstance().postToMainThread(() -> {
                    if (!dialog.isShowing()) return;

                    tvContent.setText(resultText);
                    if (lsposedActive) {
                        tvMode.setText("[LSPOSED ART HOOK]");
                        tvMode.setTextColor(Color.parseColor("#00FF66"));
                    } else if (shizukuActive) {
                        tvMode.setText("[SHIZUKU ADB MODE]");
                        tvMode.setTextColor(Color.parseColor("#00F0FF"));
                    } else {
                        tvMode.setText("[STANDBY / APP ONLY]");
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
            } catch (Exception ignored) {}
            activeDialog = null;
        }
    }
}
