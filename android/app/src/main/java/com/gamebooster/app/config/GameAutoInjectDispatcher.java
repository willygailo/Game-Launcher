package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.FpsForceUnlockEngine;
import com.gamebooster.app.engine.PlayIntegrityBypassEngine;
import com.gamebooster.app.engine.AceContainerEvasionEngine;

/**
 * Compatibility bridge for older callers. Game data is never changed by the
 * launcher; callers can retain this API without mutating another app's files,
 * process state, or network configuration.
 */
public final class GameAutoInjectDispatcher {

    private static final String TAG = "GameAutoInject";

    private GameAutoInjectDispatcher() {
    }

    public static boolean isPackageInjected(String packageName) {
        return false;
    }

    public static void resetPackageInjectionState(String packageName) {
        // No state is retained.
    }

    public static void resetAll() {
        // No state is retained.
    }

    public static void dispatchForPackage(String packageName) {
        dispatchForPackage(null, packageName, false);
    }

    public static void dispatchForPackage(String packageName, boolean force) {
        dispatchForPackage(null, packageName, force);
    }

    public static void dispatchForPackage(Context context, String packageName) {
        dispatchForPackage(context, packageName, false);
    }

    public static void dispatchForPackage(Context context, String packageName, boolean force) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return;
        }
        String pkg = packageName.trim().toLowerCase();
        try {
            Context ctx = context != null ? context : ConfigBackupManager.getAppContext();

            // ── 2026: FPS 185 Force Unlock (all layers — before game-specific patchers) ──
            try {
                FpsForceUnlockEngine.applyFps185ForceUnlock(ctx, pkg);
            } catch (Throwable t) {
                Log.w(TAG, "FpsForceUnlockEngine note for " + pkg + ": " + t.getMessage());
            }

            // ── 2026: Play Integrity Bypass + DenyList enforcement ──
            try {
                PlayIntegrityBypassEngine.applyFullBypassSuite(ctx, pkg);
            } catch (Throwable t) {
                Log.w(TAG, "PlayIntegrityBypassEngine note for " + pkg + ": " + t.getMessage());
            }

            // ── 2026: Container Evasion (warn if running in VirtualAPP/container) ──
            try {
                AceContainerEvasionEngine.ContainerStatus containerStatus =
                    AceContainerEvasionEngine.applyContainerEvasion(pkg);
                if (containerStatus == AceContainerEvasionEngine.ContainerStatus.CONTAINER_DETECTED) {
                    Log.w(TAG, "[ContainerWarning] Game running in container — ACE may flag this device!");
                }
            } catch (Throwable t) {
                Log.w(TAG, "AceContainerEvasionEngine note for " + pkg + ": " + t.getMessage());
            }

            try {
                com.gamebooster.app.spoofer.DeviceSpooferEngine.applyWorkingSpoofForGame(ctx, pkg);
            } catch (Throwable t) {
                Log.w(TAG, "Device spoof injection note for " + pkg + ": " + t.getMessage());
            }

            if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
                MlbbConfigPatcher.patchUltraExtreme185(pkg);
                MlbbConfigPatcher.patch(pkg, 185);
                MlbbConfigPatcher.patchCompetitive(pkg, 185);
                MlbbConfigPatcher.applyMlbbPrefsIntAndBootConfig(pkg, 185);
                MlbbConfigPatcher.applyFastLoadSplashBypass(pkg);
                MlbbConfigPatcher.applyFastFarmingAllHero(pkg);
                MlbbConfigPatcher.applyFastRetributionObjectiveSteal(pkg);
                MlbbConfigPatcher.applyFastAttackSpeedAllHero(pkg);
                MlbbConfigPatcher.applyAllHeroGodSuite2026(pkg);
                MlbbConfigPatcher.applyEnemyLockMaxAllScope(pkg);
                MlbbConfigPatcher.applyAutoHeadshotBulletKill(pkg);
                MlbbConfigPatcher.applyUltraDamageAllHero(pkg);
                MlbbConfigPatcher.applyArmorAllHero(pkg);
                MlbbConfigPatcher.applyMlbbGodModeFullOverdrive(pkg);
                MlbbConfigPatcher.applyMlbbCombatOverdrive2026(pkg);
                MlbbConfigPatcher.applyMlbbBasicAttackRegenOverdrive(pkg);
                MlbbConfigPatcher.applyMlbbSovereignFullWorkingCombatSuite(pkg);
                MlbbConfigPatcher.applyMlbbUniversalZeroDelayCombo(pkg);
                // NEW MAP & PATCH 2.2.16 UPDATE (Season 42+): sync camera, radar, 185 FPS, and anti-redownload keys
                MlbbConfigPatcher.applyMlbbNewMapUpdateConfig(pkg);
                MlbbConfigPatcher.applyMlbb2216PatchFix(pkg);
                int mlbbDroneTier = MlbbDroneViewPatcher.DEFAULT_TIER;
                boolean mlbbDroneEnabled = true;
                try {
                    android.content.SharedPreferences dPrefs = ctx != null
                            ? ctx.getSharedPreferences("mlbb_drone_prefs", Context.MODE_PRIVATE)
                            : null;
                    if (dPrefs != null) {
                        mlbbDroneEnabled = dPrefs.getBoolean("drone_enabled", true);
                        mlbbDroneTier = dPrefs.getInt("drone_tier", MlbbDroneViewPatcher.DEFAULT_TIER);
                    }
                } catch (Throwable ignored) {}
                if (mlbbDroneEnabled) {
                    MlbbConfigPatcher.applyMlbbUltraDroneViewMaxFov(pkg, mlbbDroneTier);
                    MlbbDroneViewPatcher.deployV3FixConfig(ctx, pkg, mlbbDroneTier, false);
                }
                MlbbConfigPatcher.applyMlbbAllRolesNoLimitSuite(pkg);
                MlbbConfigPatcher.applyMlbbAllItemsNoLimitSuite(pkg);
                if (context != null) {
                    MlbbHeroScriptDispatcher.dispatchAllHeroes(context, pkg);
                }
            } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile") || pkg.contains("pubgm")) {
                PubgConfigPatcher.patchUltraExtreme185(pkg);
                PubgConfigPatcher.patch(pkg, 185);
                PubgConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                PubgConfigPatcher.applyPubgmGodModeFullOverdrive(pkg);
                PubgConfigPatcher.applyPubgmMasterSuite(pkg);
                PubgConfigPatcher.applyPubgmZeroRecoilNoSway(pkg);
                PubgConfigPatcher.applyPubgmMagicBulletInstantHit(pkg);
                PubgConfigPatcher.applyPubgmHitboxExpander35(pkg);
                PubgConfigPatcher.applyPubgUltraDroneViewMaxFov(pkg);
                PubgConfigPatcher.deployPakPatch(pkg);
            } else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) {
                CodmConfigPatcher.patchUltraExtreme185(pkg);
                CodmConfigPatcher.patch(pkg, 185);
                CodmConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                CodmConfigPatcher.applyCodmGodModeFullOverdrive(pkg);
                CodmConfigPatcher.applyCodmMasterSuite(pkg);
                CodmConfigPatcher.applyFastReloadQuickSwap(pkg);
                CodmConfigPatcher.applyCodmInstantChamberingQuickDraw(pkg);
                CodmConfigPatcher.applyCodmSlideCancelMobility(pkg);
                CodmConfigPatcher.applyCodmUltraDroneViewMaxFov(pkg);
            } else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
                FreeFireConfigPatcher.applyFreeFireDamage10000AttackSpeedMax(pkg);
                FreeFireConfigPatcher.applyFreeFireMasterSuite(pkg);
                FreeFireConfigPatcher.applyFreeFireAutoHeadshot(pkg);
                FreeFireConfigPatcher.applyAutoDragHeadshot(pkg);
                FreeFireConfigPatcher.applyInstant360GlooWall(pkg);
                FreeFireConfigPatcher.applyFreeFireFastGlooWall(pkg);
                FreeFireConfigPatcher.applyDamageLockMax(pkg);
                FreeFireConfigPatcher.applyAimAssistLockMax(pkg);
            } else if (pkg.contains("farlight") || pkg.contains("farlight84")) {
                FarlightConfigPatcher.applyJetpackZeroCooldown(pkg);
                FarlightConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                FarlightConfigPatcher.applyFarlightMasterSuite(pkg);
                FarlightConfigPatcher.applyDamageLockMax(pkg);
                FarlightConfigPatcher.applyAimAssistLockMax(pkg);
            } else if (pkg.contains("bloodstrike")) {
                BloodStrikeConfigPatcher.applyZeroRecoil(pkg);
                BloodStrikeConfigPatcher.applySlideCancelOverdrive(pkg);
                BloodStrikeConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                BloodStrikeConfigPatcher.applyBloodStrikeMasterSuite(pkg);
            } else if (pkg.contains("arenabreakout")) {
                ArenaBreakoutConfigPatcher.applyThermalFootstepAudio(pkg);
                ArenaBreakoutConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                ArenaBreakoutConfigPatcher.applyArenaBreakoutMasterSuite(pkg);
            } else if (pkg.contains("carx")) {
                CarXConfigPatcher.applyTorqueHorsepower10000Max(pkg);
                CarXConfigPatcher.applyDamageLockMax(pkg);
                CarXConfigPatcher.applySuperFastTouch(pkg);
            } else if (pkg.contains("standoff2")) {
                Standoff2ConfigPatcher.applyTick128ZeroSpread(pkg);
                Standoff2ConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                Standoff2ConfigPatcher.applyStandoff2MasterSuite(pkg);
            } else if (pkg.contains("roblox")) {
                RobloxConfigPatcher.applyDamage10000Max(pkg);
                RobloxConfigPatcher.applyDamageLockMax(pkg);
                RobloxConfigPatcher.applyAimAssistLockMax(pkg);
            }

            // Universal Native Combat & Security Lock
            NativeConfigInjector.injectAllConfigsForPackage(pkg, 185);
            GameSecurityBypassEngine.postInjectionBypassAndLock(pkg);

            // ── 2026: Deferred FPS re-apply at T+3s (prevents OEM reverting mid-launch) ──
            try {
                FpsForceUnlockEngine.applyFps185ForceUnlockDeferred(ctx, pkg);
            } catch (Throwable t) {
                Log.w(TAG, "FpsForceUnlock deferred note: " + t.getMessage());
            }

            Log.i(TAG, "✅ [Full Inject Complete] All overrides injected & locked for " + pkg);

        } catch (Throwable t) {
            Log.e(TAG, "Error executing dispatchForPackage for " + pkg, t);
        }
    }

    public static void scheduleLobbySafeInjection(Context context, String packageName, int targetFps) {
        LobbyInjectionEngine.scheduleLobbyInjection(context, packageName, targetFps, 10);
    }

    public static void scheduleLobbySafeInjection(Context context, String packageName,
                                                  int targetFps, int delaySeconds) {
        LobbyInjectionEngine.scheduleLobbyInjection(context, packageName, targetFps, delaySeconds);
    }
}
