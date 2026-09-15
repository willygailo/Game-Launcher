package com.gamebooster.app.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gamebooster.app.core.AppExecutors
import com.gamebooster.app.device.NetworkStateObserver
import rikka.shizuku.Shizuku
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ShizukuLifecycleManager — Kotlin architecture layer for strict Shizuku official API management.
 *
 * Requirements addressed:
 *  1. Shizuku Detection: Immediately detect installation and running binder.
 *  2. Full Permission Handling: Official Shizuku.requestPermission, no false reporting.
 *  3. Reliable Connection: Official Binder Received and Binder Dead listeners with automatic recovery.
 *  4. Offline Operation: Kernel Binder IPC (/dev/binder) functions 100% offline.
 *  5. Reactive State: Observable LiveData<ShizukuStatus> powering the real-time UI dashboard.
 */
class ShizukuLifecycleManager private constructor(context: Context) :
    NetworkStateObserver.Listener,
    ShizukuManager.ShizukuStateListener {

    private val appContext = context.applicationContext
    private val networkObserver = NetworkStateObserver.getInstance(appContext)
    private val _statusLiveData = MutableLiveData<ShizukuStatus>()
    val statusLiveData: LiveData<ShizukuStatus> = _statusLiveData

    private val isInitialized = AtomicBoolean(false)

    @Volatile
    private var currentStatus = ShizukuStatus()

    fun init() {
        if (!isInitialized.compareAndSet(false, true)) {
            refreshStatus()
            return
        }

        // Register core Shizuku binder lifecycle listeners via ShizukuManager
        ShizukuManager.registerBinderListeners()
        ShizukuManager.addStateListener(this)

        // Register network listener for online/offline indicator only
        networkObserver.addListener(this)

        // Perform immediate cold-start evaluation
        refreshStatus()
    }

    /**
     * Call in Activity.onResume() or Fragment onHiddenChanged to re-verify status automatically
     * after returning to the APK from games or background state.
     * Proactively forces an immediate binder re-check, re-binds the AIDL UserService,
     * and performs a triple-pulse evaluation to catch delayed binder recovery post-gaming.
     */
    fun onResumeCheck() {
        // 1. Proactively force a connection check and reset backoff counters
        ShizukuConnectionManager.getInstance().forceReconnectCheck()

        // 2. Re-bind AIDL UserService if binder is alive
        try {
            if (Shizuku.pingBinder() && !ShizukuUserServiceConnector.getInstance().isServiceConnected) {
                ShizukuUserServiceConnector.getInstance().bindService()
            }
        } catch (ignored: Throwable) {}

        // Pass 1: Instant evaluation (0ms)
        refreshStatus()

        // Pass 2: Quick recovery pass (250ms) as IPC stabilizes
        AppExecutors.getInstance().postDelayed({
            try {
                if (Shizuku.pingBinder()) {
                    if (!ShizukuUserServiceConnector.getInstance().isServiceConnected) {
                        ShizukuUserServiceConnector.getInstance().bindService()
                    }
                    ShizukuConnectionManager.getInstance().whitelistServicesFromDoze()
                }
            } catch (ignored: Throwable) {}
            refreshStatus()
        }, 250)

        // Pass 3: Deep recovery pass (750ms) for high-load games that heavily froze background daemons
        AppExecutors.getInstance().postDelayed({
            try {
                if (Shizuku.pingBinder()) {
                    if (!ShizukuUserServiceConnector.getInstance().isServiceConnected) {
                        ShizukuUserServiceConnector.getInstance().bindService()
                    }
                }
            } catch (ignored: Throwable) {}
            refreshStatus()
        }, 750)
    }

    /**
     * Reads real-time status from the system and updates LiveData.
     */
    fun refreshStatus(): ShizukuStatus {
        val installed = ShizukuManager.isShizukuInstalled(appContext)
        val binderAlive = try {
            Shizuku.pingBinder()
        } catch (t: Throwable) {
            false
        }

        val permissionGranted = if (binderAlive) {
            try {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } catch (t: Throwable) {
                false
            }
        } else {
            false
        }

        if (binderAlive && permissionGranted) {
            try {
                if (!ShizukuUserServiceConnector.getInstance().isServiceConnected) {
                    ShizukuUserServiceConnector.getInstance().bindService()
                }
            } catch (ignored: Throwable) {}
        }

        val connState = when {
            binderAlive && permissionGranted -> ShizukuStatus.ConnectionState.ACTIVE
            binderAlive -> ShizukuStatus.ConnectionState.CONNECTED
            else -> {
                val connMgrState = ShizukuConnectionManager.getInstance().state
                when (connMgrState) {
                    ShizukuConnectionManager.State.BINDING -> ShizukuStatus.ConnectionState.CONNECTING
                    ShizukuConnectionManager.State.DEAD -> ShizukuStatus.ConnectionState.DEAD
                    ShizukuConnectionManager.State.RETRY -> ShizukuStatus.ConnectionState.CONNECTING
                    else -> ShizukuStatus.ConnectionState.DISCONNECTED
                }
            }
        }

        val online = networkObserver.isOnline
        val appState = ShizukuStatus.AppStatus.RUNNING

        val newStatus = ShizukuStatus(
            isInstalled = installed,
            isBinderAlive = binderAlive,
            isPermissionGranted = permissionGranted,
            connectionState = connState,
            isOnline = online,
            appStatus = appState
        )

        currentStatus = newStatus
        AppExecutors.getInstance().postToMainThread {
            _statusLiveData.value = newStatus
        }

        return newStatus
    }

    /**
     * Requests permission via the official Shizuku permission API.
     */
    fun requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(ShizukuManager.REQUEST_CODE_SHIZUKU)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    /**
     * Opens Shizuku manager app or installation screen.
     */
    fun openShizukuApp(context: Context) {
        ShizukuManager.openOrInstallShizukuManager(context)
    }

    /**
     * Shows standard required Shizuku dialog when not running.
     */
    fun showRequiredDialog(context: Context, featureName: String = "Game Booster Pro") {
        ShizukuManager.showShizukuPermissionDialog(context, featureName)
    }

    override fun onBinderStateChanged(alive: Boolean) {
        Log.d(TAG, "Shizuku binder state changed event received: alive=$alive")
        refreshStatus()
    }

    override fun onNetworkStateChanged(isOnline: Boolean) {
        refreshStatus()
    }

    fun getCurrentStatus(): ShizukuStatus = currentStatus

    companion object {
        private const val TAG = "ShizukuLifecycleMgr"

        @Volatile
        private var instance: ShizukuLifecycleManager? = null

        @JvmStatic
        fun getInstance(context: Context): ShizukuLifecycleManager {
            return instance ?: synchronized(this) {
                instance ?: ShizukuLifecycleManager(context).also { instance = it }
            }
        }
    }
}
