package com.gamebooster.app.shizuku;

import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rikka.shizuku.Shizuku;

/**
 * ShizukuConnectionManager — lifecycle state machine + auto-reconnection.
 *
 * States: IDLE → BINDING → READY ──binder died──▶ DEAD → RETRY (exponential
 * backoff, 500ms → 8s cap, auto-rebind) → READY.
 *
 * The single gate every privileged path awaits is {@link #ensureReady(long)}.
 * All state transitions notify registered listeners so the UI can render
 * "Reconnecting…" and recover without user action. Degraded no-Shizuku
 * operation is untouched: ensureReady returns false and callers keep their
 * existing Tier-2/Tier-3 fallbacks (no silent success, no blocking).
 */
public class ShizukuConnectionManager {

    private static final String TAG = "ShizukuConnMgr";

    private static final long BASE_BACKOFF_MS = 500;
    private static final long MAX_BACKOFF_MS = 8000;
    private static final int MAX_RETRY_ATTEMPTS = 60;
    private static final long CONNECT_POLL_STEP_MS = 50;

    public enum State { IDLE, BINDING, READY, DEAD, RETRY }

    public interface ConnectionListener {
        void onConnectionStateChanged(State state);
    }

    private static final ShizukuConnectionManager INSTANCE = new ShizukuConnectionManager();

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();
    private final Object lock = new Object();

    private volatile State state = State.IDLE;
    private volatile boolean reconnectRunning = false;
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
        if (newState == null || newState == state) return;
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
            } else if (alive) {
                setState(State.IDLE);
            } else {
                // Pending binder handshake — do not jump to DEAD immediately
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
        reconnectRunning = false;
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
                android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                if (ctx != null) {
                    ShizukuPermissionEnforcer.enforceAllPermissions(ctx);
                    ShizukuFileManager.grantAllStoragePermissions(ctx);
                    com.gamebooster.app.tweaks.TweakManagerRepository.restoreAppliedTweaksAsync(ctx);
                }
            } else if (alive) {
                setState(State.IDLE);
            } else {
                setState(State.BINDING);
            }
        } catch (Throwable t) {
            Log.w(TAG, "onBinderReceived error", t);
        }
    }

    /** Binder died — jump to DEAD and start auto-recovery in the background. */
    public void onBinderDead() {
        setState(State.DEAD);
        scheduleReconnect();
    }

    /** A bind attempt failed after waiting — log and keep state consistent. */
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

        boolean binderAlive;
        boolean permissionGranted;
        try {
            binderAlive = Shizuku.pingBinder();
            permissionGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            binderAlive = false;
            permissionGranted = false;
        }

        if (!binderAlive) {
            setState(State.DEAD);
            scheduleReconnect();
            return false;
        }
        if (!permissionGranted) {
            setState(State.IDLE);
            return false;
        }

        // Shizuku is fully granted & alive
        setState(State.READY);

        if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            ShizukuUserServiceConnector.getInstance().bindService();
            if (timeoutMs > 0 && android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
                waitForConnected(Math.min(timeoutMs, 300));
            }
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

    /** Background reconnection loop: exponential backoff, auto-rebinds. */
    private void scheduleReconnect() {
        if (!enabled) return;
        synchronized (lock) {
            if (reconnectRunning) return;
            reconnectRunning = true;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                int attempt = 0;
                while (enabled && attempt < MAX_RETRY_ATTEMPTS) {
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
                        android.content.Context ctx = com.gamebooster.app.GameBoosterApp.getInstance();
                        if (ctx != null) {
                            ShizukuPermissionEnforcer.enforceAllPermissions(ctx);
                            ShizukuFileManager.grantAllStoragePermissions(ctx);
                            com.gamebooster.app.tweaks.TweakManagerRepository.restoreAppliedTweaksAsync(ctx);
                        }
                        return;
                    }

                    if (attempt == 0 || attempt % 10 == 0) {
                        Log.d(TAG, "Reconnect attempt " + attempt + ": Shizuku not ready yet");
                    }
                    if (alive) {
                        setState(State.IDLE);
                    } else if (attempt < 3) {
                        setState(State.BINDING);
                    } else {
                        setState(State.DEAD);
                    }
                    sleepQuietly(backoffMs(attempt++));
                }
                Log.d(TAG, "Reconnect loop finished after " + MAX_RETRY_ATTEMPTS + " attempts");
            } catch (Throwable t) {
                Log.e(TAG, "Reconnect loop error", t);
            } finally {
                synchronized (lock) {
                    reconnectRunning = false;
                }
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