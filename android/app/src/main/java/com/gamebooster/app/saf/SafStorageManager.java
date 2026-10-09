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
@android.annotation.SuppressLint("ObsoleteSdkInt")
public final class SafStorageManager {

    private static final String TAG = "SafStorageManager";
    private static final String PREF_NAME = "game_booster_saf_prefs";
    private static final String KEY_SAF_URI_PREFIX = "saf_tree_uri_";
    private static final String KEY_GLOBAL_DATA_URI = "saf_global_data_uri";

    /** Request code for legacy Activity Result SAF tree picker fallback. */
    public static final int REQUEST_CODE_SAF_TREE = 8842;


    public static final String PKG_MLBB = "com.mobile.legends";
    public static final String PKG_PUBGM = "com.tencent.ig";
    public static final String PKG_CODM = "com.activision.callofduty.shooter";

    public static final String[] FAMILY_MLBB = {
        "com.mobile.legends", "com.mobilelegends.mi", "com.vng.mlbbvn",
        "com.mobile.legends.vng", "com.mobilelegends.hw", "com.mobilelegends.na",
        "com.mobile.legends.kr", "com.mobile.legends.jp", "com.mobile.legends.moonton"
    };

    public static final String[] FAMILY_PUBGM = {
        "com.tencent.ig", "com.pubg.imobile", "com.pubg.krmobile",
        "com.vng.pubgmobile", "com.tencent.iglite", "com.pubg.newstate",
        "com.krafton.bgmi", "com.tencent.tmgp.pubgm"
    };

    public static final String[] FAMILY_CODM = {
        "com.activision.callofduty.shooter", "com.garena.game.codm",
        "com.vng.codmvn", "com.tencent.tmgp.kr.codm", "com.tencent.tmgp.cod",
        "com.activision.callofduty.warzone"
    };

    public static final class TargetAppInfo {
        public final String id;
        public final String displayName;
        public final String defaultPackage;
        public final String installedPackage;
        public final boolean isInstalled;
        public final boolean hasSaf;

        public TargetAppInfo(String id, String displayName, String defaultPackage, String installedPackage, boolean isInstalled, boolean hasSaf) {
            this.id = id;
            this.displayName = displayName;
            this.defaultPackage = defaultPackage;
            this.installedPackage = installedPackage;
            this.isInstalled = isInstalled;
            this.hasSaf = hasSaf;
        }

        public String getEffectivePackage() {
            return (installedPackage != null && !installedPackage.isEmpty()) ? installedPackage : defaultPackage;
        }
    }

    private SafStorageManager() {}

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String[] getFamilyForPackage(String packageName) {
        if (packageName == null) return null;
        for (String p : FAMILY_MLBB) {
            if (p.equalsIgnoreCase(packageName)) return FAMILY_MLBB;
        }
        for (String p : FAMILY_PUBGM) {
            if (p.equalsIgnoreCase(packageName)) return FAMILY_PUBGM;
        }
        for (String p : FAMILY_CODM) {
            if (p.equalsIgnoreCase(packageName)) return FAMILY_CODM;
        }
        // Substring checks
        if (packageName.contains("mobile.legends") || packageName.contains("mobilelegends")) return FAMILY_MLBB;
        if (packageName.contains("pubg") || packageName.contains("tencent.ig")) return FAMILY_PUBGM;
        if (packageName.contains("callofduty") || packageName.contains("codm")) return FAMILY_CODM;
        return null;
    }

    public static String findInstalledPackage(Context context, String[] family) {
        if (context == null || family == null) return null;
        android.content.pm.PackageManager pm = context.getPackageManager();
        for (String pkg : family) {
            try {
                pm.getPackageInfo(pkg, 0);
                return pkg;
            } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {}
        }
        return null;
    }

    public static List<TargetAppInfo> getTargetApps(Context context) {
        List<TargetAppInfo> list = new ArrayList<>();
        if (context == null) return list;

        String mlbbInstalled = findInstalledPackage(context, FAMILY_MLBB);
        boolean mlbbHasSaf = hasSafPermission(context, mlbbInstalled != null ? mlbbInstalled : PKG_MLBB);
        list.add(new TargetAppInfo("mlbb", "Mobile Legends (MLBB)", PKG_MLBB, mlbbInstalled, mlbbInstalled != null, mlbbHasSaf));

        String pubgmInstalled = findInstalledPackage(context, FAMILY_PUBGM);
        boolean pubgmHasSaf = hasSafPermission(context, pubgmInstalled != null ? pubgmInstalled : PKG_PUBGM);
        list.add(new TargetAppInfo("pubgm", "PUBG Mobile / BGMI", PKG_PUBGM, pubgmInstalled, pubgmInstalled != null, pubgmHasSaf));

        String codmInstalled = findInstalledPackage(context, FAMILY_CODM);
        boolean codmHasSaf = hasSafPermission(context, codmInstalled != null ? codmInstalled : PKG_CODM);
        list.add(new TargetAppInfo("codm", "Call of Duty: Mobile (CODM)", PKG_CODM, codmInstalled, codmInstalled != null, codmHasSaf));

        return list;
    }

    public static String getGameDisplayName(String packageName) {
        if (packageName == null) return "Target Application";
        String[] family = getFamilyForPackage(packageName);
        if (family == FAMILY_MLBB) return "Mobile Legends (MLBB)";
        if (family == FAMILY_PUBGM) return "PUBG Mobile / BGMI";
        if (family == FAMILY_CODM) return "Call of Duty: Mobile (CODM)";
        return packageName;
    }

    public static String extractPackageFromTreeUri(Uri treeUri) {
        if (treeUri == null) return null;
        String raw = null;
        try {
            raw = DocumentsContract.getTreeDocumentId(treeUri);
        } catch (Throwable ignored) {}
        if (raw == null || raw.isEmpty()) {
            raw = treeUri.toString();
        }
        String decoded = Uri.decode(raw);

        // Direct matching of target families
        for (String pkg : FAMILY_MLBB) {
            if (decoded.contains(pkg)) return pkg;
        }
        for (String pkg : FAMILY_PUBGM) {
            if (decoded.contains(pkg)) return pkg;
        }
        for (String pkg : FAMILY_CODM) {
            if (decoded.contains(pkg)) return pkg;
        }

        // Generic Android/data/<pkg> segment extraction
        int idx = decoded.indexOf("Android/data/");
        if (idx >= 0) {
            String remainder = decoded.substring(idx + "Android/data/".length());
            int slash = remainder.indexOf('/');
            String extracted = (slash >= 0) ? remainder.substring(0, slash) : remainder;
            extracted = extracted.trim();
            if (extracted.contains(".")) {
                return extracted;
            }
        }
        return null;
    }

    /**
     * Checks whether we have a valid, persisted SAF URI permission for the target package.
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
     * Gets the saved Tree Uri for a specific package or its family aliases.
     * Never falls back to a global URI that could contaminate cross-game writes.
     */
    public static Uri getSavedTreeUri(Context context, String packageName) {
        if (context == null) return null;
        SharedPreferences prefs = getPrefs(context);
        if (packageName != null && !packageName.isEmpty()) {
            String uriStr = prefs.getString(KEY_SAF_URI_PREFIX + packageName, null);
            if (uriStr != null) {
                return Uri.parse(uriStr);
            }
            // Check family aliases
            String[] family = getFamilyForPackage(packageName);
            if (family != null) {
                for (String alias : family) {
                    String aliasUri = prefs.getString(KEY_SAF_URI_PREFIX + alias, null);
                    if (aliasUri != null) {
                        return Uri.parse(aliasUri);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Saves a granted SAF tree URI and takes persistable URI permissions.
     */
    public static boolean saveAndPersistUri(Context context, String packageName, Uri treeUri) {
        return saveAndPersistUriSmart(context, packageName, treeUri) != null;
    }

    /**
     * Smartly inspects the granted tree URI, identifies the exact target package (CODM, PUBGM, MLBB),
     * takes persistable permissions, and stores the URI under that package and its family aliases.
     *
     * @return The resolved package name that was successfully persisted, or null on failure.
     */
    public static String saveAndPersistUriSmart(Context context, String expectedPackage, Uri treeUri) {
        if (context == null || treeUri == null) return null;
        try {
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

            String detectedPkg = extractPackageFromTreeUri(treeUri);
            String targetPkg = (detectedPkg != null && !detectedPkg.isEmpty()) ? detectedPkg : expectedPackage;

            if (targetPkg == null || targetPkg.trim().isEmpty()) {
                Log.w(TAG, "Cannot determine target package for SAF tree: " + treeUri);
                return null;
            }

            SharedPreferences.Editor editor = getPrefs(context).edit();
            editor.putString(KEY_SAF_URI_PREFIX + targetPkg, treeUri.toString());

            if (expectedPackage != null && !expectedPackage.isEmpty() && !expectedPackage.equals(targetPkg)) {
                editor.putString(KEY_SAF_URI_PREFIX + expectedPackage, treeUri.toString());
            }

            // Map all family aliases so variants share the granted permission
            String[] family = getFamilyForPackage(targetPkg);
            if (family != null) {
                for (String alias : family) {
                    editor.putString(KEY_SAF_URI_PREFIX + alias, treeUri.toString());
                }
            }

            editor.apply();
            Log.i(TAG, "Successfully persisted SAF tree URI for " + targetPkg + ": " + treeUri);
            return targetPkg;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to persist SAF tree URI", t);
            return null;
        }
    }

    /**
     * Creates an Intent to launch the system folder picker (DocumentsUI) asking user to grant access
     * specifically to the target application directory (e.g. Android/data/com.mobile.legends).
     *
     * Targeting the specific com.<package> folder is REQUIRED on Android 11, 12, 13, 14, 15, and 16
     * because Android blocks granting access to the root Android/data directory directly.
     */
    public static Intent createOpenDocumentTreeIntent(String packageName) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

        String pkg = (packageName != null) ? packageName.trim() : "";
        if (!pkg.isEmpty()) {
            String docPath = "primary:Android/data/" + pkg;
            Uri initialUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", docPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
            }
        }
        return intent;
    }

    /**
     * Resolves installed package for the game family or key (e.g., "codm", "pubgm", "mlbb") and builds the intent.
     */
    public static Intent createOpenDocumentTreeIntent(Context context, String targetGameOrPkg) {
        String resolvedPkg = targetGameOrPkg;
        if (context != null && targetGameOrPkg != null) {
            if ("mlbb".equalsIgnoreCase(targetGameOrPkg)) {
                String found = findInstalledPackage(context, FAMILY_MLBB);
                resolvedPkg = (found != null) ? found : PKG_MLBB;
            } else if ("pubgm".equalsIgnoreCase(targetGameOrPkg) || "pubg".equalsIgnoreCase(targetGameOrPkg)) {
                String found = findInstalledPackage(context, FAMILY_PUBGM);
                resolvedPkg = (found != null) ? found : PKG_PUBGM;
            } else if ("codm".equalsIgnoreCase(targetGameOrPkg) || "cod".equalsIgnoreCase(targetGameOrPkg)) {
                String found = findInstalledPackage(context, FAMILY_CODM);
                resolvedPkg = (found != null) ? found : PKG_CODM;
            } else {
                String[] family = getFamilyForPackage(targetGameOrPkg);
                if (family != null) {
                    String found = findInstalledPackage(context, family);
                    if (found != null) resolvedPkg = found;
                }
            }
        }
        return createOpenDocumentTreeIntent(resolvedPkg);
    }

    /**
     * Resolves or navigates to a relative subpath within the granted SAF tree DocumentFile.
     * Correctly strips Android/data prefixes and package names so that writes go directly
     * into the granted application folder without duplicate directory structures.
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
        while (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }
        // Strip common absolute storage prefixes
        if (cleanPath.startsWith("storage/emulated/0/")) {
            cleanPath = cleanPath.substring("storage/emulated/0/".length());
        }
        if (cleanPath.startsWith("sdcard/")) {
            cleanPath = cleanPath.substring("sdcard/".length());
        }
        if (cleanPath.startsWith("Android/data/")) {
            cleanPath = cleanPath.substring("Android/data/".length());
        }

        // If cleanPath begins with an app package segment (e.g. com.mobile.legends/ or com.tencent.ig/), strip it
        int firstSlash = cleanPath.indexOf('/');
        if (firstSlash > 0) {
            String firstPart = cleanPath.substring(0, firstSlash);
            if (firstPart.contains(".")) {
                cleanPath = cleanPath.substring(firstSlash + 1);
            }
        } else if (cleanPath.contains(".")) {
            // It was just the package name itself
            cleanPath = "";
        }

        while (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }

        // If rootDoc is already the 'files' folder and cleanPath starts with 'files/', strip it
        String rootName = rootDoc.getName();
        if (rootName != null && "files".equalsIgnoreCase(rootName) && cleanPath.startsWith("files/")) {
            cleanPath = cleanPath.substring("files/".length());
        }

        if (cleanPath.isEmpty()) {
            return rootDoc;
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
