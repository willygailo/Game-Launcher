package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.PrivilegeBridgeEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SnapshotSystem — captures pre-boost system state (sysfs nodes, settings,
 * properties) and replays it on failure, thermal critical, or user restore.
 *
 * IMPROVEMENT_PLAN.md §8.1 / §13: every privileged write path is preceded by a
 * snapshot so a bad state (e.g. refresh-rate pair, thermal trip points) can be
 * reverted without a reboot.
 */
public final class SnapshotSystem {

    private static final String TAG = "SnapshotSystem";
    private static final String FILE_NAME = "latest.json";

    public enum EntryType { SYSFS, SETTING, PROP }

    public static final class Entry {
        public final EntryType type;
        public final String key;
        public final String value;

        public Entry(EntryType type, String key, String value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }
    }

    public static final class Snapshot {
        public final String label;
        public final long capturedAtMs;
        public final List<Entry> entries;

        public Snapshot(String label, long capturedAtMs, List<Entry> entries) {
            this.label = label;
            this.capturedAtMs = capturedAtMs;
            this.entries = entries;
        }
    }

    private SnapshotSystem() {}

    private static final String[] SYSFS_PATTERNS = {
            "/sys/devices/system/cpu/cpufreq/policy*/scaling_governor",
            "/sys/devices/system/cpu/cpufreq/policy*/scaling_min_freq",
            "/sys/devices/system/cpu/cpufreq/policy*/scaling_max_freq",
            "/sys/class/devfreq/*/governor",
            "/sys/class/devfreq/*/min_freq",
            "/sys/class/devfreq/*/max_freq",
            "/sys/class/kgsl/kgsl-3d0/thermal_pwrlevel",
            "/sys/class/kgsl/kgsl-3d0/throttling",
            "/sys/class/thermal/thermal_zone*/mode",
            "/sys/class/thermal/thermal_zone*/trip_point_*_temp",
            "/sys/class/thermal/cooling_device*/cur_state",
    };

    private static final String[][] SETTINGS = {
            {"system", "min_refresh_rate"},
            {"system", "peak_refresh_rate"},
            {"global", "game_driver_opt_in_apps"},
            {"global", "updatable_driver_production_opt_in_apps"},
    };

    private static final String[] PROPS = {
            "debug.thermal.throttle.disable",
            "debug.thermal.suppress_throttle",
            "vendor.thermal.mode",
            "persist.sys.thermal.disabled",
            "persist.sys.thermal.mitigation",
            "sys.thermal.mode",
            "debug.chromium.flags",
    };

    public static boolean isThermalKey(String key) {
        if (key == null) return false;
        return key.startsWith("/sys/class/thermal/")
                || key.startsWith("/sys/devices/virtual/thermal/")
                || key.startsWith("/sys/class/kgsl/")
                || key.equals("debug.thermal.throttle.disable")
                || key.equals("debug.thermal.suppress_throttle")
                || key.equals("vendor.thermal.mode")
                || key.equals("persist.sys.thermal.disabled")
                || key.equals("persist.sys.thermal.mitigation")
                || key.equals("sys.thermal.mode");
    }

    public static Snapshot capture(String label) {
        if (!PrivilegeBridgeEngine.isPrivilegedActive()) {
            Log.w(TAG, "capture skipped: no active privilege (Root/Shizuku)");
            return new Snapshot(label, System.currentTimeMillis(), new ArrayList<>());
        }

        List<Entry> entries = new ArrayList<>();

        StringBuilder glob = new StringBuilder("for f in ");
        for (String p : SYSFS_PATTERNS) {
            glob.append(p).append(' ');
        }
        glob.append("; do if [ -r \"$f\" ]; then printf '%s\\t%s\\n' \"$f\" \"$(cat \"$f\" 2>/dev/null | tr '\\n' ' ')\"; fi; done");
        String out = CommandExecutor.executeSystemCommand(glob.toString());
        if (out != null) {
            for (String line : out.split("\n")) {
                int tab = line.indexOf('\t');
                if (tab <= 0) continue;
                String key = line.substring(0, tab).trim();
                String value = line.substring(tab + 1).trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    entries.add(new Entry(EntryType.SYSFS, key, value));
                }
            }
        }

        for (String[] s : SETTINGS) {
            String value = CommandExecutor.getSystemSetting(s[0], s[1]);
            if (value != null && !value.trim().isEmpty()) {
                entries.add(new Entry(EntryType.SETTING, s[0] + "/" + s[1], value.trim()));
            }
        }

        for (String p : PROPS) {
            String value = CommandExecutor.getSystemProperty(p);
            if (value != null && !value.trim().isEmpty()) {
                entries.add(new Entry(EntryType.PROP, p, value.trim()));
            }
        }

        Log.i(TAG, "Captured snapshot '" + label + "' with " + entries.size() + " entries");
        return new Snapshot(label, System.currentTimeMillis(), entries);
    }

    public static boolean save(Context context, Snapshot snapshot) {
        if (context == null || snapshot == null) return false;
        try {
            File dir = new File(context.getFilesDir(), "snapshots");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.w(TAG, "Cannot create snapshot dir " + dir);
                return false;
            }
            File file = new File(dir, FILE_NAME);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            try {
                fos.write(serialize(snapshot).getBytes(StandardCharsets.UTF_8));
            } finally {
                fos.close();
            }
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "save failed: " + t.getMessage());
            return false;
        }
    }

    public static Snapshot loadLatest(Context context) {
        if (context == null) return null;
        try {
            File file = new File(new File(context.getFilesDir(), "snapshots"), FILE_NAME);
            if (!file.exists()) return null;
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            try {
                byte[] data = new byte[(int) file.length()];
                int off = 0;
                while (off < data.length) {
                    int n = fis.read(data, off, data.length - off);
                    if (n < 0) break;
                    off += n;
                }
                return deserialize(new String(data, 0, off, StandardCharsets.UTF_8));
            } finally {
                fis.close();
            }
        } catch (Throwable t) {
            Log.w(TAG, "loadLatest failed: " + t.getMessage());
            return null;
        }
    }

    public static boolean restoreLatest(Context context) {
        Snapshot snapshot = loadLatest(context);
        if (snapshot == null) {
            Log.w(TAG, "restoreLatest: no snapshot available");
            return false;
        }
        return restore(context, snapshot, false);
    }

    public static boolean restoreThermal(Context context) {
        Snapshot snapshot = loadLatest(context);
        if (snapshot == null) return false;
        return restore(context, snapshot, true);
    }

    public static boolean restore(Context context, Snapshot snapshot, boolean thermalOnly) {
        if (snapshot == null || snapshot.entries.isEmpty()) return false;
        if (!PrivilegeBridgeEngine.isPrivilegedActive()) {
            Log.w(TAG, "restore skipped: no active privilege");
            return false;
        }

        StringBuilder sysfs = new StringBuilder();
        List<String> others = new ArrayList<>();
        int candidate = 0;
        int applied = 0;

        String minRefresh = null;
        String peakRefresh = null;
        for (Entry e : snapshot.entries) {
            if (e.type == EntryType.SETTING && e.key.equals("system/min_refresh_rate")) minRefresh = e.value;
            if (e.type == EntryType.SETTING && e.key.equals("system/peak_refresh_rate")) peakRefresh = e.value;
        }
        boolean refreshPairOk = refreshPairSafe(minRefresh, peakRefresh);
        if (!refreshPairOk) {
            Log.w(TAG, "Unsafe refresh pair in snapshot (min=" + minRefresh + " peak="
                    + peakRefresh + ") — refresh settings will not be restored");
        }

        for (Entry e : snapshot.entries) {
            if (thermalOnly && !isThermalKey(e.key)) continue;
            candidate++;
            switch (e.type) {
                case SYSFS:
                    sysfs.append("printf '%s' '").append(escape(e.value)).append("' > '")
                            .append(escape(e.key)).append("' 2>/dev/null; ");
                    applied++;
                    break;
                case SETTING: {
                    if (e.key.equals("system/min_refresh_rate") || e.key.equals("system/peak_refresh_rate")) {
                        if (!refreshPairOk) break;
                    }
                    String ns = e.key.substring(0, e.key.indexOf('/'));
                    String name = e.key.substring(e.key.indexOf('/') + 1);
                    if ("null".equals(e.value)) {
                        others.add("settings delete " + ns + " " + name);
                    } else {
                        others.add("settings put " + ns + " " + name + " " + shellQuote(e.value));
                    }
                    applied++;
                    break;
                }
                case PROP:
                    others.add("setprop " + e.key + " " + shellQuote(e.value));
                    applied++;
                    break;
            }
        }

        if (sysfs.length() > 0) {
            CommandExecutor.executeSystemCommand(sysfs.toString());
        }
        for (String cmd : others) {
            CommandExecutor.executeSystemCommand(cmd);
        }

        Log.i(TAG, "Restored " + applied + "/" + candidate + " entries from snapshot '" + snapshot.label + "'");
        return applied > 0;
    }

    /**
     * True when the captured refresh-rate pair is safe to replay.
     * §13: a peak below min can bootloop some OEMs — never write that pair.
     */
    public static boolean refreshPairSafe(String minValue, String peakValue) {
        Integer min = parsePositiveInt(minValue);
        Integer peak = parsePositiveInt(peakValue);
        if (min == null && peak == null) return true;
        if (min == null || peak == null) return true;
        return peak >= min;
    }

    private static Integer parsePositiveInt(String s) {
        if (s == null) return null;
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String serialize(Snapshot snapshot) {
        try {
            JSONObject root = new JSONObject();
            root.put("label", snapshot.label);
            root.put("capturedAt", snapshot.capturedAtMs);
            JSONArray arr = new JSONArray();
            for (Entry e : snapshot.entries) {
                JSONObject o = new JSONObject();
                o.put("t", e.type.name());
                o.put("k", e.key);
                o.put("v", e.value);
                arr.put(o);
            }
            root.put("entries", arr);
            return root.toString();
        } catch (Exception e) {
            throw new IllegalStateException("serialize failed", e);
        }
    }

    public static Snapshot deserialize(String json) {
        try {
            JSONObject root = new JSONObject(json);
            String label = root.optString("label", "unknown");
            long capturedAt = root.optLong("capturedAt", 0L);
            JSONArray arr = root.optJSONArray("entries");
            List<Entry> entries = new ArrayList<>();
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    EntryType type = EntryType.valueOf(o.getString("t"));
                    entries.add(new Entry(type, o.getString("k"), o.getString("v")));
                }
            }
            return new Snapshot(label, capturedAt, entries);
        } catch (Exception e) {
            throw new IllegalStateException("deserialize failed", e);
        }
    }

    private static String escape(String s) {
        return s.replace("'", "'\\''").replace("\n", " ").replace("\r", " ");
    }

    private static String shellQuote(String s) {
        return "'" + escape(s) + "'";
    }
}
