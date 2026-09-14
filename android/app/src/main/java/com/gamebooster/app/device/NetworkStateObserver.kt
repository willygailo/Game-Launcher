package com.gamebooster.app.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * NetworkStateObserver — Clean, event-driven network connectivity monitor.
 *
 * Requirements addressed:
 *  - Background & Offline Reliability: Shizuku does not require internet; this observer
 *    only notifies listeners of network changes for telemetry/dashboard display.
 *  - Does NOT continuously poll the network or battery.
 *  - Uses modern ConnectivityManager.NetworkCallback.
 */
class NetworkStateObserver private constructor(context: Context) {

    interface Listener {
        fun onNetworkStateChanged(isOnline: Boolean)
    }

    private val appContext = context.applicationContext
    private val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val listeners = CopyOnWriteArrayList<Listener>()
    private val isRegistered = AtomicBoolean(false)

    @Volatile
    var isOnline: Boolean = false
        private set

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            evaluateConnectivity()
        }

        override fun onLost(network: Network) {
            evaluateConnectivity()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            evaluateConnectivity()
        }
    }

    init {
        evaluateConnectivity()
        register()
    }

    fun addListener(listener: Listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            listener.onNetworkStateChanged(isOnline)
        }
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun register() {
        if (isRegistered.compareAndSet(false, true)) {
            try {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager?.registerNetworkCallback(request, networkCallback)
                Log.d(TAG, "Registered NetworkStateObserver callback.")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register network callback: ${e.message}")
            }
        }
    }

    fun unregister() {
        if (isRegistered.compareAndSet(true, false)) {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
                Log.d(TAG, "Unregistered NetworkStateObserver callback.")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to unregister network callback: ${e.message}")
            }
        }
    }

    fun evaluateConnectivity(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNet = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNet)
        val online = caps != null && (
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        )
        val changed = (online != isOnline)
        isOnline = online
        if (changed) {
            Log.i(TAG, "Network state updated: online=$isOnline")
            for (listener in listeners) {
                try {
                    listener.onNetworkStateChanged(isOnline)
                } catch (t: Throwable) {
                    Log.w(TAG, "Error notifying network listener", t)
                }
            }
        }
        return isOnline
    }

    companion object {
        private const val TAG = "NetworkStateObserver"

        @Volatile
        private var instance: NetworkStateObserver? = null

        @JvmStatic
        fun getInstance(context: Context): NetworkStateObserver {
            return instance ?: synchronized(this) {
                instance ?: NetworkStateObserver(context).also { instance = it }
            }
        }
    }
}
