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
import com.gamebooster.app.config.ConfigBackupManager;
import com.gamebooster.app.core.AppExecutors;

import java.util.List;

public class ConfigRestoreDialog {

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

                View view = LayoutInflater.from(context).inflate(R.layout.dialog_config_restore, (ViewGroup) null, false);
                dialog.setContentView(view);

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setDimAmount(0.50f);
                }

                TextView tvCountBadge = view.findViewById(R.id.tv_restore_count_badge);
                LinearLayout container = view.findViewById(R.id.container_backup_list);
                TextView tvEmpty = view.findViewById(R.id.tv_empty_backups);
                Button btnRestoreAll = view.findViewById(R.id.btn_restore_all_games);
                Button btnDismiss = view.findViewById(R.id.btn_restore_dismiss);

                Runnable refreshList = () -> {
                    if (container != null) container.removeAllViews();
                    List<String> pkgs = ConfigBackupManager.getBackedUpPackages(context);

                    if (tvCountBadge != null) {
                        tvCountBadge.setText(pkgs.size() + " Backed Up Games");
                    }

                    if (pkgs.isEmpty()) {
                        if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                        if (btnRestoreAll != null) btnRestoreAll.setEnabled(false);
                    } else {
                        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                        if (btnRestoreAll != null) btnRestoreAll.setEnabled(true);

                        for (String pkg : pkgs) {
                            int count = ConfigBackupManager.getBackupCount(context, pkg);
                            List<String> paths = ConfigBackupManager.getBackupPaths(context, pkg);

                            LinearLayout card = new LinearLayout(context);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundResource(R.drawable.card_glass_shape);
                            card.setPadding(32, 24, 32, 24);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 0, 16);
                            card.setLayoutParams(lp);

                            LinearLayout topRow = new LinearLayout(context);
                            topRow.setOrientation(LinearLayout.HORIZONTAL);

                            TextView tvPkg = new TextView(context);
                            tvPkg.setText(pkg);
                            tvPkg.setTextColor(Color.WHITE);
                            tvPkg.setTextSize(13);
                            tvPkg.setTypeface(null, android.graphics.Typeface.BOLD);
                            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                            topRow.addView(tvPkg, nameLp);

                            Button btnRestoreSingle = new Button(context);
                            btnRestoreSingle.setText("RESTORE");
                            btnRestoreSingle.setBackgroundResource(R.drawable.btn_cyber_cyan);
                            btnRestoreSingle.setTextColor(Color.parseColor("#080B11"));
                            btnRestoreSingle.setTextSize(10);
                            btnRestoreSingle.setTypeface(null, android.graphics.Typeface.BOLD);
                            btnRestoreSingle.setPadding(24, 0, 24, 0);
                            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, 80);
                            btnRestoreSingle.setLayoutParams(btnLp);

                            btnRestoreSingle.setOnClickListener(v -> {
                                AppExecutors.getInstance().executeCommand(() -> {
                                    int restored = ConfigBackupManager.restorePackage(context, pkg);
                                    AppExecutors.getInstance().postToMainThread(() -> {
                                        Toast.makeText(context, "🛡️ Restored " + restored + " clean file(s) for " + pkg, Toast.LENGTH_SHORT).show();
                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                            show(context);
                                        }
                                    });
                                });
                            });

                            topRow.addView(btnRestoreSingle);
                            card.addView(topRow);

                            TextView tvDetails = new TextView(context);
                            tvDetails.setText("• " + count + " original configuration files backed up (SHA-256 verified)");
                            tvDetails.setTextColor(Color.parseColor("#94A3B8"));
                            tvDetails.setTextSize(10);
                            tvDetails.setPadding(0, 8, 0, 0);
                            card.addView(tvDetails);

                            container.addView(card);
                        }
                    }
                };

                refreshList.run();

                if (btnRestoreAll != null) {
                    btnRestoreAll.setOnClickListener(v -> {
                        AppExecutors.getInstance().executeCommand(() -> {
                            int total = ConfigBackupManager.restoreAll(context);
                            AppExecutors.getInstance().postToMainThread(() -> {
                                Toast.makeText(context, "🛡️ Successfully restored " + total + " original clean game files!", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
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
