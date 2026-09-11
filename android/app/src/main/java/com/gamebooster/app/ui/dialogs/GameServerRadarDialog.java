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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.GameServerPingRadar;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.core.AppExecutors;

import java.util.List;

public class GameServerRadarDialog {

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

                View view = LayoutInflater.from(context).inflate(R.layout.dialog_game_server_radar, (ViewGroup) null, false);
                dialog.setContentView(view);

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setDimAmount(0.50f);
                }

                TextView tvPacketLoss = view.findViewById(R.id.tv_radar_packet_loss);
                LinearLayout container = view.findViewById(R.id.container_server_ping_results);
                ProgressBar pbLoading = view.findViewById(R.id.pb_radar_loading);
                Button btnRetest = view.findViewById(R.id.btn_radar_retest);
                Button btnBoostNow = view.findViewById(R.id.btn_radar_boost_now);
                Button btnDismiss = view.findViewById(R.id.btn_radar_dismiss);

                Runnable runRadar = () -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
                    if (container != null) container.removeAllViews();

                    AppExecutors.getInstance().executeCommand(() -> {
                        List<GameServerPingRadar.PingResult> results = GameServerPingRadar.pingAllServers();

                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                            if (container == null || context == null) return;

                            int totalLoss = 0;
                            long avgJitter = 0;

                            for (GameServerPingRadar.PingResult r : results) {
                                totalLoss += r.packetLossPercent;
                                avgJitter += r.jitterMs;

                                // Create styled row view
                                LinearLayout row = new LinearLayout(context);
                                row.setOrientation(LinearLayout.VERTICAL);
                                row.setBackgroundResource(R.drawable.card_glass_shape);
                                row.setPadding(32, 24, 32, 24);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(0, 0, 0, 16);
                                row.setLayoutParams(lp);

                                LinearLayout topRow = new LinearLayout(context);
                                topRow.setOrientation(LinearLayout.HORIZONTAL);

                                TextView tvName = new TextView(context);
                                tvName.setText(r.gameTitle);
                                tvName.setTextColor(Color.WHITE);
                                tvName.setTextSize(13);
                                tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                                LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                                topRow.addView(tvName, nameLp);

                                TextView tvMs = new TextView(context);
                                tvMs.setText(r.reachable ? (r.latencyMs + " ms") : "TIMEOUT");
                                tvMs.setTextColor(r.getQualityColor());
                                tvMs.setTextSize(14);
                                tvMs.setTypeface(null, android.graphics.Typeface.BOLD);
                                topRow.addView(tvMs);

                                row.addView(topRow);

                                TextView tvSub = new TextView(context);
                                tvSub.setText(r.region + " • " + r.getQualityTier() + " (Jitter: " + r.jitterMs + "ms • Loss: " + r.packetLossPercent + "%)");
                                tvSub.setTextColor(Color.parseColor("#94A3B8"));
                                tvSub.setTextSize(10);
                                tvSub.setPadding(0, 8, 0, 0);
                                row.addView(tvSub);

                                container.addView(row);
                            }

                            if (tvPacketLoss != null && !results.isEmpty()) {
                                int overallLoss = totalLoss / results.size();
                                long overallJitter = avgJitter / results.size();
                                tvPacketLoss.setText(overallLoss + "% Loss • " + overallJitter + "ms Jitter");
                                tvPacketLoss.setTextColor(overallLoss == 0 ? Color.parseColor("#00FF66") : Color.parseColor("#FACC15"));
                            }
                        });
                    });
                };

                runRadar.run();

                if (btnRetest != null) {
                    btnRetest.setOnClickListener(v -> runRadar.run());
                }

                if (btnBoostNow != null) {
                    btnBoostNow.setOnClickListener(v -> {
                        AppExecutors.getInstance().executeCommand(() -> {
                            NetworkOptimizer.optimizeTcpBuffers();
                            NetworkOptimizer.flushDnsCache();
                            AppExecutors.getInstance().postToMainThread(() -> {
                                Toast.makeText(context, "⚡ TCP Fast-Open & DNS Resolver Purged! Retesting...", Toast.LENGTH_SHORT).show();
                                runRadar.run();
                            });
                        });
                    });
                }

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
