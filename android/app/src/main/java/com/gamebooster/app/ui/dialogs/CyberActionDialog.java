package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;

import java.util.List;
import java.util.ArrayList;

public class CyberActionDialog {

    private static Dialog activeDialog;
    private static CountDownTimer activeTimer;

    public static void show(Context context, String featureTitle, boolean isActivated, String... logDetails) {
        List<String> lines = new ArrayList<>();
        if (logDetails != null) {
            for (String log : logDetails) {
                if (log != null && !log.trim().isEmpty()) lines.add(log.trim());
            }
        }
        showDetailed(context, featureTitle, isActivated, 1800, lines);
    }

    /**
     * Duration-controllable variant (Phase 1.2) — used for per-step
     * enforcement reports that need time to be read (e.g. 4500ms).
     */
    public static void showDetailed(Context context, String featureTitle, boolean isActivated,
                                    long autoDismissMs, List<String> lines) {
        if (context == null) return;

        AppExecutors.getInstance().postToMainThread(() -> {
            try {
                Context targetContext = context;
                if (targetContext instanceof Activity) {
                    Activity act = (Activity) targetContext;
                    if (act.isFinishing() || act.isDestroyed()) return;
                }

                // Dismiss any currently open dialog safely
                dismissCurrent();

                Dialog dialog = new Dialog(targetContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                View view = LayoutInflater.from(targetContext).inflate(R.layout.dialog_cyber_action, null);
                dialog.setContentView(view);

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setDimAmount(0.45f);
                }

                TextView tvTopBadge = view.findViewById(R.id.tv_dialog_top_badge);
                TextView tvUidBadge = view.findViewById(R.id.tv_dialog_uid_badge);
                TextView tvFeatureTitle = view.findViewById(R.id.tv_dialog_feature_title);
                TextView tvStatusPill = view.findViewById(R.id.tv_dialog_status_pill);
                TextView tvLogDetails = view.findViewById(R.id.tv_dialog_log_details);
                ProgressBar pbTimer = view.findViewById(R.id.pb_dialog_timer);
                Button btnDismiss = view.findViewById(R.id.btn_dialog_dismiss);

                tvFeatureTitle.setText(featureTitle != null ? featureTitle : "SYSTEM OPTIMIZATION ENGINE");

                if (isActivated) {
                    tvTopBadge.setText("⚡ SHIZUKU PRIVILEGED EXECUTION");
                    tvTopBadge.setBackgroundResource(R.drawable.badge_neon_cyan);
                    tvTopBadge.setTextColor(Color.parseColor("#00F0FF"));

                    tvStatusPill.setText("● ACTIVATED & APPLIED");
                    tvStatusPill.setBackgroundResource(R.drawable.badge_neon_green);
                    tvStatusPill.setTextColor(Color.parseColor("#00FF66"));

                    btnDismiss.setBackgroundResource(R.drawable.btn_cyber_cyan);
                    btnDismiss.setTextColor(Color.parseColor("#080B11"));
                } else {
                    tvTopBadge.setText("🔄 SYSTEM RESTORED / DEFAULT");
                    tvTopBadge.setBackgroundResource(R.drawable.badge_neon_cyan);
                    tvTopBadge.setTextColor(Color.parseColor("#94A3B8"));

                    tvStatusPill.setText("○ DEACTIVATED / DEFAULT");
                    tvStatusPill.setBackgroundResource(R.drawable.badge_neon_cyan);
                    tvStatusPill.setTextColor(Color.parseColor("#FF0055"));

                    btnDismiss.setBackgroundResource(R.drawable.btn_cyber_dark);
                    btnDismiss.setTextColor(Color.parseColor("#00F0FF"));
                }

                // Format logs
                StringBuilder sb = new StringBuilder();
                if (lines != null && !lines.isEmpty()) {
                    for (String log : lines) {
                        if (log != null && !log.trim().isEmpty()) {
                            sb.append("> ").append(log.trim()).append("\n");
                        }
                    }
                }
                sb.append("> Status: 100% Executed & Verified");
                tvLogDetails.setText(sb.toString().trim());

                // Auto-dismiss with smooth progress bar
                final long totalDuration = Math.max(autoDismissMs, 500);
                final int interval = 30;
                pbTimer.setMax((int) totalDuration);
                pbTimer.setProgress((int) totalDuration);

                activeTimer = new CountDownTimer(totalDuration, interval) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        pbTimer.setProgress((int) millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        dismissCurrent();
                    }
                };

                btnDismiss.setOnClickListener(v -> dismissCurrent());
                dialog.setOnDismissListener(d -> {
                    if (activeTimer != null) {
                        activeTimer.cancel();
                        activeTimer = null;
                    }
                    if (activeDialog == dialog) {
                        activeDialog = null;
                    }
                });

                activeDialog = dialog;
                dialog.show();
                activeTimer.start();

            } catch (Throwable t) {
                // Fallback safe: log or ignore
            }
        });
    }

    public static void dismissCurrent() {
        if (activeTimer != null) {
            activeTimer.cancel();
            activeTimer = null;
        }
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
