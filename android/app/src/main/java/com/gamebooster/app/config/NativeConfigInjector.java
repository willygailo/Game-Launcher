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
    public static native boolean nativePatchXmlKey(String path, String tag, String key, String value);
    public static native boolean nativePatchJsonKey(String path, String key, String value, boolean isNumeric);
    public static native boolean nativeInjectSpeedBoost(String path, float speedMultiplier, float sprintBoost);
    public static native boolean nativeInjectDamageBoost(String path, float multiplier, float headshotMultiplier, int critRate);
    public static native boolean nativeInjectHeroDamage1000(String path, float damageMultiplier, float headshotMultiplier, int critRate, int penetration);
    public static native boolean nativeInjectZeroRecoil(String path, float recoilScale, int stability);
    public static native boolean nativeInjectScopeZeroRecoil(String path, float recoilScale, int stability);
    public static native boolean nativeInjectAimAssist(String path, int strength, int precision);
    public static native boolean nativeInjectAimAssist1000(String path, int strength, float precision);
    public static native boolean nativeInjectTrackingBullet(String path, float trackingStrength, float hitboxMultiplier);
    public static native boolean nativeInjectTrackingBullet1000(String path, float trackingStrength, float hitboxMultiplier);
    public static native boolean nativeInjectArmorDef(String path, float defBoost, float dmgReduction);
    public static native boolean nativeInjectArmorDef1000(String path, float defBoost, float dmgReduction);
    public static native boolean nativeInjectFastCooldown(String path, float cdrRatio);
    public static native boolean nativeInjectShield1500(String path, float shieldMultiplier, float defBoost);
    public static native boolean nativeInjectDroneView(String path, int fov, int height);
    public static native boolean nativeInjectUltraExtremeGraphics(String path, int targetFps);
    public static native boolean nativeInjectPerGameProfile(String path, String gameKey, int targetFps, boolean highDamage, boolean noRecoil, boolean trackingBullet, boolean aimAssist);
    public static native boolean nativeFastMemorySync(String path);
    public static native boolean nativePreserveFileTimestamps(String path, long atimeSec, long mtimeSec);
    public static native boolean nativeStealthWrite(String path, String content);
    public static native long nativeCalculateConfigCrc32(String path);
    public static native boolean nativeSetProcessCpuAffinity(int pid, int cpuMask);
    public static native boolean nativeInjectUnrealEngineIni(String path, int targetFps);
    public static native boolean nativeInjectUnityBootConfig(String path, int targetFps);
    public static native boolean nativeInjectNextGenEngineOptimizations(String path, int targetFps, int engineType);
    public static native boolean nativeSetThreadSchedulingPolicy(int pid, int policy, int priority);
    public static native boolean nativeForceVulkanPipelineCache(String path, String pkg);
    public static native boolean nativeInjectNextGenTouchSampling(String path, int pollingRateHz);
    public static native boolean nativeInjectAimHeadLock(String path, float headMagnetism, int snapSpeed);
    public static native boolean nativeInjectUltraDamageOverdrive(String path, float damageScale, float critMultiplier, float trueDamage);
    public static native boolean nativeInjectHeroAimLock(String path, int targetPriority, float lockDistance);

    // ─── High-Level Injection Engine Methods ─────────────────────────────────

    /**
     * Injects Next-Gen 2025/2026 Engine optimizations (UE5 Nanite/TSR/Bindless, Unity 6 Swappy, HoYo/Kuro custom).
     */
    public static boolean injectNextGenEngine(String path, int targetFps, int engineType) {
        if (sNativeLibraryLoaded) {
            try {
                return nativeInjectNextGenEngineOptimizations(path, targetFps, engineType);
            } catch (Throwable t) {
                Log.w(TAG, "Native Next-Gen engine injection fallback: " + t.getMessage());
            }
        }
        return false;
    }

    /**
     * Sets POSIX real-time scheduler policy (SCHED_FIFO / SCHED_RR) with priority up to 99.
     */
    public static boolean setRealtimeThreadScheduling(int pid, int priority) {
        if (sNativeLibraryLoaded) {
            try {
                return nativeSetThreadSchedulingPolicy(pid, 1, priority);
            } catch (Throwable t) {
                Log.w(TAG, "Native RT scheduling fallback: " + t.getMessage());
            }
        }
        if (pid > 0) {
            CommandExecutor.executeSystemCommand("chrt -f -p 99 " + pid);
            return true;
        }
        return false;
    }

    /**
     * Generates and forces persistent Vulkan pipeline cache descriptors.
     */
    public static boolean forceVulkanPipelineCache(String path, String pkg) {
        if (sNativeLibraryLoaded) {
            try {
                return nativeForceVulkanPipelineCache(path, pkg);
            } catch (Throwable t) {
                Log.w(TAG, "Native Vulkan cache fallback: " + t.getMessage());
            }
        }
        return false;
    }

    /**
     * Injects 1000Hz - 2000Hz gaming touch polling rates into input profiles.
     */
    public static boolean injectNextGenTouchSampling(String path, int pollingRateHz) {
        if (sNativeLibraryLoaded) {
            try {
                return nativeInjectNextGenTouchSampling(path, pollingRateHz);
            } catch (Throwable t) {
                Log.w(TAG, "Native touch sampling fallback: " + t.getMessage());
            }
        }
        return false;
    }

    /**
     * Pins a target process or thread to high-performance Big/Prime CPU cores.
     */
    public static boolean setProcessCpuAffinity(int pid, int cpuMask) {
        if (sNativeLibraryLoaded) {
            try {
                return nativeSetProcessCpuAffinity(pid, cpuMask);
            } catch (Throwable t) {
                Log.w(TAG, "Native CPU affinity fallback: " + t.getMessage());
            }
        }
        // Fallback to taskset via Shizuku/shell
        if (pid > 0) {
            String maskHex = (cpuMask > 0) ? Integer.toHexString(cpuMask) : "f0";
            CommandExecutor.executeSystemCommand("taskset -p " + maskHex + " " + pid);
            CommandExecutor.executeSystemCommand("renice -n -20 -p " + pid);
            return true;
        }
        return false;
    }

    /**
     * Injects Unreal Engine 4/5 Engine.ini graphics and FPS unlocks.
     */
    public static boolean injectUnrealEngineIni(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUnrealEngineIni(path, targetFps)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native Unreal Engine INI fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "r.VSync=0",
            "r.FinishCurrentFrame=0",
            "r.OneFrameThreadLag=0",
            "t.MaxFPS=" + targetFps,
            "r.MobileContentScaleFactor=1.0",
            "r.Streaming.PoolSize=0",
            "r.RenderTargetPoolMin=1024",
            "r.ShadowQuality=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[SystemSettings]");
    }

    /**
     * Injects Unity boot.config optimization flags.
     */
    public static boolean injectUnityBootConfig(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUnityBootConfig(path, targetFps)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native Unity boot.config fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "gfx-enable-native-gles=1",
            "wait-for-native-debugger=0",
            "player-connection-debug=0",
            "target-frame-rate=" + targetFps,
            "hdr-display-enabled=0",
            "gc-max-time-slice=3"
        };
        return ConfigFileHelper.patchKeys(path, keys, "");
    }


    /**
     * Injects configuration with stealth timestamp retention.
     */
    public static boolean stealthInjectConfig(String path, String content) {
        if (path == null || content == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeStealthWrite(path, content)) {
                    Log.d(TAG, "C++ native stealth injected config into " + path);
                    return true;
                }
            } catch (Throwable t) {
                Log.w(TAG, "C++ nativeStealthWrite fallback: " + t.getMessage());
            }
        }

        return injectConfig(path, content);
    }

    /**
     * Injects or overwrites configuration content into target file path using C++ native method or atomic ConfigFileHelper fallback.
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

        return ConfigFileHelper.writeContentAtomic(path, content);
    }

    /**
     * Injects 1000% Ultra Overdrive Damage Script keys (100.00x damage multiplier, 1000 critical damage, 1000 penetration, 99999 retribution).
     */
    public static boolean injectHighDamage(String path) {
        return injectHighDamage(path, 185);
    }

    public static boolean injectHighDamage(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectDamageBoost(path, 100.00f, 100.00f, 100)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] damageKeys = {
            // ── 1000% Ultra Overdrive Damage ───────────────────────────────────
            "DamageMultiplier=100.00",
            "PhysicalDamageBoost=100.00",
            "MagicDamageBoost=100.00",
            "TrueDamageBoost=100.00",
            "BulletDamageBoost=100.00",
            "DamageBoost=100.00",
            "DamageBoostRatio=100.00",
            "HeadshotMultiplier=100.00",
            "HeadshotDamageMultiplier=100.00",
            "CriticalHitRate=100",
            "CriticalDamage=1000",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=10.00",
            "PenetrationBoost=1000",
            "ArmorPenetration=1000",
            "PhysicalPenetrationBoost=1000",
            "MagicPenetrationBoost=1000",
            "MagicResistPenetration=1000",
            "HighDamageRateMode=1",
            "AttackSpeedMultiplier=10.00",
            "AttackSpeedBoost=10.00",
            "ReloadSpeedMultiplier=10.00",
            "FireRateMultiplier=10.00",
            "MovementSpeedMultiplier=10.00",
            "SprintSpeedMultiplier=10.00",
            "SprintSensitivity=500",
            "AgilityMultiplier=10.00",
            "SkillDamageMultiplier=100.00",
            "HeroDamageMultiplier=10.00",
            "AllHeroDamageMultiplier=10.00",
            "TankDamageMultiplier=10.00",
            "FighterDamageMultiplier=10.00",
            "AssassinDamageMultiplier=10.00",
            "MageDamageMultiplier=10.00",
            "MarksmanDamageMultiplier=10.00",
            "SupportDamageMultiplier=10.00",
            "BurstDamageMultiplier=100.00",
            "CritDamageMultiplier=100.00",
            "WeakpointDamageMultiplier=100.00",
            "ArmorPiercingRatio=100.00",
            "WeaponBaseDamageMultiplier=100.00",
            "HeavyAttackDamageScale=100.00",
            "LightAttackDamageScale=100.00",
            "ComboDamageMultiplier=100.00",
            "JungleClearSpeedMultiplier=100.00",
            "DamageAssetOverride=1",
            "AutoDamageExecutionMode=1",
            "AutoSmiteExecution=1",
            "RetributionDamageThreshold=99999",
            "SmiteTrueDamage=99999",
            "ExecuteThreshold=99999",
            "TurretDamageReduction=0.01",
            "MinionDamageBoost=100.00",
            "MonsterDamageBoost=100.00",
            "HitboxExpansion=10.00",
            "BulletVelocityMultiplier=50.00",
            "BulletVelocityScale=50.00",
            "BodyDamageMultiplier=10.00",
            "LimbDamageMultiplier=10.00",
            "ExplosiveDamageMultiplier=10.00",
            // ── 1000% Ultra Aim Assist ─────────────────────────────────────────
            "AimAssist=1",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "AimAssistRadius=1000",
            "AimPrecision=10",
            "AutoAim=1",
            "AimTracking=1",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "SmartTargetingMode=1",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "CrosshairMagnetism=100.00",
            "ScopeAimAssist=1",
            "RedDotAimAssist=1",
            "SniperAimAssist=1",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "AimLead=1",
            "AimLeadStrength=100.00",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroSensitivityRatio=100.00",
            "GyroStabilization=1",
            "GyroSmoothFactor=1",
            "GyroLatencyMode=0",
            "GyroAimAssist=1",
            // ── UE4/UE5 CVars ─────────────────────────────────────────────────
            "+CVars=r.DamageMultiplier=100.00",
            "+CVars=r.BulletDamageScale=100.00",
            "+CVars=r.HeadshotMultiplier=100.00",
            "+CVars=r.WeaponDamageScale=100.00",
            "+CVars=r.BurstDamageMultiplier=100.00",
            "+CVars=r.WeakpointMultiplier=100.00",
            "+CVars=r.ArmorPiercingRatio=50.00",
            "+CVars=r.PhysicalDamageScale=100.00",
            "+CVars=r.MagicDamageScale=100.00",
            "+CVars=r.TrueDamageScale=100.00",
            "+CVars=r.CriticalHitRate=1.00",
            "+CVars=r.HitboxExpansion=10.00",
            "+CVars=r.BulletVelocityScale=50.00",
            "+CVars=r.PenetrationPower=50.00",
            "+CVars=r.BodyDamageMultiplier=10.00",
            "+CVars=r.LimbDamageMultiplier=10.00",
            "+CVars=r.ExplosiveDamageMultiplier=10.00",
            "+CVars=r.MovementSpeedMultiplier=10.00",
            "+CVars=r.SprintSpeedMultiplier=10.00",
            "+CVars=r.AttackSpeedMultiplier=10.00",
            "+CVars=r.AimAssist=1",
            "+CVars=r.AimAssist.Strength=100.00",
            "+CVars=r.AimAssist.Magnetism=100.00",
            "+CVars=r.AimAssist.SnapSpeed=100.00",
            "+CVars=r.AimAssistRadius=1000",
            "+CVars=r.CrosshairMagnetism=100.00",
            "+CVars=r.TargetLockSensitivity=1000",
            "+CVars=r.AimSnapStrength=100.00",
            "+CVars=r.AimLead=1",
            "+CVars=r.AimLeadStrength=100.00",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroSensitivityRatio=100.00",
            "+CVars=r.GyroZeroDelay=1",
            "+CVars=r.GyroStabilization=1",
            "+CVars=r.GyroAimAssist=1",
            "+CVars=r.SniperAimAssist=1"
        };
        return batchInjectKeys(path, damageKeys, "[DamageScript]");
    }

    /**
     * Injects 1000% Hero Damage Overdrive specifically tuned for all MOBA heroes and roles (Tank, Fighter, Assassin, Mage, Marksman, Support).
     */
    public static boolean injectHeroDamage1000(String path) {
        return injectHeroDamage1000(path, 185);
    }

    public static boolean injectHeroDamage1000(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectHeroDamage1000(path, 100.00f, 100.00f, 100, 1000)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return injectHighDamage(path, targetFps);
    }

    public static boolean injectDamageScript(String path) {
        return injectHighDamage(path, 185);
    }

    public static boolean injectDamageScript(String path, int targetFps) {
        return injectHighDamage(path, targetFps);
    }

    public static boolean injectDamageBoost(String path) {
        return injectHighDamage(path, 185);
    }

    public static boolean injectDamageBoost(String path, int targetFps) {
        return injectHighDamage(path, targetFps);
    }

    /**
     * Injects Zero Recoil & Weapon Stability keys for ALL guns and ALL scopes (RecoilScale=0.00, ZeroRecoil=1, NoRecoil=1).
     */
    public static boolean injectNoRecoil(String path) {
        return injectNoRecoil(path, 185);
    }

    public static boolean injectNoRecoil(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectZeroRecoil(path, 0.00f, 500)) {
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
            "VerticalRecoilScale=0.00",
            "HorizontalRecoilScale=0.00",
            "VerticalRecoilMultiplier=0.00",
            "HorizontalRecoilMultiplier=0.00",
            "RecoilReduction=1.00",
            "WeaponStability=500",
            "ScreenShake=0",
            "CameraShake=0",
            "NoCameraShake=1",
            "GunKick=0",
            "GunKickReduction=1.00",
            "WeaponKickReduction=1.00",
            "AllGunsRecoilReduction=1.00",
            "ScopeShakeReduction=1.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=5.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00",
            "BulletSpreadReduction=1",
            "FirstBulletAccuracy=1",
            "WeaponSway=0",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "MovementStabilization=1",
            "JoystickZeroDeadzone=1",
            "TouchJitterFilter=1",
            "ZeroInputDelay=1",
            // ── All Scope Zero Recoil Keys ──
            "IronSightRecoil=0.00",
            "RedDotRecoil=0.00",
            "HoloRecoil=0.00",
            "Scope2xRecoil=0.00",
            "Scope3xRecoil=0.00",
            "Scope4xRecoil=0.00",
            "Scope6xRecoil=0.00",
            "Scope8xRecoil=0.00",
            "CantedSightRecoil=0.00",
            "ThermalScopeRecoil=0.00",
            "SniperScopeRecoil=0.00",
            // ── All Gun Zero Recoil Keys ──
            "ARRecoilReduction=1.00",
            "DMRRecoilReduction=1.00",
            "SniperRecoilReduction=1.00",
            "SMGRecoilReduction=1.00",
            "LMGRecoilReduction=1.00",
            "ShotgunRecoilReduction=1.00",
            "PistolRecoilReduction=1.00",
            "ARRecoilScale=0.00",
            "DMRRecoilScale=0.00",
            "SniperRecoilScale=0.00",
            "SMGRecoilScale=0.00",
            "LMGRecoilScale=0.00",
            "ShotgunRecoilScale=0.00",
            "PistolRecoilScale=0.00",
            // ── UE4 / UE5 Engine CVars ──
            "+CVars=r.WeaponRecoilScale=0.00",
            "+CVars=r.VerticalRecoilMultiplier=0.00",
            "+CVars=r.HorizontalRecoilMultiplier=0.00",
            "+CVars=r.GunKickReduction=1",
            "+CVars=r.CameraShake=0",
            "+CVars=r.ScreenShake=0",
            "+CVars=r.WeaponSway=0",
            "+CVars=r.BulletSpread=0.00",
            "+CVars=r.CrosshairSpread=0.00",
            "+CVars=r.ScopeStability=5.00",
            "+CVars=r.FirstBulletAccuracy=1",
            "+CVars=r.AimPunchReduction=1",
            "+CVars=r.FlinchReduction=1",
            "+CVars=r.WeaponKick=0.00",
            "+CVars=r.ViewKick=0.00",
            "+CVars=r.RedDotRecoilScale=0.00",
            "+CVars=r.HoloRecoilScale=0.00",
            "+CVars=r.Scope2xRecoilScale=0.00",
            "+CVars=r.Scope3xRecoilScale=0.00",
            "+CVars=r.Scope4xRecoilScale=0.00",
            "+CVars=r.Scope6xRecoilScale=0.00",
            "+CVars=r.Scope8xRecoilScale=0.00",
            "+CVars=r.CantedSightRecoilScale=0.00",
            "+CVars=r.IronSightRecoilScale=0.00",
            "+CVars=r.ARRecoilScale=0.00",
            "+CVars=r.DMRRecoilScale=0.00",
            "+CVars=r.SniperRecoilScale=0.00",
            "+CVars=r.SMGRecoilScale=0.00",
            "+CVars=r.LMGRecoilScale=0.00",
            "+CVars=r.ShotgunRecoilScale=0.00"
        };
        return batchInjectKeys(path, recoilKeys, "[RecoilControl]");
    }

    /**
     * Injects Scope Zero Recoil specifically for all 8 scope tiers and all gun classes.
     */
    public static boolean injectScopeZeroRecoil(String path) {
        return injectScopeZeroRecoil(path, 185);
    }

    public static boolean injectScopeZeroRecoil(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectScopeZeroRecoil(path, 0.00f, 500)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return injectNoRecoil(path, targetFps);
    }

    /**
     * Injects 1000% Aim Assist & Smart Target Lock keys (AimAssist=1, Strength=1000, Precision=10.0, 1000Hz Gyro).
     */
    public static boolean injectAimAssist(String path) {
        return injectAimAssist(path, 185);
    }

    public static boolean injectAimAssist(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectAimAssist1000(path, 1000, 10.0f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] aimKeys = {
            // ── 1000% Aim Assist & Smart Lock ──────────────────────────────────
            "AimAssist=1",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "AimPrecision=10",
            "AutoAim=1",
            "AimTracking=1",
            "TargetLock=1",
            "TargetLockSensitivity=1000",
            "SmartTargetingMode=1",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "AimAssistRadius=1000",
            "ScopeAimAssist=1",
            "RedDotAimAssist=1",
            "SniperAimAssist=1",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "AimLead=1",
            "AimLeadStrength=100.00",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroSensitivityRatio=10.00",
            "GyroStabilization=1",
            "GyroSmoothFactor=1",
            "GyroLatencyMode=0",
            "GyroAimAssist=1",
            "+CVars=r.AimAssist=1",
            "+CVars=r.AimAssist.Strength=100.00",
            "+CVars=r.AimAssist.Magnetism=100.00",
            "+CVars=r.AimAssist.SnapSpeed=100.00",
            "+CVars=r.AimAssistRadius=1000",
            "+CVars=r.CrosshairMagnetism=100.00",
            "+CVars=r.TargetLockSensitivity=1000",
            "+CVars=r.AimSnapStrength=100.00",
            "+CVars=r.AimLead=1",
            "+CVars=r.AimLeadStrength=100.00",
            "+CVars=r.GyroSampleRate=1000",
            "+CVars=r.GyroZeroDelay=1",
            "+CVars=r.GyroSensitivityRatio=10.00",
            "+CVars=r.GyroStabilization=1",
            "+CVars=r.GyroAimAssist=1",
            "+CVars=r.SniperAimAssist=1"
        };
        return batchInjectKeys(path, aimKeys, "[AimAssist]");
    }

    /**
     * Injects Aim Head Lock, Head Magnetism & Instant Reticle Snap to Enemy Head Hitbox.
     */
    public static boolean injectAimHeadLock(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectAimHeadLock(path, 100.00f, 100)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] headKeys = {
            "AimHeadLock=1",
            "AimToHead=1",
            "AutoHeadAim=1",
            "HeadMagnetism=100.00",
            "HeadSnapSpeed=100.00",
            "HeadHitboxPrioritization=1",
            "HeadshotMultiplier=1000.00",
            "HeadshotDamageMultiplier=1000.00",
            "FirstBulletHeadshot=1",
            "NeckToHeadAimCorrection=1",
            "ScopeHeadLock=1",
            "RedDotHeadLock=1",
            "SniperHeadLock=1",
            "GyroHeadSnap=1",
            "AutoHeadTracking=1",
            "HeadHitboxRadius=100.00",
            "FirstBulletAccuracy=1",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "ScopeStability=10.00",
            "+CVars=r.AimHeadLock=1",
            "+CVars=r.AimToHead=1",
            "+CVars=r.HeadMagnetism=100.00",
            "+CVars=r.HeadSnapSpeed=100.00",
            "+CVars=r.HeadHitboxPriority=1",
            "+CVars=r.HeadshotMultiplier=1000.00",
            "+CVars=r.FirstBulletHeadshot=1",
            "+CVars=r.ScopeHeadLock=1",
            "+CVars=r.SniperHeadLock=1",
            "+CVars=r.GyroHeadSnap=1",
            "+CVars=r.BulletSpread=0.00",
            "+CVars=r.ScopeStability=10.00"
        };
        return batchInjectKeys(path, headKeys, "[HeadAimLock]");
    }

    /**
     * Injects Ultra Extreme Damage Overdrive, Critical Multiplier, and True Damage Bypass.
     */
    public static boolean injectUltraDamageOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUltraDamageOverdrive(path, 1000.00f, 100.00f, 100.00f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] overdriveKeys = {
            "UltraDamageOverdrive=1",
            "DamageMultiplier=1000.00",
            "PhysicalDamageBoost=1000.00",
            "MagicDamageBoost=1000.00",
            "TrueDamageBoost=1000.00",
            "BulletDamageBoost=1000.00",
            "WeaponBaseDamageScale=1000.00",
            "TrueDamageBypass=1000.00",
            "ArmorPiercingRatio=100.00",
            "CriticalStrikeChance=100",
            "CriticalStrikeDamage=10000",
            "CriticalHitRate=100",
            "CriticalDamageMultiplier=100.00",
            "OneHitEliminationMultiplier=1000.00",
            "LethalityScaling=1000.00",
            "ArmorShredRatio=100.00",
            "MagicPenetrationBoost=1000.00",
            "PhysicalPenetrationBoost=1000.00",
            "MonsterDamageBoost=1000.00",
            "MinionDamageBoost=1000.00",
            "RetributionDamageThreshold=999999",
            "SmiteTrueDamage=999999",
            "ExecuteTrueDamageThreshold=999999",
            "HighDamageRateMode=1",
            "DamageAssetOverride=1",
            "TurretArmorBypass=100.00",
            "+CVars=r.UltraDamageOverdrive=1",
            "+CVars=r.DamageMultiplier=1000.00",
            "+CVars=r.WeaponBaseDamageScale=1000.00",
            "+CVars=r.TrueDamageBypass=1000.00",
            "+CVars=r.ArmorPiercingRatio=100.00",
            "+CVars=r.CriticalMultiplier=100.00",
            "+CVars=r.LethalityScaling=1000.00",
            "+CVars=r.PhysicalDamageScale=1000.00",
            "+CVars=r.MagicDamageScale=1000.00",
            "+CVars=r.TrueDamageScale=1000.00",
            "+CVars=r.BulletDamageScale=1000.00"
        };
        return batchInjectKeys(path, overdriveKeys, "[UltraDamageOverdrive]");
    }

    /**
     * Injects MOBA/BR Hero Aim Lock, Target Selection Priority & Crosshair Magnetism.
     */
    public static boolean injectHeroAimLock(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectHeroAimLock(path, 1, 1000.00f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] heroLockKeys = {
            "HeroAimLock=1",
            "HeroTargetPriority=1",
            "LowestHPTargetLock=1",
            "HeroPriorityLock=1",
            "TargetLockStickiness=100.00",
            "LockDistanceRadius=1000.00",
            "SmartSkillAutoCast=1",
            "TargetHeroOnly=1",
            "AutoBasicAttackLock=1",
            "TargetLockSensitivity=10000",
            "AimAssistTargetLock=1",
            "SkillAimLock=1",
            "SmartTargetingMode=1",
            "AutoTargetTracking=1",
            "CrosshairMagnetism=100.00",
            "+CVars=r.HeroAimLock=1",
            "+CVars=r.HeroTargetPriority=1",
            "+CVars=r.LowestHPTargetLock=1",
            "+CVars=r.TargetLockStickiness=100.00",
            "+CVars=r.LockDistanceRadius=1000.00",
            "+CVars=r.TargetLockSensitivity=10000",
            "+CVars=r.SkillAimLock=1"
        };
        return batchInjectKeys(path, heroLockKeys, "[HeroAimLock]");
    }

    /**
     * Injects 1000% Tracking Bullet, Bullet Magnetism & Hitbox Expansion keys.
     */
    public static boolean injectTrackingBullet(String path) {
        return injectTrackingBullet(path, 185);
    }

    public static boolean injectTrackingBullet(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectTrackingBullet1000(path, 100.00f, 50.00f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] trackingKeys = {
            // ── 1000% Ultra Tracking Bullet ───────────────────────────────────
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "BulletMagnetism=100.00",
            "HitboxExpansion=50.00",
            "TargetLockTracking=1",
            "BulletCurveFactor=50.00",
            "BulletVelocityMultiplier=100.00",
            "BulletSpread=0.00",
            "CrosshairMagnetism=100.00",
            "FirstBulletAccuracy=1",
            "ProjectileHoming=1",
            "HomingStrength=100.00",
            "AimAssist=1",
            "AimAssistStrength=1000",
            "AimAssistRadius=1000",
            "+CVars=r.BulletTracking=1",
            "+CVars=r.MagicBullet=1",
            "+CVars=r.HitboxExpansion=50.00",
            "+CVars=r.BulletMagnetism=100.00",
            "+CVars=r.BulletVelocityScale=100.00",
            "+CVars=r.BulletCurveFactor=50.00",
            "+CVars=r.ProjectileHoming=1",
            "+CVars=r.HomingStrength=100.00"
        };
        return batchInjectKeys(path, trackingKeys, "[TrackingBullet]");
    }

    /**
     * Injects 1000% Armor Defense & Damage Reduction keys (0.999 reduction, ArmorBoost=10000, ShieldMultiplier=100.00, VestDurability=100.00, 10.00x anti-burst).
     */
    public static boolean injectArmorDef(String path) {
        return injectArmorDef(path, 185);
    }

    public static boolean injectArmorDef(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectArmorDef1000(path, 100.00f, 0.999f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] armorKeys = {
            // ── 1000% Ultra Defense & Invulnerability ─────────────────────────
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
            "PhysicalDefenseMultiplier=100.00",
            "MagicDefenseMultiplier=100.00",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "ShieldMultiplier=100.00",
            "ShieldCapacity=100.00",
            "ShieldStrength=100.00",
            "MaxHPMultiplier=50.00",
            "HPBoostRatio=50.00",
            "DamageAbsorbRatio=50.00",
            "ArmorBoost=10000",
            "MagicResistBoost=10000",
            "VestDurability=100.00",
            "VestDurabilityBoost=100.00",
            "HelmetDamageReduction=0.999",
            "TenacityRatio=0.999",
            "ResilienceLevel=10",
            "ArmorLevel=10",
            "DamageResistance=0.999",
            "ShieldEfficiency=100.00",
            "ShieldPointsMultiplier=100.00",
            "ArmorPlateEfficiency=100.00",
            "KineticArmorBoost=100.00",
            "FlakJacketRatio=0.999",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=100.00",
            "HealthRegenRate=100.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.999",
            "HeadshotDamageReduction=0.999",
            "HighDamageMitigationRatio=10.00",
            "HeavyHitAbsorption=10.00",
            "BurstDamageReduction=10.00",
            // ── UE4/UE5 CVars ─────────────────────────────────────────────────
            "+CVars=r.ArmorDamageReduction=0.999",
            "+CVars=r.VestDurabilityBoost=100.00",
            "+CVars=r.HelmetDamageReduction=0.999",
            "+CVars=r.IncomingDamageScale=0.001",
            "+CVars=r.ShieldEfficiency=100.00",
            "+CVars=r.DamageResistance=0.999",
            "+CVars=r.TenacityRatio=0.999",
            "+CVars=r.HealthRegenBoost=100.00",
            "+CVars=r.FallDamageReduction=1.00",
            "+CVars=r.ExplosionResistance=0.999",
            "+CVars=r.HeadshotDamageReduction=0.999",
            "+CVars=r.HeavyDamageDampener=10.00",
            "+CVars=r.BurstDamageReduction=10.00",
            "+CVars=r.HighDamageMitigationRatio=10.00",
            "+CVars=r.MaxHPMultiplier=50.00",
            "+CVars=r.ShieldMultiplier=100.00"
        };
        return batchInjectKeys(path, armorKeys, "[DefenseConfig]");
    }

    /**
     * Injects Speed Boost & Movement Agility keys (450% Ultra — 13.50x Movement Speed, 13.50x Sprint, 13.50x Attack Speed, 25.00x Bullet Velocity, 400 sprint sensitivity).
     */
    public static boolean injectSpeedBoost(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectSpeedBoost(path, 13.50f, 13.50f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] speedKeys = {
            // ── 450% Ultra Speed & Agility ────────────────────────────────────
            "MovementSpeedMultiplier=13.50",
            "MovementSpeedBoost=13.50",
            "SprintSpeedMultiplier=13.50",
            "SprintSpeedBoost=13.50",
            "SprintSensitivity=400",
            "AgilityMultiplier=13.50",
            "AttackSpeedMultiplier=13.50",
            "AttackSpeedBoost=13.50",
            "ReloadSpeedMultiplier=13.50",
            "FireRateMultiplier=10.00",
            "BulletVelocityMultiplier=25.00",
            "BulletVelocityScale=25.00",
            "ThrottleResponse=4.50",
            "AccelerationMultiplier=4.50",
            "TopSpeedBoost=3.50",
            "SwimSpeedMultiplier=13.50",
            "ClimbSpeedMultiplier=13.50",
            "VehicleSpeedMultiplier=4.50",
            "TouchPollingRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "HighSpeedMovement=1",
            "+CVars=r.MovementSpeedMultiplier=13.50",
            "+CVars=r.SprintSpeedMultiplier=13.50",
            "+CVars=r.AttackSpeedMultiplier=13.50",
            "+CVars=r.ReloadSpeedMultiplier=13.50",
            "+CVars=r.FireRateMultiplier=10.00",
            "+CVars=r.BulletVelocityScale=25.00",
            "+CVars=r.AccelerationMultiplier=4.50",
            "+CVars=r.VehicleSpeedMultiplier=4.50",
            "+CVars=r.ZeroInputLag=1"
        };
        return batchInjectKeys(path, speedKeys, "[SpeedEngine]");
    }

    public static boolean injectSpeedBoost(String path, int targetFps) {
        return injectSpeedBoost(path);
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
     * Injects 1000% Hero Damage Overdrive across all candidate paths for a game package.
     */
    public static void applyHeroDamage1000Config(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectHeroDamage1000(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied 1000% Hero Damage Overdrive to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects Scope & Weapon Zero Recoil across all candidate paths for a game package.
     */
    public static void applyScopeZeroRecoilConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectScopeZeroRecoil(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied Scope & Weapon Zero Recoil to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects 1000% Aim Assist & Smart Target Lock across all candidate paths for a game package.
     */
    public static void applyAimAssist1000Config(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectAimAssist(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied 1000% Aim Assist to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects 1000% Tracking Bullet across all candidate paths for a game package.
     */
    public static void applyTrackingBullet1000Config(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectTrackingBullet(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied 1000% Tracking Bullet to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects 1000% Armor Defense across all candidate paths for a game package.
     */
    public static void applyArmorDef1000Config(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectArmorDef(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied 1000% Armor Defense to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects Fast Cooldown (CDR 0.99, zero animation delay, instant cast) into target config file.
     */
    public static boolean injectFastCooldown(String path) {
        return injectFastCooldown(path, 185);
    }

    public static boolean injectFastCooldown(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectFastCooldown(path, 0.99f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] cdKeys = {
            "SkillCoolDownReduceMode=1",
            "CooldownReductionBoost=0.99",
            "CooldownReduction=0.99",
            "SkillCooldownMultiplier=0.01",
            "UltimateCooldownReduction=0.99",
            "PassiveCooldownReduction=0.99",
            "SpellCooldownReduction=0.99",
            "SkillAnimationCancelZeroDelay=1",
            "SkillResponseZeroDelay=1",
            "SkillCastZeroDelay=1",
            "InstantSkillRelease=1",
            "NoCastDelay=1",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "AttackDelayReduction=1",
            "EnergyRegenRate=100.00",
            "ManaRegenRate=100.00",
            "UnlimitedEnergy=1",
            "UnlimitedMana=1",
            "NoManaCost=1",
            "NoEnergyCost=1",
            "+CVars=r.CooldownReduction=0.99",
            "+CVars=r.SkillResponseZeroDelay=1",
            "+CVars=r.InstantCast=1",
            "+CVars=r.AttackSpeedMultiplier=25.00"
        };
        return batchInjectKeys(path, cdKeys, "[FastCooldown]");
    }

    /**
     * Injects Fast Cooldown across all candidate paths for a game package.
     */
    public static void applyFastCooldownConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectFastCooldown(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied Fast Cooldown to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects 1500+ Shield Overdrive and God-Mode Damage Mitigation into target config file.
     */
    public static boolean injectShield1500(String path) {
        return injectShield1500(path, 185);
    }

    public static boolean injectShield1500(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectShield1500(path, 1500.00f, 1000.00f)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] shieldKeys = {
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "ShieldEfficiency=1500.00",
            "ShieldPointsMultiplier=1500.00",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "PhysicalDefenseMultiplier=1000.00",
            "MagicDefenseMultiplier=1000.00",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "DamageResistance=0.9999",
            "ArmorBoost=50000",
            "MagicResistBoost=50000",
            "MaxHPMultiplier=100.00",
            "HPBoostRatio=100.00",
            "DamageAbsorbRatio=100.00",
            "VestDurability=1000.00",
            "VestDurabilityBoost=1000.00",
            "HelmetDamageReduction=0.9999",
            "TenacityRatio=0.9999",
            "ResilienceLevel=10",
            "ArmorLevel=10",
            "HealthRegenDelay=0.00",
            "HealthRegenBoost=1000.00",
            "HealthRegenRate=1000.00",
            "FallDamageReduction=1.00",
            "ExplosionResistance=0.9999",
            "HeadshotDamageReduction=0.9999",
            "HighDamageMitigationRatio=100.00",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00",
            "+CVars=r.ArmorDamageReduction=0.9999",
            "+CVars=r.ShieldMultiplier=1500.00",
            "+CVars=r.ShieldEfficiency=1500.00",
            "+CVars=r.MaxHPMultiplier=100.00",
            "+CVars=r.HealthRegenBoost=1000.00",
            "+CVars=r.HeavyDamageDampener=100.00",
            "+CVars=r.BurstDamageReduction=100.00",
            "+CVars=r.HighDamageMitigationRatio=100.00"
        };
        return batchInjectKeys(path, shieldKeys, "[DefenseShield1500]");
    }

    /**
     * Injects 1500+ Shield Overdrive across all candidate paths for a game package.
     */
    public static void applyShield1500Config(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectShield1500(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied 1500+ Shield to " + paths.size() + " paths for " + packageName);
    }

    /**
     * Injects Drone View (Camera FOV 180, Height 4.0) into target config file.
     */
    public static boolean injectDroneView(String path) {
        return injectDroneView(path, 180, 4);
    }

    public static boolean injectDroneView(String path, int fov, int height) {
        if (path == null) return false;
        ensureParentDirectory(path);

        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectDroneView(path, fov, height)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        String[] droneKeys = {
            "DroneView=1",
            "DroneViewHeight=" + height,
            "CameraHeight=" + height,
            "CameraDistance=" + fov,
            "CameraFOV=" + fov,
            "FieldOfView=" + fov,
            "WideScreenMode=1",
            "UltraWideCamera=1",
            "MapOverviewScale=2.0",
            "+CVars=r.CameraFOV=" + fov,
            "+CVars=r.DroneViewHeight=" + height,
            "+CVars=r.FieldOfView=" + fov
        };
        return batchInjectKeys(path, droneKeys, "[DroneViewUltra]");
    }

    /**
     * Injects Drone View across all candidate paths for a game package.
     */
    public static void applyDroneViewConfig(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return;

        for (String path : paths) {
            injectDroneView(path);
        }
        Log.i(TAG, "NativeConfigInjector: Applied Drone View FOV 180 to " + paths.size() + " paths for " + packageName);
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

        return ConfigFileHelper.patchKeys(path, keyValues, sectionHeader);
    }

    private static String patchContentInMemory(String content, String[] keyValues, String sectionHeader, String path) {
        return ConfigFileHelper.patchContentInMemory(content, keyValues, sectionHeader, path);
    }

    public static String extractKey(String kv) {
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
     * Injects all updated competitive configurations (High Damage, No Recoil, High Aim Assist, Tracking Bullet, Armor Def, Shield 1500, Fast CD, Drone View, Super Touch, Ultra Extreme Graphics)
     * across all candidate paths for a game package in a single format-aware atomic write pass per path.
     */
    public static int injectAllConfigsForPackage(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return 0;
        String pkg = packageName.trim().toLowerCase();
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return 0;

        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);

        // Consolidated key-value pairs for full game optimization
        String[] consolidatedKeys = {
            // ── Display & FPS Unlocks ──
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
            // ── Ultra Graphics & Textures ──
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
            // ── 1000Hz Ultra Fast Touch & Gyro ──
            "HighFreqTouchHz=" + forcedFps,
            "TouchPollingRate=1000",
            "TouchSampleRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "TouchSlopReduction=1",
            "TouchResponseLevel=3",
            "TouchPressureThreshold=0.001",
            "TouchFilterSmoothing=1",
            "InputBufferRate=1000",
            "TouchInterpolation=1",
            "MultiTouchSampling=1000",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroSensitivityRatio=20.0",
            "GyroSmoothFactor=1",
            "GyroStabilization=1",
            "GyroLatencyMode=0",
            // ── 1000% Aim Assist & Lock ──
            "AimAssist=1",
            "AimAssistStrength=10000",
            "AimAssistLevel=10",
            "AimPrecision=100",
            "AutoAim=1",
            "AimTracking=1",
            "TargetLock=1",
            "TargetLockSensitivity=10000",
            "SmartTargetingMode=1",
            "HeroPriorityLock=1",
            "LowestHPTargetLock=1",
            "AimAssistRadius=5000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "ScopeAimAssist=1",
            "RedDotAimAssist=1",
            "SniperAimAssist=1",
            // ── Zero Recoil & Weapon Stability ──
            "RecoilControl=1",
            "ZeroRecoil=1",
            "NoRecoil=1",
            "RecoilScale=0.00",
            "VerticalRecoil=0.00",
            "HorizontalRecoil=0.00",
            "VerticalRecoilScale=0.00",
            "HorizontalRecoilScale=0.00",
            "VerticalRecoilMultiplier=0.00",
            "HorizontalRecoilMultiplier=0.00",
            "RecoilReduction=1.00",
            "WeaponStability=500",
            "ScreenShake=0",
            "CameraShake=0",
            "NoCameraShake=1",
            "GunKick=0",
            "GunKickReduction=1.00",
            "WeaponKickReduction=1.00",
            "AllGunsRecoilReduction=1.00",
            "ScopeShakeReduction=1.00",
            "ScopeRecoilMultiplier=0.00",
            "ScopeStability=5.00",
            "BulletSpread=0.00",
            "CrosshairSpread=0.00",
            "SpreadScale=0.00",
            "BulletSpreadReduction=1",
            "FirstBulletAccuracy=1",
            "WeaponSway=0",
            "AimPunchReduction=1",
            "FlinchReduction=1",
            "MovementStabilization=1",
            "JoystickZeroDeadzone=1",
            "TouchJitterFilter=1",
            "ZeroInputDelay=1",
            // ── 1000% Damage Overdrive ──
            "DamageMultiplier=100.00",
            "PhysicalDamageBoost=100.00",
            "MagicDamageBoost=100.00",
            "TrueDamageBoost=100.00",
            "BulletDamageBoost=100.00",
            "DamageBoost=100.00",
            "DamageBoostRatio=100.00",
            "HeadshotMultiplier=100.00",
            "HeadshotDamageMultiplier=100.00",
            "CriticalHitRate=100",
            "CriticalDamage=1000",
            "CriticalDamageRate=100",
            "CriticalDamageMultiplier=10.00",
            "PenetrationBoost=1000",
            "ArmorPenetration=1000",
            "PhysicalPenetrationBoost=1000",
            "MagicPenetrationBoost=1000",
            "MagicResistPenetration=1000",
            "HighDamageRateMode=1",
            "SkillDamageMultiplier=100.00",
            "HeroDamageMultiplier=10.00",
            "AllHeroDamageMultiplier=10.00",
            "BurstDamageMultiplier=100.00",
            "CritDamageMultiplier=100.00",
            "WeakpointDamageMultiplier=100.00",
            "SmiteTrueDamage=99999",
            "RetributionDamageThreshold=99999",
            "ExecuteThreshold=99999",
            "AutoDamageExecutionMode=1",
            "AutoSmiteExecution=1",
            // ── 1000% Tracking & Skill Homing ──
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "AutoTrackingSkill=1",
            "SkillMagnetism=100.00",
            "BulletMagnetism=100.00",
            "HitboxExpansion=100.00",
            "ProjectileHoming=1",
            "HomingStrength=100.00",
            "BulletCurveFactor=100.00",
            "BulletVelocityMultiplier=200.00",
            // ── 1500+ Shield & Armor Defense ──
            "ShieldMultiplier=1500.00",
            "ShieldCapacity=1500.00",
            "ShieldStrength=1500.00",
            "ShieldEfficiency=1500.00",
            "ShieldPointsMultiplier=1500.00",
            "PhysicalDefenseBoost=1000.00",
            "MagicDefenseBoost=1000.00",
            "PhysicalDefenseMultiplier=1000.00",
            "MagicDefenseMultiplier=1000.00",
            "DamageReductionRatio=0.9999",
            "DamageReduction=0.9999",
            "IncomingDamageReduction=0.9999",
            "DamageResistance=0.9999",
            "MaxHPMultiplier=100.00",
            "HPBoostRatio=100.00",
            "DamageAbsorbRatio=100.00",
            "ArmorBoost=50000",
            "MagicResistBoost=50000",
            "VestDurability=1000.00",
            "HelmetDamageReduction=0.9999",
            "TenacityRatio=0.9999",
            "HealthRegenBoost=1000.00",
            "HealthRegenRate=1000.00",
            "HeavyHitAbsorption=100.00",
            "BurstDamageReduction=100.00",
            "HighDamageMitigationRatio=100.00",
            // ── Fast Cooldown & Infinite Mana ──
            "FastCooldown=1",
            "CooldownReductionRatio=0.99",
            "MaxCooldownReduction=0.99",
            "CDRRatio=0.99",
            "SkillCooldownMultiplier=0.01",
            "UltimateCooldownReduction=0.99",
            "PassiveCooldownReduction=0.99",
            "SpellCooldownReduction=0.99",
            "SkillAnimationCancelZeroDelay=1",
            "SkillResponseZeroDelay=1",
            "SkillCastZeroDelay=1",
            "InstantSkillRelease=1",
            "NoCastDelay=1",
            "AttackSpeedMultiplier=25.00",
            "AttackSpeedBoost=25.00",
            "AttackDelayReduction=1",
            "EnergyRegenRate=100.00",
            "ManaRegenRate=100.00",
            "UnlimitedEnergy=1",
            "UnlimitedMana=1",
            "NoManaCost=1",
            "NoEnergyCost=1",
            // ── Drone View FOV ──
            "DroneView=1",
            "FOVLevel=180",
            "FieldOfView=180",
            "CameraDistance=180",
            "CameraHeight=180",
            "CameraFOV=180.00",
            "MaxCameraDistance=180.00",
            "WideViewMode=1",
            "UltraWideView=1",
            "RadarView=1",
            "TacticalMapVision=1",
            "FogOfWarVision=1",
            "MapAwarenessAssist=1",
            "FullMapVision=1",
            // ── UE4 / UE5 Specific CVars ──
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
            "+CVars=r.MobileHDR=1",
            "+CVars=r.VSync=0",
            "+CVars=r.FinishCurrentFrame=0",
            "+CVars=r.OneFrameThreadLag=0",
            "+CVars=r.DamageMultiplier=100.00",
            "+CVars=r.BulletDamageScale=100.00",
            "+CVars=r.HeadshotMultiplier=100.00",
            "+CVars=r.WeaponDamageScale=100.00",
            "+CVars=r.PhysicalDamageScale=100.00",
            "+CVars=r.MagicDamageScale=100.00",
            "+CVars=r.TrueDamageScale=100.00",
            "+CVars=r.PenetrationPower=50.00",
            "+CVars=r.HitboxExpansion=10.00",
            "+CVars=r.BulletVelocityScale=50.00",
            "+CVars=r.AimAssist=1",
            "+CVars=r.AimAssist.Strength=100.00",
            "+CVars=r.AimAssistRadius=1000",
            "+CVars=r.CrosshairMagnetism=100.00",
            "+CVars=r.TargetLockSensitivity=1000",
            "+CVars=r.WeaponRecoilScale=0.00",
            "+CVars=r.VerticalRecoilMultiplier=0.00",
            "+CVars=r.HorizontalRecoilMultiplier=0.00",
            "+CVars=r.GunKickReduction=1",
            "+CVars=r.CameraShake=0",
            "+CVars=r.ScreenShake=0",
            "+CVars=r.WeaponSway=0",
            "+CVars=r.BulletSpread=0.00",
            "+CVars=r.CrosshairSpread=0.00",
            "+CVars=r.ScopeStability=5.00",
            "+CVars=r.BulletTracking=1",
            "+CVars=r.MagicBullet=1",
            "+CVars=r.BulletMagnetism=100.00",
            "+CVars=r.ArmorDamageReduction=0.999",
            "+CVars=r.ShieldMultiplier=100.00",
            // ── Aim Head Lock & Precision Snapping ──
            "AimHeadLock=1",
            "AimToHead=1",
            "AutoHeadAim=1",
            "HeadMagnetism=100.00",
            "HeadSnapSpeed=100.00",
            "HeadHitboxPrioritization=1",
            "FirstBulletHeadshot=1",
            "NeckToHeadAimCorrection=1",
            "ScopeHeadLock=1",
            "RedDotHeadLock=1",
            "SniperHeadLock=1",
            "GyroHeadSnap=1",
            "AutoHeadTracking=1",
            "HeadHitboxRadius=100.00",
            "+CVars=r.AimHeadLock=1",
            "+CVars=r.AimToHead=1",
            "+CVars=r.HeadMagnetism=100.00",
            "+CVars=r.HeadSnapSpeed=100.00",
            "+CVars=r.HeadHitboxPriority=1",
            "+CVars=r.FirstBulletHeadshot=1",
            "+CVars=r.ScopeHeadLock=1",
            "+CVars=r.SniperHeadLock=1",
            "+CVars=r.GyroHeadSnap=1",
            // ── Ultra Extreme Damage Overdrive ──
            "UltraDamageOverdrive=1",
            "WeaponBaseDamageScale=1000.00",
            "TrueDamageBypass=100.00",
            "CriticalStrikeChance=100",
            "CriticalStrikeDamage=10000",
            "OneHitEliminationMultiplier=1000.00",
            "LethalityScaling=1000.00",
            "ExecuteTrueDamageThreshold=999999",
            "+CVars=r.UltraDamageOverdrive=1",
            "+CVars=r.WeaponBaseDamageScale=1000.00",
            "+CVars=r.TrueDamageBypass=100.00",
            "+CVars=r.CriticalMultiplier=100.00",
            "+CVars=r.LethalityScaling=1000.00",
            // ── Hero Aim Lock & Smart Priority ──
            "HeroAimLock=1",
            "HeroTargetPriority=1",
            "TargetLockStickiness=100.00",
            "LockDistanceRadius=1000.00",
            "SmartSkillAutoCast=1",
            "TargetHeroOnly=1",
            "AutoBasicAttackLock=1",
            "SkillAimLock=1",
            "+CVars=r.HeroAimLock=1",
            "+CVars=r.HeroTargetPriority=1",
            "+CVars=r.TargetLockStickiness=100.00",
            "+CVars=r.LockDistanceRadius=1000.00",
            "+CVars=r.SkillAimLock=1"
        };

        int count = 0;
        for (String path : paths) {
            if (path.endsWith("Active.sav")) {
                PubgConfigPatcher.patchActiveSavBinary(pkg, forcedFps);
                count++;
                continue;
            }

            if (path.endsWith("boot.config")) {
                injectUnityBootConfig(path, forcedFps);
                injectNextGenEngine(path, forcedFps, 1);
                count++;
                continue;
            }

            if (path.endsWith("Engine.ini") || path.endsWith("GameUserSettings.ini") || path.endsWith("UserCustom.ini")) {
                injectUnrealEngineIni(path, forcedFps);
                injectNextGenEngine(path, forcedFps, 0);
            }

            // Perform single-pass format-aware atomic batch patch
            boolean patched = false;
            if (sNativeLibraryLoaded) {
                try {
                    patched = nativeInjectPerGameProfile(path, pkg, forcedFps, true, true, true, true);
                } catch (Throwable ignored) {}
            }

            if (!patched) {
                String section = path.contains("UE4Game") ? "[UserCustom DeviceProfile]" : "[GameBoosterProfile]";
                patched = ConfigFileHelper.patchKeys(path, consolidatedKeys, section);
            }

            if (patched) {
                count++;
            }
        }

        forceVulkanPipelineCache("/sdcard/Android/data/" + pkg + "/cache/vulkan_pso_cache.bin", pkg);
        Log.i(TAG, "NativeConfigInjector: Applied all next-gen configs (single-pass batch) to " + count + " paths for " + packageName);
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
