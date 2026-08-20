package com.gamebooster.app.config;

import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * NativeConfigInjector — High-performance C++ / JNI Configuration Injector.
 *
 * Provides native C++ methods for direct file I/O, mmap memory manipulation,
 * binary and text configuration injection for Damage, Recoil, Aim Assist,
 * Tracking Bullet, Armor Defense, and Touch Polling.
 *
 * Automatically falls back to Shizuku temporary root / POSIX file manager
 * if native binary is unavailable or running in restricted sandbox mode.
 */
public class NativeConfigInjector {

    private static final String TAG = "NativeConfigInjector";
    private static boolean sNativeLibraryLoaded = false;

    static {
        try {
            System.loadLibrary("gamebooster_native");
            sNativeLibraryLoaded = true;
            Log.i(TAG, "Native C++ gamebooster_native library loaded successfully.");
        } catch (Throwable t) {
            sNativeLibraryLoaded = false;
            Log.d(TAG, "Native library not loaded (using pure Java/Shizuku engine): " + t.getMessage());
        }
    }

    public static boolean isNativeLoaded() {
        return sNativeLibraryLoaded;
    }

    // ─── Native C++ JNI Declarations ─────────────────────────────────────────

    public static native boolean nativeInjectConfig(String path, String content);
    public static native boolean nativePatchKey(String path, String key, String value);
    public static native boolean nativeBatchPatchKeys(String path, String[] keys, String[] values);
    public static native boolean nativeInjectDamageBoost(String path, float multiplier, float headshotMultiplier, int critRate);
    public static native boolean nativeInjectZeroRecoil(String path, float recoilScale, int stability);
    public static native boolean nativeInjectAimAssist(String path, int strength, int precision);
    public static native boolean nativeInjectTrackingBullet(String path, float trackingStrength, float hitboxMultiplier);
    public static native boolean nativeInjectArmorDef(String path, float defBoost, float dmgReduction);
    public static native boolean nativeInjectUltraExtremeGraphics(String path, int targetFps);
    public static native boolean nativeInjectPerGameProfile(String path, String gameKey, int targetFps, boolean highDamage, boolean noRecoil, boolean trackingBullet, boolean aimAssist);
    public static native boolean nativeFastMemorySync(String path);

    // ─── High-Level Injection Engine Methods ─────────────────────────────────

    /**
     * Injects or overwrites configuration content into target file path using C++ native method or Shizuku fallback.
     */
    public static boolean injectConfig(String path, String content) {
        if (path == null || content == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectConfig(path, content)) {
                    Log.d(TAG, "C++ native injected config into " + path);
                    return true;
                }
            } catch (Throwable t) {
                Log.w(TAG, "C++ nativeInjectConfig fallback: " + t.getMessage());
            }
        }

        // Java / Shizuku / Direct I/O fallback
        if (ShizukuFileManager.hasFullAccess()) {
            return ShizukuFileManager.writeFile(path, content, "666").success;
        }

        try {
            File f = new File(path);
            if (f.getParentFile() != null && !f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            f.setReadable(true, false);
            f.setWritable(true, false);
            return true;
        } catch (IOException e) {
            String cmd = "mkdir -p $(dirname '" + path + "'); echo '" + content.replace("'", "'\\''") + "' > '" + path + "'; chmod 666 '" + path + "'";
            String res;
            if (ShizukuExecutor.hasShizukuPermission()) {
                res = ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                res = CommandExecutor.executeSystemCommand(cmd);
            }
            return res != null && !res.startsWith("ERROR");
        }
    }

    /**
     * Injects High Damage Script keys (2.50x - 3.50x damage multiplier, 99% crit, penetration).
     */
    public static boolean injectHighDamage(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectDamageBoost(path, 2.50f, 3.50f, 99)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] damageKeys = {
            "DamageMultiplier=2.50",
            "PhysicalDamageBoost=2.50",
            "MagicDamageBoost=2.50",
            "TrueDamageBoost=2.50",
            "BulletDamageBoost=2.50",
            "HeadshotDamageMultiplier=3.50",
            "CriticalHitRate=99",
            "CriticalDamageMultiplier=3.50",
            "PenetrationBoost=99",
            "ArmorPenetration=99",
            "MagicPenetrationBoost=99",
            "HighDamageRateMode=1",
            "+CVars=r.DamageMultiplier=2.50",
            "+CVars=r.BulletDamageScale=2.50",
            "+CVars=r.HeadshotMultiplier=3.50"
        };
        return batchInjectKeys(path, damageKeys, "[DamageScript]");
    }

    /**
     * Injects Zero Recoil & Weapon Stability keys (RecoilScale=0.00, ZeroRecoil=1, NoRecoil=1).
     */
    public static boolean injectNoRecoil(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectZeroRecoil(path, 0.00f, 150)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] recoilKeys = {
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "VerticalRecoil=0.00",
            "HorizontalRecoil=0.00",
            "RecoilReduction=1.50",
            "WeaponStability=150",
            "ScreenShake=0",
            "GunKick=0",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "ScopeStability=1.50",
            "FirstBulletAccuracy=1",
            "+CVars=r.WeaponRecoilScale=0.00",
            "+CVars=r.VerticalRecoilMultiplier=0.00",
            "+CVars=r.HorizontalRecoilMultiplier=0.00",
            "+CVars=r.GunKickReduction=1",
            "+CVars=r.CameraShake=0",
            "+CVars=r.ScreenShake=0"
        };
        return batchInjectKeys(path, recoilKeys, "[RecoilControl]");
    }

    /**
     * Injects High Aim Assist & Smart Target Lock keys (AimAssist=1, Strength=150, Precision=3.0, 1000Hz Gyro).
     */
    public static boolean injectAimAssist(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectAimAssist(path, 150, 3)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] aimKeys = {
            "AimAssist=1",
            "AimAssistStrength=150",
            "AimAssistLevel=5",
            "AimPrecision=3",
            "AutoAim=1",
            "AimTracking=1",
            "TargetLock=1",
            "SmartTargetingMode=1",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "AimAssistRadius=200",
            "ScopeAimAssist=1",
            "RedDotAimAssist=1",
            "CrosshairMagnetism=1.50",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroSensitivityRatio=2.5",
            "GyroStabilization=1",
            "+CVars=r.AimAssist=1",
            "+CVars=r.AimAssist.Strength=2.0",
            "+CVars=r.AimAssistRadius=200"
        };
        return batchInjectKeys(path, aimKeys, "[AimAssist]");
    }

    /**
     * Injects Tracking Bullet, Bullet Magnetism & Hitbox Expansion keys.
     */
    public static boolean injectTrackingBullet(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectTrackingBullet(path, 1.50f, 1.50f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] trackingKeys = {
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "BulletMagnetism=1.50",
            "HitboxExpansion=1.50",
            "TargetLockTracking=1",
            "BulletCurveFactor=1.20",
            "BulletVelocityMultiplier=2.00",
            "BulletSpread=0.00",
            "CrosshairMagnetism=1.50",
            "FirstBulletAccuracy=1",
            "ProjectileHoming=1",
            "+CVars=r.BulletTracking=1",
            "+CVars=r.MagicBullet=1",
            "+CVars=r.HitboxExpansion=1.50",
            "+CVars=r.BulletMagnetism=1.50",
            "+CVars=r.BulletVelocityScale=2.0"
        };
        return batchInjectKeys(path, trackingKeys, "[TrackingBullet]");
    }

    /**
     * Injects Armor Defense & Damage Reduction keys (DamageReduction=0.50, ArmorBoost=150, ShieldMultiplier=2.00).
     */
    public static boolean injectArmorDef(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectArmorDef(path, 2.50f, 0.50f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] armorKeys = {
            "PhysicalDefenseBoost=2.50",
            "MagicDefenseBoost=2.50",
            "DamageReductionRatio=0.50",
            "ShieldMultiplier=2.00",
            "MaxHPMultiplier=1.50",
            "DamageAbsorbRatio=1.50",
            "ArmorBoost=150",
            "VestDurability=2.00",
            "HelmetDamageReduction=0.60",
            "TenacityRatio=0.50",
            "ResilienceLevel=3",
            "+CVars=r.ArmorDamageReduction=0.50",
            "+CVars=r.VestDurabilityBoost=2.00",
            "+CVars=r.HelmetDamageReduction=0.60"
        };
        return batchInjectKeys(path, armorKeys, "[DefenseConfig]");
    }

    /**
     * Injects Super Fast Zero-Delay Touch & 1000Hz Polling Rate keys.
     */
    public static boolean injectSuperFastTouch(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        String[] touchKeys = {
            "HighFreqTouch=1",
            "TouchResponseLevel=3",
            "HighFreqTouchHz=185",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "TouchLatencyReduction=1",
            "ZeroInputLag=1",
            "TouchSlopReduction=1"
        };
        return batchInjectKeys(path, touchKeys, "[TouchEngine]");
    }

    /**
     * Injects a full per-game profile into a specific configuration file path.
     */
    public static boolean injectPerGameConfig(String path, String gameKey, int targetFps,
                                              boolean highDamage, boolean noRecoil,
                                              boolean trackingBullet, boolean aimAssist) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPerGameProfile(path, gameKey, targetFps, highDamage, noRecoil, trackingBullet, aimAssist)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        boolean ok = true;
        if (highDamage) ok &= injectHighDamage(path);
        if (noRecoil) ok &= injectNoRecoil(path);
        if (trackingBullet) ok &= injectTrackingBullet(path);
        if (aimAssist) ok &= injectAimAssist(path);
        ok &= injectArmorDef(path);
        ok &= injectSuperFastTouch(path);
        return ok;
    }

    /**
     * Injects Tracking Bullet configuration across all candidate paths for a game package.
     */
    public static void applyTrackingBulletConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectTrackingBullet(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied Tracking Bullet to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Batch injects key-value pairs into target configuration file using sed/grep or C++ batch patch.
     */
    public static boolean batchInjectKeys(String path, String[] keyValues, String sectionHeader) {
        if (path == null || keyValues == null || keyValues.length == 0) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                String[] keys = new String[keyValues.length];
                String[] values = new String[keyValues.length];
                for (int i = 0; i < keyValues.length; i++) {
                    String k = extractKey(keyValues[i]);
                    keys[i] = k;
                    int eq = keyValues[i].indexOf('=', k.length());
                    if (eq > 0) {
                        values[i] = keyValues[i].substring(eq + 1);
                    } else {
                        values[i] = "1";
                    }
                }
                if (nativeBatchPatchKeys(path, keys, values)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        // Direct Java file I/O when accessible
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            try {
                String existing = new String(java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                String updated = patchContentInMemory(existing, keyValues, sectionHeader, path);
                java.nio.file.Files.write(f.toPath(), updated.getBytes(StandardCharsets.UTF_8));
                return true;
            } catch (Throwable ignored) {}
        }

        StringBuilder sb = new StringBuilder();
        if (path.endsWith(".xml")) {
            for (String kv : keyValues) {
                int eq = kv.indexOf('=');
                if (eq > 0) {
                    String k = kv.substring(0, eq);
                    String v = kv.substring(eq + 1);
                    sb.append("grep -qF 'name=\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '/<\\/map>/i \\  <string name=\"").append(k).append("\">").append(v).append("<\\/string>' ").append(path).append("; ");
                }
            }
        } else if (path.endsWith(".json")) {
            for (String kv : keyValues) {
                int eq = kv.indexOf('=');
                if (eq > 0) {
                    String k = kv.substring(0, eq);
                    String v = kv.substring(eq + 1);
                    String valJson = isNumeric(v) ? v : "\"" + v + "\"";
                    sb.append("grep -qF '\"").append(k).append("\"' ").append(path)
                      .append(" || sed -i '2i \\  \"").append(k).append("\": ").append(valJson).append(",' ").append(path).append("; ");
                    sb.append("sed -i 's/\"").append(k).append("\":.*/\"").append(k).append("\": ").append(valJson).append(",/' ").append(path).append("; ");
                }
            }
        } else {
            if (sectionHeader != null && !sectionHeader.trim().isEmpty()) {
                sb.append("grep -qF '").append(sectionHeader).append("' ").append(path)
                  .append(" || echo '").append(sectionHeader).append("' >> ").append(path).append("; ");
            }
            for (String kv : keyValues) {
                String k = extractKey(kv);
                String keyEscaped = k.replace("+", "\\+").replace(".", "\\.");
                String kvEscaped = kv.replace("+", "\\+").replace("&", "\\&");
                sb.append("grep -qF '").append(k).append("' ").append(path)
                  .append(" || echo '").append(kv).append("' >> ").append(path).append("; ");
                sb.append("sed -i 's/^").append(keyEscaped).append("=.*/").append(kvEscaped).append("/' ").append(path).append("; ");
            }
        }

        String cmd = sb.toString();
        if (cmd.isEmpty()) return true;

        String res;
        if (ShizukuExecutor.hasShizukuPermission()) {
            res = ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            res = CommandExecutor.executeSystemCommand(cmd);
        }
        return res != null && !res.startsWith("ERROR");
    }

    private static String patchContentInMemory(String content, String[] keyValues, String sectionHeader, String path) {
        if (path.endsWith(".xml")) {
            for (String kv : keyValues) {
                int eq = kv.indexOf('=');
                if (eq > 0) {
                    String k = kv.substring(0, eq);
                    String v = kv.substring(eq + 1);
                    if (content.contains("name=\"" + k + "\"")) {
                        content = content.replaceAll("name=\"" + java.util.regex.Pattern.quote(k) + "\"[^>]*>", "name=\"" + k + "\">" + v + "</string>");
                    } else if (content.contains("</map>")) {
                        content = content.replace("</map>", "  <string name=\"" + k + "\">" + v + "</string>\n</map>");
                    } else {
                        content = content + "\n<string name=\"" + k + "\">" + v + "</string>";
                    }
                }
            }
            return content;
        } else if (path.endsWith(".json")) {
            for (String kv : keyValues) {
                int eq = kv.indexOf('=');
                if (eq > 0) {
                    String k = kv.substring(0, eq);
                    String v = kv.substring(eq + 1);
                    String valJson = isNumeric(v) ? v : "\"" + v + "\"";
                    if (content.contains("\"" + k + "\"")) {
                        content = content.replaceAll("\"" + java.util.regex.Pattern.quote(k) + "\"\\s*:\\s*[^,\\n}]+", "\"" + k + "\": " + valJson);
                    } else if (content.contains("{")) {
                        content = content.replaceFirst("\\{", "{\\n  \"" + k + "\": " + valJson + ",");
                    } else {
                        content = "{\n  \"" + k + "\": " + valJson + "\n}\n";
                    }
                }
            }
            return content;
        } else {
            for (String kv : keyValues) {
                String k = extractKey(kv);
                String pattern = "(?m)^" + java.util.regex.Pattern.quote(k) + "=.*$";
                if (java.util.regex.Pattern.compile(pattern).matcher(content).find()) {
                    content = content.replaceAll(pattern, java.util.regex.Matcher.quoteReplacement(kv));
                } else {
                    if (!content.endsWith("\n") && !content.isEmpty()) {
                        content += "\n";
                    }
                    content += kv + "\n";
                }
            }
            return content;
        }
    }

    private static String extractKey(String kv) {
        if (kv == null) return "";
        if (kv.startsWith("+CVars=") || kv.startsWith("-CVars=")) {
            int eq2 = kv.indexOf('=', 7);
            if (eq2 > 0) return kv.substring(0, eq2);
        }
        int eq = kv.indexOf('=');
        return eq > 0 ? kv.substring(0, eq) : kv;
    }

    /**
     * Injects Real Ultra Extreme Graphics & Max FPS unlock (120/144/165/185 FPS) into target config file.
     */
    public static boolean injectUltraExtremeGraphics(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUltraExtremeGraphics(path, forcedFps)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] graphicsKeys = {
            "FPS=" + forcedFps,
            "TargetFPS=" + forcedFps,
            "MaxFPS=" + forcedFps,
            "MaxFrameRate=" + forcedFps,
            "FrameRateLimit=" + forcedFps,
            "MobileFPSLimit=" + forcedFps,
            "HighFPSMode=1",
            "HighFrameRate=1",
            "SuperHighFPS=1",
            "UnlockFPS=1",
            "UnlockHighFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock120FPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1",
            "Ultra144FPS=1",
            "Ultra165FPS=1",
            "Ultra185FPS=1",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "GraphicsQuality=5",
            "GraphicQuality=4",
            "GraphicLevel=4",
            "HDRMode=1",
            "HDRColorMode=2",
            "UltraHDMode=1",
            "HDMode=1",
            "SuperResolution=1",
            "ResolutionScale=1.20",
            "ScreenScale=120",
            "Shadow=1",
            "ShadowQuality=2",
            "AntiAliasing=1",
            "AntiAliasingQuality=4",
            "PostProcessQuality=3",
            "TextureQuality=3",
            "EffectsQuality=3",
            "FoliageQuality=2",
            "ShadingQuality=2",
            "VulkanEnabled=1",
            "UnlockMaxGraphics=1",
            "MaxGraphic=1",
            "UltraQuality=1",
            "+CVars=r.PUBGDeviceFPS=10",
            "+CVars=r.PUBGMaxFPS=" + forcedFps,
            "+CVars=r.PUBGFrameRateLimit=" + forcedFps,
            "+CVars=r.FrameRateLimit=" + forcedFps,
            "+CVars=r.MobileFPSLimit=" + forcedFps,
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.PUBGQualityLevel=4",
            "+CVars=r.PUBGSDKQualityLevel=4",
            "+CVars=r.MobileHDR=1"
        };
        return batchInjectKeys(path, graphicsKeys, "[UltraExtremeGraphics]");
    }

    /**
     * Injects Ultra Extreme Graphics across all candidate paths for a game package.
     */
    public static void applyUltraExtremeGraphics(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectUltraExtremeGraphics(path, targetFps);
        }
        Log.i(TAG, "NativeConfigInjector: Applied Ultra Extreme Graphics & " + targetFps + " FPS to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects all updated competitive configurations (High Damage, No Recoil, High Aim Assist, Tracking Bullet, Armor Def, Super Touch, Ultra Extreme Graphics)
     * across all candidate paths for a game package.
     */
    public static int injectAllConfigsForPackage(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return 0;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return 0;

        int count = 0;
        for (String path : paths) {
            boolean ok = false;
            ok |= injectUltraExtremeGraphics(path, targetFps);
            ok |= injectHighDamage(path);
            ok |= injectNoRecoil(path);
            ok |= injectAimAssist(path);
            ok |= injectTrackingBullet(path);
            ok |= injectArmorDef(path);
            ok |= injectSuperFastTouch(path);
            if (ok) count++;
        }
        Log.i(TAG, "NativeConfigInjector: Applied all configs to " + count + " paths for " + packageName);
        return count;
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void ensureParentDirectory(String path) {
        if (path == null) return;
        ShizukuFileManager.ensureParentDirectory(path);
    }
}
