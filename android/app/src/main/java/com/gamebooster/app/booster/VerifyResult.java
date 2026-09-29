package com.gamebooster.app.booster;

/**
 * VerifyResult — read-back verification outcome for a channel (§9.1).
 *
 * Produced only after reading actual device state back (settings, sysfs,
 * flag files), never derived from whether a write command "succeeded".
 */
public final class VerifyResult {

    public final String check;
    public final boolean ok;
    public final String requested;
    public final String actual;
    public final String detail;

    private VerifyResult(String check, boolean ok, String requested, String actual, String detail) {
        this.check = check;
        this.ok = ok;
        this.requested = requested;
        this.actual = actual;
        this.detail = detail;
    }

    public static VerifyResult pass(String check, String actual) {
        return new VerifyResult(check, true, null, actual, actual == null ? "" : actual);
    }

    public static VerifyResult mismatch(String check, String requested, String actual) {
        return new VerifyResult(check, false, requested, actual,
                "requested " + requested + " but read back " + actual);
    }

    public static VerifyResult fail(String check, String detail) {
        return new VerifyResult(check, false, null, null, detail);
    }

    public static VerifyResult unavailable(String check, String reason) {
        return new VerifyResult(check, false, null, null, "cannot verify: " + reason);
    }

    /** Short single-line form for enforcement reports and UI badges. */
    public String badge() {
        String icon = ok ? "✓" : "✗";
        StringBuilder sb = new StringBuilder(icon).append(' ').append(check);
        if (ok) {
            if (actual != null) sb.append(": ").append(actual);
        } else if (requested != null && actual != null) {
            sb.append(": requested ").append(requested).append(", actual ").append(actual);
        } else {
            sb.append(": ").append(detail);
        }
        return sb.toString();
    }
}
