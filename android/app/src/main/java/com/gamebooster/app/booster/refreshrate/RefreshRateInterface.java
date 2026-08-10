package com.gamebooster.app.booster.refreshrate;

public interface RefreshRateInterface {
    /**
     * Forces the refresh rate (120Hz, 144Hz, 165Hz) for the current device strategy.
     * @param targetHz Refresh rate integer.
     * @return Execution log / status string.
     */
    String forceRefreshRate(int targetHz);

    /**
     * Resets refresh rate to system automatic behavior.
     * @return Execution log / status string.
     */
    String resetRefreshRate();

    /**
     * Gets strategy name.
     */
    String getStrategyName();

    /**
     * Checks if supported by device.
     */
    boolean isSupported();
}
