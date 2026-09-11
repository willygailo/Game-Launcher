package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.overlay.GameSessionReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * PostGameReportDialog — Displays Cyberpunk match telemetry summary and exports high-res performance cards.
 */
public class PostGameReportDialog {

    private static final String TAG = "PostGameReportDialog";
    private static Dialog activeDialog;

    public static void show(Context context, GameSessionReport report) {
        if (context == null || report == null) return;
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_post_game_report, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.70f);
        }

        TextView tvTierBadge = view.findViewById(R.id.tv_report_tier_badge);
        TextView tvGameTitle = view.findViewById(R.id.tv_report_game_title);
        TextView tvPkg = view.findViewById(R.id.tv_report_pkg);
        TextView tvPlaytime = view.findViewById(R.id.tv_report_playtime);

        TextView tvAvgFps = view.findViewById(R.id.tv_report_avg_fps);
        TextView tv1LowFps = view.findViewById(R.id.tv_report_1low_fps);
        TextView tvStability = view.findViewById(R.id.tv_report_stability);
        TextView tvFpsRange = view.findViewById(R.id.tv_report_fps_range);

        TextView tvBatteryRate = view.findViewById(R.id.tv_report_battery_rate);
        TextView tvBatteryUsed = view.findViewById(R.id.tv_report_battery_used);
        TextView tvPeakTemp = view.findViewById(R.id.tv_report_peak_temp);
        TextView tvAvgTemp = view.findViewById(R.id.tv_report_avg_temp);

        Button btnClose = view.findViewById(R.id.btn_close_report);
        Button btnExport = view.findViewById(R.id.btn_export_report_image);
        View rootCard = view.findViewById(R.id.layout_report_card_root);

        // Bind data
        tvGameTitle.setText(report.gameTitle);
        tvPkg.setText(report.packageName);
        tvPlaytime.setText(report.getFormattedPlaytime());

        tvAvgFps.setText(report.averageFps + " FPS");
        tv1LowFps.setText("1% Low: " + report.onePercentLowFps + " FPS");
        tvStability.setText(report.stabilityScorePercent + "%");
        tvFpsRange.setText("Min: " + report.minFps + " • Max: " + report.maxFps);

        tvBatteryRate.setText(String.format("%.1f", report.batteryDrainRatePerHour) + "% / hr");
        int batteryUsed = Math.max(0, report.startBatteryLevel - report.endBatteryLevel);
        tvBatteryUsed.setText("Used: " + batteryUsed + "% (" + report.startBatteryLevel + "% → " + report.endBatteryLevel + "%)");

        tvPeakTemp.setText(String.format("%.1f", report.peakTemperatureC) + "°C");
        tvAvgTemp.setText("Avg Temp: " + String.format("%.1f", report.averageTemperatureC) + "°C");

        if (report.stabilityScorePercent >= 95 && report.averageFps >= 90) {
            tvTierBadge.setText("⭐ SSS ESPORTS TIER");
            tvTierBadge.setTextColor(Color.parseColor("#00FF66"));
        } else if (report.stabilityScorePercent >= 88) {
            tvTierBadge.setText("⚡ PRO GAMING TIER");
            tvTierBadge.setTextColor(Color.parseColor("#00F0FF"));
        } else {
            tvTierBadge.setText("🎮 BALANCED TIER");
            tvTierBadge.setTextColor(Color.parseColor("#FFAA00"));
        }

        btnClose.setOnClickListener(v -> dismissCurrent());

        btnExport.setOnClickListener(v -> {
            btnExport.setEnabled(false);
            btnExport.setText("⏳ EXPORTING...");
            Toast.makeText(context.getApplicationContext(), "📸 Generating match scorecard image...", Toast.LENGTH_SHORT).show();

            AppExecutors.getInstance().executeCommand(() -> {
                Uri savedUri = exportViewAsImage(context, rootCard, report.gameTitle);
                AppExecutors.getInstance().postToMainThread(() -> {
                    btnExport.setEnabled(true);
                    btnExport.setText("📸 EXPORT AS IMAGE");
                    if (savedUri != null) {
                        Toast.makeText(context.getApplicationContext(), "✅ Performance Scorecard saved to Pictures!", Toast.LENGTH_LONG).show();
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/png");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, savedUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(Intent.createChooser(shareIntent, "Share Match Performance Report"));
                        } catch (Exception ignored) {}
                    } else {
                        Toast.makeText(context.getApplicationContext(), "⚠️ Failed saving scorecard image", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        activeDialog = dialog;
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    public static void dismissCurrent() {
        if (activeDialog != null) {
            try {
                if (activeDialog.isShowing()) activeDialog.dismiss();
            } catch (Exception ignored) {}
            activeDialog = null;
        }
    }

    private static Uri exportViewAsImage(Context context, View view, String gameTitle) {
        try {
            int width = view.getWidth() > 0 ? view.getWidth() : 1080;
            int height = view.getHeight() > 0 ? view.getHeight() : 1400;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            String fileName = "GameReport_" + System.currentTimeMillis() + ".png";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GameLauncherPro");

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                    if (out != null) bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    return uri;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to render or save post-game report", e);
        }
        return null;
    }
}
