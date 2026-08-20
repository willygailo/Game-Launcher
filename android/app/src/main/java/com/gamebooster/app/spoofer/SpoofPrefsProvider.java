package com.gamebooster.app.spoofer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

/**
 * SpoofPrefsProvider — non-root config bridge for the LSPatch module.
 *
 * Under LSPosed (root) the module reads the launcher's spoof config via
 * XSharedPreferences (world-readable prefs). LSPatch (non-root) cannot read
 * another app's SharedPreferences, so the module falls back to querying this
 * exported ContentProvider instead. The provider exposes every key of
 * "device_spoofer_prefs" as (key, value) rows.
 *
 * URI: content://com.gamebooster.app.spoofprefs/spoof[?key=<prefKey>]
 *
 * The provider is caller-gated: only the launcher process, known game titles
 * (the module runs inside them), and any app when spoof_all_apps is enabled
 * may read the config. Arbitrary third-party apps (e.g. device fingerprinting /
 * anti-cheat scanners) receive an empty result instead of the spoof config.
 */
public class SpoofPrefsProvider extends ContentProvider {

    private static final String TAG = "SpoofPrefsProvider";

    public static final String AUTHORITY = "com.gamebooster.app.spoofprefs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/spoof");

    private static final String[] COLUMNS = {"key", "value"};

    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * Checks the calling UID belongs to a trusted reader: the launcher itself,
     * a known game title (the module runs inside them), or any app when the
     * user opted into spoof_all_apps.
     */
    private boolean isTrustedCaller() {
        try {
            int uid = Binder.getCallingUid();
            if (uid == Process.myUid()) return true; // launcher process

            Context context = getContext();
            if (context == null) return false;
            String[] packages = context.getPackageManager().getPackagesForUid(uid);
            if (packages != null) {
                for (String pkg : packages) {
                    if (GameSpoofSafetyRegistry.isTrustedConfigReader(pkg)) return true;
                }
            }

            // spoof_all_apps opt-in: the user chose to spoof every app, so any
            // app process running the module is a legitimate reader.
            return SpoofPreferences.isSpoofAllApps(context);
        } catch (Throwable t) {
            Log.w(TAG, "Caller verification failed, denying access: " + t.getMessage());
            return false;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Context context = getContext();
        if (context == null) return null;
        if (!isTrustedCaller()) {
            Log.w(TAG, "Denied spoof config read to untrusted caller uid=" + Binder.getCallingUid());
            return new MatrixCursor(COLUMNS);
        }

        // Record heartbeat for LSPatch active status
        try {
            int uid = Binder.getCallingUid();
            String[] packages = context.getPackageManager().getPackagesForUid(uid);
            if (packages != null && packages.length > 0) {
                for (String p : packages) {
                    if (!p.equals(context.getPackageName())) {
                        com.gamebooster.app.spoofer.lsposed.LsposedDetector.recordGameHeartbeat(p);
                    }
                }
            }
        } catch (Throwable ignored) {}

        String keyFilter = uri.getQueryParameter("key");
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        Map<String, String> all = SpoofPreferences.readAllPrefs(context);
        for (Map.Entry<String, String> e : all.entrySet()) {
            if (keyFilter != null && !keyFilter.equals(e.getKey())) continue;
            cursor.addRow(new Object[]{e.getKey(), e.getValue()});
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.gamebooster.spoofprefs";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}