package com.gamelauncher.core.shizuku

import android.util.Log
import com.gamelauncher.core.shizuku.aidl.IShellCommandService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShellExecutor — Coroutine-friendly wrapper around AIDL ShizukuUserService calls,
 * always dispatched on Dispatchers.IO.
 */
@Singleton
class ShellExecutor @Inject constructor(
    private val shizukuManager: IShizukuManager
) : IShellExecutor {

    private fun getService(): IShellCommandService? {
        var service = shizukuManager.getUserService()
        if (service == null || service.asBinder()?.isBinderAlive != true) {
            shizukuManager.bindUserService()
            service = shizukuManager.getUserService()
        }
        return if (service != null && service.asBinder()?.isBinderAlive == true) service else null
    }

    override suspend fun setPeakRefreshRate(hz: Float): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = getService() ?: return@withContext false
        try {
            service.setPeakRefreshRate(hz)
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL setPeakRefreshRate failed", e)
            false
        }
    }

    override suspend fun setMinRefreshRate(hz: Float): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = getService() ?: return@withContext false
        try {
            service.setMinRefreshRate(hz)
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL setMinRefreshRate failed", e)
            false
        }
    }

    override suspend fun setThermalOverride(disabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = getService() ?: return@withContext false
        try {
            service.setThermalOverride(disabled)
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL setThermalOverride failed", e)
            false
        }
    }

    override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = getService() ?: return@withContext false
        try {
            service.writeSetting(namespace, key, value)
        } catch (e: SecurityException) {
            Log.e("ShellExecutor", "AIDL writeSetting SecurityException for $namespace/$key=$value", e)
            throw e
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL writeSetting exception for $namespace/$key=$value", e)
            false
        }
    }

    override suspend fun readSetting(namespace: String, key: String): String? = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext null
        val service = getService() ?: return@withContext null
        try {
            service.readSetting(namespace, key)
        } catch (e: SecurityException) {
            Log.e("ShellExecutor", "AIDL readSetting SecurityException for $namespace/$key", e)
            throw e
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL readSetting exception for $namespace/$key", e)
            null
        }
    }

    override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = getService() ?: return@withContext false
        try {
            service.setDeviceConfig(namespace, key, value)
        } catch (e: SecurityException) {
            Log.e("ShellExecutor", "AIDL setDeviceConfig SecurityException for $namespace/$key=$value", e)
            throw e
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL setDeviceConfig exception for $namespace/$key=$value", e)
            false
        }
    }

    override suspend fun readDeviceConfig(namespace: String, key: String): String? = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext null
        val service = getService() ?: return@withContext null
        try {
            service.readDeviceConfig(namespace, key)
        } catch (e: SecurityException) {
            Log.e("ShellExecutor", "AIDL readDeviceConfig SecurityException for $namespace/$key", e)
            throw e
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL readDeviceConfig exception for $namespace/$key", e)
            null
        }
    }

    override suspend fun grantPermission(packageName: String, permissionName: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = getService() ?: return@withContext false
        try {
            service.grantPermission(packageName, permissionName)
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL grantPermission exception for $packageName/$permissionName", e)
            false
        }
    }

    override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = getService() ?: return@withContext false
        try {
            service.setAppOp(packageName, opName, mode)
        } catch (e: Exception) {
            Log.e("ShellExecutor", "AIDL setAppOp exception for $packageName/$opName", e)
            false
        }
    }
}
