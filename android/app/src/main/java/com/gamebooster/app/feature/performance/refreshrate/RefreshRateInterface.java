package com.gamebooster.app.feature.performance.refreshrate;

public interface RefreshRateInterface {
    /**
     * Forces the refresh rate (120Hz, 144Hz, 165Hz) globally for the current device strategy.
     * @param targetHz Refresh rate integer.
     * @return Execution log / status string.
     */
    String forceRefreshRate(int targetHz);

    /**
     * Forces the refresh rate for a specific target game package.
     * @param targetHz Refresh rate integer.
     * @param packageName Target package name (optional/nullable).
     * @return Execution log / status string.
     */
    default String forceRefreshRate(int targetHz, String packageName) {
        return forceRefreshRate(targetHz);
    }

    /**
     * Resets refresh rate to system automatic behavior.
     * @return Execution log / status string.
     */
    String resetRefreshRate();

    /**
     * Resets refresh rate and per-game overrides for target package.
     * @param packageName Target package name (optional/nullable).
     * @return Execution log / status string.
     */
    default String resetRefreshRate(String packageName) {
        return resetRefreshRate();
    }

    /**
     * Gets strategy name.
     */
    String getStrategyName();

    /**
     * Checks if supported by device.
     */
    boolean isSupported();
}

