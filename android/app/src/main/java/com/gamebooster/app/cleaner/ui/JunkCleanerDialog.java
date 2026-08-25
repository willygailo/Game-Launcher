package com.gamebooster.app.cleaner.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.cleaner.cleaner.JunkCleanerEngine;
import com.gamebooster.app.cleaner.model.CleanResult;
import com.gamebooster.app.cleaner.model.JunkScanResult;
import com.gamebooster.app.cleaner.scanner.JunkScanner;
import com.gamebooster.app.cleaner.scanner.StorageStatsHelper;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import com.gamebooster.app.ui.dialogs.CyberActionDialog;

/**
 * JunkCleanerDialog — UI Dialog for Real Android Junk Scanning & Storage Purge.
 */
public class JunkCleanerDialog {

    public interface OnCleanFinishedListener {
        void onCleanFinished(CleanResult result);
    }

    private static Dialog activeDialog;

    public static void show(Context context, OnCleanFinishedListener listener) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_junk_cleaner, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.6f);
        }

        TextView tvModeBadge = view.findViewById(R.id.tv_cleaner_mode_badge);
        TextView tvTotalSize = view.findViewById(R.id.tv_total_junk_size);
        TextView tvStatus = view.findViewById(R.id.tv_cleaner_status);
        ProgressBar pbProgress = view.findViewById(R.id.pb_cleaner_progress);
        RecyclerView rvCategories = view.findViewById(R.id.rv_junk_categories);
        Button btnRescan = view.findViewById(R.id.btn_rescan_junk);
        Button btnCleanNow = view.findViewById(R.id.btn_execute_clean);
        Button btnClose = view.findViewById(R.id.btn_cleaner_close);

        rvCategories.setLayoutManager(new LinearLayoutManager(context));

        // Auto-check / Auto-grant elevated permissions if Shizuku is connected
        if (ShizukuFileManager.hasFullAccess()) {
            StorageStatsHelper.autoGrantPrivilegedPermissions(context);
            tvModeBadge.setText("[SHIZUKU PM TRIM ACTIVE]");
            tvModeBadge.setTextColor(Color.parseColor("#00FF66"));
        } else if (StorageStatsHelper.hasUsageStatsPermission(context)) {
            tvModeBadge.setText("[STORAGE STATS ENGINE]");
            tvModeBadge.setTextColor(Color.parseColor("#00F0FF"));
        } else {
            tvModeBadge.setText("[STANDARD SAFE CLEAN]");
            tvModeBadge.setTextColor(Color.parseColor("#FFCC00"));
        }

        final JunkScanner scanner = new JunkScanner();
        final JunkCleanerEngine engine = new JunkCleanerEngine();
        final JunkScanResult[] currentScan = new JunkScanResult[1];

        Runnable startScan = () -> {
            btnRescan.setEnabled(false);
            btnCleanNow.setEnabled(false);
            pbProgress.setProgress(0);
            tvStatus.setText("🔍 Initializing storage scanner...");
            tvTotalSize.setText("0.0 MB");

            AppExecutors.getInstance().executeScan(() -> {
                JunkScanResult scanResult = scanner.scanStorage(context, new JunkScanner.OnScanProgressListener() {
                    @Override
                    public void onScanProgress(int percent, String currentPath, long bytesFoundSoFar) {
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (dialog.isShowing()) {
                                pbProgress.setProgress(percent);
                                tvTotalSize.setText(JunkScanResult.formatBytes(bytesFoundSoFar));
                                tvStatus.setText(currentPath);
                            }
                        });
                    }

                    @Override
                    public void onScanComplete(JunkScanResult result) {
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (!dialog.isShowing()) return;

                            currentScan[0] = result;
                            pbProgress.setProgress(100);
                            tvTotalSize.setText(result.getFormattedTotalSize());
                            tvStatus.setText("Scan complete! " + result.getItems().size() + " items found in " + result.getScanDurationMs() + "ms.");
                            btnRescan.setEnabled(true);
                            btnCleanNow.setEnabled(result.getTotalBytes() > 0);
                            btnCleanNow.setText("🚀 CLEAN (" + result.getFormattedSelectedSize() + ")");

                            JunkCategoryAdapter adapter = new JunkCategoryAdapter(context, result, () -> {
                                tvTotalSize.setText(result.getFormattedSelectedSize());
                                btnCleanNow.setText("🚀 CLEAN (" + result.getFormattedSelectedSize() + ")");
                                btnCleanNow.setEnabled(result.getSelectedBytes() > 0);
                            });
                            rvCategories.setAdapter(adapter);
                        });
                    }
                });
            });
        };

        btnRescan.setOnClickListener(v -> startScan.run());

        btnCleanNow.setOnClickListener(v -> {
            if (currentScan[0] == null || currentScan[0].getSelectedBytes() == 0) {
                Toast.makeText(context, "No junk selected to clean", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRescan.setEnabled(false);
            btnCleanNow.setEnabled(false);
            btnClose.setEnabled(false);
            pbProgress.setProgress(0);

            tvStatus.setText("🧹 Cleaning storage cache & files...");

            engine.cleanJunkAsync(context, currentScan[0], new JunkCleanerEngine.OnCleanProgressListener() {
                @Override
                public void onCleanProgress(int percent, String currentItem, long bytesFreedSoFar) {
                    if (!dialog.isShowing()) return;
                    pbProgress.setProgress(percent);
                    tvStatus.setText(currentItem);
                    tvTotalSize.setText(JunkScanResult.formatBytes(bytesFreedSoFar) + " Freed");
                }

                @Override
                public void onCleanComplete(CleanResult result) {
                    if (!dialog.isShowing()) return;

                    btnRescan.setEnabled(true);
                    btnClose.setEnabled(true);
                    btnCleanNow.setEnabled(false);
                    btnCleanNow.setText("✅ CLEANED");
                    tvTotalSize.setText(result.getFormattedBytesFreed());
                    tvStatus.setText("Purged " + result.getFilesDeletedCount() + " items in " + result.getDurationMs() + "ms!");

                    CyberActionDialog.show(
                            context,
                            "🧹 STORAGE & CACHE CLEANED",
                            true,
                            "Total Space Freed: " + result.getFormattedBytesFreed(),
                            "Cleaned " + result.getFilesDeletedCount() + " files & buffers",
                            "NAND Flash Storage Optimized (fstrim)"
                    );

                    if (listener != null) {
                        listener.onCleanFinished(result);
                    }
                }
            });
        });

        btnClose.setOnClickListener(v -> dismissCurrent());

        activeDialog = dialog;
        dialog.show();

        // Auto start scan on open
        startScan.run();
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
