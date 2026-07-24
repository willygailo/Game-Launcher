// core/shizuku/src/main/java/com/gamelauncher/core/shizuku/ShizukuManager.kt
package com.gamelauncher.core.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.gamelauncher.core.shizuku.aidl.IShellCommandService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShizukuManager — Manages Shizuku lifecycle, binder-death listeners, permission flow,
 * and AIDL UserService binding state with atomic binding guards.
 */
@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IShizukuManager {

    private val _state = MutableStateFlow<ShizukuState>(ShizukuState.Disconnected)
    override val state: StateFlow<ShizukuState> = _state.asStateFlow()

    @Volatile
    private var userService: IShellCommandService? = null

    private val isBinding = AtomicBoolean(false)

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkAvailability()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        userService = null
        isBinding.set(false)
        _state.value = ShizukuState.Disconnected
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                _state.value = ShizukuState.Connected
                bindUserService()
            } else {
                _state.value = ShizukuState.RunningNoPermission
            }
        }
    }

    private val serviceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(context.packageName, ShizukuUserService::class.java.name)
        ).apply {
            daemon(false)
            processNameSuffix("shell_service")
            debuggable(false)
            version(1)
        }
    }

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isBinding.set(false)
            if (service != null && service.isBinderAlive) {
                userService = IShellCommandService.Stub.asInterface(service)
                _state.value = ShizukuState.Connected
                autoGrantPermissions()
            } else {
                userService = null
                _state.value = ShizukuState.Disconnected
            }
        }

        private fun autoGrantPermissions() {
            try {
                userService?.grantPermission(context.packageName, "android.permission.WRITE_SECURE_SETTINGS")
                userService?.grantPermission(context.packageName, "android.permission.PACKAGE_USAGE_STATS")
                userService?.setAppOp(context.packageName, "SYSTEM_ALERT_WINDOW", "allow")
            } catch (e: Exception) {
                android.util.Log.e("ShizukuManager", "Auto-grant permissions failed", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
            isBinding.set(false)
            _state.value = ShizukuState.Disconnected
        }
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkAvailability()
        } catch (_: Exception) {
            _state.value = ShizukuState.NotInstalled
        }
    }

    override fun cleanup() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (_: Exception) {}
        unbindUserService()
    }

    override fun isShizukuInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    override fun checkAvailability() {
        try {
            if (!isShizukuInstalled() || Shizuku.isPreV11()) {
                _state.value = ShizukuState.NotInstalled
                return
            }

            if (!Shizuku.pingBinder()) {
                _state.value = ShizukuState.InstalledNotRunning
                return
            }

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _state.value = ShizukuState.Connected
                bindUserService()
            } else {
                _state.value = ShizukuState.RunningNoPermission
            }
        } catch (_: Throwable) {
            _state.value = ShizukuState.Disconnected
        }
    }

    override fun requestPermission() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            }
        } catch (_: Exception) {
            _state.value = ShizukuState.NotInstalled
        }
    }

    override fun isReady(): Boolean {
        return try {
            val isConnectedState = _state.value is ShizukuState.Connected
            val isBinderAlive = Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            isConnectedState || isBinderAlive
        } catch (_: Exception) {
            false
        }
    }


    override fun getUserService(): IShellCommandService? = userService

    override fun bindUserService() {
        // HARD CONSTRAINTS: Do not let any UserService bind call happen while state != Connected
        if (_state.value != ShizukuState.Connected) return
        if (userService != null && userService?.asBinder()?.isBinderAlive == true) return
        if (!isBinding.compareAndSet(false, true)) return

        try {
            Shizuku.bindUserService(serviceArgs, userServiceConnection)
        } catch (e: Exception) {
            isBinding.set(false)
        }
    }

    override fun unbindUserService() {
        try {
            Shizuku.unbindUserService(serviceArgs, userServiceConnection, true)
        } catch (_: Exception) {}
        userService = null
        isBinding.set(false)
    }

    companion object {
        const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001
    }
}
