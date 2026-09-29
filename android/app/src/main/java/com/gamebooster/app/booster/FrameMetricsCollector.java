package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * §7.1 Frame pacing metrics collection.
 *
 * <p>Parses {@code dumpsys gfxinfo <pkg> framestats} into FrameTime average,
 * variance (stddev), p99, JankCount and BigJankCount, and optionally exports a
 * CSV snapshot. Jank thresholds are relative to the target frame budget:
 * jank = frame &gt; 1.5 × budget, big jank = frame &gt; 2 × budget.
 */
public final class FrameMetricsCollector {

    private static final String TAG = "FrameMetricsCollector";

    /** Aggregated frame pacing statistics for one collection window. */
    public static final class FrameStats {
        public final int samples;
        public final double avgMs;
        public final double stddevMs;
        public final double p99Ms;
        public final int jankCount;
        public final int bigJankCount;
        public final double budgetMs;
        public final long timestampMs;

        FrameStats(int samples, double avgMs, double stddevMs, double p99Ms,
                   int jankCount, int bigJankCount, double budgetMs, long timestampMs) {
            this.samples = samples;
            this.avgMs = avgMs;
            this.stddevMs = stddevMs;
            this.p99Ms = p99Ms;
            this.jankCount = jankCount;
            this.bigJankCount = bigJankCount;
            this.budgetMs = budgetMs;
            this.timestampMs = timestampMs;
        }

        public String summary() {
            return String.format(Locale.US,
                    "%d frames, avg %.2fms, stddev %.2fms, p99 %.2fms, "
                            + "jank %d (%.1f%%), bigJank %d, budget %.2fms",
                    samples, avgMs, stddevMs, p99Ms,
                    jankCount, samples == 0 ? 0.0 : (100.0 * jankCount / samples),
                    bigJankCount, budgetMs);
        }

        public String csvHeader() {
            return "timestamp_ms,samples,avg_ms,stddev_ms,p99_ms,jank_count,"
                    + "big_jank_count,budget_ms";
        }

        public String csvRow() {
            return String.format(Locale.US, "%d,%d,%.3f,%.3f,%.3f,%d,%d,%.3f",
                    timestampMs, samples, avgMs, stddevMs, p99Ms, jankCount, bigJankCount, budgetMs);
        }
    }

    private FrameMetricsCollector() {
    }

    /**
     * Parses frame durations (ms) out of a {@code dumpsys gfxinfo ... framestats}
     * dump. Column positions come from the PROFILEDATA header (IntendedVsync ..
     * last FrameCompleted) so the parser survives column differences across
     * Android versions; falls back to the classic layout when no header found.
     */
    static List<Double> parseFrameDurations(String gfxinfoOutput) {
        List<Double> durations = new ArrayList<>();
        if (gfxinfoOutput == null) return durations;
        boolean inTable = false;
        int intendedIdx = 1;
        int completedIdx = 3;
        for (String rawLine : gfxinfoOutput.split("\n")) {
            String line = rawLine.trim();
            if (line.contains("---PROFILEDATA---")) {
                if (!durations.isEmpty()) break; // second marker closes the table
                inTable = true;
                intendedIdx = 1;
                completedIdx = 3;
                continue;
            }
            if (!inTable) continue;
            if (line.startsWith("Flags,")) {
                String[] cols = line.split(",");
                for (int i = 0; i < cols.length; i++) {
                    String col = cols[i].trim();
                    if (col.equals("IntendedVsync")) intendedIdx = i;
                    if (col.equals("FrameCompleted")) completedIdx = i; // last wins
                }
                continue;
            }
            if (line.startsWith("---") || line.isEmpty()) break;
            String[] cols = line.split(",");
            if (cols.length <= Math.max(intendedIdx, completedIdx)) continue;
            try {
                long intended = Long.parseLong(cols[intendedIdx].trim());
                long completed = Long.parseLong(cols[completedIdx].trim());
                if (completed > intended && intended >= 0) {
                    durations.add((completed - intended) / 1_000_000.0);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return durations;
    }

    /** Summarizes frame durations against a frame budget (1000/hz ms). */
    static FrameStats summarize(List<Double> durations, double budgetMs, long timestampMs) {
        int n = durations.size();
        if (n == 0) {
            return new FrameStats(0, 0, 0, 0, 0, 0, budgetMs, timestampMs);
        }
        double sum = 0;
        for (double d : durations) sum += d;
        double avg = sum / n;
        double variance = 0;
        for (double d : durations) variance += (d - avg) * (d - avg);
        double stddev = Math.sqrt(variance / n);

        List<Double> sorted = new ArrayList<>(durations);
        java.util.Collections.sort(sorted);
        int p99Idx = (int) Math.min(n - 1, Math.round(0.99 * (n - 1)));
        double p99 = sorted.get(p99Idx);

        int jank = 0;
        int bigJank = 0;
        for (double d : durations) {
            if (d > 2.0 * budgetMs) {
                jank++;
                bigJank++;
            } else if (d > 1.5 * budgetMs) {
                jank++;
            }
        }
        return new FrameStats(n, avg, stddev, p99, jank, bigJank, budgetMs, timestampMs);
    }

    /**
     * Collects frame stats for a package via dumpsys.
     *
     * @param packageName target game package
     * @param hz          target refresh rate (defines the frame budget)
     * @return stats, or null when gfxinfo produced fewer than 30 samples
     */
    public static FrameStats collect(String packageName, int hz) {
        if (packageName == null || packageName.isEmpty() || hz <= 0) return null;
        String out;
        try {
            out = CommandExecutor.executeSystemCommand(
                    "dumpsys gfxinfo " + packageName + " framestats");
        } catch (Throwable t) {
            Log.w(TAG, "gfxinfo read failed", t);
            return null;
        }
        List<Double> durations = parseFrameDurations(out);
        if (durations.size() < 30) {
            Log.i(TAG, "✗ only " + durations.size() + " frame samples for " + packageName
                    + " — not enough for stats");
            return null;
        }
        FrameStats stats = summarize(durations, 1000.0 / hz, System.currentTimeMillis());
        Log.i(TAG, "✓ " + packageName + ": " + stats.summary());
        return stats;
    }

    /**
     * Writes the stats as CSV. Always writes to app filesDir; attempts a copy
     * to /data/local/tmp/gamebooster/ when privileged execution is available.
     *
     * @return the local CSV file, or null on I/O failure
     */
    public static File writeCsv(Context context, FrameStats stats) {
        if (context == null || stats == null) return null;
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date(stats.timestampMs));
        File file = new File(context.getFilesDir(), "frame_metrics_" + stamp + ".csv");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            String csv = stats.csvHeader() + "\n" + stats.csvRow() + "\n";
            fos.write(csv.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.w(TAG, "✗ CSV write failed", e);
            return null;
        }
        try {
            CommandExecutor.executeSystemCommand(
                    "mkdir -p /data/local/tmp/gamebooster && cp " + file.getAbsolutePath()
                            + " /data/local/tmp/gamebooster/frame_metrics_" + stamp + ".csv");
        } catch (Throwable ignored) {
        }
        Log.i(TAG, "✓ frame metrics CSV: " + file.getAbsolutePath());
        return file;
    }
}
