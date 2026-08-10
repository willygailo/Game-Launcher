package com.gamebooster.app.booster.thermal;

public interface ThermalInterface {
    /**
     * Applies OEM-specific thermal mitigation override (disabling aggressive GOS/Joyose thermal throttling).
     * @return Execution log / status string.
     */
    String applyThermalOptimization();

    /**
     * Restores default system thermal throttling settings.
     * @return Execution log / status string.
     */
    String resetThermalSettings();

    /**
     * Gets strategy name.
     */
    String getStrategyName();

    /**
     * Checks if supported by device.
     */
    boolean isSupported();
}
