package com.gamebooster.app.spoofer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

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
 */
public class SpoofPrefsProvider extends ContentProvider {

    public static final String AUTHORITY = "com.gamebooster.app.spoofprefs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/spoof");

    private static final String[] COLUMNS = {"key", "value"};

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Context context = getContext();
        if (context == null) return null;
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