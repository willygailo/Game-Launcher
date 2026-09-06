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

    private ShizukuConnectionManager() {}

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
        try {
            boolean alive = Shizuku.pingBinder();
            boolean granted = alive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                setState(State.READY);
                if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    ShizukuUserServiceConnector.getInstance().bindService();
                }
                ShizukuKeepAliveWatchdog.getInstance().onShizukuConnected();
                ShizukuManager.triggerThrottledPostConnectionSync();
            } else if (alive) {
                setState(State.IDLE);
            } else {
                setState(State.BINDING);
                scheduleReconnect();
            }
        } catch (Throwable t) {
            setState(State.BINDING);
            scheduleReconnect();
        }
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
                if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    ShizukuUserServiceConnector.getInstance().bindService();
                }
                ShizukuKeepAliveWatchdog.getInstance().onShizukuConnected();
                ShizukuManager.triggerThrottledPostConnectionSync();
            } else if (alive) {
                setState(State.IDLE);
            } else {
                setState(State.BINDING);
            }
        } catch (Throwable t) {
            Log.w(TAG, "onBinderReceived error", t);
        }
    }

    /** Binder died — verify with confirmation ping before transitioning to DEAD. */
    public void onBinderDead() {
        boolean confirmedDead = true;
        try {
            if (Shizuku.pingBinder()) {
                confirmedDead = false;
            }
        } catch (Throwable ignored) {}

        if (confirmedDead) {
            setState(State.DEAD);
            scheduleReconnect();
        } else {
            Log.d(TAG, "onBinderDead fired, but Shizuku.pingBinder() is still alive. Preserving READY state.");
        }
    }

    /** A bind attempt failed after waiting — keep state consistent. */
    public void onBindFailure() {
        if (isReady()) {
            setState(State.READY);
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
                binderAlive = Shizuku.pingBinder();
            } catch (Throwable ignored) {}

            if (!binderAlive) {
                if (state != State.DEAD) {
                    setState(State.DEAD);
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

    /** Background reconnection loop: 10-attempt fast exponential burst, then infinite steady-state polling. */
    private void scheduleReconnect() {
        if (!enabled) return;
        if (!reconnectRunning.compareAndSet(false, true)) {
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                int attempt = 0;
                while (enabled) {
                    boolean alive;
                    boolean granted;
                    try {
                        alive = Shizuku.pingBinder();
                        granted = alive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
                    } catch (Throwable t) {
                        alive = false;
                        granted = false;
                    }

                    if (alive && granted) {
                        // Shizuku binder and permission are active!
                        setState(State.READY);
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
                    } else if (attempt < 3) {
                        if (state != State.BINDING) setState(State.BINDING);
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
        for (int i = 1; i < Math.min(attempt, 5); i++) {
            delay *= 2;
        }
        return Math.min(delay, MAX_BACKOFF_MS);
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}