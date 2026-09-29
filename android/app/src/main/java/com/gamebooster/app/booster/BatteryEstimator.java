package com.gamebooster.app.booster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * §8.4 Battery impact estimator.
 *
 * <p>Estimates extra battery drain per hour of gameplay for each optimization
 * profile, based on which components the profile turns on (performance CPU
 * governor, GPU frequency lock, high-Hz panel, WebView flag stack). Values are
 * conservative ranges relative to a plain 60 Hz gaming session; the estimator
 * is table-driven so the numbers stay reviewable and testable.
 */
public final class BatteryEstimator {

    /** Optimization profiles matching the plan's user-facing selector. */
    public enum Profile {
        BALANCED(6, 12,
                "stock governor", "stock GPU policy", "60-120Hz panel"),
        PERFORMANCE(10, 18,
                "performance governor", "GPU boost", "120Hz panel", "WebView perf flags"),
        EXTREME(15, 25,
                "performance governor", "GPU max-freq lock", "max-Hz panel (185Hz)",
                "WebView flagship flags");

        public final int lowPctPerHour;
        public final int highPctPerHour;
        public final String[] contributors;

        Profile(int lowPctPerHour, int highPctPerHour, String... contributors) {
            this.lowPctPerHour = lowPctPerHour;
            this.highPctPerHour = highPctPerHour;
            this.contributors = contributors;
        }
    }

    /** A drain estimate with its contributor breakdown. */
    public static final class Estimate {
        public final Profile profile;
        public final int lowPctPerHour;
        public final int highPctPerHour;
        public final List<String> contributors;

        Estimate(Profile profile) {
            this.profile = profile;
            this.lowPctPerHour = profile.lowPctPerHour;
            this.highPctPerHour = profile.highPctPerHour;
            this.contributors = Collections.unmodifiableList(
                    new ArrayList<>(java.util.Arrays.asList(profile.contributors)));
        }

        /** User-facing string from the plan §8.4, e.g. "+15-25% per hour". */
        public String message() {
            return String.format(Locale.US,
                    "Estimated battery drain: +%d-%d%% per hour of gameplay (%s)",
                    lowPctPerHour, highPctPerHour, profile.name().toLowerCase(Locale.US));
        }
    }

    private BatteryEstimator() {
    }

    public static Estimate estimate(Profile profile) {
        if (profile == null) profile = Profile.BALANCED;
        return new Estimate(profile);
    }

    /**
     * Ranks profiles by drain — used to validate the estimator ordering
     * (Balanced &lt; Performance &lt; Extreme) and to drive the profile
     * selector's warning copy.
     */
    public static List<Profile> byDrainAscending() {
        List<Profile> profiles = new ArrayList<>(java.util.Arrays.asList(Profile.values()));
        profiles.sort((a, b) -> Integer.compare(a.lowPctPerHour, b.lowPctPerHour));
        return profiles;
    }
}
