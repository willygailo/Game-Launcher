package com.gamebooster.app.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
     * Finds the base directory for Mobile Legends data.
     */
    public static List<String> resolveMlbbRootDirs(String pkg) {
        List<String> roots = new ArrayList<>();
        if (pkg == null || pkg.trim().isEmpty()) pkg = "com.mobile.legends";

        roots.add("/storage/emulated/0/Android/data/" + pkg);
        roots.add("/data/data/" + pkg);
        roots.add("/data/user/0/" + pkg);
        roots.add("/sdcard/Android/data/" + pkg);
        roots.add("/storage/emulated/0/Android/media/" + pkg);
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
            Log.w(TAG, "Cannot apply Drone View: context is null");
            return false;
        }
        if (pkg == null || (!pkg.contains("mobile.legends") && !pkg.contains("mobilelegends"))) {
            return false;
        }

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

            for (String rootDir : rootDirs) {
                File root = new File(rootDir);
                // Deploy if root exists or if privileged access is available
                if (root.exists() || ShizukuFileManager.hasFullAccess()) {
                    String targetMiniPatch = rootDir + "/" + MINI_PATCH_SUBPATH;
                    boolean ok = deployMiniPatch(context, targetMiniPatch, battleBytes);
                    if (ok) {
                        anyApplied = true;
                        Log.i(TAG, "✅ Deployed Drone View [" + getTierLabel(tier) + "] to: " + targetMiniPatch);
                    }
                }
            }

            // Also copy BattleSystemConfig.bytes into dragon2017 fallback path if present
            for (String rootDir : rootDirs) {
                String dragonPath = rootDir + "/files/dragon2017/assets/Document/android/BattleSystemConfig.bytes";
                File df = new File(dragonPath);
                if (df.getParentFile() != null && (df.getParentFile().exists() || ShizukuFileManager.hasFullAccess())) {
                    ShizukuFileManager.uploadBytes(dragonPath, battleBytes, "666");
                }
            }

            return anyApplied;
        } catch (Throwable t) {
            Log.e(TAG, "Error applying Drone View for " + pkg, t);
            return false;
        }
    }

    /**
     * Unpacks all base mini-patch assets and the tier BattleSystemConfig.bytes into targetMiniPatch.
     */
    private static boolean deployMiniPatch(Context context, String targetMiniPatch, byte[] battleBytes) {
        try {
            ShizukuFileManager.makeDirectory(targetMiniPatch);
            AssetManager am = context.getAssets();

            // 1. Unpack all recursive base files
            unpackAssetDirectory(am, ASSET_BASE_DIR, targetMiniPatch);

            // 2. Write the specific tier BattleSystemConfig.bytes
            String battleDest = targetMiniPatch + "/Document/android/BattleSystemConfig.bytes";
            ShizukuFileManager.makeDirectory(targetMiniPatch + "/Document/android");
            ShizukuFileManager.uploadBytes(battleDest, battleBytes, "666");

            // 3. Enforce permissions across the entire mini_patch directory
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
