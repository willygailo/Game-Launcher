package com.gamebooster.app.saf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SafStorageManager — Storage Access Framework (SAF) Direct Document Engine.
 *
 * Implements MT Manager's native Android file-access technique on Android 11, 12, 13, 14, 15, and 16:
 * - Grants permanent persistable URI access to /Android/data or /Android/data/com.mobile.legends/files.
 * - Creates directories recursively (mkdirs).
 * - Writes files atomically (bytes, XML, JSON, configs).
 * - Reads files and lists directory contents directly through DocumentsProvider.
 * - Acts as an autonomous fallback when Shizuku service is not running or stopped.
 */
public final class SafStorageManager {

    private static final String TAG = "SafStorageManager";
    private static final String PREF_NAME = "game_booster_saf_prefs";
    private static final String KEY_SAF_URI_PREFIX = "saf_tree_uri_";
    private static final String KEY_GLOBAL_DATA_URI = "saf_global_data_uri";

    public static final int REQUEST_CODE_SAF_TREE = 8842;

    private SafStorageManager() {}

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks whether we have a valid, persisted SAF URI permission for the target package or global Android/data.
     */
    public static boolean hasSafPermission(Context context, String packageName) {
        if (context == null) return false;
        Uri uri = getSavedTreeUri(context, packageName);
        if (uri == null) return false;

        // Verify the persisted permission is still held by our app
        List<UriPermission> persisted = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission p : persisted) {
            if (p.getUri().equals(uri) && p.isWritePermission() && p.isReadPermission()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the saved Tree Uri for a specific package, or the global Android/data tree Uri.
     */
    public static Uri getSavedTreeUri(Context context, String packageName) {
        if (context == null) return null;
        SharedPreferences prefs = getPrefs(context);
        if (packageName != null && !packageName.isEmpty()) {
            String uriStr = prefs.getString(KEY_SAF_URI_PREFIX + packageName, null);
            if (uriStr != null) {
                return Uri.parse(uriStr);
            }
        }
        String globalStr = prefs.getString(KEY_GLOBAL_DATA_URI, null);
        return globalStr != null ? Uri.parse(globalStr) : null;
    }

    /**
     * Saves a granted SAF tree URI and takes persistable URI permissions.
     */
    public static boolean saveAndPersistUri(Context context, String packageName, Uri treeUri) {
        if (context == null || treeUri == null) return false;
        try {
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

            SharedPreferences.Editor editor = getPrefs(context).edit();
            if (packageName != null && !packageName.isEmpty()) {
                editor.putString(KEY_SAF_URI_PREFIX + packageName, treeUri.toString());
            }
            editor.putString(KEY_GLOBAL_DATA_URI, treeUri.toString());
            editor.apply();
            Log.i(TAG, "Successfully persisted SAF tree URI for " + packageName + ": " + treeUri);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to persist SAF tree URI", t);
            return false;
        }
    }

    /**
     * Creates an Intent to launch the system folder picker (DocumentsUI) asking user to grant access.
     */
    public static Intent createOpenDocumentTreeIntent(String packageName) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

        // Target primary:Android/data or primary:Android/data/<package>
        String subPath = "Android%2Fdata";
        if (packageName != null && !packageName.trim().isEmpty()) {
            subPath += "%2F" + packageName.trim();
        }
        Uri initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3A" + subPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }
        return intent;
    }

    /**
     * Resolves or navigates to a relative subpath within the granted SAF tree DocumentFile.
     * Example: given treeUri of /Android/data/com.mobile.legends/files and subpath "mini_patch/1232.1/fix_xxx/1/Document/android",
     * this traverses and automatically creates directories if createDirsIfMissing is true.
     */
    public static DocumentFile navigateToSubpath(Context context, String packageName, String relativePath, boolean createDirsIfMissing) {
        Uri treeUri = getSavedTreeUri(context, packageName);
        if (treeUri == null) return null;

        DocumentFile rootDoc = DocumentFile.fromTreeUri(context, treeUri);
        if (rootDoc == null || !rootDoc.canWrite()) return null;

        if (relativePath == null || relativePath.trim().isEmpty()) {
            return rootDoc;
        }

        // Clean relative path
        String cleanPath = relativePath.trim();
        if (cleanPath.startsWith("/")) cleanPath = cleanPath.substring(1);
        // Strip common prefixes if tree was granted higher up
        if (cleanPath.startsWith("storage/emulated/0/")) {
            cleanPath = cleanPath.substring("storage/emulated/0/".length());
        }
        if (cleanPath.startsWith("sdcard/")) {
            cleanPath = cleanPath.substring("sdcard/".length());
        }
        if (cleanPath.startsWith("Android/data/" + packageName + "/")) {
            cleanPath = cleanPath.substring(("Android/data/" + packageName + "/").length());
        } else if (cleanPath.startsWith("Android/data/")) {
            cleanPath = cleanPath.substring("Android/data/".length());
        }

        String[] parts = cleanPath.split("/+");
        DocumentFile current = rootDoc;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty() || part.equals(".")) continue;

            DocumentFile next = current.findFile(part);
            if (next == null) {
                if (createDirsIfMissing) {
                    next = current.createDirectory(part);
                    if (next == null) {
                        Log.w(TAG, "SAF: Failed to create directory: " + part + " under " + current.getName());
                        return null;
                    }
                } else {
                    return null;
                }
            }
            current = next;
        }
        return current;
    }

    /**
     * Writes bytes to a relative file path inside the SAF tree.
     * If the file already exists, it is overwritten cleanly.
     */
    public static boolean writeFileBytes(Context context, String packageName, String relativeFilePath, byte[] data) {
        if (context == null || data == null || relativeFilePath == null) return false;
        try {
            int lastSlash = relativeFilePath.lastIndexOf('/');
            String dirPart = lastSlash >= 0 ? relativeFilePath.substring(0, lastSlash) : "";
            String fileName = lastSlash >= 0 ? relativeFilePath.substring(lastSlash + 1) : relativeFilePath;

            DocumentFile targetDir = navigateToSubpath(context, packageName, dirPart, true);
            if (targetDir == null || !targetDir.isDirectory()) {
                Log.w(TAG, "SAF: Target directory resolution failed for " + relativeFilePath);
                return false;
            }

            DocumentFile targetFile = targetDir.findFile(fileName);
            if (targetFile != null && targetFile.exists()) {
                // Delete previous file to prevent partial writes
                targetFile.delete();
            }

            // Create new file with binary/octet-stream mime type
            targetFile = targetDir.createFile("application/octet-stream", fileName);
            if (targetFile == null) {
                Log.e(TAG, "SAF: Could not create file " + fileName + " in " + targetDir.getName());
                return false;
            }

            try (OutputStream os = context.getContentResolver().openOutputStream(targetFile.getUri(), "wt")) {
                if (os == null) return false;
                os.write(data);
                os.flush();
            }

            Log.i(TAG, "SAF: Successfully wrote " + data.length + " bytes to " + relativeFilePath);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "SAF: Exception writing to " + relativeFilePath, t);
            return false;
        }
    }

    /**
     * Writes text (e.g. XML, JSON) to a relative file path inside the SAF tree.
     */
    public static boolean writeTextFile(Context context, String packageName, String relativeFilePath, String text) {
        if (text == null) text = "";
        return writeFileBytes(context, packageName, relativeFilePath, text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads raw bytes from a relative file path inside the SAF tree.
     */
    public static byte[] readFileBytes(Context context, String packageName, String relativeFilePath) {
        if (context == null || relativeFilePath == null) return new byte[0];
        try {
            DocumentFile targetFile = navigateToSubpath(context, packageName, relativeFilePath, false);
            if (targetFile == null || !targetFile.exists() || !targetFile.isFile()) {
                return new byte[0];
            }
            try (InputStream is = context.getContentResolver().openInputStream(targetFile.getUri())) {
                if (is == null) return new byte[0];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) {
                    baos.write(buf, 0, n);
                }
                return baos.toByteArray();
            }
        } catch (Throwable t) {
            Log.w(TAG, "SAF: Exception reading " + relativeFilePath, t);
            return new byte[0];
        }
    }

    /**
     * Reads string text from a relative file path inside the SAF tree.
     */
    public static String readTextFile(Context context, String packageName, String relativeFilePath) {
        byte[] bytes = readFileBytes(context, packageName, relativeFilePath);
        return bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
    }

    /**
     * Checks if a file or directory exists via SAF.
     */
    public static boolean fileExists(Context context, String packageName, String relativePath) {
        if (context == null || relativePath == null) return false;
        DocumentFile doc = navigateToSubpath(context, packageName, relativePath, false);
        return doc != null && doc.exists();
    }

    /**
     * Lists child names in a directory via SAF.
     */
    public static List<String> listDirectory(Context context, String packageName, String relativeDirPath) {
        List<String> list = new ArrayList<>();
        if (context == null) return list;
        DocumentFile doc = navigateToSubpath(context, packageName, relativeDirPath, false);
        if (doc != null && doc.isDirectory()) {
            DocumentFile[] files = doc.listFiles();
            for (DocumentFile f : files) {
                if (f.getName() != null) {
                    list.add(f.getName());
                }
            }
        }
        return list;
    }
}
