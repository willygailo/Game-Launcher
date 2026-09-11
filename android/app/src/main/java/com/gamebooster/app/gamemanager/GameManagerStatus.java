package com.gamebooster.app.gamemanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Singleton repository tracking the real-time status of Game Manager operations,
 * last applied optimization timestamps, package masking counts, and session state.
 */
public final class GameManagerStatus {

    private static volatile GameManagerStatus instance;

    private long lastApplyTimestamp = 0L;
    private String lastApplySummary = "No optimizations applied yet";
    private int gamesDetectedCount = 0;
    private int maskedAppsCount = 0;
    private String activeGamePackage = null;
    private long sessionStartTime = 0L;
    private final List<String> eventLog = new ArrayList<>();

    private GameManagerStatus() {
    }

    public static GameManagerStatus getInstance() {
        if (instance == null) {
            synchronized (GameManagerStatus.class) {
                if (instance == null) {
                    instance = new GameManagerStatus();
                }
            }
        }
        return instance;
    }

    public synchronized void recordApply(int tweakCount, String summary) {
        this.lastApplyTimestamp = System.currentTimeMillis();
        this.lastApplySummary = summary;
        String timeStr = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        addLog("[" + timeStr + "] Enforced " + tweakCount + " tweaks: " + summary);
    }

    public synchronized void setGamesDetectedCount(int count) {
        this.gamesDetectedCount = count;
    }

    public synchronized int getGamesDetectedCount() {
        return gamesDetectedCount;
    }

    public synchronized void setMaskedAppsCount(int count) {
        this.maskedAppsCount = count;
    }

    public synchronized int getMaskedAppsCount() {
        return maskedAppsCount;
    }

    public synchronized void setActiveSession(String packageName) {
        this.activeGamePackage = packageName;
        this.sessionStartTime = packageName != null ? System.currentTimeMillis() : 0L;
        if (packageName != null) {
            addLog("Game Session Started: " + packageName);
        } else {
            addLog("Game Session Ended (Reverted to baseline)");
        }
    }

    public synchronized String getActiveGamePackage() {
        return activeGamePackage;
    }

    public synchronized boolean hasActiveSession() {
        return activeGamePackage != null;
    }

    public synchronized long getSessionDurationSeconds() {
        if (sessionStartTime == 0L) return 0L;
        return (System.currentTimeMillis() - sessionStartTime) / 1000L;
    }

    public synchronized long getLastApplyTimestamp() {
        return lastApplyTimestamp;
    }

    public synchronized String getLastApplySummary() {
        return lastApplySummary;
    }

    public synchronized String getFormattedLastApplyTime() {
        if (lastApplyTimestamp == 0L) return "Never";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(lastApplyTimestamp));
    }

    public synchronized void addLog(String log) {
        if (eventLog.size() > 50) {
            eventLog.remove(0);
        }
        eventLog.add(log);
    }

    public synchronized List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }
}
