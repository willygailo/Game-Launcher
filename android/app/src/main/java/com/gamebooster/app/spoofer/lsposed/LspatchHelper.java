package com.gamebooster.app.spoofer.lsposed;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

/**
 * LspatchHelper — Non-Root ART Hooking Assistant & Guide for LSPatch.
 *
 * Enables seamless device and hardware spoofing on non-rooted Android 12–16
 * devices by guiding the user on how to embed or bind Game Booster into
 * target game APKs using LSPatch.
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
     * Displays an in-app interactive step-by-step guide on how to patch games
     * with Game Booster using LSPatch on non-rooted Android 12–16.
     */
    public static void showLspatchGuideDialog(Context context) {
        if (context == null) return;

        boolean installed = isLspatchInstalled(context);
        String actionBtnText = installed ? "OPEN LSPATCH APP" : "DOWNLOAD LSPATCH (GITHUB)";

        String guideMessage =
                "🧬 NON-ROOT ART HOOKING VIA LSPATCH\n\n" +
                "LSPatch allows full in-memory hardware & GPU spoofing without root.\n\n" +
                "📌 STEP-BY-STEP PATCHING GUIDE:\n\n" +
                "1️⃣ Install & Open LSPatch.\n" +
                "2️⃣ Tap the '+' button to select your target game (e.g. MLBB, PUBG, CODM, Wild Rift, Standoff 2).\n" +
                "3️⃣ Select 'Embed Module' or 'Local Mode'.\n" +
                "4️⃣ In the Module selection, check 'GAME BOOSTER'.\n" +
                "5️⃣ Tap 'Start Patch' and install the patched game APK.\n" +
                "6️⃣ Open the patched game — Game Booster will automatically inject flagship hardware (ROG 8 Pro / Adreno 750 / 185 FPS) in-memory!\n\n" +
                "💡 Note: All configs are synced in real-time via Game Booster's ContentProvider bridge.";

        new AlertDialog.Builder(context)
                .setTitle("🧬 LSPATCH NON-ROOT GUIDE")
                .setMessage(guideMessage)
                .setPositiveButton(actionBtnText, (dialog, which) -> openOrInstallLspatch(context))
                .setNegativeButton("CLOSE", null)
                .show();
    }
}
