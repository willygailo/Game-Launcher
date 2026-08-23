package com.gamebooster.app.anticheat.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.gamebooster.app.R;
import com.gamebooster.app.anticheat.AntiCheatShieldEngine;
import com.gamebooster.app.anticheat.GameAntiCheatRegistry;
import com.gamebooster.app.core.AppExecutors;

import java.util.Map;

/**
 * AntiCheatInspectorDialog — Live diagnostic inspection and audit dialog
 * for Anti-Cheat detection protection, in-memory cloaking, and file integrity.
 */
public class AntiCheatInspectorDialog {

    private static Dialog activeDialog;

    public static void show(Context context, String targetPackage) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_anticheat_inspector, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.6f);
        }

        TextView tvStatusBadge = view.findViewById(R.id.tv_ac_status_badge);
        TextView tvOutput = view.findViewById(R.id.tv_ac_terminal_output);
        Button btnRescan = view.findViewById(R.id.btn_ac_rescan);
        Button btnClose = view.findViewById(R.id.btn_ac_close);

        String pkg = (targetPackage != null && !targetPackage.isEmpty()) ? targetPackage : "com.mobile.legends";

        Runnable runAudit = () -> {
            tvOutput.setText("⏳ Auditing anti-detection shield and game integrity...");

            AppExecutors.getInstance().executeCommand(() -> {
                AntiCheatShieldEngine.AntiCheatAuditResult result =
                        AntiCheatShieldEngine.runSecurityAudit(context, pkg);

                StringBuilder sb = new StringBuilder();
                sb.append("═══════════════════════════════════════════════════════\n");
                sb.append("🛡️ [ANTI-CHEAT & INTEGRITY SHIELD AUDIT]\n");
                sb.append("═══════════════════════════════════════════════════════\n\n");

                sb.append("1. TARGET APPLICATION:\n");
                sb.append("   • Package Title   : ").append(result.securityProfile.gameTitle).append("\n");
                sb.append("   • Package Name    : ").append(result.packageName).append("\n");
                sb.append("   • Detected Engine : ").append(result.securityProfile.antiCheatType.displayName).append("\n");
                sb.append("   • Strictness Tier : Tier ").append(result.securityProfile.antiCheatType.strictnessLevel).append("/3\n\n");

                sb.append("2. ANTI-DETECTION POSTURE:\n");
                sb.append("   • LSPosed (In-Memory Hook) : ").append(result.lsposedActive ? "✔ ACTIVE (Zero File Tampering)" : "✖ Not Active").append("\n");
                sb.append("   • LSPatch (Non-Root Bridge): ").append(result.lspatchInstalled ? "✔ INSTALLED (Bridge Ready)" : "✖ Not Installed").append("\n");
                sb.append("   • Shizuku ADB Protection   : ").append(result.shizukuGranted ? "✔ GRANTED (UID 2000)" : "✖ Not Granted").append("\n");
                sb.append("   • Telemetry Sinkhole       : ").append(result.telemetrySinkholed ? "✔ ACTIVE (Log Buffers Cleared)" : "✖ Inactive").append("\n");
                sb.append("   • Inode Timestamp Retention: ").append(result.fileIntegrityPreserved ? "✔ ENABLED (utimensat/touch)" : "✖ Inactive").append("\n\n");

                sb.append("3. PROTECTED TELEMETRY SINKS:\n");
                if (result.securityProfile.telemetryEndpoints.length > 0) {
                    for (String ep : result.securityProfile.telemetryEndpoints) {
                        sb.append("   - [BLOCKED] ").append(ep).append("\n");
                    }
                } else {
                    sb.append("   - Standard Crashlytics & ANR buffers suppressed\n");
                }

                sb.append("\n4. SAFETY VERDICT:\n");
                sb.append("   ").append(result.safetyRecommendation).append("\n");

                String text = sb.toString();

                AppExecutors.getInstance().postToMainThread(() -> {
                    if (!dialog.isShowing()) return;

                    tvOutput.setText(text);
                    if (result.lsposedActive) {
                        tvStatusBadge.setText("[100% STEALTH]");
                        tvStatusBadge.setTextColor(Color.parseColor("#00FF66"));
                    } else if (result.shizukuGranted) {
                        tvStatusBadge.setText("[SHIZUKU SHIELD]");
                        tvStatusBadge.setTextColor(Color.parseColor("#00F0FF"));
                    } else {
                        tvStatusBadge.setText("[SAFE MODE]");
                        tvStatusBadge.setTextColor(Color.parseColor("#FFCC00"));
                    }
                });
            });
        };

        btnRescan.setOnClickListener(v -> runAudit.run());
        btnClose.setOnClickListener(v -> dismissCurrent());

        activeDialog = dialog;
        dialog.show();

        runAudit.run();
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
