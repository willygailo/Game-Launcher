package com.gamebooster.app.device;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NetworkStateObserver — Clean, event-driven network connectivity monitor.
 *
 * Requirements addressed:
 *  - Background & Offline Reliability: Shizuku does not require internet; this observer
 *    only notifies listeners of network changes for telemetry/dashboard display.
 *  - Does NOT continuously poll the network or battery.
 *  - Uses modern ConnectivityManager.NetworkCallback.
 */
public class NetworkStateObserver {

    public interface Listener {
        void onNetworkStateChanged(boolean isOnline);
    }

    private static final String TAG = "NetworkStateObserver";

    private static volatile NetworkStateObserver sInstance;

    public static NetworkStateObserver getInstance(Context context) {
        if (sInstance == null) {
            synchronized (NetworkStateObserver.class) {
                if (sInstance == null) {
                    sInstance = new NetworkStateObserver(context);
                }
            }
        }
        return sInstance;
    }

    private final Context appContext;
    private final ConnectivityManager connectivityManager;
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean isRegistered = new AtomicBoolean(false);

    private volatile boolean isOnline = false;

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            evaluateConnectivity();
        }

        @Override
        public void onLost(@NonNull Network network) {
            evaluateConnectivity();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            evaluateConnectivity();
        }
    };

    private NetworkStateObserver(Context context) {
        this.appContext = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        evaluateConnectivity();
        register();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void addListener(Listener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            listener.onNetworkStateChanged(isOnline);
        }
    }

    public void removeListener(Listener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void register() {
        if (isRegistered.compareAndSet(false, true)) {
            try {
                if (connectivityManager != null) {
                    NetworkRequest request = new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build();
                    connectivityManager.registerNetworkCallback(request, networkCallback);
                    Log.d(TAG, "Registered NetworkStateObserver callback.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to register network callback: " + e.getMessage());
            }
        }
    }

    public void unregister() {
        if (isRegistered.compareAndSet(true, false)) {
            try {
                if (connectivityManager != null) {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                    Log.d(TAG, "Unregistered NetworkStateObserver callback.");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to unregister network callback: " + e.getMessage());
            }
        }
    }

    public boolean evaluateConnectivity() {
        if (connectivityManager == null) return false;
        try {
            Network activeNet = connectivityManager.getActiveNetwork();
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNet);
            boolean online = caps != null && (
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            );
            boolean changed = (online != isOnline);
            isOnline = online;
            if (changed) {
                Log.i(TAG, "Network state updated: online=" + isOnline);
                for (Listener listener : listeners) {
                    try {
                        listener.onNetworkStateChanged(isOnline);
                    } catch (Throwable t) {
                        Log.w(TAG, "Error notifying network listener", t);
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error evaluating connectivity: " + t.getMessage());
        }
        return isOnline;
    }
}
