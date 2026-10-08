# Codebase Dead Code Removal & Performance Optimization Plan (Safety-Hardened)

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Safely eliminate verified dead code, orphan UI catalogs, and ghost JNI bindings while **STRICTLY PROTECTING 100% OF ALL AIMBOT, AIM ASSIST, ENEMY LOCK, TRACKING BULLET, DAMAGE, ATTACK SPEED, COMBAT MODS, AND DRONE VIEW MECHANICS**.

---

## 🛡️ STRICT ZERO-TOUCH SAFETY WHITELIST (DO NOT TOUCH OR REMOVE)

The following components are **STRICTLY WHITELISTED AND MUST NOT BE ALTERED, TOUCHED, OR REMOVED**:

### 1. Aim Assistance, Aimbot, Enemy Lock & Bullet Tracking (100% UNTOUCHED)
- **All Aimbot & Aim Assist Injectors:**
  - `SilentAimbot`, `PredictiveAim`, `HeadBoneAimPriority`, `AimMagnetism`, `AimAssistStrength`, `AimSnapSpeed`, `AimSnapThreshold`.
  - `nativeInjectUniversalAimMagnetLock`, `nativeInjectSilentAimbot`, `nativeInjectAdaptiveAimAssist`.
  - `nativeInjectCodmEnemyLockAllScope`, `nativeInjectPubgmEnemyLockAllScope`, `nativeInjectMlbbEnemyLockHeadshotSuite`.
  - `nativeInjectAutoHeadshotBulletKill`, `nativeInjectTrackingBullet`, `nativeInjectTrackingBullet1000`.
  - `nativeInjectEnemyLockMaxAllScope`, `nativeInjectScopeAimCalibration`, `nativeInjectScopeZeroRecoil`.
  - `nativeInjectRifleScopeTieredHeadshot`, `nativeInjectValorantLowLatencyHeadshot`, `nativeInjectValorantCounterStrafeAimLock`.
  - [combat_system_jni.cpp](file:///home/willygailo/Downloads/Game-Launcher/android/app/src/main/cpp/combat_system_jni.cpp) (`nativeEvaluateAimAssistTarget` 3D vector assist cone).
  - [combat_system.hpp](file:///home/willygailo/Downloads/Game-Launcher/android/app/src/main/cpp/combat_system.hpp) (`EvaluateAimAssistTarget`, dot product angular calculations).
  - [CombatSystemEngine.java](file:///home/willygailo/Downloads/Game-Launcher/android/app/src/main/java/com/gamebooster/app/config/CombatSystemEngine.java) (`getBestTarget`, `Vector3`, `TargetEntity`, `CooldownManager`).

### 2. Damage, Attack Speed & Combat Overdrive (100% UNTOUCHED)
- **All Damage & Attack Speed Injections:**
  - `inject*Damage10000AttackSpeedMax` across MLBB, CODM, PUBGM, FreeFire, HoK, WildRift, BloodStrike, DeltaForce, ArenaBreakout, Valorant, Farlight, Standoff2.
  - `universal_combat_injector.cpp`, `combat_system_jni.cpp`, `combat_system.hpp`.
  - `nativeInjectUniversalDamage10000AttackSpeedMax`, `nativeInjectUltraDamageOverdrive`, `nativeInjectUniversalGodDamageOverdrive2026`.
  - [CompetitiveCfgProfile.java](file:///home/willygailo/Downloads/Game-Launcher/android/app/src/main/java/com/gamebooster/app/config/CompetitiveCfgProfile.java) (All damage, aim, attack speed, zero recoil toggles).

### 3. Drone View Systems (100% UNTOUCHED)
- [MlbbDroneViewPatcher.java](file:///home/willygailo/Downloads/Game-Launcher/android/app/src/main/java/com/gamebooster/app/config/MlbbDroneViewPatcher.java) (1.5x, 2x, 3x, 4x, 5x camera tier injectors).
- [DroneViewInjector.java](file:///home/willygailo/Downloads/Game-Launcher/android/app/src/main/java/com/gamebooster/app/engine/DroneViewInjector.java) (Protected fallback).
- [assets/mlbb_drone/](file:///home/willygailo/Downloads/Game-Launcher/android/app/src/main/assets/mlbb_drone/) (All Unity 3D camera assets).

### 4. Game-Specific Native Injector Files (100% UNTOUCHED)
- `android/app/src/main/cpp/mlbb_injector.cpp`
- `android/app/src/main/cpp/codm_injector.cpp`
- `android/app/src/main/cpp/pubgm_injector.cpp`
- `android/app/src/main/cpp/other_games_injector.cpp`
- `android/app/src/main/cpp/universal_combat_injector.cpp`
- `android/app/src/main/cpp/combat_system_jni.cpp`
- `android/app/src/main/cpp/combat_system.hpp`
- `android/app/src/main/cpp/il2cpp_direct_scanner.cpp` & `Il2cppDirectScanner.java`
- `android/app/src/main/cpp/ace_cloak_engine.cpp` & `AceCloakEngine.java`

### 5. Privileged IPC & Shizuku System (100% UNTOUCHED)
- `ShizukuManager.java`, `ShizukuExecutor.java`, `UserService.java`, `PrivilegeBridgeEngine.java`, `ShizukuUserServiceConnector.java`.

---

## 🎯 EXACT SCOPE OF DEAD CODE TO REMOVE (ONLY SAFE TARGETS)

We ONLY touch and remove items that have **ZERO connection** to gameplay, aim, combat, or drone view:

1. **Dead Online Web Scraper Catalog (abandoned web-game browser):**
   - `android/app/src/main/java/com/gamebooster/app/api/GameApiClient.java`
   - `android/app/src/main/java/com/gamebooster/app/api/OnlineGameSearchResult.java`
   - `android/app/src/main/java/com/gamebooster/app/ui/adapters/OnlineGamesAdapter.java`
   - `android/app/src/main/res-layouts/items/layout/item_online_game_search.xml`

2. **Redundant Duplicate ART Engine:**
   - `android/app/src/main/java/com/gamebooster/app/engine/AotCompilerEngine.java` (Duplicate of `ArtCompilerEngine.java`)

3. **Standalone Unreferenced Utilities (Zero gameplay impact):**
   - `android/app/src/main/java/com/gamebooster/app/booster/PingOptimizerEngine.java`
   - `android/app/src/main/java/com/gamebooster/app/booster/BatteryEstimator.java`
   - `android/app/src/main/java/com/gamebooster/app/booster/FrameMetricsCollector.java`
   - `android/app/src/main/java/com/gamebooster/app/booster/MemoryCleanerPro.java`
   - `android/app/src/main/java/com/gamebooster/app/shizuku/ShizukuStatusViewModel.java`
   - `android/app/src/main/java/com/gamebooster/app/utils/WebViewPerformanceTuner.java`

4. **Ghost C++ File (No Java counterpart):**
   - `android/app/src/main/cpp/dlopen_hook_engine.cpp` (Ghost JNI with no Java class)

5. **No-Op Stubs that always return false:**
   - In `PerformanceChannel.java`: obsolete script stubs (`writeAndExecutePerformanceTweaksScript`, `writeAndExecuteRootTweaksScript`, `setGpuRenderMode`) and their callers.

---

## Tasks

### Task 1: Safe Pruning of Dead Web Scraper & Standalone Utilities
- [x] **Step 1.1:** Delete the dead online games web scraper (`GameApiClient.java`, `OnlineGameSearchResult.java`, `OnlineGamesAdapter.java`, `item_online_game_search.xml`).
- [x] **Step 1.2:** Delete redundant `AotCompilerEngine.java` (leaving `ArtCompilerEngine.java` active).
- [x] **Step 1.3:** Delete standalone unreferenced utilities (`PingOptimizerEngine.java`, `BatteryEstimator.java`, `FrameMetricsCollector.java`, `MemoryCleanerPro.java`, `ShizukuStatusViewModel.java`, `WebViewPerformanceTuner.java`).
- [x] **Step 1.4:** Verify Java compilation passes cleanly: `./gradlew compileDebugJavaWithJavac`.

### Task 2: Remove Ghost `dlopen_hook_engine.cpp` & Re-sync CMake
- [x] **Step 2.1:** Remove `dlopen_hook_engine.cpp` from `android/app/src/main/cpp/` and update `CMakeLists.txt`.
- [x] **Step 2.2:** Rebuild native libraries for all ABIs with `bash android/build_native_libs.sh`.
- [x] **Step 2.3:** Verify all exported JNI combat, aimbot, aim assist, damage, and attack speed symbols remain present and intact in `jniLibs/arm64-v8a/libgamebooster_native.so`.

### Task 3: Clean No-Op Script Stubs in PerformanceChannel
- [x] **Step 3.1:** Prune no-op script stubs in `PerformanceChannel.java` that always return `false`, and clean callers in `MasterOptimizationEnforcer`, `TweakManagerRepository`, and `SettingsFragment`.
- [x] **Step 3.2:** Verify Java compilation passes cleanly: `./gradlew compileDebugJavaWithJavac`.

### Task 4: Activate Dormant Game Fast Load AOT Acceleration
- [x] **Step 4.1:** Wire `verifyAndWarmupArtAot(pkg)` into `GameFastLoadAccelerator.triggerPreLaunchBurst()` so game launches automatically get asynchronous machine-code compilation without blocking the UI.
- [x] **Step 4.2:** Confirm that all pre-launch bursts and competitive profile settings properly invoke game launchers with maximum damage, attack speed, aim assist, and drone view intact.

### Task 5: End-to-End Build & Functional Verification
- [x] **Step 5.1:** Run `./gradlew compileDebugJavaWithJavac` in `android/`.
- [x] **Step 5.2:** Run `bash android/build_native_libs.sh` to confirm native libraries compile and strip without warnings.
- [x] **Step 5.3:** Run `./gradlew testDebugUnitTest` and verify all tests pass.
- [x] **Step 5.4:** Run `./gradlew assembleDebug` to confirm full APK packaging is successful.
