package com.gamelauncher.core.permissions

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.IShizukuManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RuntimePermissionManager — Checks and grants privileged AppOps & system permissions
 * strictly for our own app (context.packageName) using Shizuku (pm grant / appops set).
 * Enforces strict permission allowlist to prevent privilege escalation vectors.
 */
@Singleton
class RuntimePermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shellExecutor: IShellExecutor,
    private val shizukuManager: IShizukuManager
) {
    companion object {
        /**
         * Strict allowlist of system permissions our Game Booster app is authorized to grant.
         * Rejects any attempt to grant unlisted or third-party permissions.
         */
        val ALLOWED_PERMISSIONS = setOf(
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.DUMP",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.CHANGE_CONFIGURATION"
        )

        val ALLOWED_APPOPS = setOf(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            "SYSTEM_ALERT_WINDOW"
        )
    }

    /**
     * Checks if Usage Access (PACKAGE_USAGE_STATS) permission is granted to our app.
     */
    @Suppress("DEPRECATION")
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Attempts to grant a manifest permission to OUR APP ONLY via Shizuku ADB bridge (`pm grant <myPackage> <permission>`).
     * - Uses array-based IShellExecutor.executeArgs() to prevent command injection.
     * - Enforces context.packageName scope (cannot target third-party packages).
     * - Validates permissionName against ALLOWED_PERMISSIONS whitelist.
     */
    suspend fun grantPermissionViaShizuku(permissionName: String): Boolean = withContext(Dispatchers.IO) {
        if (!ALLOWED_PERMISSIONS.contains(permissionName)) {
            return@withContext false
        }
        if (!shizukuManager.isReady()) {
            return@withContext false
        }

        // Strictly targets context.packageName (our own app package)
        val result = shellExecutor.executeArgs("pm", "grant", context.packageName, permissionName)
        result.exitCode == 0
    }

    /**
     * Attempts to set an AppOps permission to allow for OUR APP ONLY via Shizuku (`appops set <myPackage> <op> allow`).
     */
    suspend fun setAppOpViaShizuku(opName: String): Boolean = withContext(Dispatchers.IO) {
        if (!ALLOWED_APPOPS.contains(opName)) {
            return@withContext false
        }
        if (!shizukuManager.isReady()) {
            return@withContext false
        }

        // Strictly targets context.packageName (our own app package)
        val result = shellExecutor.executeArgs("appops", "set", context.packageName, opName, "allow")
        result.exitCode == 0
    }
}
