package com.gamebooster.app.diagnostics;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.gamebooster.app.engine.MasterOptimizationEnforcer;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Builds a shareable diagnostics snapshot: app version, device identity,
 * enforcement status, spoof state and any captured crash log.
 */
public final class DiagnosticsExporter {

    public static final String TAG = "DiagnosticsExporter";
    public static final String PREFIX = "diagnostics_";
    public static final String EXTENSION = ".txt";

    private DiagnosticsExporter() {
    }

    public static List<String> buildSnapshot(String appVersion, String deviceModel,
                                             String androidRelease, int sdkInt,
                                             MasterOptimizationEnforcer.EnforcementStatus status,
                                             boolean spoofEnabled, String spoofProfileId,
                                             String crashTail) {
        List<String> lines = new ArrayList<>();
        lines.add("=== GAME BOOSTER PRO DIAGNOSTICS ===");
        lines.add("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date()));
        lines.add("App version: " + safe(appVersion));
        lines.add("Device: " + safe(deviceModel));
        lines.add("Android: " + safe(androidRelease) + " (API " + sdkInt + ")");
        lines.add("Shizuku/root available: " + (status != null && status.shizukuRootGranted));
        lines.add("AIDL service connected: " + (status != null && status.aidlConnected));
        if (status != null) {
            lines.add("Tweaks enforced: " + status.tweaksAppliedCount + " / " + status.totalSupportedTweaks);
        } else {
            lines.add("Tweaks enforced: unknown");
        }
        String profile = spoofProfileId != null && !spoofProfileId.isEmpty()
                ? spoofProfileId : "none";
        lines.add("Spoof profile: " + profile + " (enabled=" + spoofEnabled + ")");
        if (crashTail != null && !crashTail.isEmpty()) {
            lines.add("");
            lines.add("--- Last captured crash ---");
            lines.add(crashTail.trim());
        }
        lines.add("--- End ---");
        return lines;
    }

    public static File exportToFile(Context context, String content) throws Exception {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            dir = context.getFilesDir();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String name = PREFIX + System.currentTimeMillis() + EXTENSION;
        File file = new File(dir, name);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.print(content);
        }
        Log.i(TAG, "Exported diagnostics to " + file.getAbsolutePath());
        return file;
    }

    public static Intent shareSnapshot(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "Game Booster PRO diagnostics");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(share, "Share diagnostics");
    }

    public static String join(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    private static String safe(String value) {
        return value != null ? value : "unknown";
    }
}