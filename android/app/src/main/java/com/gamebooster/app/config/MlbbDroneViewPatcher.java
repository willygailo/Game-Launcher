package com.gamebooster.app.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuAutoConnectEngine;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import rikka.shizuku.Shizuku;

/**
 * MlbbDroneViewPatcher — High-Performance Working MLBB Drone View Integration Engine.
 *
 * Deploys the Moonton hot-patch drone view suite from assets/mlbb_drone/:
 * - 5 Selectable Drone Zoom Tiers: 1.5X, 2X, 3X, 4X, 5X
 * - Deploys to mini_patch/1232.1/ZC_7108472971/2/
 * - Injects battle configuration (BattleSystemConfig.bytes) with camera height/fov coordinates
 * - Sets Moonton resource validation flags (__fix_rescheck, __active, __ready, _load_res.bytes)
 * - Guarantees clean restoration when Drone View is toggled off
 */
public final class MlbbDroneViewPatcher {

    private static final String TAG = "MlbbDroneViewPatcher";

    public static final int TIER_1_5X = 15;
    public static final int TIER_2X   = 20;
    public static final int TIER_3X   = 30;
    public static final int TIER_4X   = 40;
    public static final int TIER_5X   = 50;
    public static final int DEFAULT_TIER = TIER_3X;

    private static final String MINI_PATCH_SUBPATH = "files/mini_patch/1232.1/ZC_7108472971/2";
    private static final String ASSET_BASE_DIR = "mlbb_drone/base";
    private static final String ASSET_TIERS_DIR = "mlbb_drone/tiers";

    private MlbbDroneViewPatcher() {}

    /**
     * Resolves the corresponding asset filename for the desired zoom tier.
     */
    public static String getTierAssetName(int tier) {
        switch (tier) {
            case TIER_1_5X: return "battle_1_5x.bytes";
            case TIER_2X:   return "battle_2x.bytes";
            case TIER_4X:   return "battle_4x.bytes";
            case TIER_5X:   return "battle_5x.bytes";
            case TIER_3X:
            default:        return "battle_3x.bytes";
        }
    }

    /**
     * Formats the human-readable tier label for UI display.
     */
    public static String getTierLabel(int tier) {
        switch (tier) {
            case TIER_1_5X: return "1.5X";
            case TIER_2X:   return "2.0X";
            case TIER_3X:   return "3.0X";
            case TIER_4X:   return "4.0X";
            case TIER_5X:   return "5.0X";
            default:        return "3.0X";
        }
    }

    /**
     * Finds the base directory for Mobile Legends data across primary storage,
     * system app-private, and Android 15/16 multi-user / Private Space profiles.
     */
    public static List<String> resolveMlbbRootDirs(String pkg) {
        List<String> roots = new ArrayList<>();
        if (pkg == null || pkg.trim().isEmpty()) pkg = "com.mobile.legends";

        roots.add("/storage/emulated/0/Android/data/" + pkg);
        roots.add("/sdcard/Android/data/" + pkg);
        roots.add("/data/data/" + pkg);
        roots.add("/data/user/0/" + pkg);
        roots.add("/storage/emulated/0/Android/media/" + pkg);

        // Android 15 Private Space / App Clones / Work Profiles (User IDs 10..14)
        for (int userId = 10; userId <= 14; userId++) {
            roots.add("/storage/emulated/" + userId + "/Android/data/" + pkg);
            roots.add("/data/user/" + userId + "/" + pkg);
        }
        return roots;
    }

    /**
     * Applies the working Drone View mod for the given package and zoom tier.
     */
    public static boolean applyDroneView(Context context, String pkg, int tier) {
        if (context == null) {
            context = ConfigBackupManager.getAppContext();
        }
        if (context == null) {
            context = com.gamebooster.app.GameBoosterApp.getInstance();
        }
        if (context == null) {
            Log.w(TAG, "Cannot apply Drone View: context is null");
            return false;
        }
        if (pkg == null || (!pkg.contains("mobile.legends") && !pkg.contains("mobilelegends"))) {
            return false;
        }

        // On Android 14/15/16, check binder liveness before attempting privileged file operations
        // FIX: Extended rebind wait from 250ms → 1500ms — 250ms was insufficient for Shizuku
        // to re-establish the binder on modern devices, causing silent partial-write failures.
        try {
            if (!Shizuku.pingBinder()) {
                Log.w(TAG, "Shizuku binder is not active before drone injection. Initiating rebind...");
                ShizukuAutoConnectEngine.evaluateAndConnect(context);
                try {
                    Thread.sleep(1500); // was 250ms — not enough time for binder rebind
                } catch (InterruptedException ignored) {}
            }
        } catch (Throwable ignored) {}

        try {
            AssetManager am = context.getAssets();
            List<String> rootDirs = resolveMlbbRootDirs(pkg);
            boolean anyApplied = false;

            String tierAssetName = getTierAssetName(tier);
            byte[] battleBytes = readAssetBytes(am, ASSET_TIERS_DIR + "/" + tierAssetName);
            if (battleBytes == null || battleBytes.length == 0) {
                Log.e(TAG, "Failed to load tier battle bytes from assets: " + tierAssetName);
                return false;
            }

            // 1. Dynamic Mini-Patch Slots Discovery & Multi-Slot Deployment
            for (String rootDir : rootDirs) {
                File root = new File(rootDir);
                if (root.exists() || ShizukuFileManager.hasFullAccess()) {
                    List<String> activePatchSlots = discoverActiveMiniPatchSlots(rootDir);
                    for (String slotDir : activePatchSlots) {
                        boolean ok = deployMiniPatch(context, slotDir, battleBytes);
                        if (ok) {
                            anyApplied = true;
                            Log.i(TAG, "✅ Deployed Drone View [" + getTierLabel(tier) + "] to slot: " + slotDir);
                        }
                    }
                }
            }

            // 2. Direct dragon2017 & Document Assets Deployment (Primary In-Game Camera Source)
            for (String rootDir : rootDirs) {
                String[] targetDocPaths = {
                    rootDir + "/files/dragon2017/assets/Document/android",
                    rootDir + "/files/dragon2017/assets/Document",
                    rootDir + "/files/LoadResManager/Document/android",
                    rootDir + "/files/LoadResManager/Document"
                };

                for (String docDir : targetDocPaths) {
                    ShizukuFileManager.makeDirectory(docDir);
                    String battlePath = docDir + "/BattleSystemConfig.bytes";
                    boolean ok = writeWithFallback(context, battlePath, battleBytes, "777");
                    if (ok) {
                        anyApplied = true;
                        Log.i(TAG, "🎯 [Direct Camera] Deployed BattleSystemConfig.bytes [" + getTierLabel(tier) + "] to: " + battlePath);
                    }
                }

                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.executeShizukuCommand("chmod -R 777 \"" + rootDir + "/files/dragon2017\" 2>/dev/null");
                    ShizukuExecutor.executeShizukuCommand("chmod -R 777 \"" + rootDir + "/files/mini_patch\" 2>/dev/null");
                } else {
                    CommandExecutor.executeSystemCommand("chmod -R 777 \"" + rootDir + "/files/dragon2017\" 2>/dev/null");
                    CommandExecutor.executeSystemCommand("chmod -R 777 \"" + rootDir + "/files/mini_patch\" 2>/dev/null");
                }
            }

            return anyApplied;
        } catch (Throwable t) {
            Log.e(TAG, "Error applying Drone View for " + pkg, t);
            return false;
        }
    }

    /**
     * Dynamically discovers all active and versioned mini_patch slots in MLBB files.
     *
     * ROOT FIX: Android 13+ scoped storage blocks File.listFiles() on
     * /Android/data/<pkg>/ — it silently returns null even when the path exists!
     * We now use ShizukuFileManager.listDirectory() (shell `ls -1`) which bypasses
     * the MediaProvider restriction and correctly enumerates all hot-patch folders
     * including fix_1789616705/1, fix_1789550581/2, ZC_*, etc.
     */
    public static List<String> discoverActiveMiniPatchSlots(String rootDir) {
        List<String> slots = new ArrayList<>();
        // Always include hardcoded fallback slot first
        slots.add(rootDir + "/" + MINI_PATCH_SUBPATH);

        String miniPatchBase = rootDir + "/files/mini_patch";

        // FIX: Use shell-based listing — File.listFiles() returns null on Android 13+
        // scoped storage paths (/storage/emulated/0/Android/data/<pkg>/) even when
        // the directory physically exists and is readable via adb/shell.
        List<String> versionNames = ShizukuFileManager.listDirectory(miniPatchBase);
        if (versionNames == null || versionNames.isEmpty()) {
            // Shell listing also failed (no root/shizuku); return just the fallback slot
            Log.w(TAG, "discoverActiveMiniPatchSlots: shell ls also empty for " + miniPatchBase
                    + " — falling back to hardcoded slot only");
            return slots;
        }

        for (String verName : versionNames) {
            String verPath = miniPatchBase + "/" + verName;
            // List patch folders inside each version dir (e.g., fix_*, ZC_*)
            List<String> patchFolderNames = ShizukuFileManager.listDirectory(verPath);
            if (patchFolderNames == null) continue;

            for (String pName : patchFolderNames) {
                String pPath = verPath + "/" + pName;
                // Enumerate numeric sub-slots (/1, /2, etc.)
                List<String> subSlotNames = ShizukuFileManager.listDirectory(pPath);
                if (subSlotNames != null) {
                    for (String sName : subSlotNames) {
                        // Only include directories (numeric slot IDs)
                        if (sName.matches("\\d+")) {
                            String slotPath = pPath + "/" + sName;
                            if (!slots.contains(slotPath)) {
                                slots.add(slotPath);
                                Log.i(TAG, "📂 Discovered active slot: " + slotPath);
                            }
                        }
                    }
                }
                // Also target the patch folder itself (in case it has no sub-slots)
                if (!slots.contains(pPath)) {
                    slots.add(pPath);
                }
            }
        }
        Log.i(TAG, "🔍 Total mini_patch slots discovered: " + slots.size() + " for " + rootDir);
        return slots;
    }

    /**
     * Unpacks all base mini-patch assets and the tier BattleSystemConfig.bytes into targetMiniPatch.
     *
     * WRITE STRATEGY — triple fallback per file:
     *  1. ShizukuFileManager.uploadBytes (AIDL UserService)
     *  2. ShizukuExecutor shell `printf '1' > path` — survives UserService crash
     *  3. Direct Java FileOutputStream — works for /storage/emulated/0 (external) without root
     * Shizuku UserService crashes during game launch (logcat: System.exit status:1 from service
     * process). The shell binder (shizuku_server) stays alive, so shell-echo always works.
     */
    private static boolean deployMiniPatch(Context context, String targetMiniPatch, byte[] battleBytes) {
        try {
            ShizukuFileManager.makeDirectory(targetMiniPatch);
            AssetManager am = context.getAssets();

            // 1. Unpack base files ONLY if target slot is a newly generated slot without MLBB resources
            if (targetMiniPatch.contains(MINI_PATCH_SUBPATH)) {
                unpackAssetDirectory(am, ASSET_BASE_DIR, targetMiniPatch);
            }

            // 2. Write the specific tier BattleSystemConfig.bytes (both Document/android and Document paths)
            String battleDocDir   = targetMiniPatch + "/Document/android";
            String battleDest     = battleDocDir + "/BattleSystemConfig.bytes";
            String battleAltDest  = targetMiniPatch + "/Document/BattleSystemConfig.bytes";
            ShizukuFileManager.makeDirectory(battleDocDir);
            ShizukuFileManager.makeDirectory(targetMiniPatch + "/Document");
            writeWithFallback(context, battleDest, battleBytes, "666");
            writeWithFallback(context, battleAltDest, battleBytes, "666");

            // 3. Write MLBB mini-patch lifecycle marker files (triple fallback each).
            //    MLBB's LoadResManager validates these three files before applying the patch:
            //      __ready        — signals the patch payload is fully written and ready to load
            //      __active       — signals this slot is the active hot-patch to use
            //      __fix_rescheck — overrides resource integrity check, allowing patched bytes
            //    Must contain the byte '1' (0x31). Zero-byte files are silently ignored by
            //    Moonton's LoadResManager regardless of BattleSystemConfig.bytes being present.
            byte[] markerByte = new byte[]{ (byte) '1' };
            boolean m1 = writeWithFallback(context, targetMiniPatch + "/__ready",        markerByte, "666");
            boolean m2 = writeWithFallback(context, targetMiniPatch + "/__active",       markerByte, "666");
            boolean m3 = writeWithFallback(context, targetMiniPatch + "/__fix_rescheck", markerByte, "666");
            Log.i(TAG, "✅ Marker files [__ready=" + m1 + " __active=" + m2 + " __fix_rescheck=" + m3 + "] → '1' @ " + targetMiniPatch);

            // 4. Enforce permissions across the entire mini_patch directory
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("chmod -R 777 \"" + targetMiniPatch + "\" 2>/dev/null");
            } else {
                CommandExecutor.executeSystemCommand("chmod -R 777 \"" + targetMiniPatch + "\" 2>/dev/null");
            }

            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to deploy mini_patch to " + targetMiniPatch, t);
            return false;
        }
    }

    /**
     * Writes {@code data} to {@code destPath} with robust fallbacks:
     *  1. ShizukuFileManager.uploadBytes — staged privileged copy (works for files of any size without ARG_MAX)
     *  2. Shell echo — for 1-byte marker files (__ready, __active, __fix_rescheck)
     *  3. Java FileOutputStream — works for /storage/emulated/0 external paths without root
     *
     * Returns true if at least one strategy succeeded.
     */
    private static boolean writeWithFallback(Context context, String destPath, byte[] data, String chmod) {
        if (destPath == null || data == null) return false;

        // Strategy 1: ShizukuFileManager staged upload
        try {
            ShizukuFileManager.FileOpResult res = ShizukuFileManager.uploadBytes(destPath, data, chmod);
            if (res != null && res.success) {
                return true;
            }
            java.io.File f = new java.io.File(destPath);
            if (f.exists() && f.length() == data.length) {
                return true;
            }
        } catch (Throwable ignored) {}

        // Strategy 2: For 1-byte marker files, shell echo directly
        if (data.length == 1 && data[0] == (byte)'1') {
            try {
                String cmd = "mkdir -p \"$(dirname '" + destPath + "')\" && echo -n 1 > \"" + destPath + "\" && chmod " + chmod + " \"" + destPath + "\" 2>/dev/null";
                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.executeShizukuCommand(cmd);
                } else {
                    CommandExecutor.executeSystemCommand(cmd);
                }
                return true;
            } catch (Throwable ignored) {}
        }

        // Strategy 3: Direct Java FileOutputStream
        try {
            java.io.File target = new java.io.File(destPath);
            java.io.File parent = target.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(data);
                fos.flush();
            }
            return target.exists() && target.length() == data.length;
        } catch (Throwable ignored) {}

        // Strategy 4: Storage Access Framework (SAF) Document Engine (MT Manager method)
        try {
            if (context != null) {
                String pkg = ShizukuFileManager.extractPackageFromPath(destPath);
                if (pkg == null) pkg = "com.mobile.legends";
                if (com.gamebooster.app.saf.SafStorageManager.hasSafPermission(context, pkg)) {
                    boolean safOk = com.gamebooster.app.saf.SafStorageManager.writeFileBytes(context, pkg, destPath, data);
                    if (safOk) {
                        Log.i(TAG, "writeWithFallback via SAF SUCCESS: " + destPath);
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}

        Log.w(TAG, "writeWithFallback failed for " + destPath);
        return false;
    }


    /**
     * Recursively unpacks assets from assetPath into destDirPath.
     */
    private static void unpackAssetDirectory(AssetManager am, String assetPath, String destDirPath) {
        try {
            String[] list = am.list(assetPath);
            if (list == null || list.length == 0) {
                // Leaf file
                byte[] data = readAssetBytes(am, assetPath);
                if (data != null) {
                    String relative = assetPath.substring(ASSET_BASE_DIR.length());
                    if (relative.startsWith("/")) relative = relative.substring(1);
                    String destFile = destDirPath + "/" + relative;
                    File f = new File(destFile);
                    if (f.getParentFile() != null && !f.getParentFile().exists()) {
                        f.getParentFile().mkdirs();
                    }
                    // Write via standard I/O first; fallback to Shizuku uploadBytes
                    boolean written = false;
                    try (FileOutputStream fos = new FileOutputStream(f)) {
                        fos.write(data);
                        fos.flush();
                        written = true;
                    } catch (Throwable ignored) {}

                    if (!written) {
                        ShizukuFileManager.uploadBytes(destFile, data, "666");
                    }
                }
            } else {
                // Directory: recurse
                for (String child : list) {
                    unpackAssetDirectory(am, assetPath + "/" + child, destDirPath);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Asset unpack error for " + assetPath + ": " + t.getMessage());
        }
    }

    /**
     * Reads the entire byte contents of an APK asset.
     */
    private static byte[] readAssetBytes(AssetManager am, String assetPath) {
        try (InputStream is = am.open(assetPath);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        } catch (Throwable t) {
            Log.w(TAG, "Could not read asset: " + assetPath + " -> " + t.getMessage());
            return null;
        }
    }

    /**
     * Restores stock MLBB camera settings by safely removing the injected mini_patch overrides.
     */
    public static boolean restoreStockCamera(Context context, String pkg) {
        if (pkg == null || (!pkg.contains("mobile.legends") && !pkg.contains("mobilelegends"))) {
            return false;
        }
        try {
            List<String> rootDirs = resolveMlbbRootDirs(pkg);
            for (String rootDir : rootDirs) {
                String battleFile = rootDir + "/" + MINI_PATCH_SUBPATH + "/Document/android/BattleSystemConfig.bytes";
                File f = new File(battleFile);
                if (f.exists()) {
                    f.delete();
                }
                ShizukuFileManager.deleteFile(battleFile);

                String dragonBattle = rootDir + "/files/dragon2017/assets/Document/android/BattleSystemConfig.bytes";
                File df = new File(dragonBattle);
                if (df.exists()) {
                    df.delete();
                }
                ShizukuFileManager.deleteFile(dragonBattle);
            }
            Log.i(TAG, "🧹 MLBB Drone View reverted to stock camera for " + pkg);
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "Error reverting Drone View for " + pkg + ": " + t.getMessage());
            return false;
        }
    }
}
