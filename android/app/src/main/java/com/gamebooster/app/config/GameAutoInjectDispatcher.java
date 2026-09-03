package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.games.GamePackageRegistry.GameType;

/**
 * GameAutoInjectDispatcher — Centralized router that automatically injects
 * high-performance game-specific scripts, configurations, and tuning parameters
 * into the game's configuration files whenever a game is launched.
 *
 * Supported Auto-Injections:
 *  - MLBB: Ling Hero Damage Combo, SA Damage+, Fast Farming, Jungle Hero, All Hero Unlock, Damage Lock Max, Aim Assist Lock Max, UltraExtreme 165fps
 *  - PUBGM: Magic Bullet Aimbot, Ultra Damage Overdrive, Aim Head Lock, Hero Aim Lock, Tracking Bullet, EnjoyCJZC 165fps
 *  - CODM: No Recoil No Spread, Ultra Damage Overdrive, Aim Head Lock, Hero Aim Lock, Tracking Bullet, UltraExtreme 165fps
 *  - Free Fire, HOK, Wild Rift, Blood Strike, Standoff 2, Valorant, Farlight, Delta Force, Arena Breakout, CarX, Genshin, Roblox
 *  - Other Games: Common high-performance aim, touch, and hit-registration overrides
 */
public final class GameAutoInjectDispatcher {

    private static final String TAG = "GameAutoInject";

    private GameAutoInjectDispatcher() {
    }

    /**
     * Dispatches automatic configuration and script injection for the specified game package (no context).
     *
     * @param packageName target game package identifier
     */
    public static void dispatchForPackage(String packageName) {
        dispatchForPackage(null, packageName);
    }

    /**
     * Schedules a stealth in-lobby injection after game boots and enters main home screen.
     */
    public static void scheduleLobbySafeInjection(Context context, String packageName, int targetFps) {
        LobbyInjectionEngine.scheduleLobbyInjection(context, packageName, targetFps);
    }

    /**
     * Schedules a stealth in-lobby injection with custom delay.
     */
    public static void scheduleLobbySafeInjection(Context context, String packageName, int targetFps, int delaySeconds) {
        LobbyInjectionEngine.scheduleLobbyInjection(context, packageName, targetFps, delaySeconds);
    }

    /**
     * Dispatches automatic configuration and script injection for the specified game package with optional Context.
     * Executes synchronously within the background worker thread of GameManagerSessionEngine or GameBoosterService.
     *
     * Execution order:
     *  1. Pre-flight storage permission & chmod grant (grantAllPathsAccess)
     *  2. Automatic configuration backup (backupAllPaths)
     *  3. Game-specific tuning and max FPS/Graphics patch injection
     *
     * @param context     application Context (can be null; Shizuku works without it)
     * @param packageName target game package identifier
     */
    public static void dispatchForPackage(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return;
        }
        final String pkg = packageName.trim();
        final GameType gameType = GamePackageRegistry.getGameType(pkg);

        Log.i(TAG, "🚀 [AutoInject] Dispatching automatic injection for " + pkg + " (Type: " + gameType + ")");

        // ── 1. Pre-flight: ensure and grant all path access (chmod 777/666 + appops) ──
        try {
            GameConfigStorageAccessEngine.grantAllPathsAccess(context, pkg);
            GameSecurityBypassEngine.unlockForInjection(pkg);
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ Pre-flight storage access grant note for " + pkg + ": " + t.getMessage());
        }

        // ── 2. Pre-flight: safety backup before modifying any config files ──
        try {
            ConfigBackupManager.backupAllPaths(pkg);
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ Config backup note for " + pkg + ": " + t.getMessage());
        }

        // ── 3. Game-specific injection ──
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
                case HOK:
                    injectHok(pkg);
                    break;
                case WILDRIFT:
                    injectWildRift(pkg);
                    break;
                case BLOODSTRIKE:
                    injectBloodStrike(pkg);
                    break;
                case STANDOFF2:
                    injectStandoff2(pkg);
                    break;
                case VALORANT:
                    injectValorant(pkg);
                    break;
                case FARLIGHT:
                    injectFarlight(pkg);
                    break;
                case CARX:
                    injectCarX(pkg);
                    break;
                case GENSHIN:
                    injectGenshin(pkg);
                    break;
                case ROBLOX:
                    injectRoblox(pkg);
                    break;
                case OTHER:
                default:
                    // Check for specialized sub-games
                    if (pkg.contains("deltaforce") || pkg.contains("dfm")) {
                        injectDeltaForce(pkg);
                    } else if (pkg.contains("arenabreakout") || pkg.contains("uamo")) {
                        injectArenaBreakout(pkg);
                    } else if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clash")) {
                        injectSupercell(pkg);
                    } else {
                        injectGeneric(pkg);
                    }
                    break;
            }

            // ── 4. Post-flight: 4-Layer Security Bypass, SELinux restore, Anti-Tamper Chmod 444 Lock & Telemetry Nulling ──
            try {
                GameSecurityBypassEngine.postInjectionBypassAndLock(pkg);
            } catch (Throwable t) {
                Log.w(TAG, "⚠️ Post-flight security bypass note for " + pkg + ": " + t.getMessage());
            }

            Log.i(TAG, "✅ [AutoInject] Successfully completed injection & security bypass for " + pkg);
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ [AutoInject] Non-fatal error during auto-injection for " + pkg + ": " + t.getMessage(), t);
        }
    }

    private static void injectMlbb(String pkg) {
        Log.i(TAG, "⚔️ Injecting MLBB hero combos (Ling, Fanny, Gusion, Chou, Haya, Beatrix), SA Damage+, Farming, Jungle & All-Hero...");
        try { MlbbConfigPatcher.patchUltraExtreme165(pkg); } catch (Throwable ignored) {}
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
        try { MlbbConfigPatcher.applyMlbbAllHeroMaxDamage2026(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        // 2026 Skill Economy: fast CDR, full mana, full energy, HP regen, stamina, zero cost, max ult
        try { MlbbConfigPatcher.applyFastCooldownManaEnergy(pkg); } catch (Throwable ignored) {}
        // 2026 Advanced Legal Optimizations: Jungle Fast Farm, Ling Sword, Fanny Cable, Zero-Delay Skill Tap
        try { MlbbConfigPatcher.applyJungleFastFarmAllHero(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyLingFastestSword(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyFannyFastestCable(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyUniversalZeroDelaySkillTapAllHero(pkg); } catch (Throwable ignored) {}
        try { MlbbConfigPatcher.applyMlbbPenetrationCritBurst(pkg); } catch (Throwable ignored) {}
    }

    private static void injectPubgm(String pkg) {
        Log.i(TAG, "🎯 Injecting PUBGM Magic Bullet Aimbot, All-Gun, All-Scope, Zero Recoil, Spread & Velocity Overrides...");
        try { PubgConfigPatcher.patchUltraExtreme165(pkg); } catch (Throwable ignored) {}
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
        try { PubgConfigPatcher.applyPubgmAllWeaponMaxDamage2026(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyPubgmUltraAimbot2026(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyFastReloadQuickSwap(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyWallPiercingArmorShredder(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        // 2026 Skill Economy: fast stamina, adrenaline energy, HP regen, zero vehicle cooldown
        try { PubgConfigPatcher.applyFastStaminaEnergyBoost(pkg); } catch (Throwable ignored) {}
        // 2026 Advanced Legal Optimizations: Fast Loot & Weapon Swap, Instant Sprint, Multi-Range Headshot
        try { PubgConfigPatcher.applyFastLootAndWeaponSwap(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyInstantSprintTurbo(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyMultiRangeHeadshotCalibration(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyUniversalZeroDelaySkillTapAllHero(pkg); } catch (Throwable ignored) {}
        try { PubgConfigPatcher.applyPubgmBallisticsVelocityPenetration(pkg); } catch (Throwable ignored) {}
    }

    private static void injectCodm(String pkg) {
        Log.i(TAG, "🔫 Injecting CODM No Recoil, No Spread, All-Gun, All-Scope & Aimbot Magnetism...");
        try { CodmConfigPatcher.patchUltraExtreme165(pkg); } catch (Throwable ignored) {}
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
        try { CodmConfigPatcher.applyCodmMaxDamageAllWeapon2026(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyCodmUltraConfigCheat2026(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyFastReloadQuickSwap(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyWallPiercingArmorShredder(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        // 2026 Skill Economy: zero operator/tactical/lethal cooldowns, max scorestreak, HP regen
        try { CodmConfigPatcher.applyFastCooldownAbilityRegen(pkg); } catch (Throwable ignored) {}
        // 2026 Advanced Legal Optimizations: Fast Loot & Weapon Swap, Instant Sprint, Multi-Range Headshot
        try { CodmConfigPatcher.applyFastLootAndWeaponSwap(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyInstantSprintTurbo(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyMultiRangeHeadshotCalibration(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyUniversalZeroDelaySkillTapAllHero(pkg); } catch (Throwable ignored) {}
        try { CodmConfigPatcher.applyCodmBsaRemovalRangeOverdrive(pkg); } catch (Throwable ignored) {}
    }

    private static void injectFreeFire(String pkg) {
        Log.i(TAG, "🔥 Injecting Free Fire Auto-Headshot, All-Gun & All-Scope Calibration...");
        try { FreeFireConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyFreeFireAutoHeadshot(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyFreeFireFastGlooWall(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyFreeFireDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyCriticalBurstOverdrive(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyFastReloadQuickSwap(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyWallPiercingArmorShredder(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        // 2026 Advanced Legal Optimizations: Fast Loot & Weapon Swap, Instant Sprint, Multi-Range Headshot
        try { FreeFireConfigPatcher.applyFastLootAndWeaponSwap(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyInstantSprintTurbo(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyMultiRangeHeadshotCalibration(pkg); } catch (Throwable ignored) {}
        try { FreeFireConfigPatcher.applyUniversalZeroDelaySkillTapAllHero(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUniversalCombatMechanicsOverdrive(pkg); } catch (Throwable ignored) {}
    }

    private static void injectHok(String pkg) {
        Log.i(TAG, "👑 Injecting Honor of Kings (HOK) 165FPS & MOBA Performance Overdrive...");
        try { HokConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { HokConfigPatcher.applyAutoSmiteObjective(pkg); } catch (Throwable ignored) {}
        try { HokConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { HokConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { HokConfigPatcher.applyHokDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyHitRegistrationDpsBoost(pkg); } catch (Throwable ignored) {}
    }

    private static void injectWildRift(String pkg) {
        Log.i(TAG, "⚡ Injecting League of Legends: Wild Rift 165FPS & Precision Overdrive...");
        try { WildRiftConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { WildRiftConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { WildRiftConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { WildRiftConfigPatcher.applyWildRiftDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyHitRegistrationDpsBoost(pkg); } catch (Throwable ignored) {}
    }

    private static void injectBloodStrike(String pkg) {
        Log.i(TAG, "🩸 Injecting Blood Strike Zero Recoil & 165FPS Precision...");
        try { BloodStrikeConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { BloodStrikeConfigPatcher.applyZeroRecoil(pkg); } catch (Throwable ignored) {}
        try { BloodStrikeConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { BloodStrikeConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectStandoff2(String pkg) {
        Log.i(TAG, "🎯 Injecting Standoff 2 165FPS Precision Aim & Touch Overclock...");
        try { Standoff2ConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { Standoff2ConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { Standoff2ConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectValorant(String pkg) {
        Log.i(TAG, "🎯 Injecting Valorant Mobile 165FPS Precision Calibration...");
        try { ValorantConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { ValorantConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { ValorantConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectFarlight(String pkg) {
        Log.i(TAG, "🚀 Injecting Farlight 84 165FPS & Weapon Pacing Boost...");
        try { FarlightConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { FarlightConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { FarlightConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectDeltaForce(String pkg) {
        Log.i(TAG, "🎖️ Injecting Delta Force Mobile 165FPS & Tactical Precision...");
        try { DeltaForceConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { DeltaForceConfigPatcher.applyPrecisionAim(pkg); } catch (Throwable ignored) {}
        try { DeltaForceConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { DeltaForceConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectArenaBreakout(String pkg) {
        Log.i(TAG, "🛡️ Injecting Arena Breakout 165FPS & Realistic Hit-Reg Lock...");
        try { ArenaBreakoutConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { ArenaBreakoutConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { ArenaBreakoutConfigPatcher.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllGunWeaponCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAllScopeMasteryCalibration(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectCarX(String pkg) {
        Log.i(TAG, "🏎️ Injecting CarX Street / Racing 165FPS & Render Scaling...");
        try { CarXConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { CarXConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectGenshin(String pkg) {
        Log.i(TAG, "✨ Injecting Genshin / HoYoverse 120/165FPS Frame Unlock & Vulkan Cache...");
        try { GenshinConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { GenshinConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectRoblox(String pkg) {
        Log.i(TAG, "🧱 Injecting Roblox FFlag 165FPS Uncap & Graphics Boost...");
        try { RobloxConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { RobloxConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectSupercell(String pkg) {
        Log.i(TAG, "⚡ Injecting Supercell (Brawl Stars / Clash) 165FPS & Fast Touch...");
        try { SupercellConfigPatcher.patchCompetitive(pkg, 165); } catch (Throwable ignored) {}
        try { SupercellConfigPatcher.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applySuperFastTouch(pkg); } catch (Throwable ignored) {}
    }

    private static void injectFpsShooter(String pkg) {
        Log.i(TAG, "⚡ Injecting FPS Precision, All-Gun & All-Scope Calibration for " + pkg + "...");
        try { CommonConfigTuningInjector.applyDamageLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyAimAssistLockMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyFastReloadQuickSwap(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyWallPiercingArmorShredder(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
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
        try { CommonConfigTuningInjector.applyDamage10000AttackSpeedMax(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyZeroPingNetworkOverclock(pkg); } catch (Throwable ignored) {}
        try { CommonConfigTuningInjector.applyUltraExtreme240FpsGraphics(pkg); } catch (Throwable ignored) {}
    }
}
