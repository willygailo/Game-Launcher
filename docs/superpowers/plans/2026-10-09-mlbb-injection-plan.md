# MLBB Memory Dump Injection & Combat Suite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement proven, finalized native C++ and Java memory and config injections for the Game-Launcher project targeting Mobile Legends: Bang Bang (v2.2.16.12322) using the memory dump at `/home/willygailo/Downloads/Game-Launcher/android/NewPatch/dump_mlbb`.

**Architecture:** 
1. **Dynamic Native Hooking & Memory Patching:** Utilize RVA offsets from dumped metadata (`dump.cs`, `libil2cpp.so`, `global-metadata.dat`) integrated into `MlbbIl2cppResolver.java` and `mlbb_injector.cpp`.
2. **Camera / Drone View Multiplier (1x-5x):** Leverage `UnityEngine.Camera.fieldOfView` and `CameraManager_SetHeight` (`0x019128A0` / signature `F44F01A9FD7B02A9FD0300910000001E`) together with `BattleSystemConfig.bytes` deployed across dynamic `mini_patch` paths.
3. **Hero-Wide Combat & Movement Overdrive:** Multi-hero combat matrices targeting all classes (Fighter, Assassin, Mage, Marksman, Tank, Support) and game modes (Ranked, Classic, Custom) injecting Damage (`0x0182C4D0`), Armor/Defense (`0x01859E40`), Attack Speed (`0x01831E20`), and Movement (`0x0187B2C0`).
4. **Anti-Detection & Integrity Bypass:** Update `res_skip_patch.xml` and `BinaryPatchMD5.xml` (MD5: `84432b8c9901e630556f077ca9162268`) with automated permission locking (`chmod 444` / `666`) to defeat client re-download and integrity rejections.

**Tech Stack:** C++17 (Android NDK), Java 11/17 (Android SDK 36), JNI, Frida Native Bridge, Unity IL2CPP, Shizuku Shell IPC.

**Spec:** `/home/willygailo/Downloads/instraction.txt`

## Global Constraints
- Target version: MLBB 2.2.16.12322 / Season 42+ (`realversion="2.2.16.1232.1"`).
- Base directory: `/home/willygailo/Downloads/Game-Launcher/android/NewPatch/dump_mlbb`.
- Architecture: `arm64-v8a`.
- Zero placeholders: All offsets must be verified against dumped metadata and binary signatures.
- Anti-Detection: Must preserve client acceptance via `res_skip_patch.xml` and `BinaryPatchMD5.xml`.

## Review Focus
1. Client rejection or crash upon modified binary/bytes loading (prevented via `res_skip_patch.xml` skipFix and `chmod 444`).
2. Scoped Storage access denial on Android 13-16 (prevented via Shizuku shell dispatch).
3. Mismatch between camera zoom multiplier tier calculation (1x, 2x, 3x, 4x, 5x) and game field of view scale.
4. Hero class coverage gaps across all 5 roles (ensured via `MlbbHeroScriptDispatcher` and `applyMasterCombatMatrix`).
5. Memory unmapping or race conditions during JNI memory patching (prevented via mutex-guarded atomic writes).

---

### Task 1: Environment & Offset Verification Matrix
**Files:**
- Reference: `/home/willygailo/Downloads/Game-Launcher/android/NewPatch/dump_mlbb/Document/android/BinaryPatchMD5.xml`
- Reference: `/home/willygailo/Downloads/DumpDroid/dump.cs`
- Modify: `android/app/src/main/assets/frida/mlbb_il2cpp_offsets.json`
- Test: `android/app/src/test/java/com/gamebooster/app/mods/mlbb/MlbbIl2cppResolverTest.java`

**Interfaces:**
- Consumes: Target method RVAs and signatures from dumped `dump.cs` and `BinaryPatchMD5.xml`.
- Produces: JSON table loaded by `MlbbIl2cppResolver.loadOffsets(Context)`.

- [x] **Step 1: Write test to verify IL2CPP offset loading and signature integrity**
- [x] **Step 2: Run test to confirm current state**
- [x] **Step 3: Update `mlbb_il2cpp_offsets.json` with verified RVAs for Drone View, Movement, and Combat Stats**
- [x] **Step 4: Verify offset parser passes and outputs correct hexadecimal addresses**
- [x] **Step 5: Commit changes**

---

### Task 2: Drone View 1x-5x Multiplier Engine Integration
**Files:**
- Modify: `android/app/src/main/java/com/gamebooster/app/config/MlbbDroneViewPatcher.java`
- Modify: `android/app/src/main/cpp/mlbb_injector.cpp`
- Test: `android/app/src/test/java/com/gamebooster/app/config/MlbbDroneViewPatcherTest.java`

**Interfaces:**
- Consumes: Zoom multipliers: 1x (normal), 2x (TIER_2X), 3x (TIER_3X), 4x (TIER_4X), 5x (TIER_5X).
- Produces: `MlbbDroneViewPatcher.applyDroneViewAtomic(Context, String, int)` writing calibrated `fieldOfView` and camera elevation.

- [x] **Step 1: Write unit test asserting tier-to-FOV mathematical mapping for 1x, 2x, 3x, 4x, 5x**
- [x] **Step 2: Run test to identify multiplier handling gaps**
- [x] **Step 3: Implement exact 1x-5x scale normalization and FOV injection logic in `MlbbDroneViewPatcher.java`**
- [x] **Step 4: Update `nativeInjectMlbbUltraDroneViewMaxFov` in `mlbb_injector.cpp` with exact camera height and FOV thresholds**
- [x] **Step 5: Run unit tests to confirm passing**
- [x] **Step 6: Commit changes**

---

### Task 3: Universal Combat & Hero Stats Overdrive (All Classes & Game Modes)
**Files:**
- Modify: `android/app/src/main/cpp/mlbb_injector.cpp`
- Modify: `android/app/src/main/java/com/gamebooster/app/config/MlbbConfigPatcher.java`
- Modify: `android/app/src/main/java/com/gamebooster/app/config/MlbbHeroScriptDispatcher.java`

**Interfaces:**
- Consumes: Damage (`0x0182C4D0`), Armor (`0x01859E40`), Attack Speed (`0x01831E20`), Speed (`0x0187B2C0`), and Mana/Energy flags.
- Produces: `MlbbConfigPatcher.applyRankedCombatFullSuite(String)` and `MlbbConfigPatcher.applyClassicCombatFullSuite(String)`.

- [x] **Step 1: Write verification test for all 5 hero roles (Fighter, Assassin, Mage, Marksman, Tank)**
- [x] **Step 2: Run test and observe output**
- [x] **Step 3: Update `mlbb_injector.cpp` with confirmed offsets for HeroDamageCalc, HeroDefense, MovementComponent, and EnergyRegen**
- [x] **Step 4: Implement full mode coverage (Ranked, Classic, Custom) in `MlbbConfigPatcher.java`**
- [x] **Step 5: Run tests and verify injection status**
- [x] **Step 6: Commit changes**

---

### Task 4: Anti-Detection & Integrity Whitelist Verification (`res_skip_patch` & MD5)
**Files:**
- Modify: `android/app/src/main/java/com/gamebooster/app/config/MlbbDroneViewPatcher.java`
- Modify: `android/app/src/main/java/com/gamebooster/app/config/GameSecurityBypassEngine.java`

**Interfaces:**
- Consumes: `BinaryPatchMD5.xml` (MD5: `84432b8c9901e630556f077ca9162268`) and `ResCheckConf.xml` definitions.
- Produces: Automated `res_skip_patch.xml` synchronization and immutable file locking (`chmod 444` / `chmod 666`).

- [x] **Step 1: Write verification test checking `res_skip_patch.xml` content and MD5 integrity rule formatting**
- [x] **Step 2: Run test to check current synchronization implementation**
- [x] **Step 3: Implement exact MD5 bypass and skipFix injection in `MlbbDroneViewPatcher.java` and `GameSecurityBypassEngine.java`**
- [x] **Step 4: Verify integrity bypass routines run cleanly without syntax errors**
- [x] **Step 5: Commit changes**

---

### Task 5: End-to-End Build & Validation
**Files:**
- Review: `android/app/build.gradle`
- Execute: `./gradlew assembleDebug`

- [x] **Step 1: Execute complete build compilation verification**
- [x] **Step 2: Inspect generated APK and native library symbols**
- [x] **Step 3: Generate final implementation delivery report**
