package com.gamebooster.app.spoofer.lsposed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * LspatchHelper — Non-Root ART Hooking Assistant & Guide for LSPatch.
 *
 * Enables seamless device, hardware identity, GPU, and FPS spoofing on non-rooted Android 13–16
 * devices by guiding the user and providing direct APK sharing to LSPatch.
 */
public final class LspatchHelper {

    public static final String LSPATCH_PKG = "org.lsposed.lspatch";
    public static final String LSPATCH_METAMOD_PKG = "org.lsposed.lspatch.metamod";
    public static final String LSPATCH_GITHUB_URL = "https://github.com/LSPosed/LSPatch/releases";

    private LspatchHelper() {}

    private static volatile Boolean sCachedLspatchInstalled = null;
    private static volatile long sLastLspatchCheck = 0L;
    private static final long LSPATCH_CACHE_TTL_MS = 10_000L;

    /**
     * Checks if LSPatch Manager is installed on the device (with 10s caching for 0ms UI delay).
     */
    public static boolean isLspatchInstalled(Context context) {
        if (context == null) return false;
        long now = System.currentTimeMillis();
        if (sCachedLspatchInstalled != null && (now - sLastLspatchCheck < LSPATCH_CACHE_TTL_MS)) {
            return sCachedLspatchInstalled;
        }

        boolean installed = probeLspatchInstalled(context);
        sCachedLspatchInstalled = installed;
        sLastLspatchCheck = now;
        return installed;
    }

    public static void invalidateCache() {
        sCachedLspatchInstalled = null;
        sLastLspatchCheck = 0L;
    }

    private static boolean probeLspatchInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(LSPATCH_PKG, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {}
        try {
            pm.getPackageInfo(LSPATCH_METAMOD_PKG, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {}
        return false;
    }

    /**
     * Opens the LSPatch Manager app if installed, or directs to GitHub releases.
     */
    public static void openOrInstallLspatch(Context context) {
        if (context == null) return;
        PackageManager pm = context.getPackageManager();
        String[] candidates = {LSPATCH_PKG, LSPATCH_METAMOD_PKG};
        for (String pkg : candidates) {
            try {
                Intent launch = pm.getLaunchIntentForPackage(pkg);
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launch);
                    return;
                }
            } catch (Exception ignored) {}
        }

        try {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LSPATCH_GITHUB_URL));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
            Toast.makeText(context, "Opening LSPatch Releases on GitHub...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Unable to open browser: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Extracts and shares the current Game Booster APK via FileProvider
     * so that the user can import/select it directly in LSPatch as a module.
     */
    public static void shareModuleApk(Context context) {
        if (context == null) return;
        try {
            ApplicationInfo appInfo = context.getApplicationInfo();
            File srcApk = new File(appInfo.sourceDir);
            if (!srcApk.exists()) {
                Toast.makeText(context, "Unable to locate base APK", Toast.LENGTH_SHORT).show();
                return;
            }

            File exportDir = new File(context.getCacheDir(), "exported_apk");
            if (!exportDir.exists()) exportDir.mkdirs();
            File destApk = new File(exportDir, "GameBooster_LSPatch_Module.apk");

            // Copy base APK to cache
            try (InputStream in = new FileInputStream(srcApk);
                 OutputStream out = new FileOutputStream(destApk)) {
                byte[] buf = new byte[65536];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    destApk
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.android.package-archive");
            shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(shareIntent, "Select LSPatch or File Manager"));
            Toast.makeText(context, "📤 Sharing Game Booster Module APK for LSPatch", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(context, "Failed to share APK: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Displays an in-app interactive step-by-step guide on how to patch games
     * with Game Booster using LSPatch on non-rooted Android 13–16.
     */
    public static void showLspatchGuideDialog(Context context) {
        if (context == null) return;

        boolean installed = isLspatchInstalled(context);
        String actionBtnText = installed ? "🚀 OPEN LSPATCH" : "⬇️ DOWNLOAD LSPATCH";

        String guideMessage =
                "🧬 NON-ROOT ART HOOKING VIA LSPATCH\n\n" +
                "LSPatch allows full in-memory hardware, GPU, and 185 FPS spoofing on non-rooted Android 13–16 devices.\n\n" +
                "📌 STEP-BY-STEP PATCHING GUIDE:\n\n" +
                "1️⃣ Install & Open LSPatch (or tap below to download).\n" +
                "2️⃣ Tap the '+' button to select your target game (MLBB, PUBG, CODM, Wild Rift, Standoff 2, etc.).\n" +
                "3️⃣ Select 'Embed Module' or 'Local Mode'.\n" +
                "4️⃣ In the Module list, choose 'Game Booster' (or tap 'EXTRACT APK' below to select the APK file).\n" +
                "5️⃣ Tap 'Start Patch' and install the patched game APK.\n" +
                "6️⃣ Launch the game from Game Booster — flagship hardware identity (ROG 8 Pro, Adreno 750, 185 FPS) will automatically inject in-memory!\n\n" +
                "💡 Note: All configs are synced live via Game Booster's ContentProvider bridge.";

        new AlertDialog.Builder(context)
                .setTitle("🧬 LSPATCH NON-ROOT GUIDE & COMBO")
                .setMessage(guideMessage)
                .setPositiveButton(actionBtnText, (dialog, which) -> openOrInstallLspatch(context))
                .setNeutralButton("📤 EXTRACT APK", (dialog, which) -> shareModuleApk(context))
                .setNegativeButton("CLOSE", null)
                .show();
    }
}
