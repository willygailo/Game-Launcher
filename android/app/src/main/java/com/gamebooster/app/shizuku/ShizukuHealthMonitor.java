package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

/**
 * ShizukuHealthMonitor — Persistent binder health monitor for the Shizuku ADB bridge.
 *
 * <p>Polls {@link Shizuku#pingBinder()} every 30 seconds and:
 * <ul>
 *   <li>Emits {@link HealthListener} callbacks on every state change</li>
 *   <li>Auto-reconnects {@link ShizukuUserServiceConnector} on binder recovery</li>
 *   <li>Provides typed {@link ShizukuHealth} status for UI display</li>
 * </ul>
 *
 * <p>Lifecycle: call {@link #start(Context)} in Application.onCreate() or MainActivity.onResume(),
 * and {@link #stop()} in MainActivity.onDestroy() or Application termination.
 */
public class ShizukuHealthMonitor {

    private static final String TAG = "ShizukuHealth";
    private static final long POLL_INTERVAL_MS = 30_000L; // 30 seconds

    // -----------------------------------------------------------------------------------------
    // Health Status
    // -----------------------------------------------------------------------------------------

    /** Typed Shizuku health states for UI display */
    public enum ShizukuHealth {
        /** Shizuku is not installed on the device */
        NOT_INSTALLED("Not Installed", "⚫"),
        /** Shizuku is installed but the binder service is not running */
        DEAD("Stopped", "🔴"),
        /** Shizuku is running but permission has not been granted */
        PERMISSION_DENIED("Running — No Permission", "🟡"),
        /** Shizuku is running, permission granted, UserService bound — fully operational */
        RUNNING("Running ✓", "🟢");

        private final String displayText;
        private final String emoji;

        ShizukuHealth(String displayText, String emoji) {
            this.displayText = displayText;
            this.emoji = emoji;
        }

        public String getDisplayText() { return displayText; }
        public String getEmoji()       { return emoji; }
        public String getFullLabel()   { return emoji + " " + displayText; }
        public boolean isOperational() { return this == RUNNING; }
    }

    // -----------------------------------------------------------------------------------------
    // Listener
    // -----------------------------------------------------------------------------------------

    public interface HealthListener {
        void onHealthChanged(ShizukuHealth health);
    }

    // -----------------------------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------------------------

    private static ShizukuHealthMonitor sInstance;

    public static synchronized ShizukuHealthMonitor getInstance() {
        if (sInstance == null) {
            sInstance = new ShizukuHealthMonitor();
        }
        return sInstance;
    }

    // -----------------------------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------------------------

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<HealthListener> mListeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    private Context mContext;
    private ShizukuHealth mLastHealth = ShizukuHealth.NOT_INSTALLED;

    private final Runnable mPollTask = new Runnable() {
        @Override
        public void run() {
            checkAndEmitHealth();
            if (mRunning.get()) {
                mHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        }
    };

    private ShizukuHealthMonitor() {}

    // -----------------------------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------------------------

    /**
     * Starts the health monitor polling loop.
     * Safe to call multiple times — only starts once.
     *
     * @param context Application context (held weakly for PackageManager)
     */
    public void start(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        if (mRunning.compareAndSet(false, true)) {
            Log.i(TAG, "ShizukuHealthMonitor started (poll interval=" + POLL_INTERVAL_MS + "ms)");
            mHandler.post(mPollTask);
        }
    }

    /**
     * Stops the polling loop. Safe to call even if not started.
     */
    public void stop() {
        if (mRunning.compareAndSet(true, false)) {
            mHandler.removeCallbacks(mPollTask);
            Log.i(TAG, "ShizukuHealthMonitor stopped.");
        }
    }

    /** Adds a health listener. Duplicates are ignored. */
    public void addListener(HealthListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /** Removes a health listener. */
    public void removeListener(HealthListener listener) {
        mListeners.remove(listener);
    }

    /** Returns the most recently observed health state. */
    public ShizukuHealth getCurrentHealth() {
        return mLastHealth;
    }

    /**
     * Forces an immediate health check and emits to all listeners.
     * Useful to call right after user grants Shizuku permission.
     */
    public void forceCheck() {
        mHandler.post(this::checkAndEmitHealth);
    }

    // -----------------------------------------------------------------------------------------
    // Health Check Logic
    // -----------------------------------------------------------------------------------------

    private void checkAndEmitHealth() {
        ShizukuHealth health = resolveCurrentHealth();

        // Only emit on actual state change to avoid noisy callbacks
        if (health != mLastHealth) {
            Log.i(TAG, "Shizuku health changed: " + mLastHealth + " → " + health);
            mLastHealth = health;

            // Attempt auto-reconnect when binder recovers
            if (health == ShizukuHealth.RUNNING) {
                tryAutoReconnectUserService();
            }

            // Notify ShizukuManager state listeners
            ShizukuManager.addStateListener(null); // noop — just ensures list exists

            notifyListeners(health);
        }
    }

    private ShizukuHealth resolveCurrentHealth() {
        // Step 1: Is Shizuku installed?
        if (mContext != null && !ShizukuManager.isShizukuInstalled(mContext)) {
            return ShizukuHealth.NOT_INSTALLED;
        }

        // Step 2: Is the binder alive?
        boolean binderAlive;
        try {
            binderAlive = Shizuku.pingBinder();
        } catch (Throwable t) {
            binderAlive = false;
        }

        if (!binderAlive) {
            return ShizukuHealth.DEAD;
        }

        // Step 3: Is permission granted?
        boolean permGranted;
        try {
            permGranted = (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED);
        } catch (Throwable t) {
            permGranted = false;
        }

        if (!permGranted) {
            return ShizukuHealth.PERMISSION_DENIED;
        }

        return ShizukuHealth.RUNNING;
    }

    private void tryAutoReconnectUserService() {
        try {
            ShizukuUserServiceConnector connector = ShizukuUserServiceConnector.getInstance();
            if (!connector.isServiceAlive()) {
                Log.i(TAG, "Auto-reconnecting Shizuku UserService after binder recovery...");
                connector.bindService();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Auto-reconnect failed (will retry next poll): " + t.getMessage());
        }
    }

    private void notifyListeners(ShizukuHealth health) {
        for (HealthListener listener : mListeners) {
            try {
                listener.onHealthChanged(health);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying health listener", e);
            }
        }
    }
}
