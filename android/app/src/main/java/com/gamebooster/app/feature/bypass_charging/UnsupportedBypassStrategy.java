package com.gamebooster.app.feature.bypass_charging;

/**
 * Safe default until an OEM integration has been verified on the exact model and firmware.
 *
 * <p>Android does not expose a portable charge-separation API. Guessing vendor settings or
 * writing arbitrary power-supply sysfs nodes can stop charging or damage the power path, so the
 * launcher reports the capability instead of attempting a generic fallback.</p>
 */
public final class UnsupportedBypassStrategy implements BypassChargingInterface {

    private static final String MESSAGE =
            "OEM charge separation is not verified for this device; no charging nodes were changed.";

    @Override
    public String enableBypassCharging() {
        return "ERROR: " + MESSAGE;
    }

    @Override
    public String disableBypassCharging() {
        return "ERROR: " + MESSAGE;
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public String getStrategyName() {
        return "OEM charge separation (not verified)";
    }

    @Override
    public String getBypassStatus() {
        return MESSAGE;
    }
}
