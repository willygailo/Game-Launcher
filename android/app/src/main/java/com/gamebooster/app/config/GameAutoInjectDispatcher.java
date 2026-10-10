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
 *
 * MLBB fix (2026-10): All heavy MLBB file I/O is deferred 5 seconds post-launch
 * to avoid competing with Unity's splash-screen CRC validation loop. Running asset
 * deployment + 130+ hero script writes during the splash caused OOM / stall in
 * rikka.shizuku's ParcelFileDescriptor transfer thread.
 */
public final class GameAutoInjectDispatcher {

    private static final String TAG = "GameAutoInject";

    private GameAutoInjectDispatcher() {}

    private static final java.util.concurrent.ConcurrentHashMap<String, Long> LAST_INJECTED =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long INJECTION_COOLDOWN_MS = 8000L;

    /**
     * Session-level guard: asset deployment (unity3d copies) runs at most once
     * every 2 minutes per package to avoid thrashing game directories mid-match.
     */
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> LAST_ASSET_DEPLOYED =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long ASSET_DEPLOY_COOLDOWN_MS = 120_000L;

    /**
     * Session-level guard: hero script dispatch (130+ writes) runs at most once
     * every 5 minutes per package.
     */
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> LAST_HERO_DISPATCH =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long HERO_DISPATCH_COOLDOWN_MS = 300_000L;

    public static boolean isPackageInjected(String packageName) {
        if (packageName == null) return false;
        Long ts = LAST_INJECTED.get(packageName.trim().toLowerCase());
        return ts != null && (System.currentTimeMillis() - ts) < INJECTION_COOLDOWN_MS;
    }

    public static void resetPackageInjectionState(String packageName) {
        if (packageName != null) {
            String key = packageName.trim().toLowerCase();
            LAST_INJECTED.remove(key);
            LAST_ASSET_DEPLOYED.remove(key);
            LAST_HERO_DISPATCH.remove(key);
        }
    }

    public static void resetAll() {
        LAST_INJECTED.clear();
        LAST_ASSET_DEPLOYED.clear();
        LAST_HERO_DISPATCH.clear();
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

            // ── 2026: Container Evasion ──
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
                com.gamebooster.app.spoofer.DeviceSpooferEngine.autoApplyOptimalProfileForGame(ctx, pkg);
            } catch (Throwable t) {
                Log.w(TAG, "Device spoof injection note for " + pkg + ": " + t.getMessage());
            }

            if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
                // ────────────────────────────────────────────────────────────────────────────
                // MLBB SPLASH-SAFE DEFERRED INJECTION
                //
                // deployMlbbAssets copies .unity3d / binary files. Running this during MLBB's
                // Unity splash CRC validation window causes I/O contention → OOM in
                // rikka.shizuku's ParcelFileDescriptor transfer thread → loading screen hangs.
                // ALL MLBB file I/O is deferred 5 s onto a worker thread so Unity finishes
                // its CRC pass before we write anything to the same directories.
                // ────────────────────────────────────────────────────────────────────────────

                final boolean needsAssetDeploy;
                Long lastDeploy = LAST_ASSET_DEPLOYED.get(pkg);
                if (lastDeploy == null || (System.currentTimeMillis() - lastDeploy) > ASSET_DEPLOY_COOLDOWN_MS) {
                    LAST_ASSET_DEPLOYED.put(pkg, System.currentTimeMillis());
                    needsAssetDeploy = true;
                } else {
                    needsAssetDeploy = false;
                }

                final boolean needsHeroDispatch;
                Long lastHero = LAST_HERO_DISPATCH.get(pkg);
                if (lastHero == null || (System.currentTimeMillis() - lastHero) > HERO_DISPATCH_COOLDOWN_MS) {
                    LAST_HERO_DISPATCH.put(pkg, System.currentTimeMillis());
                    needsHeroDispatch = true;
                } else {
                    needsHeroDispatch = false;
                }

                final Context fCtx = ctx;
                final String fPkg = pkg;

                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    // 5-second grace period — MLBB splash finishes CRC in ~3-4 s
                    try { Thread.sleep(5000); } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                    try {
                        MlbbConfigPatcher.cleanLegacyLoadingLocks(fPkg);
                        if (needsAssetDeploy) {
                            MlbbConfigPatcher.deployMlbbAssets(fCtx, fPkg);
                        }
                        MlbbConfigPatcher.patchUltraExtreme185(fPkg);
                        MlbbConfigPatcher.patch(fPkg, 185);
                        MlbbConfigPatcher.patchCompetitive(fPkg, 185);
                        MlbbConfigPatcher.applyMlbbPrefsIntAndBootConfig(fPkg, 185);
                        MlbbConfigPatcher.applyMlbbTacticalSettings(fPkg);
                        MlbbConfigPatcher.applyBattleConfigOverdrive(fPkg);
                        MlbbConfigPatcher.applyRankedCombatFullSuite(fPkg);
                        MlbbConfigPatcher.applyClassicCombatFullSuite(fPkg);
                        MlbbConfigPatcher.applyMlbbRankedMastery(fPkg);
                        MlbbConfigPatcher.applyFastLoadSplashBypass(fPkg);
                        MlbbConfigPatcher.applyFastFarmingAllHero(fPkg);
                        MlbbConfigPatcher.applyFastRetributionObjectiveSteal(fPkg);
                        MlbbConfigPatcher.applyFastAttackSpeedAllHero(fPkg);
                        MlbbConfigPatcher.applyAllHeroGodSuite2026(fPkg);
                        MlbbConfigPatcher.applyEnemyLockMaxAllScope(fPkg);
                        MlbbConfigPatcher.applyAutoHeadshotBulletKill(fPkg);
                        MlbbConfigPatcher.applyUltraDamageAllHero(fPkg);
                        MlbbConfigPatcher.applyArmorAllHero(fPkg);
                        MlbbConfigPatcher.applyMlbbGodModeFullOverdrive(fPkg);
                        MlbbConfigPatcher.applyMlbbCombatOverdrive2026(fPkg);
                        MlbbConfigPatcher.applyMlbbBasicAttackRegenOverdrive(fPkg);
                        MlbbConfigPatcher.applyMlbbSovereignFullWorkingCombatSuite(fPkg);
                        MlbbConfigPatcher.applyMlbbUniversalZeroDelayCombo(fPkg);
                        // NEW MAP & PATCH 2.2.16 UPDATE (Season 42+): camera, radar, 185 FPS, anti-redownload
                        MlbbConfigPatcher.applyMlbbNewMapUpdateConfig(fPkg);
                        MlbbConfigPatcher.applyMlbb2216PatchFix(fPkg);
                        int mlbbDroneTier = MlbbDroneViewPatcher.DEFAULT_TIER;
                        boolean mlbbDroneEnabled = true;
                        try {
                            android.content.SharedPreferences dPrefs = fCtx != null
                                    ? fCtx.getSharedPreferences("mlbb_drone_prefs", Context.MODE_PRIVATE)
                                    : null;
                            if (dPrefs != null) {
                                mlbbDroneEnabled = dPrefs.getBoolean("drone_enabled", true);
                                mlbbDroneTier = MlbbDroneViewPatcher.normalizeTier(dPrefs.getInt("drone_tier", MlbbDroneViewPatcher.DEFAULT_TIER));
                            }
                        } catch (Throwable ignored) {}
                        if (mlbbDroneEnabled) {
                            MlbbConfigPatcher.applyMlbbUltraDroneViewMaxFov(fPkg, mlbbDroneTier);
                            MlbbDroneViewPatcher.deployV3FixConfig(fCtx, fPkg, mlbbDroneTier, false);
                        }
                        MlbbConfigPatcher.applyMlbbAllRolesNoLimitSuite(fPkg);
                        MlbbConfigPatcher.applyMlbbAllItemsNoLimitSuite(fPkg);
                        // Hero scripts: 130+ writes, gated to once per 5-minute window
                        if (needsHeroDispatch && fCtx != null) {
                            MlbbHeroScriptDispatcher.dispatchAllHeroes(fCtx, fPkg);
                        }
                        // Native inject deferred together with rest of MLBB I/O
                        NativeConfigInjector.injectAllConfigsForPackage(fPkg, 185);
                        MlbbConfigPatcher.cleanLegacyLoadingLocks(fPkg);
                        try {
                            com.gamebooster.app.mods.mlbb.MlbbModManager.applyProfile(fCtx, fPkg, com.gamebooster.app.mods.GameModProfile.load(fCtx));
                        } catch (Throwable modErr) {
                            Log.w(TAG, "MLBB Mod Profile injection warning: " + modErr.getMessage());
                        }
                        Log.i(TAG, "✅ [MLBB Deferred Inject] All overrides applied for " + fPkg);
                    } catch (Throwable t) {
                        Log.e(TAG, "Error in MLBB deferred injection for " + fPkg, t);
                    }
                });

            } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile") || pkg.contains("pubgm")) {
                int pubgmTargetFps = 185;
                try {
                    int storedFps = GameProfilePreferences.getTargetHz(ctx, pkg);
                    if (storedFps >= 120) pubgmTargetFps = storedFps;
                } catch (Throwable ignored) {}

                if (pubgmTargetFps == 165) {
                    PubgConfigPatcher.patchUltraExtreme165(pkg);
                    PubgConfigPatcher.patchSuperSmooth165(pkg);
                    PubgConfigPatcher.apply165FpsHdrUnlock(pkg);
                } else {
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
                CodmConfigPatcher.deployCodmAssets(ctx, pkg);
                CodmConfigPatcher.deployCodmCombatAssets(ctx, pkg);
                CodmConfigPatcher.patchUltraExtreme185(pkg);
                CodmConfigPatcher.patch(pkg, 185);
                CodmConfigPatcher.applyDamage10000AttackSpeedMax(pkg);
                CodmConfigPatcher.applyCodmGodModeFullOverdrive(pkg);
                CodmConfigPatcher.applyCodmMasterSuite(pkg);
                CodmConfigPatcher.applyFastReloadQuickSwap(pkg);
                CodmConfigPatcher.applyCodmInstantChamberingQuickDraw(pkg);
                CodmConfigPatcher.applyCodmSlideCancelMobility(pkg);
                CodmConfigPatcher.applyCodmUltraDroneViewMaxFov(pkg);
                try {
                    com.gamebooster.app.mods.codm.CodmModManager.applyProfile(ctx, pkg, com.gamebooster.app.mods.GameModProfile.load(ctx));
                } catch (Throwable modErr) {
                    Log.w(TAG, "CODM Mod Profile injection warning: " + modErr.getMessage());
                }
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
            // MLBB skipped here — runs deferred in the 5-second background block above
            if (!pkg.contains("mobile.legends") && !pkg.contains("mobilelegends")) {
                NativeConfigInjector.injectAllConfigsForPackage(pkg, 185);
            }
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
