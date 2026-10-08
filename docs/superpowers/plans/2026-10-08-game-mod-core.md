# Direct Game Launcher Integration Plan: MLBB & CODM Master Combat & Patch Suite

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (Native execution) to implement this directly into the existing app without standalone/extra project folders. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract and analyze offsets directly from MLBB & CODM APKs in `android/NewPatch/`, then integrate the complete 8-feature combat mod matrix (Damage, Drone View, ESP, Speed, Armor/Defense, Zero Cooldown, No Delay, Auto Combo) and file/asset patchers directly into the existing `android/app` codebase (`src/main/cpp`, `src/main/java`, `src/main/assets`) so the project can be built directly via `build_native_libs.sh` and `./gradlew assembleRelease`.

**Architecture:** 
- In-place extraction of `libil2cpp.so` and `global-metadata.dat` from `android/NewPatch/` directly into temporary analysis work paths.
- Direct C++ integration: extend `mlbb_injector.cpp`, `codm_injector.cpp`, `universal_combat_injector.cpp`, `il2cpp_direct_scanner.cpp`, and `native_config_injector.h` inside `android/app/src/main/cpp/`.
- Direct Java integration: wire new patch routines into `MlbbConfigPatcher.java`, `CodmConfigPatcher.java`, `NativeConfigInjector.java`, and `CombatSystemEngine.java`.
- Deployment scripts & asset bypass: place `res_skip_patch` and asset sync directly into `android/app/src/main/assets/` and `android/app/src/main/java/com/gamebooster/app/config/GameModAutoSyncEngine.java`.
- Zero new external workspace folders created. Everything lives inside `android/app`.

**Tech Stack:**
- Native: C++17/20, Android NDK (`CMakeLists.txt`, `build_native_libs.sh`), JNI
- Java: Android SDK 34/35, Shizuku / SAF direct file access
- Binary & Reverse Engineering: `7z`, Python3 helper for offset parsing in `android/NewPatch/`
- Target APKs: `android/NewPatch/Mobile Legends_ Bang Bang_2.2.16.12322.apks`, `android/NewPatch/Call of Duty_1.6.57.apks`

**Spec:** `/home/willygailo/Downloads/NewPatch.txt`

## Global Constraints

- NO new standalone project/workspace directories (`artifacts/game_mod_core` is eliminated).
- All source changes must reside inside:
  - C++ Native: `/home/willygailo/Downloads/Game-Launcher/android/app/src/main/cpp/`
  - Java Engine: `/home/willygailo/Downloads/Game-Launcher/android/app/src/main/java/com/gamebooster/app/config/`
  - Assets / Bypasses: `/home/willygailo/Downloads/Game-Launcher/android/app/src/main/assets/`
- All 8 Features from `NewPatch.txt` must be implemented:
  1. Damage (Ultra Multiplier / DPS floor lock)
  2. Drone View / High FOV (Panoramic 4X / 120-180 FOV)
  3. ESP (Box + Line + HP + Name + Distance coordinate offsets)
  4. Speed (Move speed + Attack/fire rate)
  5. Armor / Defense (God mode / max damage mitigation)
  6. Near-zero Cooldown (Skill cooldown suppression)
  7. No Delay (Zero ADS/touch/activation delay)
  8. Auto Combo / Continuous Cast (Automated sequence chaining)
- Preserves manual building via `./build_native_libs.sh` and `./gradlew assembleDebug` or `assembleRelease`.

---

### Task 1: Binary & Metadata Extraction from Existing APKs

**Files:**
- Extract from: `/home/willygailo/Downloads/Game-Launcher/android/NewPatch/Mobile Legends_ Bang Bang_2.2.16.12322.apks`
- Extract from: `/home/willygailo/Downloads/Game-Launcher/android/NewPatch/Call of Duty_1.6.57.apks`
- Temp extraction folder: `/home/willygailo/Downloads/Game-Launcher/android/NewPatch/.extracted/`

**Interfaces:**
- Consumes: `.apks` zip archives
- Produces: `libil2cpp.so` (ARM64), `global-metadata.dat`, and configuration files from base and split APKs for offset verification.

- [x] **Step 1: Extract MLBB & CODM ARM64 binaries and metadata**
- [x] **Step 2: Scan and extract Il2Cpp string patterns & class offsets**
- [x] **Step 3: Document verified RVA and symbol signatures**

---

### Task 2: Native C++ Engine Updates (`android/app/src/main/cpp/`)

- [x] **Step 1: Declare master combat matrix JNI methods in `native_config_injector.h`**
- [x] **Step 2: Implement MLBB master combat matrix in `mlbb_injector.cpp`**
- [x] **Step 3: Implement CODM master combat matrix in `codm_injector.cpp`**
- [x] **Step 4: Update `il2cpp_direct_scanner.cpp` with new version signatures**

---

### Task 3: Java Launcher Layer Integration (`android/app/src/main/java/`)

- [x] **Step 1: Add native method declarations to `NativeConfigInjector.java`**
- [x] **Step 2: Update `MlbbConfigPatcher.java` and `CodmConfigPatcher.java`**
- [x] **Step 3: Update `CombatSystemEngine.java`**

---

### Task 4: Anti-Redownload & MD5 Skip Patcher Integration

- [x] **Step 1: Add asset skip template into `android/app/src/main/assets/`**
- [x] **Step 2: Wire anti-redownload logic into `GameModAutoSyncEngine.java`**

---

### Task 5: Compilation & Build Verification

- [x] **Step 1: Execute `build_native_libs.sh` to compile native C++ binaries**
- [x] **Step 2: Verify compilation and export symbols**
- [x] **Step 3: Verify clean git status and commit changes**
