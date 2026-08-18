package com.gamebooster.app.diagnostics;

import android.content.Context;
import android.util.Log;

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
 * Captures uncaught crashes to app-internal storage so real users can export
 * them via the Diagnostics section. Idempotent installer — safe to call from
 * every entry point.
 */
public final class CrashLog {

    public static final String TAG = "CrashLog";
    public static final String FILE_NAME = "crash_log.txt";

    private static volatile boolean installed = false;
    private static volatile Thread.UncaughtExceptionHandler previousHandler = null;

    private CrashLog() {
    }

    public static synchronized void install(Context context) {
        if (installed) {
            return;
        }
        installed = true;
        previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                appendCrash(context, thread, throwable);
            } catch (Throwable t) {
                Log.w(TAG, "Failed to persist crash entry: " + t.getMessage());
            }
            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    public static void appendCrash(Context context, Thread thread, Throwable throwable) {
        File file = crashFile(context);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            writer.println("=== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    .format(new Date()) + " ===");
            writer.println(formatCrash(thread, throwable));
        } catch (Exception e) {
            Log.w(TAG, "Cannot write crash log: " + e.getMessage());
        }
    }

    public static String formatCrash(Thread thread, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thread: ").append(thread != null ? thread.getName() : "unknown")
                .append(" (id=").append(thread != null ? thread.getId() : -1).append(")\n");
        sb.append("Exception: ").append(throwable != null ? throwable.toString() : "null").append('\n');
        if (throwable != null) {
            for (StackTraceElement el : throwable.getStackTrace()) {
                sb.append("    at ").append(el).append('\n');
            }
        }
        return sb.toString();
    }

    public static String readTail(Context context, int maxChars) {
        if (context == null) return "";
        try {
            File file = crashFile(context);
            if (!file.exists() || file.length() == 0) return "";
            byte[] data = new byte[(int) Math.min(file.length(), maxChars)];
            java.io.FileInputStream in = new java.io.FileInputStream(file);
            try {
                int read = in.read(data);
                if (read <= 0) return "";
                return new String(data, 0, read, StandardCharsets.UTF_8);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean hasCrashLog(Context context) {
        return context != null && crashFile(context).exists();
    }

    private static File crashFile(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    public static List<String> format(int count, List<String> lines) {
        List<String> head = new ArrayList<>(count);
        for (int i = 0; i < count && i < lines.size(); i++) {
            head.add(lines.get(i));
        }
        return head;
    }
}