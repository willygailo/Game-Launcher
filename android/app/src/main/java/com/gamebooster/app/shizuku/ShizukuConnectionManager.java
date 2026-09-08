package com.gamebooster.app.shizuku;

import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

/**
 * ShizukuConnectionManager — lifecycle state machine + auto-reconnection.
 *
 * States: IDLE → BINDING → READY ──binder died──▶ DEAD → RETRY (exponential
 * backoff, 500ms → 8s cap, auto-rebind) → READY.
 *
 * Master state is strictly governed by core Shizuku binder (moe.shizuku.privileged.api).
 * Auxiliary AIDL UserService drops do NOT declare Shizuku dead.
 */
public class ShizukuConnectionManager {

    private static final String TAG = "ShizukuConnMgr";

    private static final long BASE_BACKOFF_MS = 500;
    private static final long MAX_BACKOFF_MS = 8000;
    private static final int MAX_BURST_ATTEMPTS = 10;
    private static final long STEADY_POLL_INTERVAL_MS = 10000;
    private static final long CONNECT_POLL_STEP_MS = 50;

    public enum State { IDLE, BINDING, READY, DEAD, RETRY }

    public interface ConnectionListener {
        void onConnectionStateChanged(State state);
    }

    private static final ShizukuConnectionManager INSTANCE = new ShizukuConnectionManager();

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();
    private final Object lock = new Object();

    private volatile State state = State.IDLE;
    private final AtomicBoolean reconnectRunning = new AtomicBoolean(false);
    private volatile boolean enabled = true;
    private volatile boolean hasEverConnected = false;

    private ShizukuConnectionManager() {}

    private boolean isEverGranted(android.content.Context ctx) {
        if (hasEverConnected) return true;
        if (ctx != null) {
            try {
                android.content.SharedPreferences prefs = ctx.getSharedPreferences("game_booster_prefs", android.content.Context.MODE_PRIVATE);
                if (prefs.getBoolean("shizuku_ever_granted", false)) {
                    hasEverConnected = true;
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private void markGranted(android.content.Context ctx) {
        hasEverConnected = true;
        if (ctx != null) {
            try {
                ctx.getSharedPreferences("game_booster_prefs", android.content.Context.MODE_PRIVATE)
                        .edit().putBoolean("shizuku_ever_granted", true).apply();
            } catch (Throwable ignored) {}
            try {
                com.gamebooster.app.services.GameBoosterService.start(ctx);
            } catch (Throwable ignored) {}
        }
    }

    public static ShizukuConnectionManager getInstance() {
        return INSTANCE;
    }

    // ─── Listeners ───────────────────────────────────────────────────────────

    public void addConnectionListener(ConnectionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            try {
                listener.onConnectionStateChanged(state);
            } catch (Throwable ignored) {}
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    private void setState(State newState) {
        if (newState == null) return;
        synchronized (lock) {
            if (newState == state) return;
            state = newState;
        }
        Log.i(TAG, "State → " + newState);
        for (ConnectionListener l : listeners) {
            try {
                l.onConnectionStateChanged(newState);
            } catch (Throwable t) {
                Log.w(TAG, "listener error", t);
            }
        }
    }

    public State getState() {
        return state;
    }

    // ─── Lifecycle ──────────────────────────────────────────────────────────

    /** Reads the actual binder state and converges the state machine. */
    public void start() {
        enabled = true;
        int sdkInt = OsVersionGuard.getReliableSdkInt();
        Log.i(TAG, "Starting ShizukuConnectionManager on " + OsVersionGuard.getOsVersionName() + " (SDK " + sdkInt + ")");
        try {
            android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
            if (ctx != null && !Shizuku.pingBinder()) {
                ShizukuManager.activelyFetchAndAttachBinder(ctx);
            }
            boolean alive = Shizuku.pingBinder();
            boolean granted = alive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                setState(State.READY);
                markGranted(ctx);
                if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    ShizukuUserServiceConnector.getInstance().bindService();
                }
                ShizukuKeepAliveWatchdog.getInstance().onShizukuConnected();
                ShizukuManager.triggerThrottledPostConnectionSync();
            } else if (alive) {
                setState(State.IDLE);
            } else {
                setState(isEverGranted(ctx) ? State.RETRY : State.BINDING);
                scheduleReconnect();
            }
        } catch (Throwable t) {
            android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
            setState(isEverGranted(ctx) ? State.RETRY : State.BINDING);
            scheduleReconnect();
        }
    }

    /**
     * Immediate recheck invoked on Activity/Fragment onResume to instantly restore READY state.
     */
    public void triggerImmediateRecheck(android.content.Context context) {
        if (!enabled) enabled = true;
        AppExecutors.getInstance().executeCommand(() -> {
            if (context != null) {
                ShizukuManager.activelyFetchAndAttachBinder(context);
            }
            if (isReady()) {
                setState(State.READY);
                markGranted(context);
                ShizukuManager.forceNotifyStateChanged();
            } else {
                scheduleReconnect();
            }
        });
    }

    public void stop() {
        enabled = false;
        reconnectRunning.set(false);
        setState(State.IDLE);
        ShizukuUserServiceConnector.getInstance().unbindService();
    }

    // ─── Event sources (driven by ShizukuManager binder listeners) ───────────

    /** Binder received and permission is granted — bind the AIDL user service and mark READY. */
    public void onBinderReceived() {
        try {
            boolean alive = Shizuku.pingBinder();
            boolean granted = alive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                setState(State.READY);
                android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                markGranted(ctx);
                if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    ShizukuUserServiceConnector.getInstance().bindService();
                }
                ShizukuKeepAliveWatchdog.getInstance().onShizukuConnected();
                ShizukuManager.triggerThrottledPostConnectionSync();
            } else if (alive) {
                setState(State.IDLE);
            } else {
                android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                setState(isEverGranted(ctx) ? State.RETRY : State.BINDING);
            }
        } catch (Throwable t) {
            Log.w(TAG, "onBinderReceived error", t);
        }
    }

    /** Binder died — verify with confirmation ping and on-demand provider fetch before transitioning. */
    public void onBinderDead() {
        boolean confirmedDead = true;
        try {
            android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
            if (ctx != null) {
                boolean fetched = ShizukuManager.activelyFetchAndAttachBinder(ctx);
                if (fetched || Shizuku.pingBinder()) {
                    confirmedDead = false;
                }
            }
        } catch (Throwable ignored) {}

        if (confirmedDead) {
            // Gracefully set RETRY instead of immediately declaring DEAD to allow auto-healing
            setState(State.RETRY);
            scheduleReconnect();
        } else {
            Log.d(TAG, "onBinderDead fired, but Shizuku binder was instantly recovered. Preserving READY state.");
            setState(State.READY);
        }
    }

    /** A bind attempt failed after waiting — keep state consistent without killing core Shizuku status. */
    public void onBindFailure() {
        if (isReady()) {
            setState(State.READY);
            return;
        }
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                setState(State.READY);
            } else {
                setState(State.IDLE);
            }
            return;
        }
        scheduleReconnect();
    }

    // ─── The gate ───────────────────────────────────────────────────────────

    /**
     * Best-effort wait until the AIDL user service is connected or Shizuku core is available.
     *
     * @return true when READY (permission granted + binder alive)
     */
    public boolean ensureReady(long timeoutMs) {
        if (!enabled) return false;

        boolean binderAlive = false;
        boolean permissionGranted = false;
        try {
            binderAlive = Shizuku.pingBinder();
            if (binderAlive) {
                permissionGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            }
        } catch (Throwable t) {
            binderAlive = false;
            permissionGranted = false;
        }

        if (binderAlive && permissionGranted) {
            if (state != State.READY) {
                setState(State.READY);
            }
            if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().bindService();
                if (timeoutMs > 0 && android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                    waitForConnected(Math.min(timeoutMs, 250));
                }
            }
            return true;
        }

        if (!binderAlive) {
            // Secondary confirmation check before declaring DEAD to filter out transient blips
            sleepQuietly(60);
            try {
                android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                if (ctx != null) {
                    ShizukuManager.activelyFetchAndAttachBinder(ctx);
                }
                binderAlive = Shizuku.pingBinder();
            } catch (Throwable ignored) {}

            if (!binderAlive) {
                android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                if (isEverGranted(ctx)) {
                    if (state != State.RETRY) {
                        setState(State.RETRY);
                    }
                } else {
                    if (state != State.DEAD) {
                        setState(State.DEAD);
                    }
                }
                scheduleReconnect();
                return false;
            }
        }

        if (!permissionGranted) {
            if (state != State.IDLE) {
                setState(State.IDLE);
            }
            return false;
        }

        if (state != State.READY) {
            setState(State.READY);
        }
        return true;
    }

    // ─── Internals ──────────────────────────────────────────────────────────

    public boolean isReady() {
        try {
            return Shizuku.pingBinder()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean waitForConnected(long timeoutMs) {
        long deadline = System.currentTimeMillis() + Math.max(timeoutMs, 0);
        while (System.currentTimeMillis() < deadline) {
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                return true;
            }
            try {
                Thread.sleep(CONNECT_POLL_STEP_MS);
            } catch (InterruptedException ignored) {}
        }
        return ShizukuUserServiceConnector.getInstance().isServiceConnected();
    }

    /** Background reconnection loop: 15-attempt fast burst, then resilient steady-state polling. */
    private void scheduleReconnect() {
        if (!enabled) return;
        if (!reconnectRunning.compareAndSet(false, true)) {
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                int attempt = 0;
                android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                while (enabled) {
                    boolean alive;
                    boolean granted;
                    try {
                        if (ctx != null) {
                            ShizukuManager.activelyFetchAndAttachBinder(ctx);
                        }
                        alive = Shizuku.pingBinder();
                        granted = alive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
                    } catch (Throwable t) {
                        alive = false;
                        granted = false;
                    }

                    if (alive && granted) {
                        // Shizuku binder and permission are active!
                        setState(State.READY);
                        markGranted(ctx);
                        if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                            ShizukuUserServiceConnector.getInstance().bindService();
                        }
                        ShizukuKeepAliveWatchdog.getInstance().onShizukuConnected();
                        ShizukuManager.triggerThrottledPostConnectionSync();
                        return;
                    }

                    if (attempt == 0 || attempt % 6 == 0) {
                        Log.d(TAG, "Reconnect attempt " + attempt + ": Shizuku not ready yet (alive=" + alive + ", granted=" + granted + ")");
                    }

                    if (alive) {
                        if (state != State.IDLE) setState(State.IDLE);
                    } else if (isEverGranted(ctx)) {
                        // If granted before, NEVER declare permanent DEAD — stay in RETRY and auto-heal
                        if (state != State.RETRY) setState(State.RETRY);
                    } else if (attempt < 5) {
                        if (state != State.RETRY) setState(State.RETRY);
                    } else {
                        if (state != State.DEAD) setState(State.DEAD);
                    }

                    long sleepMs = (attempt < MAX_BURST_ATTEMPTS) ? backoffMs(attempt) : STEADY_POLL_INTERVAL_MS;
                    attempt++;
                    sleepQuietly(sleepMs);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Reconnect loop error", t);
            } finally {
                reconnectRunning.set(false);
            }
        });
    }

    private static long backoffMs(int attempt) {
        if (attempt <= 0) return BASE_BACKOFF_MS;
        long delay = BASE_BACKOFF_MS;
        for (int i = 1; i < Math.min(attempt, 4); i++) {
            delay *= 2;
        }
        return Math.min(delay, 2500);
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}