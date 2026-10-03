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

    private GameAutoInjectDispatcher() {}
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> LAST_INJECTED =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long INJECTION_COOLDOWN_MS = 8000L;

    public static boolean isPackageInjected(String packageName) {
        if (packageName == null) return false;
        Long ts = LAST_INJECTED.get(packageName.trim().toLowerCase());
        return ts != null && (System.currentTimeMillis() - ts) < INJECTION_COOLDOWN_MS;
    }

    public static void resetPackageInjectionState(String packageName) {
        if (packageName != null) {
            LAST_INJECTED.remove(packageName.trim().toLowerCase());
        }
    }

    public static void resetAll() {
        LAST_INJECTED.clear();
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
        if (!force && isPackageInjected(pkg)) {
            Log.i(TAG, "🛡️ [ConflictPrevention] " + pkg + " already injected in current window — skipping duplicate pass");
            return;
        }
        LAST_INJECTED.put(pkg, System.currentTimeMillis());
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
                if (com.gamebooster.app.spoofer.SpoofPreferences.isSpoofEnabled(ctx)) {
                    com.gamebooster.app.spoofer.DeviceSpooferEngine.applyWorkingSpoofForGame(ctx, pkg);
                }
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
                // Read user's preferred FPS for this game — honors 165fps selection properly
                int pubgmTargetFps = 185;
                try {
                    int storedFps = GameProfilePreferences.getTargetHz(ctx, pkg);
                    if (storedFps >= 120) pubgmTargetFps = storedFps;
                } catch (Throwable ignored) {}

                if (pubgmTargetFps == 165) {
                    // Dedicated 165fps SuperSmooth + HDR path
                    PubgConfigPatcher.patchUltraExtreme165(pkg);
                    PubgConfigPatcher.patchSuperSmooth165(pkg);
                    PubgConfigPatcher.apply165FpsHdrUnlock(pkg);
                } else {
                    // Default: 185fps Ultra Extreme
                    PubgConfigPatcher.patchUltraExtreme185(pkg);
                }
                PubgConfigPatcher.patch(pkg, pubgmTargetFps);

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
                FreeFireConfigPatcher.patchUltraExtreme185(pkg);
                FreeFireConfigPatcher.patch(pkg, 185);
                FreeFireConfigPatcher.applyFreeFireDamage10000AttackSpeedMax(pkg);
                FreeFireConfigPatcher.applyFreeFireMasterSuite(pkg);
                FreeFireConfigPatcher.applyFreeFireAutoHeadshot(pkg);
                FreeFireConfigPatcher.applyAutoDragHeadshot(pkg);
                FreeFireConfigPatcher.applyInstant360GlooWall(pkg);
                FreeFireConfigPatcher.applyFreeFireFastGlooWall(pkg);
                FreeFireConfigPatcher.applyDamageLockMax(pkg);
                FreeFireConfigPatcher.applyAimAssistLockMax(pkg);
                FreeFireConfigPatcher.applyVulkanPipelinePrime(pkg);
            } else if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("hkrpg") || pkg.contains("honkai") || pkg.contains("zenless") || pkg.contains("nap") || pkg.contains("wutheringwaves") || pkg.contains("kurogame") || pkg.contains("aki.intl")) {
                GenshinConfigPatcher.patchUltraExtreme185(pkg);
                GenshinConfigPatcher.patch(pkg, 185);
                GenshinConfigPatcher.applyDamage10000ElementalBurstMax(pkg);
                GenshinConfigPatcher.applyDamageLockMax(pkg);
                GenshinConfigPatcher.applyAimAssistLockMax(pkg);
                GenshinConfigPatcher.applyVulkanPipelinePrime(pkg);
                GenshinConfigPatcher.applySuperFastTouch(pkg);
            } else if (pkg.contains("hok") || pkg.contains("honorofkings") || pkg.contains("arenaofvalor") || pkg.contains("ngame.allstar") || pkg.contains("sgame") || pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("kgid")) {
                HokConfigPatcher.patchUltraExtreme185(pkg);
                HokConfigPatcher.patch(pkg, 185);
                HokConfigPatcher.applyAutoSmiteObjective(pkg);
                HokConfigPatcher.applyDamageLockMax(pkg);
                HokConfigPatcher.applyAimAssistLockMax(pkg);
                HokConfigPatcher.applyVulkanPipelinePrime(pkg);
            } else if (pkg.contains("wildrift") || pkg.contains("leagueoflegends") || pkg.contains("riotgames.league")) {
                WildRiftConfigPatcher.patchUltraExtreme185(pkg);
                WildRiftConfigPatcher.patch(pkg, 185);
                WildRiftConfigPatcher.applyDamageLockMax(pkg);
                WildRiftConfigPatcher.applyAimAssistLockMax(pkg);
                WildRiftConfigPatcher.applySuperFastTouch(pkg);
            } else if (pkg.contains("deltaforce") || pkg.contains("dfm")) {
                DeltaForceConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                DeltaForceConfigPatcher.applyDeltaForceMasterSuite(pkg);
                DeltaForceConfigPatcher.applyDamageLockMax(pkg);
                DeltaForceConfigPatcher.applyAimAssistLockMax(pkg);
                DeltaForceConfigPatcher.applyVulkanPipelinePrime(pkg);
            } else if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashofclans") || pkg.contains("clashroyale") || pkg.contains("squad")) {
                SupercellConfigPatcher.patchUltraExtreme185(pkg);
                SupercellConfigPatcher.patch(pkg, 185);
                SupercellConfigPatcher.applyDamageLockMax(pkg);
                SupercellConfigPatcher.applyAimAssistLockMax(pkg);
                SupercellConfigPatcher.applyVulkanPipelinePrime(pkg);
            } else if (pkg.contains("farlight") || pkg.contains("farlight84") || pkg.contains("solarland")) {
                FarlightConfigPatcher.applyJetpackZeroCooldown(pkg);
                FarlightConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                FarlightConfigPatcher.applyFarlightMasterSuite(pkg);
                FarlightConfigPatcher.applyDamageLockMax(pkg);
                FarlightConfigPatcher.applyAimAssistLockMax(pkg);
            } else if (pkg.contains("bloodstrike") || pkg.contains("newspike")) {
                BloodStrikeConfigPatcher.applyZeroRecoil(pkg);
                BloodStrikeConfigPatcher.applySlideCancelOverdrive(pkg);
                BloodStrikeConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                BloodStrikeConfigPatcher.applyBloodStrikeMasterSuite(pkg);
            } else if (pkg.contains("arenabreakout") || pkg.contains("uamo")) {
                ArenaBreakoutConfigPatcher.applyThermalFootstepAudio(pkg);
                ArenaBreakoutConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                ArenaBreakoutConfigPatcher.applyArenaBreakoutMasterSuite(pkg);
            } else if (pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row") || pkg.contains("speeddrifters") || pkg.contains("nfs")) {
                CarXConfigPatcher.applyTorqueHorsepower10000Max(pkg);
                CarXConfigPatcher.applyDamageLockMax(pkg);
                CarXConfigPatcher.applySuperFastTouch(pkg);
            } else if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
                Standoff2ConfigPatcher.applyTick128ZeroSpread(pkg);
                Standoff2ConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                Standoff2ConfigPatcher.applyStandoff2MasterSuite(pkg);
            } else if (pkg.contains("roblox")) {
                RobloxConfigPatcher.applyDamage10000Max(pkg);
                RobloxConfigPatcher.applyDamageLockMax(pkg);
                RobloxConfigPatcher.applyAimAssistLockMax(pkg);
            } else if (pkg.contains("projectc") || pkg.contains("valorant")) {
                ValorantConfigPatcher.patchUltraExtreme185(pkg);
                ValorantConfigPatcher.patch(pkg, 185);
                ValorantConfigPatcher.applyValorantMasterSuite(pkg);
                ValorantConfigPatcher.applyCounterStrafeAimLock(pkg);
                ValorantConfigPatcher.applyDamageLockMax(pkg);
                ValorantConfigPatcher.applyAimAssistLockMax(pkg);
                ValorantConfigPatcher.applyVulkanPipelinePrime(pkg);
            } else if (pkg.contains("pesam") || pkg.contains("fifamobile") || pkg.contains("easports") || pkg.contains("soccer") || pkg.contains("dls7")) {
                CommonConfigTuningInjector.applyDamageLockMax(pkg);
                CommonConfigTuningInjector.applyAimAssistLockMax(pkg);
                CommonConfigTuningInjector.applyVulkanPipelinePrime(pkg);
                CommonConfigTuningInjector.applySuperFastTouch(pkg);
                CommonConfigTuningInjector.applyNetworkLagCompensation(pkg);
            } else if (pkg.contains("sololv") || pkg.contains("mrevolution") || pkg.contains("lifeafter") || pkg.contains("lbsg")) {
                CommonConfigTuningInjector.applyDamageLockMax(pkg);
                CommonConfigTuningInjector.applyAimAssistLockMax(pkg);
                CommonConfigTuningInjector.applyVulkanPipelinePrime(pkg);
                CommonConfigTuningInjector.applySuperFastTouch(pkg);
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
