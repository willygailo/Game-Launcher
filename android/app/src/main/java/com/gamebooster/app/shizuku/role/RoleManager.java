package com.gamebooster.app.shizuku.role;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import rikka.shizuku.Shizuku;

/**
 * RoleManager — Manages the active ShizukuRole for this user session.
 *
 * <p>Stores the selected role in SharedPreferences and enforces it as a gate before
 * any privileged Shizuku operation. Changing to ADMIN requires Shizuku permission to be
 * already granted — preventing privilege escalation without the underlying system bridge.
 *
 * <p>Usage:
 * <pre>
 *   RoleManager.getInstance(context).setRole(ShizukuRole.ADMIN);
 *   RoleManager.getInstance(context).requireAdmin(() -> ShizukuExecutor.executeShizukuCommand("..."));
 * </pre>
 */
public class RoleManager {

    private static final String TAG = "RoleManager";
    private static final String PREFS_NAME = "gamebooster_role_prefs";
    private static final String KEY_ROLE = "pref_shizuku_role";

    // -----------------------------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------------------------

    private static RoleManager sInstance;

    public static synchronized RoleManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RoleManager(context.getApplicationContext());
        }
        return sInstance;
    }

    // -----------------------------------------------------------------------------------------
    // Instance
    // -----------------------------------------------------------------------------------------

    private final SharedPreferences mPrefs;
    private ShizukuRole mCurrentRole;

    private RoleManager(Context context) {
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Load persisted role, default to USER
        String stored = mPrefs.getString(KEY_ROLE, ShizukuRole.USER.name());
        mCurrentRole = ShizukuRole.fromString(stored);
        Log.i(TAG, "RoleManager initialized with role: " + mCurrentRole);
    }

    // -----------------------------------------------------------------------------------------
    // Role Access
    // -----------------------------------------------------------------------------------------

    /** Returns the currently active role. */
    public ShizukuRole getRole() {
        return mCurrentRole;
    }

    /**
     * Sets the active role and persists it to SharedPreferences.
     *
     * <p>Attempting to set ADMIN without Shizuku permission active will automatically
     * downgrade the request to USER and return false.
     *
     * @param role The role to set
     * @return true if the role was set as requested; false if downgraded
     */
    public boolean setRole(ShizukuRole role) {
        if (role == ShizukuRole.ADMIN && !isShizukuPermissionGranted()) {
            Log.w(TAG, "Cannot set ADMIN role — Shizuku permission not granted. Defaulting to USER.");
            mCurrentRole = ShizukuRole.USER;
            persist(ShizukuRole.USER);
            return false;
        }

        mCurrentRole = role;
        persist(role);
        Log.i(TAG, "Role set to: " + role);
        return true;
    }

    // -----------------------------------------------------------------------------------------
    // Enforcement Gates
    // -----------------------------------------------------------------------------------------

    /**
     * Executes the given action only if the current role is ADMIN.
     * Logs a warning and does nothing if role is insufficient.
     *
     * @param action The privileged action to run
     * @return true if action was executed; false if blocked by role
     */
    public boolean requireAdmin(Runnable action) {
        if (mCurrentRole.canExecuteCommands()) {
            action.run();
            return true;
        }
        Log.w(TAG, "requireAdmin BLOCKED — current role is " + mCurrentRole + " (need ADMIN)");
        return false;
    }

    /**
     * Executes the given action if the current role can apply tweaks (ADMIN or USER).
     *
     * @param action The tweak action to run
     * @return true if action was executed; false if blocked
     */
    public boolean requireTweakAccess(Runnable action) {
        if (mCurrentRole.canApplyTweaks()) {
            action.run();
            return true;
        }
        Log.w(TAG, "requireTweakAccess BLOCKED — current role is " + mCurrentRole);
        return false;
    }

    /**
     * Returns true if the current role allows Force Apply engine.
     */
    public boolean canForceApply() {
        return mCurrentRole.canForceApply();
    }

    /**
     * Returns true if the current role allows terminal command execution.
     */
    public boolean canUseTerminal() {
        return mCurrentRole.canUseTerminal();
    }

    /**
     * Returns a human-readable description of what the current role can do.
     */
    public String getCurrentRoleDescription() {
        return mCurrentRole.getEmoji() + " " + mCurrentRole.getDisplayName()
                + " — " + mCurrentRole.getDescription();
    }

    // -----------------------------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------------------------

    private void persist(ShizukuRole role) {
        mPrefs.edit().putString(KEY_ROLE, role.name()).apply();
    }

    private boolean isShizukuPermissionGranted() {
        try {
            return Shizuku.pingBinder()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }
}
