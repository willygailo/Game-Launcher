package com.gamebooster.app.bypasscharging;

public interface BypassChargingInterface {
    /**
     * Enables bypass charging for the current device strategy.
     * @return String output log or execution result status ("SUCCESS" or error details).
     */
    String enableBypassCharging();

    /**
     * Disables bypass charging and restores normal battery charging.
     * @return String output log or execution result status.
     */
    String disableBypassCharging();

    /**
     * Checks if bypass charging is supported on the device by verifying setting keys or sysfs nodes.
     * @return true if strategy nodes or settings exist on device.
     */
    boolean isSupported();

    /**
     * Gets the human-readable strategy name.
     * @return Vendor Strategy Name (e.g., "Samsung Pause USB Power Delivery").
     */
    String getStrategyName();

    /**
     * Gets current bypass charging status report.
     * @return Human-readable status string.
     */
    String getBypassStatus();
}
