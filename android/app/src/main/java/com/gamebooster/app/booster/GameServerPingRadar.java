package com.gamebooster.app.booster;

import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameServerPingRadar {

    private static final String TAG = "GameServerPingRadar";

    public static class ServerTarget {
        public final String gameTitle;
        public final String region;
        public final String host;
        public final int testPort;

        public ServerTarget(String gameTitle, String region, String host, int testPort) {
            this.gameTitle = gameTitle;
            this.region = region;
            this.host = host;
            this.testPort = testPort;
        }
    }

    public static class PingResult {
        public final String gameTitle;
        public final String region;
        public final long latencyMs;
        public final long jitterMs;
        public final int packetLossPercent;
        public final boolean reachable;

        public PingResult(String gameTitle, String region, long latencyMs, long jitterMs, int packetLossPercent, boolean reachable) {
            this.gameTitle = gameTitle;
            this.region = region;
            this.latencyMs = latencyMs;
            this.jitterMs = jitterMs;
            this.packetLossPercent = packetLossPercent;
            this.reachable = reachable;
        }

        public String getQualityTier() {
            if (!reachable || latencyMs < 0) return "OFFLINE / TIMEOUT";
            if (latencyMs < 35) return "⚡ ULTRA LOW PING (TOURNAMENT)";
            if (latencyMs < 65) return "● EXCELLENT (SMOOTH)";
            if (latencyMs < 100) return "○ GOOD / ACCEPTABLE";
            return "▲ HIGH LATENCY";
        }

        public int getQualityColor() {
            if (!reachable || latencyMs < 0) return 0xFFEF4444; // Red
            if (latencyMs < 35) return 0xFF00FF66; // Neon Green
            if (latencyMs < 65) return 0xFF00F0FF; // Cyan
            if (latencyMs < 100) return 0xFFFACC15; // Yellow
            return 0xFFF97316; // Orange
        }
    }

    public static final List<ServerTarget> DEFAULT_TARGETS;

    static {
        List<ServerTarget> list = new ArrayList<>();
        list.add(new ServerTarget("Mobile Legends: Bang Bang", "🇵🇭 PH / 🇸🇬 SG Core", "161.117.155.1", 443));
        list.add(new ServerTarget("PUBG Mobile", "🇭🇰 HK / 🇸🇬 SG Gateway", "43.153.25.1", 443));
        list.add(new ServerTarget("Call of Duty: Mobile", "🇸🇬 Garena SEA Gateway", "203.117.155.1", 443));
        list.add(new ServerTarget("League of Legends: Wild Rift", "🇸🇬 Riot SEA Gateway", "103.152.34.1", 443));
        list.add(new ServerTarget("Roblox Asia", "🌏 Asia Pacific Edge", "128.116.119.1", 443));
        list.add(new ServerTarget("Cloudflare Gaming DNS", "🌐 Fast Global 1.1.1.1", "1.1.1.1", 53));
        list.add(new ServerTarget("Google Public DNS", "🌐 Fast Global 8.8.8.8", "8.8.8.8", 53));
        DEFAULT_TARGETS = Collections.unmodifiableList(list);
    }

    public static PingResult pingServer(ServerTarget target) {
        if (target == null) return new PingResult("Unknown", "N/A", -1, 0, 100, false);

        final int sampleCount = 3;
        List<Long> samples = new ArrayList<>();
        int failedSamples = 0;

        for (int i = 0; i < sampleCount; i++) {
            long t0 = System.currentTimeMillis();
            boolean ok = false;
            try {
                // Try TCP socket connect with short 1200ms timeout
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(target.host, target.testPort), 1200);
                    ok = true;
                }
            } catch (Throwable t) {
                // Fallback to ICMP reachable
                try {
                    InetAddress address = InetAddress.getByName(target.host);
                    ok = address.isReachable(1000);
                } catch (Throwable ignored) {}
            }

            long elapsed = System.currentTimeMillis() - t0;
            if (ok && elapsed > 0) {
                samples.add(elapsed);
            } else {
                failedSamples++;
            }

            try { Thread.sleep(30); } catch (InterruptedException ignored) {}
        }

        if (samples.isEmpty()) {
            return new PingResult(target.gameTitle, target.region, -1, 0, 100, false);
        }

        long sum = 0;
        for (long s : samples) sum += s;
        long avgLatency = sum / samples.size();

        long jitterSum = 0;
        for (long s : samples) jitterSum += Math.abs(s - avgLatency);
        long jitter = jitterSum / samples.size();

        int packetLoss = (int) Math.round(((double) failedSamples / sampleCount) * 100.0);

        return new PingResult(target.gameTitle, target.region, avgLatency, jitter, packetLoss, true);
    }

    public static List<PingResult> pingAllServers() {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(DEFAULT_TARGETS.size());
        List<java.util.concurrent.Future<PingResult>> futures = new ArrayList<>();
        for (ServerTarget target : DEFAULT_TARGETS) {
            futures.add(executor.submit(() -> pingServer(target)));
        }
        List<PingResult> results = new ArrayList<>();
        for (java.util.concurrent.Future<PingResult> f : futures) {
            try {
                results.add(f.get(3, java.util.concurrent.TimeUnit.SECONDS));
            } catch (Throwable t) {
                // Ignore timeout
            }
        }
        executor.shutdown();
        return results;
    }
}
