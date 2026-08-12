package com.gamebooster.app.booster;

import android.util.Log;

/** Safe facade: network buffers, DNS, and carrier policy are not rewritten globally. */
public final class NetworkTweaksChannel {
    private NetworkTweaksChannel() { }

    public static boolean enableLowLatencyNetwork() {
        Log.i("NetworkTweaksChannel", "Using the system network stack and user-selected DNS.");
        return false;
    }

    public static boolean applyGameNetworkPriority(String packageName) { return false; }
    public static boolean revertNetworkTweaks() { return true; }
}
