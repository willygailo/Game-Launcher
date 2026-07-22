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
 * ShizukuManager — Manages Shizuku lifecycle, sticky Binder listeners, permission flow,
 * and AIDL UserService binding state with atomic binding guards.
 */
@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IShizukuManager {
    private val _availability = MutableStateFlow<ShizukuAvailability>(ShizukuAvailability.Stopped)
    override val availability: StateFlow<ShizukuAvailability> = _availability.asStateFlow()

    @Volatile
    private var userService: IShellCommandService? = null

    private val isBinding = AtomicBoolean(false)

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkAvailability()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        userService = null
        isBinding.set(false)
        _availability.value = ShizukuAvailability.Stopped
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                _availability.value = ShizukuAvailability.Ready
                bindUserService()
            } else {
                _availability.value = ShizukuAvailability.PermissionDenied
            }
        }
    }

    private val serviceArgs = Shizuku.UserServiceArgs(
        ComponentName("com.gamelauncher", ShizukuUserService::class.java.name)
    ).apply {
        daemon(false)
        processNameSuffix("shell_service")
        debuggable(false)
        version(1)
    }

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isBinding.set(false)
            if (service != null && service.isBinderAlive) {
                userService = IShellCommandService.Stub.asInterface(service)
                _availability.value = ShizukuAvailability.Ready
            } else {
                userService = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
            isBinding.set(false)
            _availability.value = ShizukuAvailability.Stopped
        }
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkAvailability()
        } catch (_: Exception) {
            _availability.value = ShizukuAvailability.NotInstalled
        }
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
                _availability.value = ShizukuAvailability.NotInstalled
                return
            }

            if (!Shizuku.pingBinder()) {
                _availability.value = ShizukuAvailability.Stopped
                return
            }

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _availability.value = ShizukuAvailability.Ready
                bindUserService()
            } else {
                _availability.value = ShizukuAvailability.PermissionDenied
            }
        } catch (e: Throwable) {
            _availability.value = ShizukuAvailability.Stopped
        }
    }

    override fun requestPermission() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            }
        } catch (_: Exception) {
            _availability.value = ShizukuAvailability.NotInstalled
        }
    }

    override fun isReady(): Boolean = availability.value is ShizukuAvailability.Ready

    override fun getUserService(): IShellCommandService? = userService

    override fun bindUserService() {
        if (userService != null) return
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
