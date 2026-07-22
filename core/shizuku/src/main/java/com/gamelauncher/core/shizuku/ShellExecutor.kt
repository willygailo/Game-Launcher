// core/shizuku/src/main/java/com/gamelauncher/core/shizuku/ShellExecutor.kt
package com.gamelauncher.core.shizuku

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

    override suspend fun setPeakRefreshRate(hz: Float): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = shizukuManager.getUserService() ?: return@withContext false
        try {
            service.setPeakRefreshRate(hz)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setMinRefreshRate(hz: Float): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = shizukuManager.getUserService() ?: return@withContext false
        try {
            service.setMinRefreshRate(hz)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setThermalOverride(disabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = shizukuManager.getUserService() ?: return@withContext false
        try {
            service.setThermalOverride(disabled)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = shizukuManager.getUserService() ?: return@withContext false
        try {
            service.writeSetting(namespace, key, value)
        } catch (e: SecurityException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun readSetting(namespace: String, key: String): String? = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext null
        val service = shizukuManager.getUserService() ?: return@withContext null
        try {
            service.readSetting(namespace, key)
        } catch (e: SecurityException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = shizukuManager.getUserService() ?: return@withContext false
        try {
            service.setDeviceConfig(namespace, key, value)
        } catch (e: SecurityException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun readDeviceConfig(namespace: String, key: String): String? = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext null
        val service = shizukuManager.getUserService() ?: return@withContext null
        try {
            service.readDeviceConfig(namespace, key)
        } catch (e: SecurityException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun grantPermission(packageName: String, permissionName: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = shizukuManager.getUserService() ?: return@withContext false
        try {
            service.grantPermission(packageName, permissionName)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val service = shizukuManager.getUserService() ?: return@withContext false
        try {
            service.setAppOp(packageName, opName, mode)
        } catch (_: Exception) {
            false
        }
    }
}
