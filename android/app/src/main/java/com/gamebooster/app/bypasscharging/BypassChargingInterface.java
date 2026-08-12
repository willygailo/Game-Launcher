package com.gamebooster.app.bypasscharging;

public interface BypassChargingInterface {
    /**
     * Enables verified OEM charge separation for the current device strategy.
     * @return String output log or execution result status ("SUCCESS" or error details).
     */
    String enableBypassCharging();

    /**
     * Disables verified OEM charge separation and restores normal charging.
     * @return String output log or execution result status.
     */
    String disableBypassCharging();

    /**
     * Checks if an exact-model OEM charge-separation integration is verified.
     * @return true if strategy nodes or settings exist on device.
     */
    boolean isSupported();

    /**
     * Gets the human-readable strategy name.
     * @return Vendor Strategy Name (e.g., "Samsung Pause USB Power Delivery").
     */
    String getStrategyName();

    /**
     * Gets the current charge-separation status report.
     * @return Human-readable status string.
     */
    String getBypassStatus();
}
