package com.gamebooster.app.feature.performance.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * NetworkStateHelper — Detects active network connections and types.
 *
 * Used by Network & Latency Optimization module to:
 * 1. Know whether device is on WiFi, Mobile Data, or BOTH simultaneously
 * 2. Detect if mobile data supports 5G/6G (NR) or falls back to 4G LTE
 * 3. Branch optimization commands appropriately
 */
public class NetworkStateHelper {

    public enum NetworkType {
        NONE,
        WIFI_ONLY,
        MOBILE_4G_ONLY,
        MOBILE_5G_ONLY,
        MOBILE_6G_ONLY,
        DUAL_WIFI_AND_MOBILE
    }

    /**
     * Returns the current active network type.
     * On Android 10+ (API 29+) uses NetworkCapabilities for precise detection.
     */
    public static NetworkType getActiveNetworkType(Context context) {
        if (context == null) return NetworkType.NONE;

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return NetworkType.NONE;

        boolean hasWifi   = false;
        boolean hasMobile = false;
        boolean has5G     = false;
        boolean has6G     = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network[] allNetworks = cm.getAllNetworks();
            for (Network network : allNetworks) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps == null) continue;

                if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) continue;
                if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) continue;

                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    hasWifi = true;
                }

                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    hasMobile = true;
                    // Check for 5G NR
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            // NET_CAPABILITY_NOT_METERED on NR = 5G SA
                            // More reliable: check bandwidth
                            int downBandwidth = caps.getLinkDownstreamBandwidthKbps();
                            // 5G: typically > 100 Mbps downstream
                            if (downBandwidth >= 100_000) {
                                has5G = true;
                            }
                            // 6G (future bands): > 1 Gbps estimated
                            if (downBandwidth >= 1_000_000) {
                                has6G = true;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        }

        if (hasWifi && hasMobile) return NetworkType.DUAL_WIFI_AND_MOBILE;
        if (hasWifi) return NetworkType.WIFI_ONLY;
        if (hasMobile) {
            if (has6G) return NetworkType.MOBILE_6G_ONLY;
            if (has5G) return NetworkType.MOBILE_5G_ONLY;
            return NetworkType.MOBILE_4G_ONLY;
        }
        return NetworkType.NONE;
    }

    /**
     * Human-readable label for status display.
     */
    public static String getNetworkLabel(NetworkType type) {
        switch (type) {
            case WIFI_ONLY:          return "📶 Wi-Fi";
            case MOBILE_4G_ONLY:     return "📡 4G LTE";
            case MOBILE_5G_ONLY:     return "📡 5G NR";
            case MOBILE_6G_ONLY:     return "📡 6G";
            case DUAL_WIFI_AND_MOBILE: return "⚡ Dual (Wi-Fi + Mobile)";
            default:                 return "❌ No Connection";
        }
    }

    /**
     * Returns true if any internet connection is active.
     */
    public static boolean isConnected(Context context) {
        NetworkType type = getActiveNetworkType(context);
        return type != NetworkType.NONE;
    }

    /**
     * Returns true if mobile data (any gen) is active.
     */
    public static boolean hasMobileData(NetworkType type) {
        return type == NetworkType.MOBILE_4G_ONLY
                || type == NetworkType.MOBILE_5G_ONLY
                || type == NetworkType.MOBILE_6G_ONLY
                || type == NetworkType.DUAL_WIFI_AND_MOBILE;
    }

    /**
     * Returns true if WiFi is active.
     */
    public static boolean hasWifi(NetworkType type) {
        return type == NetworkType.WIFI_ONLY
                || type == NetworkType.DUAL_WIFI_AND_MOBILE;
    }
}
