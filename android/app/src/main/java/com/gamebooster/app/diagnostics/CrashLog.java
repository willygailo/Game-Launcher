package com.gamebooster.app.diagnostics;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Captures uncaught crashes and non-fatal exceptions to app-internal storage so real users
 * and developers can review and export them via the Diagnostics section.
 *
 * Thread-safe and idempotent installer — logs full exception chains, device state,
 * available memory, and enforces a 100KB file rotation limit to prevent unbounded storage growth.
 */
public final class CrashLog {

    public static final String TAG = "CrashLog";
    public static final String FILE_NAME = "crash_log.txt";
    private static final long MAX_FILE_BYTES = 100 * 1024; // 100 KB max
    private static final Object FILE_LOCK = new Object();

    private static volatile boolean installed = false;
    private static volatile Thread.UncaughtExceptionHandler previousHandler = null;

    private CrashLog() {
    }

    public static synchronized void install(Context context) {
        if (installed || context == null) {
            return;
        }
        installed = true;
        final Context appContext = context.getApplicationContext();
        previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                appendCrash(appContext, thread, throwable);
            } catch (Throwable t) {
                Log.w(TAG, "Failed to persist crash entry: " + t.getMessage());
            }
            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable);
            } else {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        });
        Log.i(TAG, "Global CrashLog handler installed.");
    }

    public static void appendCrash(Context context, Thread thread, Throwable throwable) {
        if (context == null) return;
        synchronized (FILE_LOCK) {
            File file = crashFile(context);
            ensureParentDir(file);
            rotateIfNeeded(file);

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
                writer.println("==================================================");
                writer.println("CRASH REPORT: " + getFormattedTimestamp());
                writer.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.DEVICE + ")");
                writer.println("Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")");
                writer.println("RAM Free: " + getAvailableMemoryMb(context) + " MB");
                writer.println(formatCrash(thread, throwable));
                writer.println("==================================================");
                writer.println();
            } catch (Exception e) {
                Log.w(TAG, "Cannot write crash log: " + e.getMessage());
            }
        }
    }

    public static void logException(Context context, String tag, Throwable throwable) {
        if (context == null || throwable == null) return;
        synchronized (FILE_LOCK) {
            File file = crashFile(context);
            ensureParentDir(file);
            rotateIfNeeded(file);

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
                writer.println("--------------------------------------------------");
                writer.println("HANDLED EXCEPTION [" + (tag != null ? tag : "APP") + "]: " + getFormattedTimestamp());
                writer.println(formatCrash(Thread.currentThread(), throwable));
                writer.println("--------------------------------------------------");
                writer.println();
            } catch (Exception e) {
                Log.w(TAG, "Cannot write non-fatal exception log: " + e.getMessage());
            }
        }
    }

    public static String formatCrash(Thread thread, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thread: ").append(thread != null ? thread.getName() : "unknown")
                .append(" (id=").append(thread != null ? thread.getId() : -1)
                .append(", priority=").append(thread != null ? thread.getPriority() : -1).append(")\n");

        if (throwable == null) {
            sb.append("Exception: <null throwable>\n");
            return sb.toString();
        }

        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 10) {
            if (depth == 0) {
                sb.append("Exception: ").append(current.getClass().getName()).append(": ").append(current.getMessage()).append('\n');
            } else {
                sb.append("Caused by: ").append(current.getClass().getName()).append(": ").append(current.getMessage()).append('\n');
            }
            for (StackTraceElement el : current.getStackTrace()) {
                sb.append("    at ").append(el.toString()).append('\n');
            }
            current = current.getCause();
            depth++;
        }
        return sb.toString();
    }

    /**
     * Reads the tail (last N characters) of the crash log file with UTF-8 boundary protection.
     */
    public static String readTail(Context context, int maxChars) {
        if (context == null) return "";
        synchronized (FILE_LOCK) {
            try {
                File file = crashFile(context);
                if (!file.exists() || file.length() == 0) return "";
                long len = file.length();
                int bytesToRead = (int) Math.min(len, maxChars);
                byte[] data = new byte[bytesToRead];

                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    if (len > bytesToRead) {
                        raf.seek(len - bytesToRead);
                    }
                    int read = raf.read(data);
                    if (read <= 0) return "";

                    // Skip partial UTF-8 continuation bytes at start of sliced buffer
                    int offset = 0;
                    if (len > bytesToRead) {
                        while (offset < read && (data[offset] & 0xC0) == 0x80) {
                            offset++;
                        }
                    }
                    return new String(data, offset, read - offset, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                Log.w(TAG, "readTail error: " + e.getMessage());
                return "";
            }
        }
    }

    public static boolean hasCrashLog(Context context) {
        if (context == null) return false;
        synchronized (FILE_LOCK) {
            File file = crashFile(context);
            return file.exists() && file.length() > 0;
        }
    }

    public static boolean clear(Context context) {
        if (context == null) return false;
        synchronized (FILE_LOCK) {
            try {
                File file = crashFile(context);
                boolean del1 = true;
                if (file.exists()) {
                    del1 = file.delete();
                }
                File backup = new File(file.getParentFile(), FILE_NAME + ".old");
                if (backup.exists()) {
                    backup.delete();
                }
                return del1;
            } catch (Exception e) {
                Log.w(TAG, "Failed to clear crash log: " + e.getMessage());
                return false;
            }
        }
    }

    public static long getLogSizeBytes(Context context) {
        if (context == null) return 0L;
        synchronized (FILE_LOCK) {
            File file = crashFile(context);
            return file.exists() ? file.length() : 0L;
        }
    }

    private static File crashFile(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    private static void ensureParentDir(File file) {
        if (file != null && file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    private static void rotateIfNeeded(File file) {
        if (file != null && file.exists() && file.length() > MAX_FILE_BYTES) {
            File backup = new File(file.getParentFile(), FILE_NAME + ".old");
            if (backup.exists()) {
                backup.delete();
            }
            if (!file.renameTo(backup)) {
                // Fallback: copy and truncate if renameTo fails
                try {
                    try (FileInputStream in = new FileInputStream(file);
                         FileOutputStream out = new FileOutputStream(backup)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) > 0) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    try (FileOutputStream truncate = new FileOutputStream(file, false)) {
                        // truncate by opening and immediately closing
                    }
                } catch (Throwable ignored) {}
            }
        }
    }

    private static String getFormattedTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
    }

    private static long getAvailableMemoryMb(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                return mi.availMem / (1024 * 1024);
            }
        } catch (Throwable ignored) {}
        return -1L;
    }

    public static float getLogSizeKb(Context context) {
        return getLogSizeBytes(context) / 1024f;
    }

    public static String exportToShareableString(Context context) {
        if (context == null) return "No context";
        return readTail(context, 100000);
    }

    public static List<String> getHead(List<String> lines, int count) {
        List<String> head = new ArrayList<>(count);
        if (lines == null) return head;
        for (int i = 0; i < count && i < lines.size(); i++) {
            head.add(lines.get(i));
        }
        return head;
    }

    public static List<String> getTail(List<String> lines, int count) {
        List<String> tail = new ArrayList<>(count);
        if (lines == null) return tail;
        int start = Math.max(0, lines.size() - count);
        for (int i = start; i < lines.size(); i++) {
            tail.add(lines.get(i));
        }
        return tail;
    }

    public static List<String> format(int count, List<String> lines) {
        return getHead(lines, count);
    }
}