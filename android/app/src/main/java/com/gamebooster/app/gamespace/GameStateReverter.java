package com.gamebooster.app.gamespace;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.NetworkTweaksChannel;
import com.gamebooster.app.booster.ThermalChannel;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.engine.NativeFrameworkBridge;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

/**
 * GameStateReverter — restores the user's baseline system state when a game session ends.
 *
 * The launch path (AutoGameMonitorService) forces the display to the game target Hz,
 * switches CPU/GPU governors to performance, overrides thermal control, enables low
 * latency network tweaks and gaming DND. This class reverts each of those channels
 * back to the values captured by GameSessionSettings.begin() so the device does not
 * stay locked at 185 Hz / boosted state in the launcher.
 */
public final class GameStateReverter {

    private static final String TAG = "GameStateReverter";

    static final int DEFAULT_BASELINE_HZ = 120;

    private GameStateReverter() {}

    public static final class RevertReport {
        public final boolean sessionActive;
        public final boolean refreshRateRestored;
        public final boolean governorRestored;
        public final boolean thermalRestored;
        public final boolean networkRestored;
        public final boolean dndRestored;
        public final int restoredHz;
        public final String message;

        private RevertReport(boolean sessionActive, boolean refreshRateRestored,
                             boolean governorRestored, boolean thermalRestored,
                             boolean networkRestored, boolean dndRestored,
                             int restoredHz, String message) {
            this.sessionActive = sessionActive;
            this.refreshRateRestored = refreshRateRestored;
            this.governorRestored = governorRestored;
            this.thermalRestored = thermalRestored;
            this.networkRestored = networkRestored;
            this.dndRestored = dndRestored;
            this.restoredHz = restoredHz;
            this.message = message;
        }

        static RevertReport idle(int restoredHz, String message) {
            return new RevertReport(false, false, false, false, false, false, restoredHz, message);
        }

        static RevertReport active(int restoredHz, String message) {
            return new RevertReport(true, true, true, true, true, true, restoredHz, message);
        }
    }

    /**
     * Pure decision logic — computes what a revert should do from the captured baseline.
     * Unit-testable on the JVM without any Android system service access.
     *
     * @param previousHz    refresh rate captured by GameSessionSettings.begin() (0 = unknown)
     * @param previousDnd   gaming DND state captured at session start
     * @param sessionActive whether a game session is currently open
     */
    public static RevertReport evaluate(int previousHz, boolean previousDnd, boolean sessionActive) {
        if (!sessionActive) {
            return RevertReport.idle(0, "No active game session — nothing to revert");
        }
        int hz = previousHz > 0 ? previousHz : DEFAULT_BASELINE_HZ;
        return RevertReport.active(hz, "Reverting to baseline: display " + hz
                + "Hz, default CPU/GPU governor, thermal, network and DND state restored");
    }

    /**
     * Reverts every optimization channel applied at game launch back to the baseline
     * captured by GameSessionSettings.begin(). Safe to call at any time — reports idle
     * (and does nothing) when no session is open.
     */
    public static RevertReport revertToBaseline(Context context) {
        if (context == null) {
            return RevertReport.idle(0, "No active game session — nothing to revert");
        }
        boolean sessionActive = GameSessionSettings.hasActiveSession(context);
        int previousHz = GameSessionSettings.getStoredPreviousHz(context);
        boolean previousDnd = GameSessionSettings.getStoredPreviousDnd(context);
        RevertReport report = evaluate(previousHz, previousDnd, sessionActive);
        if (!sessionActive) {
            Log.i(TAG, "revertToBaseline: no active session — " + report.message);
            return report;
        }

        final int hz = report.restoredHz;
        try { MaxHzForceChannel.forceApply(hz); } catch (Throwable t) { Log.w(TAG, "Refresh rate revert failed", t); }
        try { HzFpsChannel.forceSetRefreshRate(context, hz); } catch (Throwable t) { Log.w(TAG, "HzFpsChannel revert failed", t); }
        try { ShizukuUserServiceConnector.getInstance().forceDisplayRefreshRate(hz); } catch (Throwable t) { Log.w(TAG, "Display service revert failed", t); }
        try { ShizukuUserServiceConnector.getInstance().restoreCpuGpuGovernors(); } catch (Throwable t) { Log.w(TAG, "CPU/GPU governor revert failed", t); }
        try { ThermalChannel.setThermalOverride(false); } catch (Throwable t) { Log.w(TAG, "Thermal revert failed", t); }
        try { NetworkTweaksChannel.restoreLowLatencyNetwork(); } catch (Throwable t) { Log.w(TAG, "Network revert failed", t); }
        try { NativeFrameworkBridge.releaseLowLatencyWifiLock(); } catch (Throwable t) { Log.w(TAG, "Wifi lock release failed", t); }
        try { NativeFrameworkBridge.releaseSustainedPerformanceLock(); } catch (Throwable t) { Log.w(TAG, "Wake lock release failed", t); }
        try { NativeFrameworkBridge.stopAdpfSession(); } catch (Throwable t) { Log.w(TAG, "ADPF session stop failed", t); }
        try { GameSpaceDndManager.setGamingDndMode(context, previousDnd); } catch (Throwable t) { Log.w(TAG, "DND restore failed", t); }

        GameSessionSettings.closeSession(context);
        Log.i(TAG, "revertToBaseline done: " + hz + "Hz — " + report.message);
        return report;
    }
}