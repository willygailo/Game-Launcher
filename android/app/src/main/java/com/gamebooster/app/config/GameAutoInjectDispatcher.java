package com.gamebooster.app.config;

import android.util.Log;

import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.games.GamePackageRegistry.GameType;

/**
 * GameAutoInjectDispatcher — Centralized router that automatically injects
 * high-performance game-specific scripts, configurations, and tuning parameters
 * into the game's configuration files whenever a game is launched.
 *
 * Supported Auto-Injections:
 *  - MLBB: Ling Hero Damage Combo, SA Damage+, Fast Farming, Jungle Hero, All Hero Unlock, Damage Lock Max, Aim Assist Lock Max
 *  - PUBGM: Magic Bullet Aimbot, Ultra Damage Overdrive, Aim Head Lock, Hero Aim Lock, Tracking Bullet
 *  - CODM: No Recoil No Spread, Ultra Damage Overdrive, Aim Head Lock, Hero Aim Lock, Tracking Bullet
 *  - Other Games: Common high-performance aim, touch, and hit-registration overrides
 */
public final class GameAutoInjectDispatcher {

    private static final String TAG = "GameAutoInject";

    private GameAutoInjectDispatcher() {
    }

    /**
     * Dispatches automatic configuration and script injection for the specified game package.
     * Executes synchronously within the background worker thread of GameManagerSessionEngine or GameBoosterService.
     *
     * @param packageName target game package identifier
     */
    public static void dispatchForPackage(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return;
        }
        final String pkg = packageName.trim();
        final GameType gameType = GamePackageRegistry.getGameType(pkg);

        Log.i(TAG, "🚀 [AutoInject] Dispatching automatic injection for " + pkg + " (Type: " + gameType + ")");

        try {
            switch (gameType) {
                case MLBB:
                    injectMlbb(pkg);
                    break;
                case PUBGM:
                    injectPubgm(pkg);
                    break;
                case CODM:
                    injectCodm(pkg);
                    break;
                case FREEFIRE:
                    injectFreeFire(pkg);
                    break;
                case BLOODSTRIKE:
                case STANDOFF2:
                case VALORANT:
                case FARLIGHT:
                    injectFpsShooter(pkg);
                    break;
                case HOK:
                case WILDRIFT:
                case CARX:
                case GENSHIN:
                case ROBLOX:
                case OTHER:
                default:
                    injectGeneric(pkg);
                    break;
            }
            Log.i(TAG, "✅ [AutoInject] Successfully completed injection for " + pkg);
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ [AutoInject] Non-fatal error during auto-injection for " + pkg + ": " + t.getMessage(), t);
        }
    }

    private static void injectMlbb(String pkg) {
        Log.i(TAG, "⚔️ Injecting MLBB hero combos (Ling, Fanny, Gusion, Chou, Haya, Beatrix), SA Damage+, Farming, Jungle & All-Hero...");
        try { MlbbConfigPatcher.applyLingHeroDamageCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyUltraDamageAllHero(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyArmorAllHero(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyFastAttackSpeedAllHero(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyAllHeroItemSkillBoost(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyFannyAutoFullEnergy(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyLingFastestComboAutoSword(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyGusionUltraOverdrive(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyKaguraCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyZilongAutoSlash(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applySaberCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyAlucardLifestealCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyYiSunShinCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyChouFreestyleCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyLancelotDashCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyFrancoHookCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyFannyFastCableCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyGusionDaggerCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyChouKickCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyHayabusaShadowCombo(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyBeatrixAllGunDamage(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyCriticalBurstOverdrive(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applySaDamagePlus(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyFastFarming(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyJungleHero(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyAllHeroUnlock(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyHeroAimLockConfig(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyAimHeadLockConfig(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyUltraDamageOverdriveConfig(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyTrackingBulletConfig(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectPubgm(String pkg) {
        Log.i(TAG, "🎯 Injecting PUBGM Magic Bullet Aimbot, All-Gun, All-Scope, Zero Recoil, Spread & Velocity Overrides...");
        try { PubgConfigPatcher.applyMagicBulletAimbot(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyNoScopeAimbot(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyAllScopeAimbot(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyLongRangeScopeHeadshot(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyMidRangeAutoHeadshot(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyPubgmFastAttackSpeed(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyUltraDamageOverdriveConfig(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyAimHeadLockConfig(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyHeroAimLockConfig(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyTrackingBulletConfig(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectCodm(String pkg) {
        Log.i(TAG, "🔫 Injecting CODM No Recoil, No Spread, All-Gun, All-Scope & Aimbot Magnetism...");
        try { CodmConfigPatcher.applyNoRecoilNoSpread(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyNoScopeAimbot(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyAllScopeAimbot(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyLongRangeHeadshot(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyMidRangeHeadshot(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyFastAttackSpeed(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyUltraDamageOverdriveConfig(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyAimHeadLockConfig(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyHeroAimLockConfig(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyTrackingBulletConfig(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectFreeFire(String pkg) {
        Log.i(TAG, "🔥 Injecting Free Fire Auto-Headshot, All-Gun & All-Scope Calibration...");
        try { NativeConfigInjector.injectFreeFireAutoHeadshot(GameConfigPathResolver.getPrimaryConfigPath(pkg)); } catch (Throwable ignored) {}
        try { NativeConfigInjector.injectFreeFireFastGlooWall(GameConfigPathResolver.getPrimaryConfigPath(pkg)); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyCriticalBurstOverdrive(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectFpsShooter(String pkg) {
        Log.i(TAG, "⚡ Injecting FPS Precision, All-Gun & All-Scope Calibration for " + pkg + "...");
        try { CommonConfigTuningInjector.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyHitRegistrationDpsBoost(pkg); } catch (Throwable ignored) {}
    }

    private static void injectGeneric(String pkg) {
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyHitRegistrationDpsBoost(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
    }
}
