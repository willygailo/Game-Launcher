package com.gamebooster.app.overlay;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GameNetworkLatencyEngine — Real-Time Gaming Ping, Jitter & Latency Engine.
 *
 * Continuously measures network response time and packet jitter to top gaming DNS
 * and server clusters. Supplies live ping telemetry to the in-game HUD and Game Turbo Drawer.
 */
public final class GameNetworkLatencyEngine {

    private static final String TAG = "GameNetworkLatency";
    private static GameNetworkLatencyEngine sInstance;

    public enum ConnectionQuality {
        EXCELLENT("🟢 EXCELLENT", "#00FF66"),
        GOOD("🟡 GOOD", "#FFCC00"),
        FAIR("🟠 FAIR", "#FF9900"),
        POOR("🔴 HIGH PING", "#FF0055");

        public final String label;
        public final String hexColor;

        ConnectionQuality(String label, String hexColor) {
            this.label = label;
            this.hexColor = hexColor;
        }
    }

    public static class NetworkTelemetry {
        public final int pingMs;
        public final int jitterMs;
        public final ConnectionQuality quality;

        public NetworkTelemetry(int pingMs, int jitterMs, ConnectionQuality quality) {
            this.pingMs = pingMs;
            this.jitterMs = jitterMs;
            this.quality = quality;
        }
    }

    public interface LatencyListener {
        void onLatencyUpdate(NetworkTelemetry telemetry);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private LatencyListener listener;

    private int lastPing = -1;

    public static synchronized GameNetworkLatencyEngine getInstance() {
        if (sInstance == null) {
            sInstance = new GameNetworkLatencyEngine();
        }
        return sInstance;
    }

    private GameNetworkLatencyEngine() {}

    public synchronized void start(LatencyListener listener) {
        this.listener = listener;
        if (isRunning.getAndSet(true)) {
            return;
        }

        executor.execute(this::runPingLoop);
    }

    public synchronized void stop() {
        isRunning.set(false);
        listener = null;
    }

    private void runPingLoop() {
        // Fast DNS servers used for low-overhead TCP handshake ping
        final String[] probeTargets = {"1.1.1.1", "8.8.8.8", "9.9.9.9"};
        int targetIdx = 0;

        while (isRunning.get()) {
            String target = probeTargets[targetIdx % probeTargets.length];
            targetIdx++;

            long start = System.currentTimeMillis();
            boolean success = false;
            int ping = -1;

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(target, 53), 1200);
                long elapsed = System.currentTimeMillis() - start;
                ping = (int) elapsed;
                success = true;
            } catch (IOException ignored) {
                // Socket connect timeout or network unreachable
            }

            if (!success || ping <= 0) {
                // Fallback to minimal positive indication or retry
                ping = 999;
            }

            int jitter = 0;
            if (lastPing > 0 && ping < 900) {
                jitter = Math.abs(ping - lastPing);
            }
            lastPing = ping;

            ConnectionQuality quality;
            if (ping <= 35) {
                quality = ConnectionQuality.EXCELLENT;
            } else if (ping <= 65) {
                quality = ConnectionQuality.GOOD;
            } else if (ping <= 115) {
                quality = ConnectionQuality.FAIR;
            } else {
                quality = ConnectionQuality.POOR;
            }

            NetworkTelemetry telemetry = new NetworkTelemetry(ping, jitter, quality);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onLatencyUpdate(telemetry);
                }
            });

            try {
                // Measure every 1.5 seconds to save battery and network bandwidth
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
