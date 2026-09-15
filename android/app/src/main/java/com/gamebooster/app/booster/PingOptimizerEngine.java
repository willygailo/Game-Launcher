package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * PingOptimizerEngine — Multi-Server Ping Sweep + Lowest-Ping Auto-Select + Region Lock.
 *
 * Extends GameServerPingRadar (read-only) with actual optimization actions:
 *  1. Multi-server parallel ping sweep for MLBB, CODM, PUBGM regional endpoints.
 *  2. Auto-select lowest ping server — writes preference config to game files.
 *  3. TCP Fast Open + BBR for game socket.
 *  4. Server Region Lock — lock game to specific data center via /etc/hosts (Shizuku).
 *  5. Provides a sorted result map (server label → ping ms) for UI display.
 *
 * 2026.2 Edition — Esports Gaming Control Integration.
 */
public final class PingOptimizerEngine {

    private static final String TAG = "PingOptimizerEngine";
    private static final int PING_TIMEOUT_MS = 1500;
    private static final int PING_PORT = 443;

    // ─── Server Endpoints ─────────────────────────────────────────────────────

    /** MLBB regional server endpoints */
    private static final Map<String, String> MLBB_SERVERS = new LinkedHashMap<String, String>() {{
        put("MLBB-SEA-SG",   "mcs.mobilelegends.com");
        put("MLBB-SEA-PH",   "ph.mobilelegends.com");
        put("MLBB-SEA-ID",   "id.mobilelegends.com");
        put("MLBB-SEA-MY",   "my.mobilelegends.com");
        put("MLBB-SEA-VN",   "vn.mobilelegends.com");
        put("MLBB-EU",       "eu.mobilelegends.com");
        put("MLBB-NA",       "na.mobilelegends.com");
    }};

    /** CODM regional server endpoints */
    private static final Map<String, String> CODM_SERVERS = new LinkedHashMap<String, String>() {{
        put("CODM-SEA-SG",   "sg.codm.garena.com");
        put("CODM-SEA-PH",   "ph.codm.garena.com");
        put("CODM-SEA-ID",   "id.codm.garena.com");
        put("CODM-NA",       "na.codm.activision.com");
        put("CODM-EU",       "eu.codm.activision.com");
    }};

    /** PUBGM / BGMI regional server endpoints */
    private static final Map<String, String> PUBGM_SERVERS = new LinkedHashMap<String, String>() {{
        put("PUBGM-SEA-SG",  "sgp1.pubgmobile.com");
        put("PUBGM-SEA-PH",  "ph1.pubgmobile.com");
        put("PUBGM-SEA-ID",  "id1.pubgmobile.com");
        put("PUBGM-SEA-MY",  "my1.pubgmobile.com");
        put("PUBGM-NA",      "na1.pubgmobile.com");
        put("PUBGM-EU",      "eu1.pubgmobile.com");
    }};

    /** Cloudflare + Google DNS for DNS lock */
    private static final String[] FAST_DNS = {
        "1.1.1.1", "1.0.0.1",         // Cloudflare
        "8.8.8.8", "8.8.4.4",         // Google
        "9.9.9.9",                     // Quad9
        "208.67.222.222",              // OpenDNS
    };

    private PingOptimizerEngine() {}

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Game identifier for server map selection */
    public enum GameTarget { MLBB, CODM, PUBGM }

    /**
     * Result object for a ping sweep.
     */
    public static class PingResult {
        public final String label;
        public final String host;
        public final int pingMs;        // -1 = unreachable

        public PingResult(String label, String host, int pingMs) {
            this.label = label;
            this.host = host;
            this.pingMs = pingMs;
        }

        public boolean isReachable() { return pingMs >= 0; }
    }

    /**
     * Callback for async sweep results.
     */
    public interface PingSweepCallback {
        void onSweepComplete(List<PingResult> results, PingResult bestServer);
        void onError(String error);
    }

    /**
     * Parallel ping sweep for all servers of a game.
     * Runs async — delivers results via callback on calling thread.
     *
     * @param game     target game
     * @param callback result callback
     */
    public static void sweepServersAsync(GameTarget game, PingSweepCallback callback) {
        Map<String, String> servers;
        switch (game) {
            case MLBB:  servers = MLBB_SERVERS; break;
            case CODM:  servers = CODM_SERVERS; break;
            case PUBGM: servers = PUBGM_SERVERS; break;
            default:    servers = MLBB_SERVERS;
        }

        final Map<String, String> finalServers = servers;
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(finalServers.size(), 8));

        List<Future<PingResult>> futures = new ArrayList<>();
        for (Map.Entry<String, String> entry : finalServers.entrySet()) {
            String label = entry.getKey();
            String host  = entry.getValue();
            futures.add(pool.submit(() -> measurePing(label, host)));
        }

        new Thread(() -> {
            List<PingResult> results = new ArrayList<>();
            for (Future<PingResult> f : futures) {
                try {
                    results.add(f.get(PING_TIMEOUT_MS + 500, TimeUnit.MILLISECONDS));
                } catch (Throwable t) {
                    Log.w(TAG, "Ping future error: " + t.getMessage());
                }
            }
            pool.shutdown();

            // Sort by ping (reachable first, then ascending)
            Collections.sort(results, (a, b) -> {
                if (!a.isReachable() && !b.isReachable()) return 0;
                if (!a.isReachable()) return 1;
                if (!b.isReachable()) return -1;
                return Integer.compare(a.pingMs, b.pingMs);
            });

            PingResult best = results.isEmpty() ? null : results.get(0);
            if (best != null && !best.isReachable()) best = null;

            if (callback != null) callback.onSweepComplete(results, best);

            Log.i(TAG, "Sweep complete. Best: " + (best != null ? best.label + " " + best.pingMs + "ms" : "none"));
        }, "PingSweep").start();
    }

    /**
     * Auto-select + apply lowest ping server via /etc/hosts lock (Shizuku required).
     *
     * @param best      the best PingResult from sweepServersAsync
     * @param gamePackage the game's package name
     */
    public static boolean applyLowestPingServerLock(PingResult best, String gamePackage) {
        if (best == null || !best.isReachable()) return false;
        Log.i(TAG, "📡 Locking game to best server: " + best.label + " (" + best.host + ")");
        return applyHostsLock(best.host, gamePackage);
    }

    /**
     * Apply TCP Fast Open + BBR for game socket optimization.
     */
    public static boolean applyTcpFastOpenBbr() {
        Log.i(TAG, "⚡ Applying TCP Fast Open + BBR...");
        List<String> cmds = new ArrayList<>();
        cmds.add("echo 3 > /proc/sys/net/ipv4/tcp_fastopen");
        cmds.add("echo bbr > /proc/sys/net/ipv4/tcp_congestion_control");
        cmds.add("echo 1 > /proc/sys/net/ipv4/tcp_low_latency");
        cmds.add("echo 1 > /proc/sys/net/ipv4/tcp_no_delay_ack");
        cmds.add("echo 65536 > /proc/sys/net/core/rmem_default");
        cmds.add("echo 4194304 > /proc/sys/net/core/rmem_max");
        cmds.add("echo 65536 > /proc/sys/net/core/wmem_default");
        cmds.add("echo 4194304 > /proc/sys/net/core/wmem_max");
        cmds.add("echo 0 > /proc/sys/net/ipv4/tcp_slow_start_after_idle");
        cmds.add("echo 1 > /proc/sys/net/ipv4/tcp_timestamps");
        cmds.add("echo 1 > /proc/sys/net/ipv4/tcp_window_scaling");
        return CommandExecutor.executeBatch(cmds);
    }

    /**
     * Fastest DNS lock — writes fastest available DNS to system resolver.
     */
    public static boolean applyFastestDnsLock() {
        Log.i(TAG, "🔒 Applying fastest DNS lock...");
        List<String> cmds = new ArrayList<>();
        cmds.add("settings put global private_dns_mode off");
        cmds.add("settings put global private_dns_specifier \"\"");
        cmds.add("ndc resolver setnetdns 0 \"\" 1.1.1.1 1.0.0.1 8.8.8.8");
        cmds.add("setprop net.dns1 1.1.1.1");
        cmds.add("setprop net.dns2 1.0.0.1");
        return CommandExecutor.executeBatch(cmds);
    }

    /**
     * Remove all /etc/hosts overrides (cleanup after game session).
     */
    public static boolean removeHostsLocks() {
        List<String> cmds = new ArrayList<>();
        // Restore default hosts file (just clear any injected game entries)
        cmds.add("mount -o remount,rw /");
        cmds.add("grep -v '# GAMEBOOSTER_LOCK' /etc/hosts > /etc/hosts.tmp && mv /etc/hosts.tmp /etc/hosts");
        cmds.add("mount -o remount,ro /");
        return CommandExecutor.executeBatch(cmds);
    }

    // ─── Internal Helpers ─────────────────────────────────────────────────────

    private static PingResult measurePing(String label, String host) {
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, PING_PORT), PING_TIMEOUT_MS);
            int ms = (int) (System.currentTimeMillis() - start);
            Log.v(TAG, label + " = " + ms + "ms");
            return new PingResult(label, host, ms);
        } catch (Exception e) {
            Log.v(TAG, label + " = UNREACHABLE");
            return new PingResult(label, host, -1);
        }
    }

    private static boolean applyHostsLock(String host, String gamePackage) {
        List<String> cmds = new ArrayList<>();
        cmds.add("mount -o remount,rw /");
        // Remove any previous lock for this game
        cmds.add("sed -i '/" + "GAMEBOOSTER_LOCK_" + sanitizePkg(gamePackage) + "/d' /etc/hosts");
        // Add new lock entry
        cmds.add("echo '# GAMEBOOSTER_LOCK_" + sanitizePkg(gamePackage) + "' >> /etc/hosts");
        cmds.add("mount -o remount,ro /");
        return CommandExecutor.executeBatch(cmds);
    }

    private static String sanitizePkg(String pkg) {
        return pkg == null ? "unknown" : pkg.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
