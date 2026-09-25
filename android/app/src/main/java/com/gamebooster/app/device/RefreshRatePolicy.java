package com.gamebooster.app.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Pure refresh-rate selection rules.
 *
 * POLICY: Hardware-Adaptive Refresh Rate & Frame Pacing.
 * Supports standard gaming tiers: 60, 90, 120, 144, 165, 185 FPS and Hz.
 * Dynamically adapts to what the hardware display exposes without forcing
 * an artificial 120Hz minimum on 60Hz or 90Hz panels.
 */
public final class RefreshRatePolicy {

    public static final int HZ_60  = 60;
    public static final int HZ_90  = 90;
    public static final int HZ_120 = 120;
    public static final int HZ_144 = 144;
    public static final int HZ_165 = 165;
    public static final int HZ_185 = 185;

    public static final int DEFAULT_MIN_HZ = 60;
    public static final int DEFAULT_MAX_HZ = 185;

    private RefreshRatePolicy() {}

    public static List<Integer> sanitizeReportedRates(Collection<Integer> reportedRates) {
        List<Integer> rates = new ArrayList<>();
        if (reportedRates != null) {
            for (Integer rate : reportedRates) {
                if (rate != null && rate > 0 && !rates.contains(rate)) {
                    rates.add(rate);
                }
            }
        }
        Collections.sort(rates);
        return Collections.unmodifiableList(rates);
    }

    public static boolean supportsRate(Collection<Integer> reportedRates, int requestedHz) {
        return requestedHz > 0 && sanitizeReportedRates(reportedRates).contains(requestedHz);
    }

    /**
     * Resolves to the best available display mode matching the requested rate,
     * respecting hardware display capabilities across 60, 90, 120, 144, 165, and 185 Hz.
     */
    public static int resolveRate(Collection<Integer> reportedRates, int requestedHz) {
        List<Integer> rates = sanitizeReportedRates(reportedRates);

        // No modes reported at all — return requested or default 60Hz baseline
        if (rates.isEmpty()) {
            return requestedHz > 0 ? requestedHz : HZ_60;
        }

        // "Use highest available" path (e.g. requestedHz <= 0)
        if (requestedHz <= 0) {
            return rates.get(rates.size() - 1);
        }

        // Exact match check
        if (rates.contains(requestedHz)) {
            return requestedHz;
        }

        // Best fit: find highest supported rate that does not exceed requestedHz
        int resolved = rates.get(0);
        for (int rate : rates) {
            if (rate <= requestedHz) {
                resolved = rate;
            } else {
                break;
            }
        }

        // If requested rate is higher than max hardware rate, return max hardware rate
        int maxHardware = rates.get(rates.size() - 1);
        return Math.min(resolved, maxHardware);
    }
}
