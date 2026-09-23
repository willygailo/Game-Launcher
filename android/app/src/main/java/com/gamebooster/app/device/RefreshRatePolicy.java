package com.gamebooster.app.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Pure refresh-rate selection rules shared by capability discovery and game
 * profiles. Android-reported modes are the only source of truth.
 */
public final class RefreshRatePolicy {

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
     * Resolves to a reported display mode without ever exceeding a positive
     * request. A non-positive request means "use the highest known mode".
     * Returns zero when Android did not report a mode.
     */
    public static int resolveRate(Collection<Integer> reportedRates, int requestedHz) {
        List<Integer> rates = sanitizeReportedRates(reportedRates);
        if (rates.isEmpty()) return 0;
        if (requestedHz <= 0) return rates.get(rates.size() - 1);

        int resolved = rates.get(0);
        for (int rate : rates) {
            if (rate > requestedHz) break;
            resolved = rate;
        }
        return resolved;
    }
}
