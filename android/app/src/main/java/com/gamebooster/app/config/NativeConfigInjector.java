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
 * Armor Defense, and Touch Polling.
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
    public static native boolean nativeInjectArmorDef(String path, float defBoost, float dmgReduction);
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
            "HighDamageRateMode=1"
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
            "FirstBulletAccuracy=1"
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
            "GyroStabilization=1"
        };
        return batchInjectKeys(path, aimKeys, "[AimAssist]");
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
            "ResilienceLevel=3"
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
                    int eq = keyValues[i].indexOf('=');
                    if (eq > 0) {
                        keys[i] = keyValues[i].substring(0, eq);
                        values[i] = keyValues[i].substring(eq + 1);
                    } else {
                        keys[i] = keyValues[i];
                        values[i] = "1";
                    }
                }
                if (nativeBatchPatchKeys(path, keys, values)) {
                    return true;
                }
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
                int eq = kv.indexOf('=');
                if (eq > 0) {
                    String k = kv.substring(0, eq);
                    String keyEscaped = k.replace("+", "\\+");
                    String kvEscaped = kv.replace("+", "\\+");
                    sb.append("grep -qF '").append(k).append("' ").append(path)
                      .append(" || echo '").append(kv).append("' >> ").append(path).append("; ");
                    sb.append("sed -i 's/^").append(keyEscaped).append("=.*/").append(kvEscaped).append("/' ").append(path).append("; ");
                }
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

    /**
     * Injects all updated competitive configurations (High Damage, No Recoil, High Aim Assist, Armor Def, Super Touch)
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
            ok |= injectHighDamage(path);
            ok |= injectNoRecoil(path);
            ok |= injectAimAssist(path);
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
