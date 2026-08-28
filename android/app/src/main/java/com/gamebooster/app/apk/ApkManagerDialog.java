package com.gamebooster.app.apk;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameLauncherHelper;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.ui.dialogs.CyberActionDialog;

import java.util.ArrayList;
import java.util.List;

public class ApkManagerDialog {

    public interface OnApkManagerChangeListener {
        void onDataChanged();
    }

    private static Dialog activeDialog;

    public static void show(Context context, OnApkManagerChangeListener changeListener) {
        if (context == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_apk_manager, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        TextView tvShizukuBadge = view.findViewById(R.id.tv_apk_shizuku_badge);
        TextView btnClose = view.findViewById(R.id.btn_dialog_apk_close);
        TextView tvCountInfo = view.findViewById(R.id.tv_apk_count_info);
        Button btnRescan = view.findViewById(R.id.btn_apk_rescan);
        Button btnPickFile = view.findViewById(R.id.btn_apk_pick_file);
        ProgressBar pbLoading = view.findViewById(R.id.pb_apk_loading);
        RecyclerView rvApks = view.findViewById(R.id.rv_apk_list);
        View layoutEmpty = view.findViewById(R.id.layout_apk_empty);

        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        if (hasShizuku) {
            tvShizukuBadge.setText("[SHIZUKU 1-CLICK READY]");
            tvShizukuBadge.setTextColor(0xFF00FF66);
        } else {
            tvShizukuBadge.setText("[LIMITED INSTALLER]");
            tvShizukuBadge.setTextColor(0xFFFFB800);
        }

        rvApks.setLayoutManager(new LinearLayoutManager(context));
        rvApks.setHasFixedSize(false);

        List<ApkItem> apkList = new ArrayList<>();
        final ApkAdapter[] adapterRef = new ApkAdapter[1];
        ApkAdapter adapter = new ApkAdapter(context, apkList, new ApkAdapter.OnApkActionListener() {
            @Override
            public void onInstallRequested(ApkItem item) {
                Toast.makeText(context, "⚡ Installing " + item.getAppName() + "...", Toast.LENGTH_SHORT).show();
                ApkInstallerEngine.installApk(context, item.getFilePath(), (success, message) -> {
                    if (success) {
                        GameLauncherHelper.addCustomPackage(context, item.getPackageName());
                        CyberActionDialog.show(
                                context,
                                "📦 APK INSTALLED & ADDED TO HOME",
                                true,
                                "App: " + item.getAppName(),
                                "Package: " + item.getPackageName(),
                                "Version: v" + item.getVersionName(),
                                "Status: 1-Click Installation Succeeded"
                        );
                        if (changeListener != null) changeListener.onDataChanged();
                    } else {
                        Toast.makeText(context, "⚠️ Installation notice: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onApkDeleted(ApkItem item) {
                // Refresh list
                startScan(context, adapterRef[0], pbLoading, tvCountInfo, layoutEmpty, rvApks);
                if (changeListener != null) changeListener.onDataChanged();
            }

            @Override
            public void onAddedToHome(ApkItem item) {
                if (changeListener != null) changeListener.onDataChanged();
            }
        });
        adapterRef[0] = adapter;
        rvApks.setAdapter(adapter);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismissCurrent());
        }

        if (btnRescan != null) {
            btnRescan.setOnClickListener(v -> {
                startScan(context, adapter, pbLoading, tvCountInfo, layoutEmpty, rvApks);
            });
        }

        if (btnPickFile != null) {
            btnPickFile.setOnClickListener(v -> {
                Toast.makeText(context, "Scanning storage automatically...", Toast.LENGTH_SHORT).show();
                startScan(context, adapter, pbLoading, tvCountInfo, layoutEmpty, rvApks);
            });
        }

        startScan(context, adapter, pbLoading, tvCountInfo, layoutEmpty, rvApks);

        activeDialog = dialog;
        dialog.show();
    }

    private static void startScan(Context context, ApkAdapter adapter, ProgressBar pbLoading,
                                  TextView tvCountInfo, View layoutEmpty, RecyclerView rvApks) {
        if (context == null) return;
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (tvCountInfo != null) tvCountInfo.setText("Scanning storage for .apk packages...");

        AppExecutors.getInstance().executeScan(() -> {
            List<ApkItem> results = ApkInstallerEngine.scanStorageApks(context);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (adapter != null) adapter.updateList(results);

                if (tvCountInfo != null) {
                    tvCountInfo.setText("Found " + results.size() + " APK packages in internal storage");
                }

                if (results.isEmpty()) {
                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                    if (rvApks != null) rvApks.setVisibility(View.GONE);
                } else {
                    if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                    if (rvApks != null) rvApks.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    public static void dismissCurrent() {
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
            } catch (Throwable ignored) {}
            activeDialog = null;
        }
    }
}
